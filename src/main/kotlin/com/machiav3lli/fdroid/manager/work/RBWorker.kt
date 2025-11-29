package com.machiav3lli.fdroid.manager.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.machiav3lli.fdroid.ARG_EXCEPTION
import com.machiav3lli.fdroid.NeoApp
import com.machiav3lli.fdroid.TAG_SYNC_PERIODIC
import com.machiav3lli.fdroid.data.content.Cache
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.data.database.entity.RBData
import com.machiav3lli.fdroid.data.database.entity.RBLog
import com.machiav3lli.fdroid.data.database.entity.RBLogs
import com.machiav3lli.fdroid.data.repository.PrivacyRepository
import com.machiav3lli.fdroid.manager.network.Downloader
import org.koin.android.annotation.KoinWorker
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinWorker
class RBWorker(
    private val context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params), KoinComponent {
    private val privacyRepository: PrivacyRepository by inject()

    override suspend fun doWork(): Result {
        return runCatching {
            fetchLogs()
        }.fold(
            onSuccess = {
                Result.success()
            },
            onFailure = {
                Log.e(TAG, "Failed fetching exodus trackers", it)
                Result.failure(workDataOf(ARG_EXCEPTION to it.message))
            }
        )
    }

    private suspend fun fetchLogs() {
        val url = "${Preferences[Preferences.Key.RBProvider].url}/index.json"
        val lastModified = Preferences[Preferences.Key.RBLogsLastModified]

        // Create temporary file for download
        val tempFile = Cache.getTemporaryFile(context)

        try {
            val result = Downloader.download(
                url = url,
                target = tempFile,
                lastModified = lastModified,
                entityTag = "",
                authentication = "",
                rated = false,
                callback = { _, _, _ -> }
            )

            when {
                result.isNotModified -> {
                    Log.i(TAG, "RB index not modified")
                }

                result.success       -> {
                    // Update last modified preference
                    Preferences[Preferences.Key.RBLogsLastModified] = result.lastModified

                    // Parse and store logs
                    val logsMap = RBLogs.fromStream(tempFile.inputStream())
                    privacyRepository.upsertRBLogs(logsMap.toLogs())

                    Log.i(TAG, "Successfully fetched ${logsMap.size} RB logs")
                }

                else                 -> {
                    Log.w(TAG, "Failed to fetch RB index: ${result.statusCode}")
                }
            }
        } finally {
            // Clean up temporary file
            tempFile.delete()
        }
    }

    companion object {
        private const val TAG = "RBWorker"

        // TODO Make periodic instead of sync-bound
        fun fetchRBLogs() {
            NeoApp.wm.enqueueUniqueWork(
                "rb_index",
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<RBWorker>()
                    .addTag(TAG_SYNC_PERIODIC)
                    .build()
            )
        }
    }
}

private fun RBData.toLog(hash: String): RBLog = RBLog(
    hash = hash,
    repository = repository,
    apk_url = apk_url,
    appid = appid,
    version_code = version_code,
    version_name = version_name,
    tag = tag,
    commit = commit,
    timestamp = timestamp,
    reproducible = reproducible,
    error = error
)

private fun Map<String, List<RBData>>.toLogs(): List<RBLog> {
    return this.flatMap { (hash, data) ->
        data.map { it.toLog(hash) }
    }
}