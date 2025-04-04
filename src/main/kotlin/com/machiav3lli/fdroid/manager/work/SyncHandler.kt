package com.machiav3lli.fdroid.manager.work

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Data
import androidx.work.WorkInfo
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.data.entity.DownloadState
import com.machiav3lli.fdroid.data.entity.SyncState
import com.machiav3lli.fdroid.utils.syncNotificationBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class SyncStateHandler(
    private val context: Context,
    scope: CoroutineScope,
    private val syncStates: WorkStateHolder<SyncState>,
    private val notificationManager: NotificationManagerCompat
) {
    init {
        scope.launch {
            syncStates.observeStates()
                .collect { states ->
                    states.forEach { (key, state) ->
                        handleSyncState(key, state)
                    }
                }
        }
    }

    private suspend fun handleSyncState(key: String, state: SyncState) {
        updateNotification(key, state)
    }

    fun updateState(key: String, state: SyncState?) {
        syncStates.updateState(key, state)
    }

    private fun updateNotification(key: String, state: SyncState) {
        val builder = context.createNotificationBuilder(state)
        if (state == null || state is DownloadState.Success || state is DownloadState.Cancel) {
            notificationManager.cancel(key.hashCode())
        } else if (builder != null && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) notificationManager.notify(
            key.hashCode(),
            builder.setOngoing(state !is SyncState.Failed)
                .build()
        )
    }

    private fun Context.createNotificationBuilder(state: SyncState): NotificationCompat.Builder? {
        val title = getString(
            R.string.syncing_FORMAT,
            state.repoName
        )
        val notificationBuilder = syncNotificationBuilder(title)

        return when (state) {
            is SyncState.Failed    -> {
                notificationBuilder
                    .setContentText(getString(R.string.action_failed)) // TODO Add error message
                    .setSmallIcon(R.drawable.ic_new_releases)
            }

            is SyncState.Finishing -> {
                notificationBuilder
            }

            // SyncState.Connecting, SyncState.Syncing,
            else                   -> null
        }
    }
}

/**
 * @return if this is a new state we haven't processed
 */
class SyncsTracker {
    private val activeWorks =
        ConcurrentHashMap<String, Pair<WorkInfo.State, SyncState>>()

    fun trackWork(workInfo: WorkInfo, data: Data): Boolean {
        val previousState = activeWorks[workInfo.id.toString()]?.first
        val previousProgress = activeWorks[workInfo.id.toString()]?.second
        val currentState = workInfo.state
        val currentProgress = SyncWorker.getState(data)

        activeWorks[workInfo.id.toString()] = Pair(currentState, currentProgress)

        if (currentState.isFinished) activeWorks.remove(workInfo.id.toString())


        return previousState != currentState ||
                (currentState == WorkInfo.State.RUNNING && previousProgress != currentProgress)
    }
}
