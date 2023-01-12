package com.machiav3lli.fdroid.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import android.util.Log
import android.view.ContextThemeWrapper
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import androidx.work.Configuration
import com.machiav3lli.fdroid.BuildConfig
import com.machiav3lli.fdroid.EXODUS_TRACKERS_SYNC
import com.machiav3lli.fdroid.NOTIFICATION_CHANNEL_SYNCING
import com.machiav3lli.fdroid.NOTIFICATION_CHANNEL_UPDATES
import com.machiav3lli.fdroid.NOTIFICATION_CHANNEL_VULNS
import com.machiav3lli.fdroid.NOTIFICATION_ID_SYNCING
import com.machiav3lli.fdroid.NOTIFICATION_ID_UPDATES
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.database.DatabaseX
import com.machiav3lli.fdroid.database.entity.ExodusInfo
import com.machiav3lli.fdroid.database.entity.Product
import com.machiav3lli.fdroid.database.entity.Repository
import com.machiav3lli.fdroid.database.entity.Tracker
import com.machiav3lli.fdroid.entity.AntiFeature
import com.machiav3lli.fdroid.entity.Order
import com.machiav3lli.fdroid.entity.ProductItem
import com.machiav3lli.fdroid.entity.Section
import com.machiav3lli.fdroid.index.RepositoryUpdater
import com.machiav3lli.fdroid.network.RExodusAPI
import com.machiav3lli.fdroid.utility.Utils
import com.machiav3lli.fdroid.utility.displayUpdatesNotification
import com.machiav3lli.fdroid.utility.displayVulnerabilitiesNotification
import com.machiav3lli.fdroid.utility.extension.android.Android
import com.machiav3lli.fdroid.utility.extension.android.notificationManager
import com.machiav3lli.fdroid.utility.extension.resources.getColorFromAttr
import com.machiav3lli.fdroid.utility.extension.text.formatSize
import com.machiav3lli.fdroid.utility.showNotificationError
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class SyncService : ConnectionService<SyncService.Binder>() {
    companion object {
        private const val ACTION_CANCEL = "${BuildConfig.APPLICATION_ID}.intent.action.CANCEL"

        private val mutableStateSubject = MutableSharedFlow<State>()
        private val mutableFinishState = MutableSharedFlow<Unit>()

        private val stateSubject = mutableStateSubject.asSharedFlow()
        private val finishState = mutableFinishState.asSharedFlow()
    }

    private sealed class State {
        data class Connecting(val name: String) : State()
        data class Syncing(
            val name: String, val stage: RepositoryUpdater.Stage,
            val read: Long, val total: Long?,
        ) : State()

        object Finishing : State()
    }

    private class Task(val repositoryId: Long, val manual: Boolean)
    private data class CurrentTask(
        val task: Task?, val disposable: Any,
        val hasUpdates: Boolean, val lastState: State,
    )

    private enum class Started { NO, AUTO, MANUAL }

    private val scope = CoroutineScope(Dispatchers.Default)

    private var started = Started.NO
    private val tasks = mutableListOf<Task>()
    private var currentTask: CurrentTask? = null

    private var updateNotificationBlockerFragment: WeakReference<Fragment>? = null

    private val downloadConnection =
        Connection(DownloadService::class.java, onBind = { _, dBinder ->
            CoroutineScope(Dispatchers.Default).launch {
                dBinder.stateSubject
                    .collectLatest { binder.updateDownloadState(it) }
            }
        })

    private val downloadServiceMutex = Mutex()

    enum class SyncRequest { AUTO, MANUAL, FORCE }

    @Inject
    lateinit var repoExodusAPI: RExodusAPI

    inner class Binder : android.os.Binder() {
        val finish: SharedFlow<Unit>
            get() = finishState

        private val _downloadState = MutableStateFlow<DownloadService.State?>(null)
        val downloadState: StateFlow<DownloadService.State?> = _downloadState

        private fun sync(ids: List<Long>, request: SyncRequest) {
            val cancelledTask =
                cancelCurrentTask { request == SyncRequest.FORCE && it.task?.repositoryId in ids }
            cancelTasks { !it.manual && it.repositoryId in ids }
            val currentIds =
                synchronized(tasks) { tasks.map { it.repositoryId }.toSet() }
            val manual = request != SyncRequest.AUTO
            synchronized(tasks) {
                tasks += ids.filter {
                    it !in currentIds &&
                            it != currentTask?.task?.repositoryId
                }.map { Task(it, manual) }
            }
            handleNextTask(cancelledTask?.hasUpdates == true)
            if (request != SyncRequest.AUTO && started == Started.AUTO) {
                started = Started.MANUAL
                startSelf()
                handleSetStarted()
                currentTask?.lastState?.let { publishForegroundState(true, it) }
            }
        }

        fun updateApps(products: List<ProductItem>) = batchUpdate(products)
        fun installApps(products: List<ProductItem>) = batchUpdate(products, true)

        fun fetchExodusInfo(packageName: String) = fetchExodusData(packageName)

        fun sync(request: SyncRequest) {
            scope.launch {
                val ids = db.repositoryDao.all.filter { it.enabled }.map { it.id }
                    .toList() + EXODUS_TRACKERS_SYNC
                sync(ids, request)
            }
        }

        fun sync(repository: Repository) {
            if (repository.enabled) {
                sync(listOf(repository.id), SyncRequest.FORCE)
            }
        }

        fun cancelAuto(): Boolean {
            val removed = cancelTasks { !it.manual }
            val currentTask = cancelCurrentTask { it.task?.manual == false }
            handleNextTask(currentTask?.hasUpdates == true)
            return removed || currentTask != null
        }

        fun setUpdateNotificationBlocker(fragment: Fragment?) { // TODO should the notification be canceled on opening a specific page?
            updateNotificationBlockerFragment = fragment?.let(::WeakReference)
            if (fragment != null) {
                notificationManager.cancel(NOTIFICATION_ID_UPDATES)
            }
        }

        suspend fun setEnabled(repository: Repository, enabled: Boolean): Boolean =
            withContext(Dispatchers.IO) {
                db.repositoryDao.put(repository.enable(enabled))
                if (enabled) {
                    if (repository.id != currentTask?.task?.repositoryId && !tasks.any { it.repositoryId == repository.id }) {
                        synchronized(tasks) { tasks += Task(repository.id, true) }
                        handleNextTask(false)
                    }
                } else {
                    cancelTasks { it.repositoryId == repository.id }
                    synchronized(tasks) { db.cleanUp(setOf(Pair(repository.id, false))) }
                    val cancelledTask = cancelCurrentTask { it.task?.repositoryId == repository.id }
                    handleNextTask(cancelledTask?.hasUpdates == true)
                }
                true
            }

        suspend fun isCurrentlySyncing(repositoryId: Long): Boolean = withContext(Dispatchers.IO) {
            currentTask?.task?.repositoryId == repositoryId
        }

        suspend fun deleteRepository(repositoryId: Long): Boolean = withContext(Dispatchers.IO) {
            val repository = db.repositoryDao.get(repositoryId)
            repository != null && run {
                setEnabled(repository, false)
                db.repositoryDao.deleteById(repository.id)
                true
            }
        }

        suspend fun updateDownloadState(state: DownloadService.State) {
            _downloadState.emit(state)
        }
    }

    private val binder = Binder()
    override fun onBind(intent: Intent): Binder = binder
    lateinit var db: DatabaseX

    override fun onCreate() {
        super.onCreate()

        db = DatabaseX.getInstance(applicationContext)
        if (Android.sdk(26)) {
            NotificationChannel(
                NOTIFICATION_CHANNEL_SYNCING,
                getString(R.string.syncing), NotificationManager.IMPORTANCE_LOW
            )
                .apply { setShowBadge(false) }
                .let(notificationManager::createNotificationChannel)
            NotificationChannel(
                NOTIFICATION_CHANNEL_UPDATES,
                getString(R.string.updates), NotificationManager.IMPORTANCE_LOW
            )
                .let(notificationManager::createNotificationChannel)
            NotificationChannel(
                NOTIFICATION_CHANNEL_VULNS,
                getString(R.string.vulnerabilities), NotificationManager.IMPORTANCE_HIGH
            ).let(notificationManager::createNotificationChannel)
        }

        downloadConnection.bind(this)
        stateSubject.onEach { publishForegroundState(false, it) }.launchIn(scope)
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.launch {
            downloadServiceMutex.withLock {
                downloadConnection.unbind(this@SyncService)
            }
        }
        cancelTasks { true }
        cancelCurrentTask { true }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_CANCEL) {
            synchronized(tasks) { tasks.clear() }
            val cancelledTask = cancelCurrentTask { it.task != null }
            handleNextTask(cancelledTask?.hasUpdates == true)
        }
        return START_NOT_STICKY
    }

    private fun cancelTasks(condition: (Task) -> Boolean): Boolean = synchronized(tasks) {
        tasks.removeAll(condition)
    }

    private fun cancelCurrentTask(condition: ((CurrentTask) -> Boolean)): CurrentTask? {
        return currentTask?.let {
            if (condition(it)) {
                currentTask = null
                if (it.disposable is kotlinx.coroutines.Job) it.disposable.cancel()
                else if (it.disposable is Disposable) it.disposable.dispose()
                RepositoryUpdater.await()
                it
            } else {
                null
            }
        }
    }

    private val stateNotificationBuilder by lazy {
        NotificationCompat
            .Builder(this, NOTIFICATION_CHANNEL_SYNCING)
            .setSmallIcon(R.drawable.ic_sync)
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
        if (force || currentTask?.lastState != state) {
            currentTask = currentTask?.copy(lastState = state)
            if (started == Started.MANUAL) {
                startForeground(NOTIFICATION_ID_SYNCING, stateNotificationBuilder.apply {
                    when (state) {
                        is State.Connecting -> {
                            setContentTitle(getString(R.string.syncing_FORMAT, state.name))
                            setContentText(getString(R.string.connecting))
                            setProgress(0, 0, true)
                        }
                        is State.Syncing    -> {
                            setContentTitle(getString(R.string.syncing_FORMAT, state.name))
                            when (state.stage) {
                                RepositoryUpdater.Stage.DOWNLOAD -> {
                                    if (state.total != null) {
                                        setContentText("${state.read.formatSize()} / ${state.total.formatSize()}")
                                        setProgress(
                                            100,
                                            (100f * state.read / state.total).roundToInt(),
                                            false
                                        )
                                    } else {
                                        setContentText(state.read.formatSize())
                                        setProgress(0, 0, true)
                                    }
                                }
                                RepositoryUpdater.Stage.PROCESS  -> {
                                    val progress =
                                        state.total?.let { 100f * state.read / it }?.roundToInt()
                                    setContentText(
                                        getString(
                                            R.string.processing_FORMAT,
                                            "${progress ?: 0}%"
                                        )
                                    )
                                    setProgress(100, progress ?: 0, progress == null)
                                }
                                RepositoryUpdater.Stage.MERGE    -> {
                                    val progress = (100f * state.read / (state.total
                                        ?: state.read)).roundToInt()
                                    setContentText(
                                        getString(
                                            R.string.merging_FORMAT,
                                            "${state.read} / ${state.total ?: state.read}"
                                        )
                                    )
                                    setProgress(100, progress, false)
                                }
                                RepositoryUpdater.Stage.COMMIT   -> {
                                    setContentText(getString(R.string.saving_details))
                                    setProgress(0, 0, true)
                                }
                            }
                        }
                        is State.Finishing  -> {
                            setContentTitle(getString(R.string.syncing))
                            setContentText(null)
                            setProgress(0, 0, true)
                        }
                    }::class
                }.build())
            }
        }
    }

    private fun handleSetStarted() {
        stateNotificationBuilder.setWhen(System.currentTimeMillis())
    }

    private fun handleNextTask(hasUpdates: Boolean) {
        if (currentTask == null) {
            scope.launch {
                if (tasks.isNotEmpty()) {
                    val task = tasks.removeAt(0)
                    val repository = db.repositoryDao.get(task.repositoryId)
                    if (repository != null && repository.enabled && task.repositoryId != EXODUS_TRACKERS_SYNC) {
                        val lastStarted = started
                        val newStarted =
                            if (task.manual || lastStarted == Started.MANUAL) Started.MANUAL else Started.AUTO
                        started = newStarted
                        if (newStarted == Started.MANUAL && lastStarted != Started.MANUAL) {
                            startSelf()
                            handleSetStarted()
                        }
                        val initialState = State.Connecting(repository.name)
                        publishForegroundState(true, initialState)
                        val unstable = Preferences[Preferences.Key.UpdateUnstable]
                        lateinit var disposable: Disposable
                        disposable = RepositoryUpdater
                            .update(
                                this@SyncService,
                                repository,
                                unstable
                            ) { stage, progress, total ->
                                if (!disposable.isDisposed) {
                                    scope.launch {
                                        mutableStateSubject.emit(
                                            State.Syncing(
                                                repository.name,
                                                stage,
                                                progress,
                                                total
                                            )
                                        )
                                    }
                                }
                            }
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe { result, throwable ->
                                currentTask = null
                                throwable?.printStackTrace()
                                if (throwable != null && task.manual) {
                                    showNotificationError(repository, throwable as Exception)
                                }
                                handleNextTask(result == true || hasUpdates)
                            }
                        currentTask = CurrentTask(task, disposable, hasUpdates, initialState)
                    } else if (task.repositoryId == EXODUS_TRACKERS_SYNC) {
                        fetchTrackers()
                        handleNextTask(hasUpdates)
                    } else {
                        handleNextTask(hasUpdates)
                    }
                } else if (started != Started.NO) {
                    val disposable = scope.launch {
                        db.productDao
                            .queryObject(
                                installed = true,
                                updates = true,
                                section = Section.All,
                                order = Order.NAME,
                                ascending = true,
                            ).map { it.toItem() }.let { result ->
                                if (result.isNotEmpty()) {
                                    if (hasUpdates && Preferences[Preferences.Key.UpdateNotify] &&
                                        updateNotificationBlockerFragment?.get()?.isAdded != true &&
                                        result.isNotEmpty()
                                    ) displayUpdatesNotification(
                                        result,
                                        currentTask?.task?.manual == true
                                    )
                                    if (Preferences[Preferences.Key.InstallAfterSync]) {
                                        downloadServiceMutex.lock()
                                        batchUpdate(result)
                                    }
                                }
                                currentTask = null
                                //handleNextTask(false)
                            }
                    }
                    scope.launch {
                        db.productDao
                            .queryObject(
                                installed = true,
                                updates = false,
                                section = Section.All,
                                order = Order.NAME,
                                ascending = true,
                            ).filter { it.antiFeatures.contains(AntiFeature.KNOWN_VULN.key) }
                            .let { installedWithVulns ->
                                if (installedWithVulns.isNotEmpty())
                                    displayVulnerabilitiesNotification(
                                        installedWithVulns.map(Product::toItem)
                                    )
                            }
                    }
                    if (hasUpdates) {
                        currentTask = CurrentTask(null, disposable, true, State.Finishing)
                    } else {
                        scope.launch {
                            disposable.join()
                            downloadServiceMutex.withLock {
                                Log.i(this::javaClass.name, "emitting finish: had no updates")
                                mutableFinishState.emit(Unit)
                            }
                        }
                        val needStop = started == Started.MANUAL
                        started = Started.NO
                        if (needStop) {
                            stopForeground(true)
                            stopSelf()
                        }
                    }
                }
            }
        }
    }

    /**
     * Performs automatic update after a repo sync if it is enabled. Otherwise, it continues on to
     * displayUpdatesNotification.
     *
     * @param productItems a list of apps pending updates
     * @see SyncService.displayUpdatesNotification
     */
    private fun batchUpdate(productItems: List<ProductItem>, install: Boolean = false) {
        scope.launch {
            // run startUpdate on every item
            productItems.map { productItem ->
                Triple(
                    productItem.packageName,
                    db.installedDao.get(productItem.packageName),
                    db.repositoryDao.get(productItem.repositoryId)
                )
            }
                .filter { (_, installed, repo) -> (install || installed != null) && repo != null }
                .map { (packageName, installed, repo) ->
                    val productRepository = db.productDao.get(packageName)
                        .filterNotNull()
                        .filter { product -> product.repositoryId == repo!!.id }
                        .map { product -> Pair(product, repo!!) }

                    scope.launch {
                        Utils.startUpdate(
                            packageName,
                            installed,
                            productRepository,
                            downloadConnection
                        )
                    }
                }.let {
                    it.forEach { job -> job.join() }
                    downloadServiceMutex.unlock()
                }
        }
    }

    private fun fetchTrackers() {
        scope.launch {
            try {
                val trackerList = repoExodusAPI.getTrackers()

                // TODO **conditionally** update DB with the trackers
                db.trackerDao.insertReplace(
                    *trackerList.trackers
                        .map { (key, value) ->
                            Tracker(
                                key.toInt(),
                                value.name,
                                value.network_signature,
                                value.code_signature,
                                value.creation_date,
                                value.website,
                                value.description,
                                value.categories
                            )
                        }.toTypedArray()
                )
            } catch (e: Exception) {
                Log.e(this::javaClass.name, "Failed fetching exodus trackers", e)
            }
        }
    }

    private fun fetchExodusData(packageName: String) {
        scope.launch {
            try {
                val exodusDataList = repoExodusAPI.getExodusInfo(packageName)
                val latestExodusApp = exodusDataList.maxByOrNull { it.version_code.toLong() }
                    ?: ExodusInfo()

                val exodusInfo = latestExodusApp.toExodusInfo(packageName)
                Log.e(this::javaClass.name, exodusInfo.toString())
                db.exodusInfoDao.insertReplace(exodusInfo)
            } catch (e: Exception) {
                Log.e(this::javaClass.name, "Failed fetching exodus info", e)
            }
        }
    }

    class Job : JobService() {
        init {
            Configuration.Builder().setJobSchedulerJobIdRange(1000, 2000).build()
        }

        private val jobScope = CoroutineScope(Dispatchers.Default)
        private var syncParams: JobParameters? = null
        private val syncConnection =
            Connection(SyncService::class.java, onBind = { connection, binder ->
                jobScope.launch {
                    binder.finish.collect {
                        val params = syncParams
                        if (params != null) {
                            syncParams = null
                            connection.unbind(this@Job)
                            jobFinished(params, false)
                        }
                    }
                }
                binder.sync(SyncRequest.AUTO)
            }, onUnbind = { _, binder ->
                binder.cancelAuto()
                jobScope.cancel()
                val params = syncParams
                if (params != null) {
                    syncParams = null
                    jobFinished(params, true)
                }
            })

        override fun onStartJob(params: JobParameters): Boolean {
            syncParams = params
            syncConnection.bind(this)
            return true
        }

        override fun onStopJob(params: JobParameters): Boolean {
            syncParams = null
            jobScope.cancel()
            val reschedule = syncConnection.binder?.cancelAuto() == true
            syncConnection.unbind(this)
            return reschedule
        }
    }
}