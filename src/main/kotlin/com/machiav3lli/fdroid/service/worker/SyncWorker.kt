package com.machiav3lli.fdroid.service.worker

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.Worker
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
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.TAG_SYNC_ONETIME
import com.machiav3lli.fdroid.TAG_SYNC_PERIODIC
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.index.RepositoryUpdater
import com.machiav3lli.fdroid.service.ActionReceiver
import com.machiav3lli.fdroid.ui.activities.MainActivityX
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.future
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class SyncWorker(
    private val context: Context,
    workerParams: WorkerParameters,
) : Worker(context, workerParams) {
    private val scope = CoroutineScope(Dispatchers.Default)

    data class Progress(
        val stage: RepositoryUpdater.Stage,
        val read: Long,
        val total: Long,
    ) {
        val percentage: Int
            get() = (100f * read / total).roundToInt()
    }

    private var repoId = inputData.getLong(ARG_REPOSITORY_ID, -1L)
    private var request = SyncRequest.values()[
            inputData.getInt(ARG_SYNC_REQUEST, 0)
    ]

    companion object {

        fun enqueue(
            request: SyncRequest,
            vararg ids: Long,
        ) {
            ids.map { repoId ->
                val data = workDataOf(
                    ARG_REPOSITORY_ID to repoId,
                    ARG_SYNC_REQUEST to request.ordinal,
                )

                MainApplication.wm.workManager.enqueueUniqueWork(
                    "sync_$repoId",
                    ExistingWorkPolicy.REPLACE,
                    OneTimeWorkRequestBuilder<SyncWorker>()
                        .setInputData(data)
                        .addTag(TAG_SYNC_ONETIME)
                        .addTag("sync_$repoId")
                        .build()
                )
            }
        }

        fun enqueueAll(request: SyncRequest) {
            CoroutineScope(Dispatchers.IO).launch {
                enqueue(
                    request,
                    *(MainApplication.db.repositoryDao.all
                        .filter { it.enabled }
                        .map { it.id } + EXODUS_TRACKERS_SYNC).toLongArray()
                )
            }
        }

        fun enqueuePeriodic(
            connectionType: NetworkType,
            chargingBattery: Boolean,
        ) {
            val constraints = Constraints.Builder()
                .setRequiresCharging(chargingBattery)
                .setRequiredNetworkType(connectionType)
                .build()

            CoroutineScope(Dispatchers.IO).launch {
                (MainApplication.db.repositoryDao.all
                    .filter { it.enabled }
                    .map { it.id } + EXODUS_TRACKERS_SYNC)
                    .forEach { repoId ->
                        val data = workDataOf(
                            ARG_REPOSITORY_ID to repoId,
                            ARG_SYNC_REQUEST to SyncRequest.AUTO.ordinal,
                        )

                        val workRequest = PeriodicWorkRequestBuilder<SyncWorker>(
                            Preferences[Preferences.Key.AutoSyncInterval].toLong(),
                            TimeUnit.MINUTES,
                        )
                            .setInitialDelay(
                                Preferences[Preferences.Key.AutoSyncInterval].toLong(),
                                TimeUnit.MINUTES,
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

        fun getTask(data: Data) = SyncTask(
            data.getLong(ARG_REPOSITORY_ID, -1L),
            SyncRequest.values()[
                    data.getInt(ARG_SYNC_REQUEST, 0)
            ],
        )

        fun getState(data: Data) = SyncState.values()[
                data.getInt(ARG_STATE, 0)
        ]

        fun getProgress(data: Data) = Progress(
            RepositoryUpdater.Stage.values()[
                    data.getInt(ARG_STAGE, 0)
            ],
            data.getLong(ARG_READ, 0L),
            data.getLong(ARG_TOTAL, -1L),
        )
    }

    override fun doWork(): Result {
        val task = SyncTask(repoId, request)

        return if (repoId != -1L) {
            handleSync(task)
        } else Result.success(
            workDataOf(
                ARG_REPOSITORY_ID to repoId,
                ARG_SYNC_REQUEST to request.ordinal,
            )
        )
    }

    private fun handleSync(task: SyncTask): Result {
        scope.launch {
            setForegroundAsync(foregroundInfo)
        }
        val repository = scope.future {
            MainApplication.db.repositoryDao.get(task.repositoryId)
        }.join()

        Log.i(this::class.java.simpleName, "sync repository: ${task.repositoryId}")
        if (repository != null && repository.enabled && task.repositoryId != EXODUS_TRACKERS_SYNC) {
            setProgressAsync(
                workDataOf(
                    ARG_STATE to SyncState.CONNECTING.ordinal,
                    ARG_REPOSITORY_NAME to repository.name,
                )
            )
            val unstable = Preferences[Preferences.Key.UpdateUnstable]

            try {
                val changed = scope.future {
                    RepositoryUpdater.update(
                        context,
                        repository,
                        unstable
                    ) { stage, progress, total ->
                        setProgressAsync(
                            workDataOf(
                                ARG_STATE to SyncState.SYNCING.ordinal,
                                ARG_REPOSITORY_NAME to repository.name,
                                ARG_STAGE to stage.ordinal,
                                ARG_READ to progress,
                                ARG_TOTAL to total,
                            )
                        )
                    }
                }.join()
                return Result.success(
                    workDataOf(
                        ARG_REPOSITORY_ID to repoId,
                        ARG_SYNC_REQUEST to request.ordinal,
                        ARG_STATE to SyncState.FINISHING.ordinal,
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
                        ARG_STATE to SyncState.FAILED.ordinal,
                        ARG_REPOSITORY_NAME to repository.name,
                        ARG_EXCEPTION to throwable.message,
                    )
                )
            }
            //} else if (task.repositoryId == EXODUS_TRACKERS_SYNC) {
            // TODO fetchTrackers()
        } else {
            return Result.success(
                workDataOf(
                    ARG_REPOSITORY_ID to repoId,
                    ARG_SYNC_REQUEST to request.ordinal,
                    ARG_STATE to SyncState.FINISHING.ordinal,
                    ARG_REPOSITORY_NAME to repository?.name,
                    ARG_CHANGED to false,
                )
            )
        }
    }

    override fun getForegroundInfo(): ForegroundInfo {
        val contentPendingIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivityX::class.java),
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
        return ForegroundInfo(NOTIFICATION_ID_SYNCING, notification)
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
}