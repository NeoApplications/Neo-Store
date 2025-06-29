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
import com.machiav3lli.fdroid.ARG_EXCEPTION
import com.machiav3lli.fdroid.ARG_REPOSITORY_NAME
import com.machiav3lli.fdroid.ARG_SUCCESS
import com.machiav3lli.fdroid.ARG_SYNC_REQUEST
import com.machiav3lli.fdroid.ContextWrapperX
import com.machiav3lli.fdroid.NOTIFICATION_CHANNEL_SYNCING
import com.machiav3lli.fdroid.NeoActivity
import com.machiav3lli.fdroid.NeoApp
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.TAG_BATCH_SYNC_ONETIME
import com.machiav3lli.fdroid.TAG_BATCH_SYNC_PERIODIC
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.data.database.entity.EmbeddedProduct
import com.machiav3lli.fdroid.data.entity.AntiFeature
import com.machiav3lli.fdroid.data.entity.Order
import com.machiav3lli.fdroid.data.entity.Section
import com.machiav3lli.fdroid.data.entity.SyncRequest
import com.machiav3lli.fdroid.data.repository.ExtrasRepository
import com.machiav3lli.fdroid.data.repository.ProductsRepository
import com.machiav3lli.fdroid.data.repository.RepositoriesRepository
import com.machiav3lli.fdroid.manager.service.ActionReceiver
import com.machiav3lli.fdroid.manager.service.InstallerReceiver
import com.machiav3lli.fdroid.utils.displayUpdatesNotification
import com.machiav3lli.fdroid.utils.displayVulnerabilitiesNotification
import com.machiav3lli.fdroid.utils.extension.android.Android
import com.machiav3lli.fdroid.utils.extension.takeUntilSignal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
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
    private var notification: Notification? = null
    private var request = SyncRequest.entries[
        inputData.getInt(ARG_SYNC_REQUEST, 0)
    ]
    private val langContext = ContextWrapperX.wrap(applicationContext)
    private val productRepo: ProductsRepository by inject()
    private val extrasRepo: ExtrasRepository by inject()
    private val reposRepo: RepositoriesRepository by inject()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO + scheduleJob) {
        try {
            val result = handleSync()
            if (!result) {
                return@withContext Result.failure()
            }
            Result.success()
        } catch (e: Exception) {
            Log.e(this::class.java.simpleName, e.message, e)
            Result.failure()
        }
    }

    private suspend fun handleSync(): Boolean = supervisorScope {
        val finishSignal = MutableStateFlow(false)
        val selectedRepos = reposRepo.loadAll().filter { it.enabled }
        totalRepositories = selectedRepos.size

        if (selectedRepos.isEmpty()) return@supervisorScope true

        val worksList = mutableListOf<OneTimeWorkRequest>()

        var errors = ""
        var resultsSuccess = true
        val completionStatus = selectedRepos.associate { it.id to false }.toMutableMap()

        val workJobs = selectedRepos.map { repo ->
            val oneTimeWorkRequest = SyncWorker.Request(
                repoId = repo.id,
                request = request,
                repoName = repo.name,
            )
            worksList.add(oneTimeWorkRequest)

            launch {
                workManager.getWorkInfoByIdFlow(
                    oneTimeWorkRequest.id
                )
                    .takeUntilSignal(finishSignal)
                    .collect { workInfo ->
                        when (workInfo?.state) {
                            androidx.work.WorkInfo.State.SUCCEEDED,
                            androidx.work.WorkInfo.State.FAILED,
                            androidx.work.WorkInfo.State.CANCELLED -> {
                                completedRepositories++
                                val repoName =
                                    workInfo.outputData.getString(ARG_REPOSITORY_NAME) ?: ""
                                val succeeded =
                                    workInfo.outputData.getBoolean(ARG_SUCCESS, false)
                                val errorMessage =
                                    workInfo.outputData.getString(ARG_EXCEPTION) ?: ""

                                if (errorMessage.isNotEmpty()) {
                                    errors = "$errors$repoName: $errorMessage\n"
                                }
                                resultsSuccess = resultsSuccess && succeeded

                                updateNotification()

                                completionStatus[repo.id] = true
                                if (completionStatus.values.all { it }) {
                                    handleCompletion(errors)
                                    finishSignal.update { true }
                                }
                            }

                            else                                   -> {
                                updateNotification()
                                if (workInfo == null && completionStatus[repo.id] == true) // For some edge cases
                                    finishSignal.update { true }
                            }
                        }
                    }
            }
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
        withTimeout(TimeUnit.HOURS.toMillis(1)) {
            workJobs.joinAll()
        }
        true
    }

    private suspend fun updateNotification() {
        setForeground(getForegroundInfo())
    }

    private suspend fun handleCompletion(errors: String) {
        Log.e(this::class.java.simpleName, errors)
        productRepo
            .loadList(
                installed = true,
                updates = true,
                section = Section.All,
                order = Order.NAME,
                ascending = true,
            )
            .map { it.toItem() }
            .let { result ->
                if (result.isNotEmpty() && Preferences[Preferences.Key.UpdateNotify])
                    langContext.displayUpdatesNotification(
                        result,
                        true
                    )
                if (Preferences[Preferences.Key.InstallAfterSync]) {
                    NeoApp.wm.update(*result.toTypedArray())
                }
            }
        productRepo
            .loadList(
                installed = true,
                updates = false,
                section = Section.All,
                order = Order.NAME,
                ascending = true,
            ).filter { product ->
                product.product.antiFeatures.contains(AntiFeature.KNOWN_VULN.key)
                        && extrasRepo.load(product.product.packageName)?.ignoreVulns != true
            }.let { installedWithVulns ->
                if (installedWithVulns.isNotEmpty())
                    langContext.displayVulnerabilitiesNotification(
                        installedWithVulns.map(EmbeddedProduct::toItem)
                    )
            }
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

        val cancelAllIntent = Intent(NeoApp.context, ActionReceiver::class.java).apply {
            action = ActionReceiver.COMMAND_CANCEL_SYNC_ALL
        }
        val cancelAllPendingIntent = PendingIntent.getBroadcast(
            NeoApp.context,
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
                    R.string.syncing_batch_FORMAT,
                    completedRepositories,
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
                if (completedRepositories >= totalRepositories)
                    setTimeoutAfter(InstallerReceiver.INSTALLED_NOTIFICATION_TIMEOUT)
            }
            .build()
    }

    companion object {
        private const val NOTIFICATION_ID_BATCH_SYNCING = 12345

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