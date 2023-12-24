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
import com.machiav3lli.fdroid.ARG_PACKAGE_NAME
import com.machiav3lli.fdroid.ARG_REPOSITORY_NAME
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
import com.machiav3lli.fdroid.service.worker.ErrorType
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
import com.machiav3lli.fdroid.utility.updateWithError
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.dsl.module

class WorkerManager(appContext: Context) {

    var workManager: WorkManager
    var actionReceiver: ActionReceiver
    var context: Context = appContext
    val notificationManager: NotificationManagerCompat
    private val scope = CoroutineScope(Dispatchers.Default)
    private val ioScope = CoroutineScope(Dispatchers.IO)

    init {
        workManager = WorkManager.getInstance(context)
        actionReceiver = ActionReceiver()

        context.registerReceiver(actionReceiver, IntentFilter())

        notificationManager = NotificationManagerCompat.from(context)
        if (Android.sdk(Build.VERSION_CODES.O)) createNotificationChannels()

        workManager.pruneWork()
        scope.launch {
            workManager.getWorkInfosByTagFlow(
                SyncWorker::class.qualifiedName!!
            ).stateIn(
                scope,
                SharingStarted.Eagerly,
                mutableListOf()
            ).collectLatest {
                onSyncProgress(this@WorkerManager, it)
            }
        }
        scope.launch {
            workManager.getWorkInfosByTagFlow(
                DownloadWorker::class.qualifiedName!!
            ).stateIn(
                scope,
                SharingStarted.Eagerly,
                mutableListOf()
            ).collectLatest {
                onDownloadProgress(this@WorkerManager, it)
            }
        }
        scope.launch {
            MainApplication.db.getInstallTaskDao()
                .getAllFlow() // Add similar table for DownloadTasks
                .stateIn(
                    scope,
                    SharingStarted.Eagerly,
                    mutableListOf()
                )
                .collectLatest {
                    if (it.isNotEmpty()) {
                        prune()
                        launchInstaller(it)
                    }
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
        MainApplication.wm.prune()
        SyncWorker::class.qualifiedName?.let {
            workManager.cancelAllWorkByTag(it)
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
        MainApplication.wm.prune()
        DownloadWorker::class.qualifiedName?.let {
            workManager.cancelAllWorkByTag(it)
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

    fun install(vararg product: ProductItem) = batchUpdate(product.toList(), true)

    fun update(vararg product: ProductItem) = batchUpdate(product.toList(), false)

    private fun batchUpdate(productItems: List<ProductItem>, enforce: Boolean = false) {
        scope.launch {
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
                    scope.launch {
                        Utils.startUpdate(
                            packageName,
                            installed,
                            productRepository
                        )
                    }
                }

        }
    }

    fun launchInstaller(installTasks: List<InstallTask>) = ioScope.launch {
        installTasks.forEach {
            InstallWorker.enqueue(it.packageName, it.label, it.cacheFileName)
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
        private val syncsRunning = SnapshotStateMap<Long, SyncState>()
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
            synchronized(lockSyncProgress) {
                onSyncProgressNoSync(handler, syncs)
            }
        }

        fun onDownloadProgress(handler: WorkerManager, downloads: MutableList<WorkInfo>? = null) {
            synchronized(lockDownloadProgress) {
                onDownloadProgressNoSync(handler, downloads)
            }
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
            syncsRunning.clear() // TODO re-evaluate

            syncs.forEach { wi ->
                val data = wi.outputData.takeIf {
                    it != Data.EMPTY
                } ?: wi.progress

                data.takeIf { it != Data.EMPTY }?.let { data ->
                    val task = SyncWorker.getTask(data)
                    val state = SyncWorker.getState(data)
                    val progress = SyncWorker.getProgress(data)
                    val repoName = data.getString(ARG_REPOSITORY_NAME)

                    val notificationBuilder = context.syncNotificationBuilder()

                    syncsRunning.compute(task.repositoryId) { _, _ ->
                        when (wi.state) {
                            WorkInfo.State.ENQUEUED,
                            WorkInfo.State.BLOCKED,
                            WorkInfo.State.SUCCEEDED,
                            WorkInfo.State.CANCELLED,
                            -> {
                                null
                            }

                            WorkInfo.State.RUNNING,
                            -> {
                                when (state) {
                                    SyncState.CONNECTING -> {
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

                                    SyncState.SYNCING    -> {
                                        notificationBuilder
                                            .setContentTitle(
                                                context.getString(
                                                    R.string.syncing_FORMAT,
                                                    repoName
                                                )
                                            )
                                            .updateProgress(context, progress)
                                    }

                                    else                 -> {}
                                }
                                state
                            }

                            WorkInfo.State.FAILED,
                            -> {
                                notificationBuilder
                                    .setContentTitle(
                                        context.getString(
                                            R.string.syncing_FORMAT,
                                            repoName
                                        )
                                    )
                                    .setContentText(context.getString(R.string.action_failed))
                                    .setSmallIcon(R.drawable.ic_new_releases)
                                SyncState.FAILED
                            }
                        }
                    }.let {
                        if (ActivityCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            if (it != null) MainApplication.wm.notificationManager
                                .notify(
                                    NOTIFICATION_ID_SYNCING + task.repositoryId.toInt(),
                                    notificationBuilder.setOngoing(it != SyncState.FAILED).build()
                                ) else MainApplication.wm.notificationManager
                                .cancel(NOTIFICATION_ID_SYNCING + task.repositoryId.toInt())
                        }
                    }
                }
            }

            if (syncsRunning.values.any { it != SyncState.FAILED })
                MainApplication.wm.notificationManager.notify(
                    NOTIFICATION_ID_SYNCING,
                    syncNotificationBuilder.build()
                )
            else CoroutineScope(Dispatchers.Default).launch {
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

        private fun onDownloadProgressNoSync(
            manager: WorkerManager,
            workInfos: MutableList<WorkInfo>? = null,
        ) {
            val downloads = workInfos
                ?: manager.workManager
                    .getWorkInfosByTag(DownloadWorker::class.qualifiedName!!)
                    .get()
                ?: return

            val appContext = MainApplication.context
            downloadsRunning.clear() // TODO re-evaluate

            var allCount = downloads.size
            var allProcessed = 0
            var allFailed = 0

            downloads.forEach { wi ->
                val data = wi.outputData.takeIf {
                    it != Data.EMPTY
                } ?: wi.progress
                data.takeIf { it != Data.EMPTY }?.let { data ->
                    val task = DownloadWorker.getTask(data)
                    val progress = DownloadWorker.getProgress(data)
                    val resultCode = data.getInt(ARG_RESULT_CODE, 0)
                    val validationError = ValidationError.values()[
                        data.getInt(ARG_VALIDATION_ERROR, 0)
                    ]

                    val notificationBuilder = appContext.downloadNotificationBuilder()
                    var cancelNotification = false

                    val cancelIntent = Intent(appContext, ActionReceiver::class.java).apply {
                        action = ActionReceiver.COMMAND_CANCEL_DOWNLOAD
                        putExtra(ARG_PACKAGE_NAME, task.packageName)
                    }
                    val cancelPendingIntent = PendingIntent.getBroadcast(
                        appContext,
                        task.key.hashCode(),
                        cancelIntent,
                        PendingIntent.FLAG_IMMUTABLE
                    )

                    downloadsRunning.compute(task.key) { _, _ ->
                        when (wi.state) {
                            WorkInfo.State.ENQUEUED  -> {
                                notificationBuilder
                                    .setContentTitle(
                                        appContext.getString(
                                            R.string.downloading_FORMAT,
                                            "${task.name} (${task.release.version})"
                                        )
                                    )
                                    .setContentText(appContext.getString(R.string.pending))
                                    .setProgress(1, 0, true)
                                    .addAction(
                                        R.drawable.ic_cancel,
                                        appContext.getString(R.string.cancel),
                                        cancelPendingIntent
                                    )
                                DownloadState.Pending(
                                    task.packageName,
                                    task.name,
                                    task.release.version,
                                    task.release.cacheFileName,
                                    task.repoId,
                                    wi.state == WorkInfo.State.BLOCKED
                                )
                            }

                            WorkInfo.State.RUNNING   -> {
                                notificationBuilder
                                    .setContentTitle(
                                        appContext.getString(
                                            R.string.downloading_FORMAT,
                                            "${task.name} (${task.release.version})"
                                        )
                                    )
                                    .setContentText("${progress.read.formatSize()} / ${progress.total.formatSize()}")
                                    .setProgress(100, progress.progress, false)
                                    .addAction(
                                        R.drawable.ic_cancel,
                                        appContext.getString(R.string.cancel),
                                        cancelPendingIntent
                                    )
                                DownloadState.Downloading(
                                    task.packageName,
                                    task.name,
                                    task.release.version,
                                    task.release.cacheFileName,
                                    task.repoId,
                                    progress.read,
                                    progress.total,
                                )
                            }

                            WorkInfo.State.CANCELLED -> {
                                allCount -= 1
                                notificationBuilder
                                    .setOngoing(false)
                                    .setContentTitle(
                                        appContext.getString(
                                            R.string.downloading_FORMAT,
                                            "${task.name} (${task.release.version})"
                                        )
                                    )
                                    .setContentText("${appContext.getString(R.string.canceled)}. stopReason: ${wi.stopReason}") // TODO add stopReason string
                                    .setTimeoutAfter(InstallerReceiver.INSTALLED_NOTIFICATION_TIMEOUT)
                                cancelNotification = true
                                DownloadState.Cancel(
                                    packageName = task.packageName,
                                    name = task.name,
                                    version = task.release.version,
                                    cacheFileName = task.release.cacheFileName,
                                    repoId = task.repoId,
                                )
                            }

                            WorkInfo.State.SUCCEEDED -> {
                                allProcessed += 1
                                notificationBuilder.setOngoing(false)
                                    .setContentTitle(
                                        appContext.getString(
                                            R.string.downloaded_FORMAT,
                                            task.name
                                        )
                                    )
                                // TODO add to InstallTask & Launch InstallerWork
                                CoroutineScope(Dispatchers.IO).launch {
                                    MainApplication.db.getInstallTaskDao().put(task.toInstallTask())
                                }
                                if (!Preferences[Preferences.Key.KeepInstallNotification]) {
                                    notificationBuilder.setTimeoutAfter(InstallerReceiver.INSTALLED_NOTIFICATION_TIMEOUT)
                                    cancelNotification = true
                                }
                                DownloadState.Success(
                                    packageName = task.packageName,
                                    name = task.name,
                                    version = task.release.version,
                                    cacheFileName = task.release.cacheFileName,
                                    repoId = task.repoId,
                                    release = task.release,
                                )
                            }

                            WorkInfo.State.FAILED    -> {
                                allProcessed += 1
                                allFailed += 1
                                val errorType = when {
                                    validationError != ValidationError.NONE
                                    -> ErrorType.Validation(validationError)

                                    resultCode != HttpStatusCode.GatewayTimeout.value
                                    -> ErrorType.Http(resultCode)

                                    else
                                    -> ErrorType.Network
                                }
                                notificationBuilder.updateWithError(appContext, task, errorType)
                                Log.i(
                                    this::class.java.name,
                                    "download error for package: ${task.packageName}. stopReason: ${wi.stopReason}"
                                )
                                DownloadState.Error(
                                    task.packageName,
                                    task.name,
                                    task.release.version,
                                    task.release.cacheFileName,
                                    task.repoId,
                                    resultCode,
                                    validationError,
                                )
                            }

                            WorkInfo.State.BLOCKED   -> null
                        }
                    }?.let {
                        CoroutineScope(Dispatchers.Default).launch { // TODO manage abrupt breaks
                            MainApplication.db.getDownloadedDao().upsert(
                                Downloaded(
                                    it.packageName,
                                    it.version,
                                    it.cacheFileName,
                                    System.currentTimeMillis(),
                                    it
                                )
                            )
                        }
                    }
                    val stopped = wi.stopReason != WorkInfo.STOP_REASON_NOT_STOPPED
                    if (stopped) Log.i(
                        this::class.java.name,
                        "stopReason: ${wi.stopReason} for download task: ${task.packageName}"
                    )

                    if (cancelNotification)
                        MainApplication.wm.notificationManager.cancel(task.key.hashCode())
                    else if (ActivityCompat.checkSelfPermission(
                            appContext,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                    ) MainApplication.wm.notificationManager
                        .notify(
                            task.key.hashCode(),
                            notificationBuilder.build(),
                        )
                }
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
    }
}

val workmanagerModule = module {
    single { WorkerManager(get()) }
}
