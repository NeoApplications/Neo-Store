package com.machiav3lli.fdroid.service

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import android.view.ContextThemeWrapper
import androidx.core.app.NotificationCompat
import com.machiav3lli.fdroid.BuildConfig
import com.machiav3lli.fdroid.EXODUS_TRACKERS_SYNC
import com.machiav3lli.fdroid.NOTIFICATION_CHANNEL_SYNCING
import com.machiav3lli.fdroid.NOTIFICATION_ID_SYNCING
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
import com.machiav3lli.fdroid.utility.extension.resources.getColorFromAttr
import com.machiav3lli.fdroid.utility.extension.text.formatSize
import com.machiav3lli.fdroid.utility.showNotificationError
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class SyncService : ConnectionService<SyncService.Binder>() {
    companion object {
        const val ACTION_CANCEL = "${BuildConfig.APPLICATION_ID}.intent.action.CANCEL"

        private val mutableStateSubject = MutableSharedFlow<State>()
        private val stateSubject = mutableStateSubject.asSharedFlow()
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
        val task: Task?, val job: kotlinx.coroutines.Job,
        val hasUpdates: Boolean, val lastState: State,
    )

    private enum class Started { NO, AUTO, MANUAL }

    private val scope = CoroutineScope(Dispatchers.Default)

    private var started = Started.NO
    private val tasks = mutableListOf<Task>()
    private var currentTask: CurrentTask? = null

    private val downloadServiceMutex = Mutex()

    @Inject
    lateinit var repoExodusAPI: RExodusAPI

    inner class Binder : android.os.Binder() { // TODO migrate to Worker

        fun updateApps(products: List<ProductItem>) = batchUpdate(products)
        fun installApps(products: List<ProductItem>) = batchUpdate(products, true)

        fun fetchExodusInfo(packageName: String) = fetchExodusData(packageName)

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

        suspend fun deleteRepository(repositoryId: Long): Boolean = withContext(Dispatchers.IO) {
            val repository = db.repositoryDao.get(repositoryId)
            repository != null && run {
                setEnabled(repository, false)
                db.repositoryDao.deleteById(repository.id)
                true
            }
        }
    }

    private val binder = Binder()
    override fun onBind(intent: Intent): Binder = binder
    lateinit var db: DatabaseX

    override fun onCreate() {
        super.onCreate()
        db = DatabaseX.getInstance(applicationContext)

        stateSubject.onEach { publishForegroundState(false, it) }.launchIn(scope)
    }

    override fun onDestroy() {
        super.onDestroy()
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
                it.job.cancel()
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
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
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

                        val job = scope.launch {
                            try {
                                val result = RepositoryUpdater.update(
                                    this@SyncService,
                                    repository,
                                    unstable
                                ) { stage, progress, total ->
                                    CoroutineScope(Dispatchers.Main).launch {
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

                                currentTask = null
                                handleNextTask(result || hasUpdates)
                            } catch (throwable: Throwable) {
                                throwable.printStackTrace()
                                if (task.manual) {
                                    showNotificationError(repository, throwable as Exception)
                                }
                                handleNextTask(hasUpdates)
                            }
                        }
                        currentTask = CurrentTask(task, job, hasUpdates, initialState)
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
                                handleNextTask(false)
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
                            ).filter {
                                it.antiFeatures.contains(AntiFeature.KNOWN_VULN.key)
                                        && db.extrasDao[it.packageName]?.ignoreVulns != true
                            }.let { installedWithVulns ->
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
                            }
                            val needStop = started == Started.MANUAL
                            started = Started.NO

                            Log.i(this::javaClass.name, "need stop: $needStop")
                            if (needStop) {
                                stopForeground(true)
                                Log.i(this::javaClass.name, "stopping self")
                                stopSelf()
                            }
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
        // run startUpdate on every item
        scope.launch {
            runBlocking {
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
                            .filter { product -> product.repositoryId == repo!!.id }
                            .map { product -> Pair(product, repo!!) }
                        async {
                            Utils.startUpdate(
                                packageName,
                                installed,
                                productRepository
                            )
                        }
                    }.forEach { it.await() }
            }
            if (downloadServiceMutex.isLocked) downloadServiceMutex.unlock()
        }
    }

    private fun fetchTrackers() {
        if (Preferences[Preferences.Key.ShowTrackers]) {
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
    }

    private fun fetchExodusData(packageName: String) {
        if (Preferences[Preferences.Key.ShowTrackers]) {
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
    }
}