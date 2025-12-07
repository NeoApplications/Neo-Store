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
import androidx.work.workDataOf
import com.machiav3lli.fdroid.ARG_REPOSITORY_IDS
import com.machiav3lli.fdroid.ARG_SYNC_REQUEST
import com.machiav3lli.fdroid.ContextWrapperX
import com.machiav3lli.fdroid.NOTIFICATION_CHANNEL_SYNCING
import com.machiav3lli.fdroid.NeoActivity
import com.machiav3lli.fdroid.NeoApp
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.TAG_BATCH_SYNC_ONETIME
import com.machiav3lli.fdroid.TAG_BATCH_SYNC_PERIODIC
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.data.database.entity.Repository
import com.machiav3lli.fdroid.data.entity.SyncRequest
import com.machiav3lli.fdroid.data.entity.SyncState
import com.machiav3lli.fdroid.data.index.RepositoryUpdater
import com.machiav3lli.fdroid.data.repository.InstalledRepository
import com.machiav3lli.fdroid.data.repository.RepositoriesRepository
import com.machiav3lli.fdroid.manager.service.ActionReceiver
import com.machiav3lli.fdroid.utils.buildSyncLine
import com.machiav3lli.fdroid.utils.displayVulnerabilitiesNotification
import com.machiav3lli.fdroid.utils.extension.android.Android
import com.machiav3lli.fdroid.utils.reportSyncFail
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.java.KoinJavaComponent.get
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.incrementAndFetch
import kotlin.math.roundToInt

// TODO Add support for local repositories e.g. Calyx local repository
@OptIn(ExperimentalAtomicApi::class)
class BatchSyncWorker(
    private val context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams), KoinComponent {
    private val langContext = ContextWrapperX.wrap(applicationContext)
    private val reposRepo: RepositoriesRepository by inject()
    private val installedRepo: InstalledRepository by inject()
    private val updatesManager: UpdatesNotificationManager by inject()
    private val syncSemaphore = Semaphore(3) // TODO add it as option to prefs
    private val notificationMutex = Mutex()
    private val activeSyncs = ConcurrentHashMap<Long, SyncProgressInfo>()

    // TODO consider if still needed
    private var request = SyncRequest.entries[
        inputData.getInt(ARG_SYNC_REQUEST, 0)
    ]

    private val repositoryIds: Set<Long> = inputData.getLongArray(ARG_REPOSITORY_IDS)?.toSet()
        ?: emptySet()

    var totalRepos: Int = 0
    val reposCompleted = AtomicInt(0)
    val reposSucceeded = AtomicInt(0)

    override suspend fun doWork(): Result {
        return runCatching {
            syncRepositories()
        }.fold(
            onSuccess = { (total, succeeded, failed) ->
                Log.i(
                    TAG,
                    "Batch sync complete: $succeeded succeeded, $failed failed out of $total"
                )

                // Run additional sync tasks
                if (succeeded > 0) {
                    ExodusWorker.fetchTrackers()
                    if (Preferences[Preferences.Key.RBProvider] != Preferences.RBProvider.None) {
                        RBWorker.fetchRBLogs()
                    }
                    DownloadStatsWorker.enqueuePeriodic()
                }

                Result.success(
                    workDataOf(
                        "total" to total,
                        "succeeded" to succeeded,
                        "failed" to failed
                    )
                )
            },
            onFailure = { e ->
                Log.e(TAG, "Batch sync failed: ${e.message}", e)
                Result.failure(workDataOf("error" to (e.message ?: "Unknown error")))
            }
        )
    }

    private suspend fun syncRepositories(): Triple<Int, Int, Int> = coroutineScope {
        val repositories = if (repositoryIds.isEmpty()) {
            reposRepo.loadAll().filter { it.enabled }
        } else {
            repositoryIds.mapNotNull { id -> reposRepo.load(id) }.filter { it.enabled }
        }

        if (repositories.isEmpty()) {
            Log.i(TAG, "No repositories to sync")
            return@coroutineScope Triple(0, 0, 0)
        }

        totalRepos = repositories.size
        val reposFailed = mutableListOf<Long>()

        Log.d(TAG, "Starting sync for $totalRepos repositories")
        setForeground(createForegroundInfo(totalRepos, 0))

        repositories.map { repo ->
            async {
                syncSemaphore.withPermit {
                    val result = syncRepository(repo)

                    when {
                        result.success -> {
                            Log.d(TAG, "Successfully synced repository: ${repo.name}")
                            setForeground(
                                createForegroundInfo(
                                    totalRepos,
                                    reposCompleted.incrementAndFetch(),
                                    activeSyncs.values.toList(),
                                )
                            )

                            if (result.changed) {
                                handleRepositoryCompletion(repo.id)
                            }
                        }

                        else           -> {
                            reposFailed.add(repo.id)
                            Log.w(TAG, "Failed to sync repository: ${repo.name}")
                            langContext.reportSyncFail(
                                repo.id, SyncState.Failed(
                                    repo.id,
                                    request,
                                    repo.name,
                                    result.error ?: "",
                                )
                            )
                            setForeground(
                                createForegroundInfo(
                                    totalRepos,
                                    reposCompleted.incrementAndFetch(),
                                    activeSyncs.values.toList(),
                                )
                            )
                        }
                    }

                    removeSyncProgress(repo.id)
                }
            }
        }.awaitAll()

        if (reposFailed.isNotEmpty()) {
            Log.w(TAG, "Failed repositories: ${reposFailed.joinToString()}")
        }

        Triple(totalRepos, reposSucceeded.load(), reposFailed.size)
    }

    private suspend fun syncRepository(repository: Repository): SyncResult = coroutineScope {
        Log.i(TAG, "Syncing repository: ${repository.name} (${repository.id})")

        updateSyncProgress(
            repoId = repository.id,
            repoName = repository.name,
            state = SyncState.Enum.CONNECTING,
        )

        val unstable = Preferences[Preferences.Key.UpdateUnstable]
        val progressChannel = Channel<SyncProgress>(Channel.CONFLATED)

        val progressJob = async {
            for (progress in progressChannel) {
                runCatching {
                    updateSyncProgress(
                        repoId = repository.id,
                        repoName = repository.name,
                        state = SyncState.Enum.SYNCING,
                        progress = SyncProgress(
                            progress.stage,
                            progress.read,
                            progress.total
                        ),
                    )
                }
            }
        }

        var lastPerCent = -1
        var lastStage: RepositoryUpdater.Stage? = null

        return@coroutineScope try {
            val changed = RepositoryUpdater.update(
                context,
                repository,
                unstable
            ) { stage, progress, total ->
                val perCent =
                    if (total != null && total != 0L) (100f * progress / total).roundToInt()
                    else (progress / 100_000).toInt()

                if (stage != lastStage || perCent != lastPerCent) {
                    runCatching {
                        lastPerCent = perCent
                        lastStage = stage
                        progressChannel.trySend(SyncProgress(stage, progress, total ?: -1L))
                    }
                }
            }

            progressChannel.close()
            progressJob.join()

            SyncResult(success = true, changed = changed)
        } catch (throwable: Throwable) {
            progressChannel.close()
            progressJob.join()

            Log.e(TAG, "Repository sync failed: ${repository.name}", throwable)
            SyncResult(success = false, changed = false, error = throwable.message)
        }
    }

    private suspend fun handleRepositoryCompletion(repoId: Long) = coroutineScope {
        val updatesDeferred = async {
            installedRepo.loadUpdatedProducts()
                .map { it.toItem() }
                .filter { it.repositoryId == repoId }
        }

        val vulnsDeferred = async {
            installedRepo.loadListWithVulns(repoId)
        }

        val updates = updatesDeferred.await()
        if (updates.isNotEmpty() && Preferences[Preferences.Key.UpdateNotify]) {
            updatesManager.addUpdates(*updates.toTypedArray())
        }

        if (Preferences[Preferences.Key.InstallAfterSync]) {
            NeoApp.wm.update(
                *updates.map { Pair(it.packageName, it.repositoryId) }.toTypedArray()
            )
        }

        val installedWithVulns = vulnsDeferred.await()
        if (installedWithVulns.isNotEmpty()) {
            langContext.displayVulnerabilitiesNotification(
                installedWithVulns.map { it.toItem() }
            )
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return createForegroundInfo(repositoryIds.size, 0)
    }

    private fun createForegroundInfo(
        total: Int,
        completed: Int,
        activeSyncList: List<SyncProgressInfo> = emptyList(),
    ): ForegroundInfo {
        val notification = createForegroundNotification(total, completed, activeSyncList)

        return ForegroundInfo(
            NOTIFICATION_ID_BATCH_SYNCING,
            notification,
            if (Android.sdk(Build.VERSION_CODES.Q)) ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            else 0
        )
    }

    private fun createForegroundNotification(
        total: Int,
        completed: Int,
        activeSyncsList: List<SyncProgressInfo> = emptyList()
    ): Notification {
        val nActiveSyncs = activeSyncsList.size

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

        val builder = NotificationCompat.Builder(langContext, NOTIFICATION_CHANNEL_SYNCING)
            //.setGroup(NOTIFICATION_CHANNEL_SYNCING)
            //.setGroupSummary(true)
            .setSortKey("0")
            .setSmallIcon(R.drawable.ic_sync)
            .setOngoing(completed < total)
            .setSilent(true)
            .setContentIntent(contentPendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setProgress(total, completed, false)
            .addAction(
                R.drawable.ic_cancel,
                langContext.getString(R.string.cancel_all),
                cancelAllPendingIntent
            )


        when {
            nActiveSyncs > 0 -> {
                builder.setContentTitle(
                    langContext.getString(
                        R.string.syncing_repositories_FORMAT,
                        nActiveSyncs
                    )
                )

                val inboxStyle = NotificationCompat.InboxStyle()
                for (syncInfo in activeSyncsList.take(SYNC_MAX_LINES)) {
                    val line = langContext.buildSyncLine(syncInfo)
                    inboxStyle.addLine(line)
                }
                if (activeSyncsList.size > SYNC_MAX_LINES) {
                    val summary = langContext.getString(
                        R.string.plus_more_FORMAT,
                        activeSyncsList.size - SYNC_MAX_LINES
                    )
                    inboxStyle.addLine(summary)
                }
                builder.setStyle(inboxStyle)
            }

            else             -> {
                builder.setContentTitle(
                    langContext.getString(R.string.batch_sync_progress_FORMAT, completed, total)
                )
                    .setTimeoutAfter(NOTIFICATION_TIMEOUT)
            }
        }
        return builder.build()
    }

    private suspend fun updateSyncProgress(
        repoId: Long,
        repoName: String,
        state: SyncState.Enum,
        progress: SyncProgress? = null
    ) {
        Log.d(
            TAG,
            "Update sync progress - Repo ID: $repoId, State: $state, Progress: ${progress?.percentage}%"
        )

        when (state) {
            SyncState.Enum.CONNECTING,
            SyncState.Enum.SYNCING -> {
                activeSyncs[repoId] = SyncProgressInfo(repoId, repoName, state, progress)
            }

            SyncState.Enum.FINISHING,
            SyncState.Enum.FAILED  -> {
                activeSyncs.remove(repoId)
            }
        }

        setForeground(
            createForegroundInfo(
                totalRepos,
                reposCompleted.load(),
                activeSyncs.values.toList(),
            )
        )
    }

    private suspend fun removeSyncProgress(repoId: Long) {
        Log.d(TAG, "Removing sync progress - Repo ID: $repoId")
        activeSyncs.remove(repoId)
        setForeground(
            createForegroundInfo(
                totalRepos,
                reposCompleted.load(),
                activeSyncs.values.toList(),
            )
        )
    }

    data class SyncProgress(
        val stage: RepositoryUpdater.Stage,
        val read: Long,
        val total: Long,
    ) {
        val percentage: Int
            get() = if (total != 0L) (100f * read / total).roundToInt() else (read / 100_000).toInt()
    }

    data class SyncProgressInfo(
        val repoId: Long,
        val repoName: String,
        val state: SyncState.Enum,
        val progress: SyncProgress? = null
    )

    private data class SyncResult(
        val success: Boolean,
        val changed: Boolean,
        val error: String? = null,
    )

    companion object {
        private const val TAG = "BatchSyncWorker"
        private const val NOTIFICATION_ID_BATCH_SYNCING = 12345
        private const val NOTIFICATION_TIMEOUT = 10_000L
        private const val SYNC_MAX_LINES: Int = 5

        private fun Request(
            request: SyncRequest,
            repositoryIds: Set<Long> = emptySet()
        ): OneTimeWorkRequest {
            return OneTimeWorkRequestBuilder<BatchSyncWorker>()
                .setInputData(
                    workDataOf(
                        ARG_SYNC_REQUEST to request.ordinal,
                        ARG_REPOSITORY_IDS to repositoryIds.toLongArray(),
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
            repositoryIds: Set<Long> = emptySet()
        ) {
            get<WorkManager>(WorkManager::class.java).enqueueUniqueWork(
                TAG_BATCH_SYNC_ONETIME,
                ExistingWorkPolicy.REPLACE,
                Request(request, repositoryIds),
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