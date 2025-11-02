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
import com.machiav3lli.fdroid.data.content.Cache
import com.machiav3lli.fdroid.data.database.entity.ClientCounts
import com.machiav3lli.fdroid.data.database.entity.DownloadStatsData
import com.machiav3lli.fdroid.data.database.entity.toDownloadStats
import com.machiav3lli.fdroid.data.repository.PrivacyRepository
import com.machiav3lli.fdroid.manager.network.Downloader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.plusMonth
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.yearMonth
import org.koin.android.annotation.KoinWorker
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@KoinWorker
class DownloadStatsWorker(
    private val context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params), KoinComponent {
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

    // TODO add progress indication
    private suspend fun fetchData(): Int = withContext(Dispatchers.IO) {
        val existingModifiedDates = getExistingModifiedDates()
        val monthlyResults = fetchMonthlyStats(existingModifiedDates)

        var filesProcessed = 0
        var filesUpdated = 0
        val failedFiles = mutableListOf<String>()

        monthlyResults.forEach { result ->
            when {
                result.success && result.data != null -> {
                    processMonthlyData(result)
                    filesProcessed++
                    filesUpdated++
                    Log.d(TAG, "Processed updated file: ${result.fileName}")
                }

                // File not modified
                result.success && result.data == null -> {
                    filesProcessed++
                    Log.d(TAG, "File not modified: ${result.fileName}")
                }

                else                                  -> {
                    failedFiles.add(result.fileName)
                    Log.w(TAG, "Failed to fetch: ${result.fileName}")
                }
            }
        }

        Log.i(
            TAG,
            "Monthly data fetch complete: $filesUpdated updated, " +
                    "${filesProcessed - filesUpdated} unchanged, ${failedFiles.size} failed"
        )

        if (failedFiles.isNotEmpty()) {
            Log.w(TAG, "Failed files: ${failedFiles.joinToString()}")
        }

        return@withContext filesProcessed
    }

    private suspend fun fetchMonthlyStats(
        existingModifiedDates: Map<String, String>
    ): List<MonthlyFileResult> = coroutineScope {
        val fileNames = generateMonthlyFileNames()
        Log.d(TAG, "Fetching ${fileNames.size} monthly files")

        fileNames.map { fileName ->
            async {
                fetchMonthlyFile(fileName, existingModifiedDates[fileName])
            }
        }.awaitAll()
    }

    /**
     * Generates list of monthly file names from start date to current month
     * Format: YYYY-MM.json
     */
    @OptIn(ExperimentalTime::class)
    private fun generateMonthlyFileNames(): List<String> {
        val current =
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.yearMonth
        val start = YearMonth(2024, 12) // year and month of first report

        val fileNames = mutableListOf<String>()
        var ym = start
        while (ym <= current) {
            fileNames.add("$ym.json")
            ym = ym.plusMonth()
        }
        return fileNames
    }

    private suspend fun fetchMonthlyFile(
        fileName: String,
        lastModified: String?
    ): MonthlyFileResult {
        val url =
            "https://dlstats.izzyondroid.org/iod-stats-collector/stats/upstream/monthly-in-days/$fileName"
        val tempFile = Cache.getDownloadStatsFile(context, fileName)

        return try {
            // TODO add download progress notification
            val callback: suspend (Long, Long?, Long) -> Unit = { _, _, _ -> }

            val result = Downloader.download(
                url = url,
                target = tempFile,
                lastModified = lastModified ?: "",
                entityTag = "",
                authentication = "",
                callback = callback
            )

            when {
                result.isNotChanged -> {
                    Log.d(TAG, "File download_stats/$fileName not modified since last fetch")
                    MonthlyFileResult(
                        fileName = fileName,
                        data = null,
                        lastModified = lastModified,
                        success = true
                    )
                }

                result.success      -> {
                    val data = parseStatsFile(tempFile)
                    tempFile.delete()

                    Log.d(TAG, "Successfully fetched download_stats/$fileName")
                    MonthlyFileResult(
                        fileName = fileName,
                        data = data,
                        lastModified = result.lastModified.ifEmpty { null },
                        success = true
                    )
                }

                else                -> {
                    tempFile.delete()
                    Log.w(TAG, "Failed to fetch download_stats/$fileName: ${result.statusCode}")
                    MonthlyFileResult(
                        fileName = fileName,
                        data = null,
                        lastModified = null,
                        success = false
                    )
                }
            }
        } catch (e: Exception) {
            tempFile.delete()
            Log.e(TAG, "Exception fetching $fileName", e)
            MonthlyFileResult(
                fileName = fileName,
                data = null,
                lastModified = null,
                success = false
            )
        }
    }

    private fun parseStatsFile(file: File): Map<String, Map<String, ClientCounts>>? {
        return try {
            file.inputStream().use { stream ->
                DownloadStatsData.fromStream(stream)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse stats file", e)
            null
        }
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
                    TAG,
                    "Saved ${downloadStats.size} download stat records from ${result.fileName}"
                )
            } catch (e: Exception) {
                Log.e(
                    TAG,
                    "Failed to process data from ${result.fileName}",
                    e
                )
                throw e
            }
            // TODO add clean up call?
        }
    }

    private suspend fun getExistingModifiedDates(): Map<String, String> =
        withContext(Dispatchers.IO) {
            try {
                privacyRepository.loadDownloadStatsModifiedMap()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get existing modified dates", e)
                emptyMap()
            }
        }

    data class MonthlyFileResult(
        val fileName: String,
        val data: Map<String, Map<String, ClientCounts>>?,
        val lastModified: String?,
        val success: Boolean
    )

    companion object {
        private const val TAG = "DownloadStatsWorker"

        // TODO Make periodic instead of sync-bound
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