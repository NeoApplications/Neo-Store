package com.machiav3lli.fdroid.manager.work

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Data
import androidx.work.WorkInfo
import com.machiav3lli.fdroid.NeoApp
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.data.entity.SyncState
import com.machiav3lli.fdroid.utils.syncNotificationBuilder
import com.machiav3lli.fdroid.utils.updateProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class SyncStateHandler(
    scope: CoroutineScope,
    private val syncStates: WorkStateHolder<SyncState>,
    private val notificationManager: NotificationManagerCompat
) {
    private val _syncEvents = Channel<UpdateEvent>(Channel.BUFFERED)

    init {
        scope.launch {
            syncStates.observeStates()
                .collect { states ->
                    states.forEach { (key, state) ->
                        handleSyncState(key, state)
                    }
                }
        }

        scope.launch {
            _syncEvents.consumeEach { event ->
                updateNotification(event)
            }
        }
    }

    private suspend fun handleSyncState(key: String, state: SyncState) {
        _syncEvents.send(
            UpdateEvent(
                key = key,
                state = state
            )
        )
    }

    fun updateState(key: String, state: SyncState?) {
        syncStates.updateState(key, state)
        if (state == null)
            notificationManager.cancel(key.hashCode())
    }

    private fun updateNotification(event: UpdateEvent) {
        val appContext = NeoApp.context
        val builder = createNotificationBuilder(event.state)
        if (ActivityCompat.checkSelfPermission(
                appContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) notificationManager.notify(
            event.key.hashCode(),
            builder.setOngoing(event.state !is SyncState.Failed)
                .build()
        )
    }

    private fun createNotificationBuilder(state: SyncState): NotificationCompat.Builder {
        val appContext = NeoApp.context
        val notificationBuilder = appContext.syncNotificationBuilder()

        return when (state) {
            is SyncState.Connecting -> {
                notificationBuilder
                    .setContentTitle(
                        appContext.getString(
                            R.string.syncing_FORMAT,
                            state.repoName
                        )
                    )
                    .setContentText(appContext.getString(R.string.connecting))
                    .setProgress(0, 0, true)
            }

            is SyncState.Syncing    -> {
                notificationBuilder
                    .setContentTitle(
                        appContext.getString(
                            R.string.syncing_FORMAT,
                            state.repoName
                        )
                    )
                    .updateProgress(appContext, state.progress)
            }

            is SyncState.Failed     -> {
                notificationBuilder
                    .setContentTitle(
                        appContext.getString(
                            R.string.syncing_FORMAT,
                            state.repoName
                        )
                    )
                    .setContentText(appContext.getString(R.string.action_failed)) // TODO Add error message
                    .setSmallIcon(R.drawable.ic_new_releases)
            }

            else                    -> {
                notificationBuilder
            }
        }
    }

    companion object {
        private data class UpdateEvent(
            val key: String,
            val state: SyncState,
        )
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
