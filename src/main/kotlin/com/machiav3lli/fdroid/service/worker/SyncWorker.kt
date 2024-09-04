package com.machiav3lli.fdroid.service.worker

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.common.util.concurrent.ListenableFuture
import com.machiav3lli.fdroid.ARG_CHANGED
import com.machiav3lli.fdroid.ARG_EXCEPTION
import com.machiav3lli.fdroid.ARG_READ
import com.machiav3lli.fdroid.ARG_REPOSITORY_ID
import com.machiav3lli.fdroid.ARG_REPOSITORY_NAME
import com.machiav3lli.fdroid.ARG_STAGE
import com.machiav3lli.fdroid.ARG_STATE
import com.machiav3lli.fdroid.ARG_SYNC_REQUEST
import com.machiav3lli.fdroid.ARG_TOTAL
import com.machiav3lli.fdroid.EXODUS_TRACKERS_SYNC
import com.machiav3lli.fdroid.MainApplication
import com.machiav3lli.fdroid.NOTIFICATION_CHANNEL_SYNCING
import com.machiav3lli.fdroid.NOTIFICATION_ID_SYNCING
import com.machiav3lli.fdroid.NeoActivity
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.TAG_SYNC_ONETIME
import com.machiav3lli.fdroid.TAG_SYNC_PERIODIC
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.database.entity.Repository
import com.machiav3lli.fdroid.index.RepositoryUpdater
import com.machiav3lli.fdroid.service.ActionReceiver
import com.machiav3lli.fdroid.utility.extension.android.Android
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.future
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class SyncWorker(
    private val context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {
    private var repoId = inputData.getLong(ARG_REPOSITORY_ID, -1L)
    private var request = SyncRequest.entries[
        inputData.getInt(ARG_SYNC_REQUEST, 0)
    ]

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val task = SyncTask(repoId, request)

        return@withContext if (repoId != -1L) {
            handleSync(task)
        } else Result.success(
            workDataOf(
                ARG_REPOSITORY_ID to repoId,
                ARG_SYNC_REQUEST to request.ordinal,
            )
        )
    }

    private fun CoroutineScope.handleSync(task: SyncTask): Result {
        val repository = MainApplication.db.getRepositoryDao().get(task.repositoryId)

        Log.i(this::class.java.simpleName, "sync repository: ${task.repositoryId}")
        if (repository != null && repository.enabled && task.repositoryId != EXODUS_TRACKERS_SYNC) {
            launch {
                setProgress(
                    workDataOf(
                        ARG_STATE to SyncState.Enum.CONNECTING.ordinal,
                        ARG_REPOSITORY_NAME to repository.name,
                    )
                )
            }
            val unstable = Preferences[Preferences.Key.UpdateUnstable]

            try {
                val changed = future {
                    RepositoryUpdater.update(
                        context,
                        repository,
                        unstable
                    ) { stage, progress, total ->
                        launch {
                            setProgress(
                                workDataOf(
                                    ARG_STATE to SyncState.Enum.SYNCING.ordinal,
                                    ARG_REPOSITORY_NAME to repository.name,
                                    ARG_STAGE to stage.ordinal,
                                    ARG_READ to progress,
                                    ARG_TOTAL to total,
                                )
                            )
                        }
                    }
                }.join()
                return Result.success(
                    workDataOf(
                        ARG_REPOSITORY_ID to repoId,
                        ARG_SYNC_REQUEST to request.ordinal,
                        ARG_STATE to SyncState.Enum.FINISHING.ordinal,
                        ARG_REPOSITORY_NAME to repository.name,
                        ARG_CHANGED to changed,
                    )
                )
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
                return Result.failure(
                    workDataOf(
                        ARG_REPOSITORY_ID to repoId,
                        ARG_SYNC_REQUEST to request.ordinal,
                        ARG_STATE to SyncState.Enum.FAILED.ordinal,
                        ARG_REPOSITORY_NAME to repository.name,
                        ARG_EXCEPTION to throwable.message,
                    )
                )
            }
        } else {
            return Result.success(
                workDataOf(
                    ARG_REPOSITORY_ID to repoId,
                    ARG_SYNC_REQUEST to request.ordinal,
                    ARG_STATE to SyncState.Enum.FINISHING.ordinal,
                    ARG_REPOSITORY_NAME to repository?.name,
                    ARG_CHANGED to false,
                )
            )
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val contentPendingIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, NeoActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val cancelAllIntent =
            Intent(MainApplication.context, ActionReceiver::class.java).apply {
                action = ActionReceiver.COMMAND_CANCEL_SYNC_ALL
            }
        val cancelAllPendingIntent = PendingIntent.getBroadcast(
            MainApplication.context,
            "<ALL>".hashCode(),
            cancelAllIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_SYNCING)
            .setContentTitle(context.getString(R.string.syncing))
            .setSmallIcon(R.drawable.ic_sync)
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(contentPendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .addAction(
                R.drawable.ic_cancel,
                context.getString(R.string.cancel_all),
                cancelAllPendingIntent
            )
            .build()
        return ForegroundInfo(
            NOTIFICATION_ID_SYNCING,
            notification,
            if (Android.sdk(Build.VERSION_CODES.Q)) ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            else 0
        )
    }

    override fun setProgressAsync(data: Data): ListenableFuture<Void> {
        return super.setProgressAsync(
            Data.Builder()
                .putAll(data)
                .putLong(ARG_REPOSITORY_ID, repoId)
                .putInt(ARG_SYNC_REQUEST, request.ordinal)
                .build()
        )
    }

    data class Progress(
        val stage: RepositoryUpdater.Stage,
        val read: Long,
        val total: Long,
    ) {
        val percentage: Int
            get() = (100f * read / total).roundToInt()
    }

    companion object {
        private fun enqueue(
            request: SyncRequest,
            vararg ids: Long,
        ) {
            ids.map { repoId ->

                if (repoId != EXODUS_TRACKERS_SYNC) {
                    val data = workDataOf(
                        ARG_REPOSITORY_ID to repoId,
                        ARG_SYNC_REQUEST to request.ordinal,
                    )

                    MainApplication.wm.workManager.enqueueUniqueWork(
                        "sync_$repoId",
                        ExistingWorkPolicy.KEEP,
                        OneTimeWorkRequestBuilder<SyncWorker>()
                            .setInputData(data)
                            .addTag(TAG_SYNC_ONETIME)
                            .build()
                    )
                } else ExodusWorker.fetchTrackers()
            }
        }

        suspend fun enqueueAll(request: SyncRequest) {
            withContext(Dispatchers.IO) {
                enqueue(
                    request,
                    *(MainApplication.db.getRepositoryDao().getAll()
                        .filter { it.enabled }
                        .map { it.id } + EXODUS_TRACKERS_SYNC).toLongArray()
                )
            }
        }

        suspend fun enqueuePeriodic(
            connectionType: NetworkType,
            chargingBattery: Boolean,
        ) {
            val constraints = Constraints.Builder()
                .setRequiresCharging(chargingBattery)
                .setRequiredNetworkType(connectionType)
                .build()

            withContext(Dispatchers.IO) {
                (MainApplication.db.getRepositoryDao().getAll()
                    .filter { it.enabled }
                    .map { it.id } + EXODUS_TRACKERS_SYNC)
                    .forEach { repoId ->
                        val data = workDataOf(
                            ARG_REPOSITORY_ID to repoId,
                            ARG_SYNC_REQUEST to SyncRequest.AUTO.ordinal,
                        )

                        val workRequest = PeriodicWorkRequestBuilder<SyncWorker>(
                            Preferences[Preferences.Key.AutoSyncInterval].toLong(),
                            TimeUnit.HOURS,
                        )
                            .setInitialDelay(
                                Preferences[Preferences.Key.AutoSyncInterval].toLong(),
                                TimeUnit.HOURS,
                            )
                            .setConstraints(constraints)
                            .setInputData(data)
                            .addTag(TAG_SYNC_PERIODIC)
                            .build()

                        MainApplication.wm.workManager.enqueueUniquePeriodicWork(
                            "sync_periodic_$repoId",
                            ExistingPeriodicWorkPolicy.UPDATE,
                            workRequest,
                        )
                    }
            }
        }

        suspend fun enableRepo(repository: Repository, enabled: Boolean): Boolean =
            withContext(Dispatchers.IO) {
                MainApplication.db.getRepositoryDao().put(repository.enable(enabled))
                val isEnabled = !repository.enabled && repository.lastModified.isEmpty()
                val cooldownedSync = System.currentTimeMillis() -
                        MainApplication.latestSyncs.getOrDefault(repository.id, 0L) >=
                        10_000L
                if (enabled && isEnabled && cooldownedSync) {
                    MainApplication.latestSyncs[repository.id] = System.currentTimeMillis()
                    enqueue(SyncRequest.MANUAL, repository.id)
                } else {
                    MainApplication.wm.cancelSync(repository.id)
                    MainApplication.db.cleanUp(Pair(repository.id, false))
                }
                true
            }

        suspend fun deleteRepo(repoId: Long): Boolean = withContext(Dispatchers.IO) {
            val repository = MainApplication.db.getRepositoryDao().get(repoId)
            repository != null && run {
                enableRepo(repository, false)
                MainApplication.db.getRepositoryDao().deleteById(repoId)
                true
            }
        }

        fun getTask(data: Data) = SyncTask(
            data.getLong(ARG_REPOSITORY_ID, -1L),
            SyncRequest.entries[
                data.getInt(ARG_SYNC_REQUEST, 0)
            ],
        )

        fun getState(data: Data): SyncState = when (data.getInt(ARG_STATE, 0)) {
            SyncState.Enum.FAILED.ordinal -> SyncState.Failed
            SyncState.Enum.FINISHING.ordinal -> SyncState.Finishing
            SyncState.Enum.SYNCING.ordinal -> SyncState.Syncing(getProgress(data))
            else -> SyncState.Connecting // SyncState.Enum.CONNECTING
        }

        private fun getProgress(data: Data) = Progress(
            RepositoryUpdater.Stage.entries[
                data.getInt(ARG_STAGE, 0)
            ],
            data.getLong(ARG_READ, 0L),
            data.getLong(ARG_TOTAL, -1L),
        )
    }
}