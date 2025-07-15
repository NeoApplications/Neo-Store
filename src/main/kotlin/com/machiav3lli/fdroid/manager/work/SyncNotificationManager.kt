package com.machiav3lli.fdroid.manager.work

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.machiav3lli.fdroid.NOTIFICATION_CHANNEL_SYNCING
import com.machiav3lli.fdroid.NOTIFICATION_ID_BATCH_SYNCING
import com.machiav3lli.fdroid.NeoActivity
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.data.entity.SyncState
import com.machiav3lli.fdroid.data.index.RepositoryUpdater
import com.machiav3lli.fdroid.manager.service.ActionReceiver
import com.machiav3lli.fdroid.utils.extension.text.formatSize
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SyncNotificationManager(
    private val context: Context,
    private val notificationManager: NotificationManagerCompat,
) {
    private val mutex = Mutex()
    private val activeSyncs = mutableMapOf<Long, SyncProgressInfo>()

    data class SyncProgressInfo(
        val repoId: Long,
        val repoName: String,
        val state: SyncState.Enum,
        val progress: SyncWorker.Progress? = null
    )

    suspend fun updateSyncProgress(
        repoId: Long,
        repoName: String,
        state: SyncState.Enum,
        progress: SyncWorker.Progress? = null
    ) = mutex.withLock {
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
        updateNotification()
    }

    suspend fun removeSyncProgress(repoId: Long) = mutex.withLock {
        activeSyncs.remove(repoId)
        updateNotification()
    }

    private fun updateNotification() {
        if (activeSyncs.isEmpty()) {
            notificationManager.cancel(NOTIFICATION_ID_BATCH_SYNCING)
            return
        }

        val notification = createSyncNotification()
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) notificationManager.notify(NOTIFICATION_ID_BATCH_SYNCING, notification)
    }

    private fun createSyncNotification(): Notification {
        val activeSyncsList = activeSyncs.values.toList()
        val totalSyncs = activeSyncsList.size

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
            "consolidated_sync".hashCode(),
            cancelAllIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_SYNCING)
            //.setGroup(NOTIFICATION_CHANNEL_SYNCING)
            //.setGroupSummary(true)
            .setSortKey("0")
            .setSmallIcon(R.drawable.ic_sync)
            .setContentTitle(
                context.getString(
                    R.string.syncing_repositories_FORMAT,
                    totalSyncs
                )
            )
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(contentPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .addAction(
                R.drawable.ic_cancel,
                context.getString(R.string.cancel_all),
                cancelAllPendingIntent
            )

        // Use InboxStyle to show multiple repositories
        val inboxStyle = NotificationCompat.InboxStyle()
        activeSyncsList.forEach { syncInfo ->
            val line = buildSyncLine(syncInfo)
            inboxStyle.addLine(line)
        }

        builder.setStyle(inboxStyle)

        return builder.build()
    }

    private fun buildSyncLine(syncInfo: SyncProgressInfo): String {
        return when (syncInfo.state) {
            SyncState.Enum.CONNECTING -> {
                "${syncInfo.repoName}: ${context.getString(R.string.connecting)}"
            }

            SyncState.Enum.SYNCING    -> {
                val progress = syncInfo.progress
                if (progress != null) {
                    val progressText = when (progress.stage) {
                        RepositoryUpdater.Stage.DOWNLOAD -> {
                            if (progress.total >= 1) {
                                "${progress.read.formatSize()} / ${progress.total.formatSize()}"
                            } else {
                                progress.read.formatSize()
                                //context.getString(R.string.downloading)
                            }
                        }

                        RepositoryUpdater.Stage.PROCESS  -> context.getString(
                            R.string.processing_FORMAT,
                            "${progress.percentage}%"
                        )

                        RepositoryUpdater.Stage.MERGE    -> context.getString(
                            R.string.merging_FORMAT,
                            "${progress.read} / ${progress.total}"
                        )

                        RepositoryUpdater.Stage.COMMIT   -> context.getString(R.string.saving_details)
                    }
                    "${syncInfo.repoName}: $progressText"
                } else {
                    "${syncInfo.repoName}: ${context.getString(R.string.syncing)}"
                }
            }

            else                      -> "${syncInfo.repoName}: ${context.getString(R.string.syncing)}"
        }
    }
}