package com.machiav3lli.fdroid.manager.work

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Data
import androidx.work.WorkInfo
import com.machiav3lli.fdroid.NeoApp
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.data.database.entity.Downloaded
import com.machiav3lli.fdroid.data.entity.DownloadState
import com.machiav3lli.fdroid.data.repository.DownloadedRepository
import com.machiav3lli.fdroid.manager.service.InstallerReceiver
import com.machiav3lli.fdroid.utils.downloadNotificationBuilder
import com.machiav3lli.fdroid.utils.updateWithError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class DownloadStateHandler(
    private val context: Context,
    private val scope: CoroutineScope,
    private val downloadStates: WorkStateHolder<DownloadState>,
    private val notificationManager: NotificationManagerCompat,
    private val downloadedRepo: DownloadedRepository,
) {
    init {
        scope.launch {
            downloadStates.observeStates()
                .collect { states ->
                    states.forEach { (key, state) ->
                        handleDownloadState(key, state)
                    }
                }
        }
    }

    private fun handleDownloadState(key: String, state: DownloadState) {
        scope.launch {
            downloadedRepo.update(
                Downloaded(
                    packageName = state.packageName,
                    version = state.version,
                    repositoryId = state.repoId,
                    cacheFileName = state.cacheFileName,
                    changed = System.currentTimeMillis(),
                    state = state,
                )
            )
        }

        when (state) {
            is DownloadState.Success -> scope.launch {
                runCatching {
                    // TODO update notification
                    Log.d(
                        TAG,
                        "Download successful for ${state.packageName}, preparing installation"
                    )
                    NeoApp.db.getInstallTaskDao().upsert(state.toInstallTask())
                    InstallWorker.enqueue(
                        packageName = state.packageName,
                        label = state.name,
                        fileName = state.cacheFileName,
                        enforce = true,
                    )
                    downloadStates.updateState(key, null)
                    updateNotification(key, state)
                }.onFailure { e ->
                    Log.e(
                        TAG,
                        "Error processing successful download for ${state.packageName}: ${e.message}",
                        e
                    )
                }
            }

            is DownloadState.Error   -> {
                Log.e(
                    "DownloadState", "Download failed: ${state.packageName}",
                    Exception(state.validationError.toString())
                )
                if (state.stopReason != WorkInfo.STOP_REASON_NOT_STOPPED) Log.i(
                    this::class.java.name,
                    "stopReason: ${state.stopReason} for download task: ${state.packageName}"
                )
                updateNotification(key, state)
            }

            is DownloadState.Cancel  -> {
                Log.i(
                    "DownloadState", "Download canceled: ${state.packageName}"
                )
                updateNotification(key, state)
            }

            else                     -> {}
        }
    }

    fun updateState(key: String, state: DownloadState) {
        downloadStates.updateState(key, state)
    }

    private fun updateNotification(key: String, state: DownloadState) {
        val builder = context.createNotificationBuilder(state)
        if (state is DownloadState.Success || state is DownloadState.Cancel) {
            notificationManager.cancel(key.hashCode())
        } else if (builder != null && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) notificationManager.notify(key.hashCode(), builder.build())
    }

    private fun Context.createNotificationBuilder(state: DownloadState): NotificationCompat.Builder? {
        val title = getString(
            R.string.downloading_FORMAT,
            "${state.name} (${state.version})"
        )
        val notificationBuilder = downloadNotificationBuilder(title)

        return when (state) {
            is DownloadState.Cancel
                 -> notificationBuilder
                .setOngoing(false)
                .setContentText(getString(R.string.canceled))
                .setTimeoutAfter(InstallerReceiver.INSTALLED_NOTIFICATION_TIMEOUT)

            is DownloadState.Success
                 -> notificationBuilder
                .setOngoing(false)
                .setContentTitle(
                    getString(
                        R.string.downloaded_FORMAT,
                        state.name
                    )
                )
                .setTicker(
                    getString(
                        R.string.downloaded_FORMAT,
                        state.name
                    )
                )
                .apply {
                    if (!Preferences[Preferences.Key.KeepInstallNotification]) {
                        setTimeoutAfter(InstallerReceiver.INSTALLED_NOTIFICATION_TIMEOUT)
                    }
                }

            is DownloadState.Error
                 -> notificationBuilder
                .setOngoing(false)
                .updateWithError(this, state, state.validationError)
                .setTimeoutAfter(InstallerReceiver.INSTALLED_NOTIFICATION_TIMEOUT)

            // DownloadState.Pending, DownloadState.Connecting, DownloadState.Downloading
            else -> null
        }
    }

    companion object {
        const val TAG = "DownloadHandler"
    }
}

/**
 * @return if this is a new state we haven't processed
 */
class DownloadsTracker {
    private val activeWorks =
        ConcurrentHashMap<String, Pair<WorkInfo.State, DownloadWorker.Progress>>()

    fun trackWork(workInfo: WorkInfo, data: Data): Boolean {
        val previousState = activeWorks[workInfo.id.toString()]?.first
        val previousProgress = activeWorks[workInfo.id.toString()]?.second
        val currentState = workInfo.state
        val currentProgress = DownloadWorker.getProgress(data)

        activeWorks[workInfo.id.toString()] = Pair(currentState, currentProgress)

        if (currentState.isFinished) activeWorks.remove(workInfo.id.toString())

        return previousState != currentState ||
                (currentState == WorkInfo.State.RUNNING && previousProgress != currentProgress)
    }
}
