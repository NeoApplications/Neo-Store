package com.machiav3lli.fdroid.manager.network

import android.util.Log
import com.machiav3lli.fdroid.CLIENT_CONNECT_TIMEOUT
import com.machiav3lli.fdroid.CLIENT_READ_TIMEOUT
import com.machiav3lli.fdroid.CLIENT_WRITE_TIMEOUT
import com.machiav3lli.fdroid.data.database.entity.ClientCounts
import com.machiav3lli.fdroid.data.database.entity.DownloadStatsData
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.plusMonth
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.yearMonth
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class DownloadStatsAPI {
    private val onionProxy = Proxy(Proxy.Type.SOCKS, InetSocketAddress("127.0.0.1", 9050))
    var proxy: Proxy? = null
        set(value) {
            if (field != value) {
                field = value
            }
        }

    private fun getProxy(onion: Boolean) = if (onion) onionProxy else proxy

    val client = HttpClient(OkHttp) {
        engine {
            config {
                connectTimeout(CLIENT_CONNECT_TIMEOUT, TimeUnit.SECONDS)
                readTimeout(CLIENT_READ_TIMEOUT, TimeUnit.SECONDS)
                writeTimeout(CLIENT_WRITE_TIMEOUT, TimeUnit.SECONDS)
                proxy(getProxy(false))
                retryOnConnectionFailure(true)
            }
        }
    }

    private suspend fun fetchMonthlyFile(
        fileName: String,
        lastModified: String?
    ): MonthlyFileResult {
        val request = HttpRequestBuilder().apply {
            url("https://codeberg.org/IzzyOnDroid/iod-stats-collector/raw/branch/pages/stats/upstream/monthly-in-days/$fileName")
            headers {
                if (!lastModified.isNullOrEmpty()) {
                    append(HttpHeaders.IfModifiedSince, lastModified)
                }
            }
        }

        return try {
            val result = client.get(request)

            when {
                result.status.isSuccess()                   -> {
                    val data = DownloadStatsData.fromStream(result.bodyAsChannel().toInputStream())
                    val newLastModified = result.headers[HttpHeaders.LastModified]

                    Log.d(this::class.java.simpleName, "Successfully fetched $fileName")
                    MonthlyFileResult(
                        fileName = fileName,
                        data = data,
                        lastModified = newLastModified,
                        success = true
                    )
                }

                result.status == HttpStatusCode.NotModified -> {
                    Log.d(
                        this::class.java.simpleName,
                        "File $fileName not modified since last fetch"
                    )
                    MonthlyFileResult(
                        fileName = fileName,
                        data = null,
                        lastModified = lastModified,
                        success = true
                    )
                }

                else                                        -> {
                    Log.w(
                        this::class.java.simpleName,
                        "Failed to fetch $fileName: ${result.status}"
                    )
                    MonthlyFileResult(
                        fileName = fileName,
                        data = null,
                        lastModified = null,
                        success = false
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(this::class.java.simpleName, "Exception fetching $fileName", e)
            MonthlyFileResult(
                fileName = fileName,
                data = null,
                lastModified = null,
                success = false
            )
        }
    }

    suspend fun getMonthlyStats(
        existingModifiedDates: Map<String, String> = emptyMap()
    ): List<MonthlyFileResult> = coroutineScope {
        val fileNames = generateMonthlyFileNames()

        Log.d(this::class.java.simpleName, "Fetching ${fileNames.size} monthly files")

        val results = fileNames.map { fileName ->
            async {
                val lastModified = existingModifiedDates[fileName]
                fetchMonthlyFile(fileName, lastModified)
            }
        }.awaitAll()

        val successfulResults = results.filter { it.success }
        val updatedResults = results.filter { it.success && it.data != null }

        Log.i(
            this::class.java.simpleName,
            "Fetch summary: ${successfulResults.size}/${fileNames.size} successful, " +
                    "${updatedResults.size} updated, ${fileNames.size - successfulResults.size} failed"
        )

        return@coroutineScope results
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
}

data class MonthlyFileResult(
    val fileName: String,
    val data: Map<String, Map<String, ClientCounts>>?,
    val lastModified: String?,
    val success: Boolean
)