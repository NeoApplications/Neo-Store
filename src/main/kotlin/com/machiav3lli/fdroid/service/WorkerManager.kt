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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.withResumed
import androidx.work.Data
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.machiav3lli.fdroid.ARG_PACKAGE_NAME
import com.machiav3lli.fdroid.ARG_RESULT_CODE
import com.machiav3lli.fdroid.ARG_VALIDATION_ERROR
import com.machiav3lli.fdroid.MainApplication
import com.machiav3lli.fdroid.NOTIFICATION_CHANNEL_DOWNLOADING
import com.machiav3lli.fdroid.NOTIFICATION_CHANNEL_SYNCING
import com.machiav3lli.fdroid.NOTIFICATION_CHANNEL_UPDATES
import com.machiav3lli.fdroid.NOTIFICATION_CHANNEL_VULNS
import com.machiav3lli.fdroid.NOTIFICATION_ID_DOWNLOADING
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.installer.AppInstaller
import com.machiav3lli.fdroid.installer.InstallerService
import com.machiav3lli.fdroid.installer.LegacyInstaller
import com.machiav3lli.fdroid.service.works.DownloadState
import com.machiav3lli.fdroid.service.works.DownloadTask
import com.machiav3lli.fdroid.service.works.DownloadWorker
import com.machiav3lli.fdroid.service.works.ErrorType
import com.machiav3lli.fdroid.service.works.ValidationError
import com.machiav3lli.fdroid.utility.downloadNotificationBuilder
import com.machiav3lli.fdroid.utility.extension.android.Android
import com.machiav3lli.fdroid.utility.extension.text.formatSize
import com.machiav3lli.fdroid.utility.updateWithError
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class WorkerManager(appContext: Context) {

    var workManager: WorkManager
    var actionReceiver: ActionReceiver
    var context: Context = appContext
    val notificationManager: NotificationManagerCompat

    private val appsToBeInstalled = SnapshotStateList<DownloadTask>()

    fun enqueueToBeInstalled(task: DownloadTask) {
        appsToBeInstalled.add(task)
    }

    init {
        workManager = WorkManager.getInstance(context)
        actionReceiver = ActionReceiver()

        context.registerReceiver(actionReceiver, IntentFilter())

        notificationManager = NotificationManagerCompat.from(context)
        if (Android.sdk(Build.VERSION_CODES.O)) createNotificationChannels()

        workManager.pruneWork()
        /*workManager.getWorkInfosByTagLiveData(
            SyncWorker::class.qualifiedName!!
        ).observeForever {
            onSyncProgress(this, it)
        }*/
        workManager.getWorkInfosByTagLiveData(
            DownloadWorker::class.qualifiedName!!
        ).observeForever {
            onDownloadProgress(this, it)
        }

        CoroutineScope(Dispatchers.IO).launch {
            snapshotFlow { appsToBeInstalled.toList() }.collectLatest {
                val lock = Mutex()

                lock.withLock { // TODO improve handling to-be-installed logic
                    appsToBeInstalled.firstOrNull()?.let { task ->
                        val installer = suspend {
                            val installerInstance = AppInstaller.getInstance(context)
                            installerInstance?.defaultInstaller?.install(
                                task.name,
                                task.release.cacheFileName
                            )
                            appsToBeInstalled.remove(task)
                        }

                        if (MainApplication.mainActivity != null &&
                            AppInstaller.getInstance(context)?.defaultInstaller is LegacyInstaller
                        ) {
                            CoroutineScope(Dispatchers.Default).launch {
                                Log.i(
                                    this::javaClass.name,
                                    "Waiting activity to install: ${task.packageName}"
                                )
                                MainApplication.mainActivity?.withResumed {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        installer()
                                    }
                                }
                            }
                        } else {
                            Log.i(this::javaClass.name, "Installing downloaded: ${task.url}")
                            CoroutineScope(Dispatchers.IO).launch { installer() }
                        }
                    }
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
        /*MainApplication.wm.prune()
        SyncWorker::class.qualifiedName?.let {
            wm.cancelAllWorkByTag(it)
        }
        MainApplication.setProgress() // TODO re-consider
         */
    }

    fun cancelSync(repoId: Long = -1) {
        /*SyncWorker::class.qualifiedName?.let {
            wm.cancelAllWorkByTag(
                if (repoId != -1L) "sync_$repoId"
                else it
            )
        }*/
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
        val downloadsRunning = SnapshotStateMap<String, DownloadState>()

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
                        "<ALL>".hashCode(),
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
            // TODO implementation
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
                        putExtra(ARG_PACKAGE_NAME, wi.id.toString())
                    }
                    val cancelPendingIntent = PendingIntent.getBroadcast(
                        appContext,
                        task.key.hashCode(),
                        cancelIntent,
                        PendingIntent.FLAG_IMMUTABLE
                    )

                    downloadsRunning.compute(task.key) { _, _ ->
                        when (wi.state) {
                            WorkInfo.State.ENQUEUED, WorkInfo.State.BLOCKED -> {
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

                            WorkInfo.State.RUNNING                          -> {
                                notificationBuilder
                                    .setContentTitle(
                                        appContext.getString(
                                            R.string.downloading_FORMAT,
                                            "${task.name} (${task.release.version})"
                                        )
                                    )
                                    .setContentText("${progress.read.formatSize()} / ${progress.total.formatSize()}")
                                    .setProgress(100, progress.progress, false)
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

                            WorkInfo.State.CANCELLED                        -> {
                                allCount -= 1
                                notificationBuilder
                                    .setOngoing(false)
                                    .setContentTitle(
                                        appContext.getString(
                                            R.string.downloading_FORMAT,
                                            "${task.name} (${task.release.version})"
                                        )
                                    )
                                    .setContentText(appContext.getString(R.string.canceled))
                                    .setTimeoutAfter(InstallerService.INSTALLED_NOTIFICATION_TIMEOUT)
                                cancelNotification = true
                                DownloadState.Cancel(
                                    packageName = task.packageName,
                                    name = task.name,
                                    version = task.release.version,
                                    cacheFileName = task.release.cacheFileName,
                                    repoId = task.repoId,
                                )
                            }

                            WorkInfo.State.SUCCEEDED                        -> {
                                allProcessed += 1
                                notificationBuilder.setOngoing(false)
                                    .setContentTitle(
                                        appContext.getString(R.string.downloaded_FORMAT, task.name)
                                    )
                                MainApplication.wm.enqueueToBeInstalled(task)
                                if (!Preferences[Preferences.Key.KeepInstallNotification]) {
                                    notificationBuilder.setTimeoutAfter(InstallerService.INSTALLED_NOTIFICATION_TIMEOUT)
                                    cancelNotification = true
                                    null
                                } else DownloadState.Success(
                                    packageName = task.packageName,
                                    name = task.name,
                                    version = task.release.version,
                                    cacheFileName = task.release.cacheFileName,
                                    repoId = task.repoId,
                                    release = task.release,
                                )
                            }

                            WorkInfo.State.FAILED                           -> {
                                allProcessed += 1
                                allFailed += 1
                                val errorType = when {
                                    validationError != ValidationError.NONE
                                    -> ErrorType.Validation(validationError)

                                    resultCode != HttpStatusCode.GatewayTimeout.value
                                    -> ErrorType.Http

                                    else
                                    -> ErrorType.Network
                                }
                                notificationBuilder.updateWithError(appContext, task, errorType)
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
                        }
                    }

                    manager.prune()


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

            downloadNotificationBuilder
                .setProgress(allCount, allProcessed, false)
                .setContentText("$allProcessed / $allCount")
                .apply {
                    setOngoing(allCount > 0)

                    if (ActivityCompat.checkSelfPermission(
                            appContext,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED && allCount > 0
                    ) MainApplication.wm.notificationManager.notify(
                        NOTIFICATION_CHANNEL_DOWNLOADING,
                        NOTIFICATION_ID_DOWNLOADING,
                        downloadNotificationBuilder.build()
                    )
                }
        }
    }
}
