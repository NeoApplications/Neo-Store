package com.machiav3lli.fdroid.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import android.view.ContextThemeWrapper
import androidx.core.app.NotificationCompat
import androidx.lifecycle.whenResumed
import com.machiav3lli.fdroid.BuildConfig
import com.machiav3lli.fdroid.MainApplication
import com.machiav3lli.fdroid.NOTIFICATION_CHANNEL_DOWNLOADING
import com.machiav3lli.fdroid.NOTIFICATION_ID_DOWNLOADING
import com.machiav3lli.fdroid.NOTIFICATION_ID_SYNCING
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.content.Cache
import com.machiav3lli.fdroid.database.entity.Release
import com.machiav3lli.fdroid.database.entity.Repository
import com.machiav3lli.fdroid.installer.AppInstaller
import com.machiav3lli.fdroid.installer.LegacyInstaller
import com.machiav3lli.fdroid.network.Downloader
import com.machiav3lli.fdroid.utility.Utils
import com.machiav3lli.fdroid.utility.extension.android.Android
import com.machiav3lli.fdroid.utility.extension.android.notificationManager
import com.machiav3lli.fdroid.utility.extension.android.singleSignature
import com.machiav3lli.fdroid.utility.extension.android.versionCodeCompat
import com.machiav3lli.fdroid.utility.extension.resources.getColorFromAttr
import com.machiav3lli.fdroid.utility.extension.text.formatSize
import com.machiav3lli.fdroid.utility.extension.text.hex
import com.machiav3lli.fdroid.utility.extension.text.nullIfEmpty
import com.machiav3lli.fdroid.utility.showNotificationError
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.File
import java.security.MessageDigest
import kotlin.math.roundToInt

// TODO maybe replace by using WorkManager instead?
class DownloadService : ConnectionService<DownloadService.Binder>() {
    companion object {
        private const val ACTION_CANCEL = "${BuildConfig.APPLICATION_ID}.intent.action.CANCEL"

        private val mutableDownloadState = MutableSharedFlow<State.Downloading>()
        private val downloadState = mutableDownloadState.asSharedFlow()
    }

    private val scope = CoroutineScope(Dispatchers.Default)

    sealed class State(val packageName: String, val name: String, val version: String) {
        class Pending(packageName: String, name: String, version: String) :
            State(packageName, name, version)

        class Connecting(packageName: String, name: String, version: String) :
            State(packageName, name, version)

        class Downloading(
            packageName: String,
            name: String,
            version: String,
            val read: Long,
            val total: Long?,
        ) : State(packageName, name, version)

        class Success(
            packageName: String, name: String, val release: Release,
        ) : State(packageName, name, release.version)

        class Error(packageName: String, name: String, version: String) :
            State(packageName, name, version)

        class Cancel(packageName: String, name: String, version: String) :
            State(packageName, name, version)
    }

    private val mutableStateSubject = MutableSharedFlow<State>()

    class Task(
        val packageName: String, val name: String, val release: Release,
        val url: String, val authentication: String,
    ) {
        val notificationTag: String
            get() = "download-$packageName"
    }

    private data class CurrentTask(val task: Task, val disposable: Disposable, val lastState: State)

    private var started = false
    private val tasks = mutableListOf<Task>()
    private var currentTask: CurrentTask? = null

    inner class Binder : android.os.Binder() {
        val stateSubject = mutableStateSubject.asSharedFlow()
            .distinctUntilChanged { old, new ->
                new.packageName == old.packageName &&
                        old::class.java == new::class.java && (
                        if (old is State.Downloading && new is State.Downloading && new.total != null)
                            new.read - old.read <= new.total / 100
                        else true
                        )
            }

        fun enqueue(packageName: String, name: String, repository: Repository, release: Release) {
            val task = Task(
                packageName,
                name,
                release,
                release.getDownloadUrl(repository),
                repository.authentication
            )
            if (Cache.getReleaseFile(this@DownloadService, release.cacheFileName).exists()) {
                publishSuccess(task)
            } else {
                cancelTasks(packageName)
                cancelCurrentTask(packageName)
                notificationManager.cancel(task.notificationTag, NOTIFICATION_ID_DOWNLOADING)
                tasks += task
                if (currentTask == null) {
                    handleDownload()
                } else {
                    scope.launch {
                        mutableStateSubject.emit(
                            State.Pending(
                                packageName,
                                name,
                                release.version
                            )
                        )
                    }
                }
            }
        }

        fun cancel(packageName: String) {
            cancelTasks(packageName)
            cancelCurrentTask(packageName)
            handleDownload()
        }
    }

    private val binder = Binder()
    override fun onBind(intent: Intent): Binder = binder

    override fun onCreate() {
        super.onCreate()

        if (Android.sdk(26)) {
            NotificationChannel(
                NOTIFICATION_CHANNEL_DOWNLOADING,
                getString(R.string.downloading), NotificationManager.IMPORTANCE_LOW
            )
                .apply { setShowBadge(false) }
                .let(notificationManager::createNotificationChannel)
        }

        downloadState.onEach { publishForegroundState(false, it) }.launchIn(scope)
    }

    override fun onDestroy() {
        super.onDestroy()

        scope.cancel()
        cancelTasks(null)
        cancelCurrentTask(null)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_CANCEL) {
            currentTask?.let { binder.cancel(it.task.packageName) }
        }
        return START_NOT_STICKY
    }

    private fun cancelTasks(packageName: String?) {
        tasks.removeAll {
            (packageName == null || it.packageName == packageName) && run {
                scope.launch {
                    mutableStateSubject.emit(
                        State.Cancel(
                            it.packageName,
                            it.name,
                            "–"
                        )
                    )
                }
                true
            }
        }
    }

    private fun cancelCurrentTask(packageName: String?) {
        currentTask?.let {
            if (packageName == null || it.task.packageName == packageName) {
                currentTask = null
                scope.launch {
                    mutableStateSubject.emit(
                        State.Cancel(
                            it.task.packageName,
                            it.task.name,
                            "–"
                        )
                    )
                }
                it.disposable.dispose()
            }
        }
    }

    enum class ValidationError { INTEGRITY, FORMAT, METADATA, SIGNATURE, PERMISSIONS }

    sealed class ErrorType {
        object Network : ErrorType()
        object Http : ErrorType()
        class Validation(val validateError: ValidationError) : ErrorType()
    }

    private fun publishSuccess(task: Task) {
        var consumed = false
        scope.launch {
            mutableStateSubject.emit(State.Success(task.packageName, task.name, task.release))
            consumed = true
        }
        val installer = suspend {
            AppInstaller.getInstance(MainApplication.mainActivity)
                ?.defaultInstaller?.install(task.name, task.release.cacheFileName)
        }
        if (!consumed && MainApplication.mainActivity != null &&
            AppInstaller.getInstance(MainApplication.mainActivity)?.defaultInstaller is LegacyInstaller
        ) { //TODO investigate if there's resulting issues
            CoroutineScope(Dispatchers.Default).launch {
                Log.i(this::javaClass.name, "Waiting activity to install: ${task.packageName}")
                MainApplication.mainActivity?.whenResumed { installer() }
            }
        } else if (!consumed) {
            Log.i(this::javaClass.name, "Installing downloaded: ${task.url}")
            CoroutineScope(Dispatchers.IO).launch { installer() }
        }
    }

    private fun validatePackage(task: Task, file: File): ValidationError? {
        val hash = try {
            val hashType = task.release.hashType.nullIfEmpty() ?: "SHA256"
            val digest = MessageDigest.getInstance(hashType)
            file.inputStream().use {
                val bytes = ByteArray(8 * 1024)
                generateSequence { it.read(bytes) }.takeWhile { it >= 0 }
                    .forEach { digest.update(bytes, 0, it) }
                digest.digest().hex()
            }
        } catch (e: Exception) {
            ""
        }
        return if (hash.isEmpty() || hash != task.release.hash) {
            ValidationError.INTEGRITY
        } else {
            val packageInfo = try {
                packageManager.getPackageArchiveInfo(
                    file.path,
                    Android.PackageManager.signaturesFlag
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
            if (packageInfo == null) {
                ValidationError.FORMAT
            } else if (packageInfo.packageName != task.packageName ||
                packageInfo.versionCodeCompat != task.release.versionCode
            ) {
                ValidationError.METADATA
            } else {
                val signature = packageInfo.singleSignature?.let(Utils::calculateHash).orEmpty()
                if (signature.isEmpty() || signature != task.release.signature) {
                    ValidationError.SIGNATURE
                } else {
                    val permissions =
                        packageInfo.permissions?.asSequence().orEmpty().map { it.name }.toSet()
                    if (!task.release.permissions.containsAll(permissions)) {
                        ValidationError.PERMISSIONS
                    } else {
                        null
                    }
                }
            }
        }
    }

    private val stateNotificationBuilder by lazy {
        NotificationCompat
            .Builder(this, NOTIFICATION_CHANNEL_DOWNLOADING)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setColor(
                ContextThemeWrapper(this, R.style.Theme_Main_Amoled)
                    .getColorFromAttr(android.R.attr.colorPrimary).defaultColor
            )
            .addAction(
                0, getString(R.string.cancel), PendingIntent.getService(
                    this,
                    0,
                    Intent(this, this::class.java).setAction(ACTION_CANCEL),
                    if (Android.sdk(23))
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    else
                        PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
    }

    private fun publishForegroundState(force: Boolean, state: State) {
        if (force || currentTask != null) {
            currentTask = currentTask?.copy(lastState = state)
            startForeground(NOTIFICATION_ID_SYNCING, stateNotificationBuilder.apply {
                when (state) {
                    is State.Connecting                                                 -> {
                        setContentTitle(getString(R.string.downloading_FORMAT, state.name))
                        setContentText(getString(R.string.connecting))
                        setProgress(1, 0, true)
                    }
                    is State.Downloading                                                -> {
                        setContentTitle(getString(R.string.downloading_FORMAT, state.name))
                        if (state.total != null) {
                            setContentText("${state.read.formatSize()} / ${state.total.formatSize()}")
                            setProgress(100, (100f * state.read / state.total).roundToInt(), false)
                        } else {
                            setContentText(state.read.formatSize())
                            setProgress(0, 0, true)
                        }
                    }
                    is State.Pending, is State.Success, is State.Error, is State.Cancel -> {
                        throw IllegalStateException()
                    }
                }::class
            }.build())
            scope.launch { mutableStateSubject.emit(state) }
        }
    }

    private fun handleDownload() {
        if (currentTask == null) {
            if (tasks.isNotEmpty()) {
                val task = tasks.removeAt(0)
                if (!started) {
                    started = true
                    startSelf()
                }
                val initialState = State.Connecting(
                    task.packageName,
                    task.name,
                    task.release.version
                )
                stateNotificationBuilder.setWhen(System.currentTimeMillis())
                publishForegroundState(true, initialState)
                val partialReleaseFile =
                    Cache.getPartialReleaseFile(this, task.release.cacheFileName)
                lateinit var disposable: Disposable
                disposable = Downloader
                    .download(
                        task.url,
                        partialReleaseFile,
                        "",
                        "",
                        task.authentication
                    ) { read, total ->
                        if (!disposable.isDisposed) {
                            scope.launch {
                                mutableDownloadState.emit(
                                    State.Downloading(
                                        task.packageName,
                                        task.name,
                                        task.release.version,
                                        read,
                                        total
                                    )
                                )
                            }
                        }
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { result, throwable: Throwable? ->
                        currentTask = null
                        throwable?.printStackTrace()
                        if (result == null || !result.success) {
                            showNotificationError(
                                task,
                                if (result != null) ErrorType.Http else ErrorType.Network
                            )
                            scope.launch {
                                mutableStateSubject.emit(
                                    State.Error(
                                        task.packageName,
                                        task.name,
                                        task.release.version,
                                    )
                                )
                            }
                        } else {
                            val validationError = validatePackage(task, partialReleaseFile)
                            if (validationError == null) {
                                val releaseFile =
                                    Cache.getReleaseFile(this, task.release.cacheFileName)
                                partialReleaseFile.renameTo(releaseFile)
                                publishSuccess(task)
                            } else {
                                partialReleaseFile.delete()
                                showNotificationError(task, ErrorType.Validation(validationError))
                                scope.launch {
                                    mutableStateSubject.emit(
                                        State.Error(
                                            task.packageName,
                                            task.name,
                                            task.release.version,
                                        )
                                    )
                                }
                            }
                        }
                        handleDownload()
                    }
                currentTask = CurrentTask(task, disposable, initialState)
            } else if (started) {
                started = false
                stopForeground(true)
                stopSelf()
            }
        }
    }
}
