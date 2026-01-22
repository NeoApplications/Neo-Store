package com.machiav3lli.fdroid.manager.work

import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.machiav3lli.fdroid.ARG_EXCEPTION
import com.machiav3lli.fdroid.ARG_FORCE_WORK
import com.machiav3lli.fdroid.ContextWrapperX
import com.machiav3lli.fdroid.NOTIFICATION_ID_DOWNLOAD_STATS
import com.machiav3lli.fdroid.NeoApp
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.TAG_DOWNLOAD_STATS_PERIODIC
import com.machiav3lli.fdroid.TAG_SYNC_PERIODIC
import com.machiav3lli.fdroid.data.content.Cache
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.data.database.entity.ClientCounts
import com.machiav3lli.fdroid.data.database.entity.DownloadStatsData
import com.machiav3lli.fdroid.data.database.entity.toDownloadStats
import com.machiav3lli.fdroid.data.repository.PrivacyRepository
import com.machiav3lli.fdroid.manager.network.Downloader
import com.machiav3lli.fdroid.utils.downloadStatsNotificationBuilder
import com.machiav3lli.fdroid.utils.extension.android.Android
import com.machiav3lli.fdroid.utils.extension.text.nullIfEmpty
import com.machiav3lli.fdroid.utils.extension.text.toInt
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.plusMonth
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.yearMonth
import org.koin.android.annotation.KoinWorker
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.java.KoinJavaComponent.get
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.incrementAndFetch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@KoinWorker
class DownloadStatsWorker(
    private val context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params), KoinComponent {
    private val privacyRepository: PrivacyRepository by inject()
    private val downloadSemaphore = Semaphore(3)
    private val langContext = ContextWrapperX.wrap(applicationContext)

    override suspend fun doWork(): Result {
        return runCatching {
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

    private suspend fun fetchData(): Int {
        val existingModifiedDates = getExistingModifiedDates()
        val (nFilesProcessed, nFilesUpdated, filesFailed) =
            fetchAndProcessMonthlyStats(existingModifiedDates)

        Log.i(
            TAG,
            "Monthly data fetch complete: $nFilesUpdated updated, " +
                    "${nFilesProcessed - nFilesUpdated - filesFailed.size} unchanged, ${filesFailed.size} failed"
        )

        if (filesFailed.isNotEmpty()) {
            Log.w(TAG, "Failed files: ${filesFailed.joinToString()}")
        }

        return nFilesProcessed
    }

    @OptIn(ExperimentalAtomicApi::class)
    private suspend fun fetchAndProcessMonthlyStats(
        existingModifiedDates: Map<String, String>
    ): Triple<Int, Int, List<String>> = coroutineScope {
        val fileNames = generateMonthlyFileNames()
        Log.d(TAG, "Fetching ${fileNames.size} monthly files")

        val filesProcessed = AtomicInt(0)
        val filesUpdated = AtomicInt(0)
        val filesFailed = mutableListOf<String>()

        fileNames.map { (fileName, dateInt) ->
            async {
                downloadSemaphore.withPermit {
                    fetchMonthlyFile(
                        fileName = fileName,
                        lastModified = existingModifiedDates[fileName]
                    ).let { result ->
                        when {
                            result.success && result.data != null -> {
                                processMonthlyData(result, dateInt)
                                Log.d(TAG, "Processed updated file: ${result.fileName}")
                                setForeground(
                                    createForegroundInfo(
                                        fileNames.size,
                                        filesProcessed.incrementAndFetch(),
                                        filesUpdated.incrementAndFetch(),
                                        filesFailed.size
                                    )
                                )
                            }

                            // File not modified
                            result.success && result.data == null -> {
                                Log.d(TAG, "File not modified: ${result.fileName}")
                                setForeground(
                                    createForegroundInfo(
                                        fileNames.size,
                                        filesProcessed.incrementAndFetch(),
                                        filesUpdated.load(),
                                        filesFailed.size
                                    )
                                )
                            }

                            else                                  -> {
                                filesFailed.add(result.fileName)
                                Log.w(TAG, "Failed to fetch: ${result.fileName}")
                                setForeground(
                                    createForegroundInfo(
                                        fileNames.size,
                                        filesProcessed.incrementAndFetch(),
                                        filesUpdated.load(),
                                        filesFailed.size
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }.awaitAll()
        Triple(filesProcessed.load(), filesUpdated.load(), filesFailed)
    }

    /**
     * Generates list of monthly file names from start date to current month
     * Format: YYYY-MM.json
     */
    @OptIn(ExperimentalTime::class)
    private fun generateMonthlyFileNames(): List<Pair<String, Int>> {
        val current =
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.yearMonth
        val start = YearMonth(2024, 12) // year and month of first report

        val fileNames = mutableListOf<Pair<String, Int>>()
        var ym = start
        while (ym <= current) {
            fileNames.add(Pair("$ym.json", ym.toInt()))
            ym = ym.plusMonth()
        }
        return fileNames
    }

    private suspend fun fetchMonthlyFile(
        fileName: String,
        lastModified: String?
    ): MonthlyFileResult {
        val url =
            "${Preferences[Preferences.Key.DLStatsProvider].url}/stats/upstream/monthly/$fileName"
        val tempFile = Cache.getDownloadStatsFile(context, fileName)

        return try {
            val callback: suspend (Long, Long?, Long) -> Unit = { _, _, _ -> }

            val result = Downloader.download(
                url = url,
                target = tempFile,
                lastModified = lastModified ?: "",
                entityTag = "",
                authentication = "",
                rated = false,
                callback = callback
            )

            when {
                result.isNotModified -> {
                    Log.d(TAG, "File download_stats/$fileName not modified since last fetch")
                    MonthlyFileResult(
                        fileName = fileName,
                        data = null,
                        lastModified = lastModified,
                        success = true
                    )
                }

                result.success       -> {
                    val data = parseStatsFile(tempFile)
                    tempFile.delete()

                    Log.d(TAG, "Successfully fetched download_stats/$fileName")
                    MonthlyFileResult(
                        fileName = fileName,
                        data = data,
                        lastModified = result.lastModified.nullIfEmpty(),
                        success = true
                    )
                }

                else                 -> {
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

    private fun parseStatsFile(file: File): Map<String, ClientCounts>? {
        return try {
            file.inputStream().use { stream ->
                DownloadStatsData.fromStream(stream)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse stats file", e)
            null
        }
    }

    private suspend fun processMonthlyData(result: MonthlyFileResult, dateInt: Int) {
        result.data?.let { data ->
            try {
                val downloadStats = data.toDownloadStats(dateInt)
                privacyRepository.upsertDownloadStats(downloadStats)
                result.lastModified?.let { lastModified ->
                    privacyRepository.upsertDownloadStatsFileMetadata(
                        fileName = result.fileName,
                        lastModified = lastModified,
                        recordsCount = downloadStats.size,
                    )
                }

                Log.i(
                    TAG,
                    "Saved ${downloadStats.size} download stat records from ${result.fileName}"
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to process data from ${result.fileName}", e)
            }
            // TODO add clean up call?
        }
    }

    private suspend fun getExistingModifiedDates(): Map<String, String> = try {
        privacyRepository.loadDownloadStatsModifiedMap()
    } catch (e: Exception) {
        Log.e(TAG, "Failed to get existing modified dates", e)
        emptyMap()
    }

    private val notificationBuilder by lazy {
        langContext.downloadStatsNotificationBuilder()
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            NOTIFICATION_ID_DOWNLOAD_STATS,
            notificationBuilder.build(),
            if (Android.sdk(Build.VERSION_CODES.Q)) ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            else 0
        )
    }

    private fun createForegroundInfo(
        total: Int = 1,
        done: Int = -1,
        updated: Int = 0,
        failed: Int = 0,
    ): ForegroundInfo {
        // TODO add cancel intent
        val notification = notificationBuilder
            .setProgress(total, done, false)
            .setContentText(
                context.getString(
                    R.string.download_stats_notification_message,
                    done, total, updated, done - updated - failed, failed,
                )
            )
            .build()

        return ForegroundInfo(
            NOTIFICATION_ID_DOWNLOAD_STATS,
            notification,
            if (Android.sdk(Build.VERSION_CODES.Q)) ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            else 0
        )
    }

    data class MonthlyFileResult(
        val fileName: String,
        val data: Map<String, ClientCounts>?,
        val lastModified: String?,
        val success: Boolean
    )

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

        private fun PeriodicRequest(
            networkType: NetworkType = NetworkType.CONNECTED,
            requiresCharging: Boolean = false,
        ): PeriodicWorkRequest = PeriodicWorkRequestBuilder<DownloadStatsWorker>(
            24L,
            TimeUnit.HOURS,
        )
            .setInputData(workDataOf(ARG_FORCE_WORK to false))
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(networkType)
                    .setRequiresCharging(requiresCharging)
                    .build()
            )
            .addTag(TAG_DOWNLOAD_STATS_PERIODIC)
            .build()

        fun enqueuePeriodic() {
            if (Preferences[Preferences.Key.DLStatsProvider] != Preferences.DLStatsProvider.None) {
                val autoSyncPref = Preferences[Preferences.Key.AutoSync]
                get<WorkManager>(WorkManager::class.java)
                    .enqueueUniquePeriodicWork(
                        TAG_DOWNLOAD_STATS_PERIODIC,
                        ExistingPeriodicWorkPolicy.UPDATE,
                        PeriodicRequest(
                            autoSyncPref.connectionType(),
                            autoSyncPref.requireBattery(),
                        ),
                    )
            } else get<WorkManager>(WorkManager::class.java)
                .cancelUniqueWork(TAG_DOWNLOAD_STATS_PERIODIC)
        }
    }
}