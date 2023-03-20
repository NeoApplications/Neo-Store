package com.machiav3lli.fdroid.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import android.view.ContextThemeWrapper
import androidx.core.app.NotificationCompat
import androidx.lifecycle.whenResumed
import com.anggrayudi.storage.callback.FileCallback
import com.anggrayudi.storage.file.copyFileTo
import com.anggrayudi.storage.file.toDocumentFile
import com.machiav3lli.fdroid.BuildConfig
import com.machiav3lli.fdroid.MainApplication
import com.machiav3lli.fdroid.NOTIFICATION_CHANNEL_DOWNLOADING
import com.machiav3lli.fdroid.NOTIFICATION_ID_DOWNLOADING
import com.machiav3lli.fdroid.NOTIFICATION_ID_SYNCING
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.content.Cache
import com.machiav3lli.fdroid.content.Preferences
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
import com.machiav3lli.fdroid.utility.getDownloadFolder
import com.machiav3lli.fdroid.utility.isDownloadExternal
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
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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

    @Serializable
    sealed class State {
        abstract val packageName: String
        abstract val name: String
        abstract val version: String
        abstract val cacheFileName: String
        abstract val repoId: Long

        @Serializable
        class Pending(
            override val packageName: String,
            override val name: String,
            override val version: String,
            override val cacheFileName: String,
            override val repoId: Long,
        ) : State()

        @Serializable
        class Connecting(
            override val packageName: String,
            override val name: String,
            override val version: String,
            override val cacheFileName: String,
            override val repoId: Long,
        ) : State()

        @Serializable
        class Downloading(
            override val packageName: String,
            override val name: String,
            override val version: String,
            override val cacheFileName: String,
            override val repoId: Long,
            val read: Long,
            val total: Long?,
        ) : State()

        @Serializable
        class Success(
            override val packageName: String,
            override val name: String,
            override val version: String,
            override val cacheFileName: String,
            override val repoId: Long,
            val release: Release,
        ) : State()

        @Serializable
        class Error(
            override val packageName: String,
            override val name: String,
            override val version: String,
            override val cacheFileName: String,
            override val repoId: Long,
        ) : State()

        @Serializable
        class Cancel(
            override val packageName: String,
            override val name: String,
            override val version: String,
            override val cacheFileName: String,
            override val repoId: Long,
        ) : State()

        fun toJSON() = Json.encodeToString(this)

        companion object {
            fun fromJson(json: String) = Json.decodeFromString<State>(json)
        }
    }

    private val mutableStateSubject = MutableSharedFlow<State>()

    class Task(
        val packageName: String, val name: String, val release: Release,
        val url: String, val repoId: Long, val authentication: String,
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
                repository.id,
                repository.authentication,
            )
            if (Cache.getReleaseFile(this@DownloadService, release.cacheFileName).exists()) {
                Log.i(this::javaClass.name, "Running publish success from fun enqueue")
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
                                release.version,
                                task.release.cacheFileName,
                                task.repoId,
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
        mutableStateSubject.onEach { (application as MainApplication).db.downloadedDao.insertReplace() }
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
        tasks.removeAll { task ->
            (packageName == null || task.packageName == packageName) && run {
                scope.launch {
                    mutableStateSubject.emit(
                        State.Cancel(
                            task.packageName,
                            task.name,
                            task.release.version,
                            task.release.cacheFileName,
                            task.repoId,
                        )
                    )
                }
                true
            }
        }
    }

    private fun cancelCurrentTask(packageName: String?) {
        currentTask?.let { current ->
            if (packageName == null || current.task.packageName == packageName) {
                currentTask = null
                scope.launch {
                    mutableStateSubject.emit(
                        State.Cancel(
                            current.task.packageName,
                            current.task.name,
                            current.task.release.version,
                            current.task.release.cacheFileName,
                            current.task.repoId,
                        )
                    )
                }
                current.disposable.dispose()
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
        scope.launch {
            mutableStateSubject.emit(
                State.Success(
                    task.packageName,
                    task.name,
                    task.release.version,
                    task.release.cacheFileName,
                    task.repoId,
                    task.release
                )
            )
        }
        if (isDownloadExternal) {
            getDownloadFolder()?.let { downloadFolder ->
                val cacheFile = Cache.getReleaseFile(applicationContext, task.release.cacheFileName)
                    .toDocumentFile(applicationContext)
                scope.launch {
                    if (downloadFolder.findFile(task.release.cacheFileName)?.exists() != true) {
                        cacheFile?.copyFileTo(
                            applicationContext,
                            downloadFolder,
                            null,
                            object : FileCallback() {}
                        )
                    }
                }
            }
        }
        val installer = suspend {
            val installerInstance = AppInstaller.getInstance(applicationContext)
            installerInstance?.defaultInstaller?.install(task.name, task.release.cacheFileName)
        }
        if (MainApplication.mainActivity != null &&
            AppInstaller.getInstance(MainApplication.mainActivity)?.defaultInstaller is LegacyInstaller
        ) { //TODO investigate if there's resulting issues
            CoroutineScope(Dispatchers.Default).launch {
                Log.i(this::javaClass.name, "Waiting activity to install: ${task.packageName}")
                MainApplication.mainActivity?.whenResumed { installer() }
            }
        } else {
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
                if ((signature.isEmpty() || signature != task.release.signature)
                    && !Preferences[Preferences.Key.DisableSignatureCheck]
                ) {
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
                    task.release.version,
                    task.release.cacheFileName,
                    task.repoId,
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
                                        task.release.cacheFileName,
                                        task.repoId,
                                        read,
                                        total,
                                    )
                                )
                            }
                        }
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { result: Downloader.Result?, throwable: Throwable? ->
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
                                        task.release.cacheFileName,
                                        task.repoId,
                                    )
                                )
                            }
                        } else {
                            val validationError = validatePackage(task, partialReleaseFile)
                            if (validationError == null) {
                                val releaseFile =
                                    Cache.getReleaseFile(this, task.release.cacheFileName)
                                partialReleaseFile.renameTo(releaseFile)
                                Log.i(
                                    this::javaClass.name,
                                    "Running publish success from fun handleDownload"
                                )
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
                                            task.release.cacheFileName,
                                            task.repoId,
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
