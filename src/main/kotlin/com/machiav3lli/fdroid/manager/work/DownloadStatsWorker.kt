package com.machiav3lli.fdroid.manager.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.machiav3lli.fdroid.ARG_EXCEPTION
import com.machiav3lli.fdroid.ARG_FORCE_WORK
import com.machiav3lli.fdroid.NeoApp
import com.machiav3lli.fdroid.TAG_SYNC_PERIODIC
import com.machiav3lli.fdroid.data.database.entity.toDownloadStats
import com.machiav3lli.fdroid.data.repository.PrivacyRepository
import com.machiav3lli.fdroid.manager.network.DownloadStatsAPI
import com.machiav3lli.fdroid.manager.network.MonthlyFileResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.android.annotation.KoinWorker
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinWorker
class DownloadStatsWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params), KoinComponent {
    private val dsAPI: DownloadStatsAPI by inject()
    private val privacyRepository: PrivacyRepository by inject()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        runCatching {
            fetchData()
        }.fold(
            onSuccess = { filesProcessed ->
                Log.i(TAG, "Successfully processed $filesProcessed monthly files")
                Result.success(workDataOf("files_processed" to filesProcessed))
            },
            onFailure = { throwable ->
                Log.e(TAG, "Failed fetching download stats", throwable)
                Result.failure(workDataOf(ARG_EXCEPTION to throwable.message))
            }
        )
    }

    private suspend fun fetchData(): Int = withContext(Dispatchers.IO) {
        val existingModifiedDates = getExistingModifiedDates()
        val monthlyResults = dsAPI.getMonthlyStats(existingModifiedDates)

        var filesProcessed = 0
        var filesUpdated = 0
        val failedFiles = mutableListOf<String>()

        monthlyResults.forEach { result ->
            when {
                result.success && result.data != null -> {
                    processMonthlyData(result)
                    filesProcessed++
                    filesUpdated++

                    saveLastModifiedDate(result.fileName, result.lastModified)

                    Log.d(this::class.java.simpleName, "Processed updated file: ${result.fileName}")
                }

                // File not modified
                result.success && result.data == null -> {
                    filesProcessed++
                    Log.d(this::class.java.simpleName, "File not modified: ${result.fileName}")
                }

                else                                  -> {
                    failedFiles.add(result.fileName)
                    Log.w(this::class.java.simpleName, "Failed to fetch: ${result.fileName}")
                }
            }
        }

        Log.i(
            this::class.java.simpleName,
            "Monthly data fetch complete: $filesUpdated updated, " +
                    "${filesProcessed - filesUpdated} unchanged, ${failedFiles.size} failed"
        )

        if (failedFiles.isNotEmpty()) {
            Log.w(this::class.java.simpleName, "Failed files: ${failedFiles.joinToString()}")
        }

        return@withContext filesProcessed
    }

    private suspend fun processMonthlyData(result: MonthlyFileResult) {
        result.data?.let { data ->
            try {
                val downloadStats = data.toDownloadStats()
                privacyRepository.upsertDownloadStats(downloadStats)
                result.lastModified?.let { lastModified ->
                    privacyRepository.upsertDownloadStatsFileMetadata(
                        fileName = result.fileName,
                        lastModified = lastModified,
                        recordsCount = downloadStats.size
                    )
                }

                Log.d(
                    this::class.java.simpleName,
                    "Saved ${downloadStats.size} download stat records from ${result.fileName}"
                )
            } catch (e: Exception) {
                Log.e(
                    this::class.java.simpleName,
                    "Failed to process data from ${result.fileName}",
                    e
                )
                throw e
            }
        }
    }

    private suspend fun saveLastModifiedDate(fileName: String, lastModified: String?) {
        withContext(Dispatchers.IO) {
            lastModified?.let { modifiedDate ->
                try {
                    privacyRepository.upsertDownloadStatsFileMetadata(
                        fileName = fileName,
                        lastModified = modifiedDate
                    )
                    Log.d(
                        this::class.java.simpleName,
                        "Saved lastModified for $fileName: $modifiedDate"
                    )
                } catch (e: Exception) {
                    Log.e(
                        this::class.java.simpleName,
                        "Failed to save lastModified for $fileName",
                        e
                    )
                }
            }
        }
    }

    private suspend fun getExistingModifiedDates(): Map<String, String> =
        withContext(Dispatchers.IO) {
            try {
                privacyRepository.loadDownloadStatsModifiedMap()
            } catch (e: Exception) {
                Log.e(this::class.java.simpleName, "Failed to get existing modified dates", e)
                emptyMap()
            }
        }

    companion object {
        private const val TAG = "DownloadStatsWorker"

        fun fetchDownloadStats(force: Boolean = false) {
            NeoApp.wm.enqueueUniqueWork(
                "download_stats",
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<DownloadStatsWorker>()
                    .addTag(TAG_SYNC_PERIODIC)
                    .setInputData(workDataOf(ARG_FORCE_WORK to force))
                    .build()
            )
        }
    }
}