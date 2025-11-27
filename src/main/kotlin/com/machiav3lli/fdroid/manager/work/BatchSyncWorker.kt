package com.machiav3lli.fdroid.manager.work

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.await
import androidx.work.workDataOf
import com.machiav3lli.fdroid.ARG_SYNC_REQUEST
import com.machiav3lli.fdroid.ContextWrapperX
import com.machiav3lli.fdroid.NOTIFICATION_CHANNEL_SYNCING
import com.machiav3lli.fdroid.NeoActivity
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.TAG_BATCH_SYNC_ONETIME
import com.machiav3lli.fdroid.TAG_BATCH_SYNC_PERIODIC
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.data.entity.SyncRequest
import com.machiav3lli.fdroid.data.repository.RepositoriesRepository
import com.machiav3lli.fdroid.manager.service.ActionReceiver
import com.machiav3lli.fdroid.utils.extension.android.Android
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.java.KoinJavaComponent.get
import org.koin.java.KoinJavaComponent.inject
import java.util.concurrent.TimeUnit

class BatchSyncWorker(
    private val context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams), KoinComponent {
    private val workManager by inject<WorkManager>(WorkManager::class.java)
    private var totalRepositories = 0
    private var completedRepositories = 0
    private val scheduleJob = SupervisorJob()
    private var request = SyncRequest.entries[
        inputData.getInt(ARG_SYNC_REQUEST, 0)
    ]
    private val langContext = ContextWrapperX.wrap(applicationContext)
    private val reposRepo: RepositoriesRepository by inject()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO + scheduleJob) {
        try {
            val result = handleSync()
            if (!result) {
                return@withContext Result.failure()
            }
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, e.message, e)
            Result.failure()
        }
    }

    private suspend fun handleSync(): Boolean = supervisorScope {
        val selectedRepos = reposRepo.loadAll().filter { it.enabled }
        totalRepositories = selectedRepos.size

        if (selectedRepos.isEmpty()) return@supervisorScope true

        val worksList = mutableListOf<OneTimeWorkRequest>()

        selectedRepos.forEach { repo ->
            val oneTimeWorkRequest = SyncWorker.Request(
                repoId = repo.id,
                request = request,
                repoName = repo.name,
            )
            worksList.add(oneTimeWorkRequest)
        }

        if (worksList.isEmpty()) return@supervisorScope true

        worksList.forEach { request ->
            workManager.enqueueUniqueWork(
                "batch_sync_${request.id}",
                ExistingWorkPolicy.KEEP,
                request
            ).await()
        }
        ExodusWorker.fetchTrackers()
        if (Preferences[Preferences.Key.RBProvider] != Preferences.RBProvider.None)
            RBWorker.fetchRBLogs()
        DownloadStatsWorker.enqueuePeriodic()
        true
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val notification = createForegroundNotification()

        return ForegroundInfo(
            NOTIFICATION_ID_BATCH_SYNCING,
            notification,
            if (Android.sdk(Build.VERSION_CODES.Q)) ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            else 0
        )
    }

    private fun createForegroundNotification(): Notification {
        val contentPendingIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, NeoActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val cancelAllIntent = Intent(context, ActionReceiver::class.java).apply {
            action = ActionReceiver.COMMAND_CANCEL_SYNC_ALL
        }
        val cancelAllPendingIntent = PendingIntent.getBroadcast(
            context,
            "batch_sync_all".hashCode(),
            cancelAllIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(langContext, NOTIFICATION_CHANNEL_SYNCING)
            .setGroup(NOTIFICATION_CHANNEL_SYNCING)
            .setGroupSummary(true)
            .setSortKey("0")
            .setContentTitle(context.getString(R.string.syncing))
            .setContentText(
                context.getString(
                    R.string.syncing_repositories_FORMAT,
                    totalRepositories
                )
            )
            .setSmallIcon(R.drawable.ic_sync)
            .setOngoing(completedRepositories < totalRepositories)
            .setSilent(true)
            .setContentIntent(contentPendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setProgress(totalRepositories, completedRepositories, false)
            .addAction(
                R.drawable.ic_cancel,
                context.getString(R.string.cancel_all),
                cancelAllPendingIntent
            )
            .apply {
                if (completedRepositories >= totalRepositories) {
                    setTimeoutAfter(NOTIFICATION_TIMEOUT)
                    Log.i(TAG, "Setting sync notification progress to timeout")
                }
            }
            .build()
    }

    companion object {
        private const val TAG = "BatchSyncWorker"
        private const val NOTIFICATION_ID_BATCH_SYNCING = 12345
        private const val NOTIFICATION_TIMEOUT = 10_000L

        private fun Request(request: SyncRequest): OneTimeWorkRequest {
            return OneTimeWorkRequestBuilder<BatchSyncWorker>()
                .setInputData(
                    workDataOf(
                        ARG_SYNC_REQUEST to request.ordinal,
                    )
                )
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .addTag(TAG_BATCH_SYNC_ONETIME)
                .build()
        }

        private fun PeriodicRequest(
            networkType: NetworkType = NetworkType.CONNECTED,
            requiresCharging: Boolean = false,
        ): PeriodicWorkRequest {
            return PeriodicWorkRequestBuilder<BatchSyncWorker>(
                Preferences[Preferences.Key.AutoSyncInterval].toLong(),
                TimeUnit.HOURS,
            )
                .setInitialDelay(
                    Preferences[Preferences.Key.AutoSyncInterval].toLong(),
                    TimeUnit.HOURS,
                )
                .setInputData(
                    workDataOf(
                        ARG_SYNC_REQUEST to SyncRequest.AUTO.ordinal,
                    )
                )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(networkType)
                        .setRequiresCharging(requiresCharging)
                        .build()
                )
                .addTag(TAG_BATCH_SYNC_PERIODIC)
                .build()
        }

        fun enqueue(
            request: SyncRequest,
        ) {
            get<WorkManager>(WorkManager::class.java).enqueueUniqueWork(
                TAG_BATCH_SYNC_ONETIME,
                ExistingWorkPolicy.REPLACE,
                Request(request),
            )
        }

        fun enqueuePeriodic(
            connectionType: NetworkType,
            chargingBattery: Boolean,
        ) {
            get<WorkManager>(WorkManager::class.java).enqueueUniquePeriodicWork(
                TAG_BATCH_SYNC_PERIODIC,
                ExistingPeriodicWorkPolicy.UPDATE,
                PeriodicRequest(connectionType, chargingBattery),
            )
        }
    }
}