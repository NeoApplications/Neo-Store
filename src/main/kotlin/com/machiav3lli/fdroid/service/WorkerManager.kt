package com.machiav3lli.fdroid.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Data
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
import com.machiav3lli.fdroid.ARG_PACKAGE_NAME
import com.machiav3lli.fdroid.ARG_RESULT_CODE
import com.machiav3lli.fdroid.ARG_VALIDATION_ERROR
import com.machiav3lli.fdroid.MainApplication
import com.machiav3lli.fdroid.NOTIFICATION_CHANNEL_DOWNLOADING
import com.machiav3lli.fdroid.NOTIFICATION_CHANNEL_SYNCING
import com.machiav3lli.fdroid.NOTIFICATION_CHANNEL_UPDATES
import com.machiav3lli.fdroid.NOTIFICATION_CHANNEL_VULNS
import com.machiav3lli.fdroid.NOTIFICATION_ID_DOWNLOADING
import com.machiav3lli.fdroid.NOTIFICATION_ID_SYNCING
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.TAG_SYNC_ONETIME
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.database.entity.Downloaded
import com.machiav3lli.fdroid.database.entity.InstallTask
import com.machiav3lli.fdroid.database.entity.Product
import com.machiav3lli.fdroid.entity.AntiFeature
import com.machiav3lli.fdroid.entity.Order
import com.machiav3lli.fdroid.entity.ProductItem
import com.machiav3lli.fdroid.entity.Section
import com.machiav3lli.fdroid.service.worker.DownloadState
import com.machiav3lli.fdroid.service.worker.DownloadWorker
import com.machiav3lli.fdroid.service.worker.InstallWorker
import com.machiav3lli.fdroid.service.worker.SyncState
import com.machiav3lli.fdroid.service.worker.SyncWorker
import com.machiav3lli.fdroid.service.worker.ValidationError
import com.machiav3lli.fdroid.utility.Utils
import com.machiav3lli.fdroid.utility.displayUpdatesNotification
import com.machiav3lli.fdroid.utility.displayVulnerabilitiesNotification
import com.machiav3lli.fdroid.utility.downloadNotificationBuilder
import com.machiav3lli.fdroid.utility.extension.android.Android
import com.machiav3lli.fdroid.utility.extension.text.formatSize
import com.machiav3lli.fdroid.utility.syncNotificationBuilder
import com.machiav3lli.fdroid.utility.updateProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.dsl.module

class WorkerManager(appContext: Context) : KoinComponent {

    val workManager: WorkManager by inject()
    private val actionReceiver: ActionReceiver by inject()
    var context: Context = appContext
    val notificationManager: NotificationManagerCompat
    private val syncWorkInfoFlow = MutableStateFlow<List<WorkInfo>>(emptyList())
    private val downloadWorkInfoFlow = MutableStateFlow<List<WorkInfo>>(emptyList())
    private val installMutex = Mutex()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        context.registerReceiver(actionReceiver, IntentFilter())

        notificationManager = NotificationManagerCompat.from(context)
        if (Android.sdk(Build.VERSION_CODES.O)) createNotificationChannels()

        workManager.pruneWork()
        scope.launch {
            workManager.getWorkInfosByTagFlow(SyncWorker::class.qualifiedName!!)
                .collect { onSyncProgress(this@WorkerManager, it.toMutableList()) }
        }
        scope.launch {
            workManager.getWorkInfosByTagFlow(DownloadWorker::class.qualifiedName!!)
                .collect { onDownloadProgress(this@WorkerManager, it.toMutableList()) }
        }
        scope.launch {
            MainApplication.db.getInstallTaskDao()
                .getAllFlow() // Add similar table for DownloadTasks
                .distinctUntilChanged()
                .collectLatest { tasks ->
                    if (tasks.isNotEmpty()) {
                        //prune()
                        enqueueTasks(tasks)
                    }
                }
        }
    }

    val repositorySyncWorkersFlow: Flow<List<WorkInfo>> get() {
        return workManager.getWorkInfosByTagFlow(SyncWorker::class.qualifiedName.orEmpty())
            .map { syncWorkerList ->
                syncWorkerList.filter { workInfo ->
                    val task = SyncWorker.getTask(workInfo.progress)
                    task.repositoryId != NON_EXISTENT_REPOSITORY_ID
                }
            }
    }

    fun release(): WorkerManager? {
        context.unregisterReceiver(actionReceiver)
        return null
    }

    fun prune() {
        workManager.pruneWork()
    }

    fun cancelSyncAll() {
        SyncWorker::class.qualifiedName?.let {
            workManager.cancelAllWorkByTag(it)
            prune()
        }
        MainApplication.setProgress() // TODO re-consider
    }

    fun cancelSync(repoId: Long = -1) {
        SyncWorker::class.qualifiedName?.let {
            workManager.cancelAllWorkByTag(
                if (repoId != -1L) "sync_$repoId"
                else TAG_SYNC_ONETIME
            )
        }
    }

    fun cancelDownloadAll() {
        DownloadWorker::class.qualifiedName?.let {
            workManager.cancelAllWorkByTag(it)
            prune()
        }
        MainApplication.setProgress() // TODO re-consider
    }

    fun cancelDownload(packageName: String?) {
        DownloadWorker::class.qualifiedName?.let {
            workManager.cancelAllWorkByTag(
                if (packageName != null) "download_$packageName"
                else it
            )
            // TODO upsert Error to DB.Downloaded
        }
    }

    fun cancelInstall(packageName: String) {
        DownloadWorker::class.qualifiedName?.let {
            workManager.cancelUniqueWork("Installer_$packageName")
            scope.launch {
                MainApplication.db.getInstallTaskDao().delete(packageName)
            }
            //prune()
        }
    }

    fun install(vararg product: ProductItem) = batchUpdate(product.toList(), true)

    fun update(vararg product: ProductItem) = batchUpdate(product.toList(), false)

    private fun batchUpdate(productItems: List<ProductItem>, enforce: Boolean = false) {
        scope.launch(Dispatchers.IO) {
            productItems.map { productItem ->
                Triple(
                    productItem.packageName,
                    MainApplication.db.getInstalledDao().get(productItem.packageName),
                    MainApplication.db.getRepositoryDao().get(productItem.repositoryId)
                )
            }
                .filter { (_, installed, repo) -> (enforce || installed != null) && repo != null }
                .forEach { (packageName, installed, repo) ->
                    val productRepository = MainApplication.db.getProductDao().get(packageName)
                        .filter { product -> product.repositoryId == repo!!.id }
                        .map { product -> Pair(product, repo!!) }
                    scope.launch(Dispatchers.IO) {
                        Utils.startUpdate(
                            packageName,
                            installed,
                            productRepository
                        )
                    }
                }

        }
    }

    private fun launchInstaller(installTasks: List<InstallTask>) = scope.launch(Dispatchers.IO) {
        installTasks.forEach {
            InstallWorker.enqueue(it.packageName, it.label, it.cacheFileName)
        }
    }

    private suspend fun enqueueTasks(tasks: List<InstallTask>) = installMutex.withLock {
        val enqeuedWorks = workManager.getWorkInfos(
            WorkQuery.Builder
                .fromTags(listOf(InstallWorker::class.java.name))
                .addStates(listOf(WorkInfo.State.RUNNING, WorkInfo.State.ENQUEUED))
                .build()
        ).get()

        if (enqeuedWorks.isEmpty()) {
            // No InstallWorker is currently running, so we can start a new one
            val nextTask = tasks.firstOrNull()
            nextTask?.let {
                InstallWorker.enqueue(
                    packageName = it.packageName,
                    label = it.label,
                    fileName = it.cacheFileName
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannels() {
        NotificationChannel(
            NOTIFICATION_CHANNEL_DOWNLOADING,
            context.getString(R.string.downloading),
            NotificationManager.IMPORTANCE_LOW
        )
            .apply { setShowBadge(false) }
            .let(notificationManager::createNotificationChannel)
        NotificationChannel(
            NOTIFICATION_CHANNEL_SYNCING,
            context.getString(R.string.syncing),
            NotificationManager.IMPORTANCE_LOW
        )
            .apply { setShowBadge(false) }
            .let(notificationManager::createNotificationChannel)
        NotificationChannel(
            NOTIFICATION_CHANNEL_UPDATES,
            context.getString(R.string.updates), NotificationManager.IMPORTANCE_LOW
        )
            .let(notificationManager::createNotificationChannel)
        NotificationChannel(
            NOTIFICATION_CHANNEL_VULNS,
            context.getString(R.string.vulnerabilities), NotificationManager.IMPORTANCE_HIGH
        ).let(notificationManager::createNotificationChannel)
    }

    companion object {
        private const val NON_EXISTENT_REPOSITORY_ID = -1L
        private val syncsRunning = SnapshotStateMap<Long, SyncState?>()
        private val downloadsRunning = SnapshotStateMap<String, DownloadState>()

        private val syncNotificationBuilder by lazy {
            NotificationCompat
                .Builder(MainApplication.context, NOTIFICATION_CHANNEL_SYNCING)
                .setGroup(NOTIFICATION_CHANNEL_SYNCING)
                .setGroupSummary(true)
                .setSortKey("0")
                .setSmallIcon(R.drawable.ic_sync)
                .setContentTitle(MainApplication.context.getString(R.string.syncing))
                .setOngoing(true)
                .setSilent(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .addAction(
                    R.drawable.ic_cancel,
                    MainApplication.context.getString(R.string.cancel_all),
                    PendingIntent.getBroadcast(
                        MainApplication.context,
                        "<SYNC_ALL>".hashCode(),
                        Intent(MainApplication.context, ActionReceiver::class.java).apply {
                            action = ActionReceiver.COMMAND_CANCEL_SYNC_ALL
                        },
                        PendingIntent.FLAG_IMMUTABLE
                    )
                )
        }

        private val downloadNotificationBuilder by lazy {
            NotificationCompat
                .Builder(MainApplication.context, NOTIFICATION_CHANNEL_DOWNLOADING)
                .setGroup(NOTIFICATION_CHANNEL_DOWNLOADING)
                .setGroupSummary(true)
                .setSortKey("0")
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setOngoing(true)
                .setSilent(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .addAction(
                    R.drawable.ic_cancel,
                    MainApplication.context.getString(R.string.cancel_all),
                    PendingIntent.getBroadcast(
                        MainApplication.context,
                        "<DOWNLOAD_ALL>".hashCode(),
                        Intent(MainApplication.context, ActionReceiver::class.java).apply {
                            action = ActionReceiver.COMMAND_CANCEL_DOWNLOAD_ALL
                        },
                        PendingIntent.FLAG_IMMUTABLE
                    )
                )
        }

        private var lockSyncProgress = object {}
        private var lockDownloadProgress = object {}

        fun onSyncProgress(handler: WorkerManager, syncs: MutableList<WorkInfo>? = null) {
            onSyncProgressNoSync(handler, syncs)
        }

        fun onDownloadProgress(handler: WorkerManager, downloads: MutableList<WorkInfo>? = null) {
            onDownloadProgressNoSync(handler, downloads)
        }

        private fun onSyncProgressNoSync(
            manager: WorkerManager,
            workInfos: MutableList<WorkInfo>? = null,
        ) {
            val syncs = workInfos
                ?: manager.workManager
                    .getWorkInfosByTag(SyncWorker::class.qualifiedName!!)
                    .get()
                ?: return

            val context = MainApplication.context
            updateSyncsRunning(syncs)

            syncsRunning.forEach { (repoId, state) ->
                val notificationBuilder = context.syncNotificationBuilder()
                val repoName = MainApplication.db.getRepositoryDao().getRepoName(repoId)

                when (state) {
                    is SyncState.Connecting -> {
                        notificationBuilder
                            .setContentTitle(
                                context.getString(
                                    R.string.syncing_FORMAT,
                                    repoName
                                )
                            )
                            .setContentText(context.getString(R.string.connecting))
                            .setProgress(0, 0, true)
                    }

                    is SyncState.Syncing    -> {
                        notificationBuilder
                            .setContentTitle(
                                context.getString(
                                    R.string.syncing_FORMAT,
                                    repoName
                                )
                            )
                            .updateProgress(context, state.progress)
                    }

                    is SyncState.Failed     -> {
                        notificationBuilder
                            .setContentTitle(
                                context.getString(
                                    R.string.syncing_FORMAT,
                                    repoName
                                )
                            )
                            .setContentText(context.getString(R.string.action_failed))
                            .setSmallIcon(R.drawable.ic_new_releases)

                    }

                    else                    -> {}
                }

                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    if (state != null) MainApplication.wm.notificationManager
                        .notify(
                            NOTIFICATION_ID_SYNCING + repoId.toInt(),
                            notificationBuilder.setOngoing(state !is SyncState.Failed)
                                .build()
                        ) else MainApplication.wm.notificationManager
                        .cancel(NOTIFICATION_ID_SYNCING + repoId.toInt())
                }
            }

            if (syncsRunning.values.any { it?.isRunning == true })
                MainApplication.wm.notificationManager.notify(
                    NOTIFICATION_ID_SYNCING,
                    syncNotificationBuilder.build()
                )
            else CoroutineScope(Dispatchers.IO).launch {
                MainApplication.wm.notificationManager
                    .cancel(NOTIFICATION_ID_SYNCING)
                MainApplication.db.getRepositoryDao().getAllEnabledIds().forEach {
                    if (syncsRunning[it] == null) MainApplication.wm.notificationManager
                        .cancel(NOTIFICATION_ID_SYNCING + it.toInt())
                }
                MainApplication.db.getProductDao()
                    .queryObject(
                        installed = true,
                        updates = true,
                        section = Section.All,
                        order = Order.NAME,
                        ascending = true,
                    )
                    .map { it.toItem() }
                    .let { result ->
                        if (result.isNotEmpty() && Preferences[Preferences.Key.UpdateNotify])
                            context.displayUpdatesNotification(result, true)
                        if (Preferences[Preferences.Key.InstallAfterSync]) {
                            MainApplication.wm.update(*result.toTypedArray())
                        }
                    }
                MainApplication.db.getProductDao()
                    .queryObject(
                        installed = true,
                        updates = false,
                        section = Section.All,
                        order = Order.NAME,
                        ascending = true,
                    ).filter { product ->
                        product.antiFeatures.contains(AntiFeature.KNOWN_VULN.key)
                                && MainApplication.db.getExtrasDao()[product.packageName]?.ignoreVulns != true
                    }.let { installedWithVulns ->
                        if (installedWithVulns.isNotEmpty())
                            context.displayVulnerabilitiesNotification(
                                installedWithVulns.map(Product::toItem)
                            )
                    }
            }
        }

        private fun updateSyncsRunning(workers: List<WorkInfo>) {
            val tasks = workers.mapNotNull { wi ->
                (wi.outputData.takeIf { it != Data.EMPTY } ?: wi.progress)
                    .takeIf { it != Data.EMPTY }?.let { data ->
                        val task = SyncWorker.getTask(data)
                        val dataState = SyncWorker.getState(data)

                        syncsRunning.compute(task.repositoryId) { _, _ ->
                            when (wi.state) {
                                WorkInfo.State.ENQUEUED,
                                WorkInfo.State.BLOCKED,
                                WorkInfo.State.SUCCEEDED,
                                WorkInfo.State.CANCELLED,
                                -> null

                                WorkInfo.State.RUNNING,
                                -> dataState

                                WorkInfo.State.FAILED,
                                -> SyncState.Failed
                            }
                        }
                        task
                    }
            }
            syncsRunning.keys.retainAll { repoId ->
                tasks.any { task -> task.repositoryId == repoId }
            }
        }

        private fun onDownloadProgressNoSync(
            manager: WorkerManager,
            workInfos: MutableList<WorkInfo>? = null,
        ) {
            val ioScope = CoroutineScope(Dispatchers.IO)
            val downloads = workInfos
                ?: manager.workManager
                    .getWorkInfosByTag(DownloadWorker::class.qualifiedName!!)
                    .get()
                ?: return

            val appContext = MainApplication.context
            updateDownloadsRunning(downloads)

            var allCount = downloads.size
            var allProcessed = 0
            var allFailed = 0

            downloadsRunning.forEach { (key, state) ->
                val notificationBuilder = appContext.downloadNotificationBuilder()
                var cancelNotification = false

                MainApplication.db.getDownloadedDao().upsert(
                    Downloaded(
                        packageName = state.packageName,
                        version = state.version,
                        repositoryId = state.repoId,
                        cacheFileName = state.cacheFileName,
                        changed = System.currentTimeMillis(),
                        state = state,
                    )
                )

                val cancelIntent = Intent(appContext, ActionReceiver::class.java).apply {
                    action = ActionReceiver.COMMAND_CANCEL_DOWNLOAD
                    putExtra(ARG_PACKAGE_NAME, state.packageName)
                }
                val cancelPendingIntent = PendingIntent.getBroadcast(
                    appContext,
                    key.hashCode(),
                    cancelIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )

                when (state) {
                    is DownloadState.Pending,
                    is DownloadState.Connecting
                    -> {
                        notificationBuilder
                            .setContentTitle(
                                appContext.getString(
                                    R.string.downloading_FORMAT,
                                    "${state.name} (${state.version})"
                                )
                            )
                            .setContentText(appContext.getString(R.string.pending))
                            .setProgress(1, 0, true)
                            .addAction(
                                R.drawable.ic_cancel,
                                appContext.getString(R.string.cancel),
                                cancelPendingIntent
                            )
                    }

                    is DownloadState.Downloading
                    -> {
                        notificationBuilder
                            .setContentTitle(
                                appContext.getString(
                                    R.string.downloading_FORMAT,
                                    "${state.name} (${state.version})"
                                )
                            )
                            .setContentText("${state.read.formatSize()} / ${state.total?.formatSize()}")
                            .setProgress(100, state.progress, false)
                            .addAction(
                                R.drawable.ic_cancel,
                                appContext.getString(R.string.cancel),
                                cancelPendingIntent
                            )
                    }

                    is DownloadState.Cancel
                    -> {
                        allCount -= 1
                        notificationBuilder
                            .setOngoing(false)
                            .setContentTitle(
                                appContext.getString(
                                    R.string.downloading_FORMAT,
                                    "${state.name} (${state.version})"
                                )
                            )
                            .setContentText(appContext.getString(R.string.canceled)) //. stopReason: ${wi.stopReason}")
                            .setTimeoutAfter(InstallerReceiver.INSTALLED_NOTIFICATION_TIMEOUT)
                        cancelNotification = true
                    }

                    is DownloadState.Success
                    -> {
                        allProcessed += 1
                        notificationBuilder.setOngoing(false)
                            .setContentTitle(
                                appContext.getString(
                                    R.string.downloaded_FORMAT,
                                    state.name
                                )
                            )
                        ioScope.launch {
                            MainApplication.db.getInstallTaskDao()
                                .put(state.toInstallTask())
                        }
                        if (!Preferences[Preferences.Key.KeepInstallNotification]) {
                            notificationBuilder.setTimeoutAfter(InstallerReceiver.INSTALLED_NOTIFICATION_TIMEOUT)
                            cancelNotification = true
                        }
                    }

                    is DownloadState.Error
                    -> {
                        allProcessed += 1
                        allFailed += 1
                        Log.i(
                            this::class.java.name,
                            "download error for package: ${state.packageName}. stopReason: ${state.stopReason}" // TODO add stopReason string
                        )
                    }
                }

                if (state is DownloadState.Error && state.stopReason != WorkInfo.STOP_REASON_NOT_STOPPED) Log.i(
                    this::class.java.name,
                    "stopReason: ${state.stopReason} for download task: ${state.packageName}"
                )

                if (cancelNotification)
                    MainApplication.wm.notificationManager.cancel(key.hashCode())
                else if (ActivityCompat.checkSelfPermission(
                        appContext,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) MainApplication.wm.notificationManager
                    .notify(
                        key.hashCode(),
                        notificationBuilder.build(),
                    )
            }

            manager.prune()
            downloadNotificationBuilder
                .setProgress(allCount, allProcessed, false)
                .setContentText("$allProcessed / $allCount")
                .apply { // TODO fix pinned notification
                    setOngoing(allCount > allProcessed)
                    if (allProcessed == allCount)
                        setTimeoutAfter(InstallerReceiver.INSTALLED_NOTIFICATION_TIMEOUT)

                    if (ActivityCompat.checkSelfPermission(
                            appContext,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED && allCount > 0
                    ) MainApplication.wm.notificationManager.notify(
                        NOTIFICATION_CHANNEL_DOWNLOADING,
                        NOTIFICATION_ID_DOWNLOADING,
                        this.build()
                    )
                }
        }

        private fun updateDownloadsRunning(workers: List<WorkInfo>) {
            val tasks = workers.mapNotNull { wi ->
                (wi.outputData.takeIf { it != Data.EMPTY } ?: wi.progress)
                    .takeIf { it != Data.EMPTY }?.let { data ->
                        val task = DownloadWorker.getTask(data)
                        val progress = DownloadWorker.getProgress(data)
                        val resultCode = data.getInt(ARG_RESULT_CODE, 0)
                        val validationError = ValidationError.entries[
                            data.getInt(ARG_VALIDATION_ERROR, 0)
                        ]

                        downloadsRunning.compute(task.key) { _, _ ->
                            when (wi.state) {
                                WorkInfo.State.ENQUEUED,
                                WorkInfo.State.BLOCKED   -> DownloadState.Pending(
                                    packageName = task.packageName,
                                    name = task.name,
                                    version = task.release.version,
                                    cacheFileName = task.release.cacheFileName,
                                    repoId = task.repoId,
                                    blocked = wi.state == WorkInfo.State.BLOCKED
                                )

                                WorkInfo.State.RUNNING   -> DownloadState.Downloading(
                                    packageName = task.packageName,
                                    name = task.name,
                                    version = task.release.version,
                                    cacheFileName = task.release.cacheFileName,
                                    repoId = task.repoId,
                                    read = progress.read,
                                    total = progress.total,
                                )

                                WorkInfo.State.CANCELLED -> DownloadState.Cancel(
                                    packageName = task.packageName,
                                    name = task.name,
                                    version = task.release.version,
                                    cacheFileName = task.release.cacheFileName,
                                    repoId = task.repoId,
                                )

                                WorkInfo.State.SUCCEEDED -> DownloadState.Success(
                                    packageName = task.packageName,
                                    name = task.name,
                                    version = task.release.version,
                                    cacheFileName = task.release.cacheFileName,
                                    repoId = task.repoId,
                                    release = task.release,
                                )

                                WorkInfo.State.FAILED    -> DownloadState.Error(
                                    packageName = task.packageName,
                                    name = task.name,
                                    version = task.release.version,
                                    cacheFileName = task.release.cacheFileName,
                                    repoId = task.repoId,
                                    resultCode = resultCode,
                                    validationError = validationError,
                                    stopReason = wi.stopReason
                                )
                            }
                        }
                        task
                    }
            }
            downloadsRunning.keys.retainAll { key ->
                tasks.any { task -> task.key == key }
            }
        }
    }
}

val workmanagerModule = module {
    single { WorkerManager(get()) }
    single { WorkManager.getInstance(get()) }
    single { ActionReceiver() }
}
