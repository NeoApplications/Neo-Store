package com.machiav3lli.fdroid.network

import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.database.getIntOrNull
import androidx.core.database.getLongOrNull
import androidx.core.net.toUri
import com.machiav3lli.fdroid.BuildConfig
import com.machiav3lli.fdroid.CLIENT_CONNECT_TIMEOUT_MS
import com.machiav3lli.fdroid.POOL_DEFAULT_KEEP_ALIVE_DURATION_M
import com.machiav3lli.fdroid.POOL_DEFAULT_MAX_IDLE_CONNECTIONS
import com.machiav3lli.fdroid.service.worker.DownloadTask
import com.machiav3lli.fdroid.utility.dmReasonToHttpResponse
import com.machiav3lli.fdroid.utility.extension.text.formatDateTime
import io.ktor.client.HttpClient
import io.ktor.client.engine.ProxyBuilder
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.engine.resolveAddress
import io.ktor.client.plugins.BodyProgress
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.logging.ANDROID
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.onDownload
import io.ktor.client.plugins.retry
import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.client.request.host
import io.ktor.client.request.port
import io.ktor.client.request.prepareGet
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.CacheControl
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.ifNoneMatch
import io.ktor.http.isSuccess
import io.ktor.util.network.hostname
import io.ktor.util.network.port
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import okhttp3.ConnectionPool
import okhttp3.ConnectionSpec
import org.koin.dsl.module
import org.koin.mp.KoinPlatform.getKoin
import java.io.File
import java.io.FileOutputStream
import java.net.Proxy
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.pow

object Downloader {

    private val client: HttpClient by getKoin().inject()

    private val retries = mutableMapOf<String, AtomicInteger>()

    var proxy: Proxy? = null
        set(value) {
            Log.i(this.javaClass.name, "updating main proxy to $value")
            if (field != value) {
                field = value
            }
        }

    class Result(val statusCode: HttpStatusCode, val lastModified: String, val entityTag: String) {
        val success: Boolean
            get() = statusCode.isSuccess()

        val isNotChanged: Boolean
            get() = statusCode == HttpStatusCode.NotModified

        constructor(response: HttpResponse) : this(
            response.status,
            response.headers["Last-Modified"].orEmpty(),
            response.headers["ETag"].orEmpty()
        )
    }

    private fun getProxy(onion: Boolean) =
        if (onion) ProxyBuilder.socks("localhost", 9050)
        else proxy


    suspend fun download(
        url: String,
        target: File,
        lastModified: String,
        entityTag: String,
        authentication: String,
        callback: (suspend (read: Long, total: Long?) -> Unit)?,
    ): Result {
        return coroutineScope {
            var start = if (target.exists()) target.length().coerceAtLeast(0L)
            else null
            Log.i(this.javaClass.name, "download start byte = $start")

            try {
                client.prepareGet {
                    url(url)
                    getProxy(url.endsWith(".onion"))?.resolveAddress()?.let {
                        this.host = it.hostname
                        this.port = it.port
                    }
                    headers {
                        append(HttpHeaders.IfModifiedSince, lastModified)
                        append(HttpHeaders.Authorization, authentication)
                        append(HttpHeaders.AcceptEncoding, "gzip, deflate")
                        append(HttpHeaders.CacheControl, CacheControl.MaxAge(60).toString())
                    }
                    ifNoneMatch(entityTag)
                    if (start != null) header(HttpHeaders.Range, start?.let { "bytes=$it-" })
                    retry {
                        modifyRequest {
                            start = if (target.exists()) target.length().coerceAtLeast(0L)
                            else null
                            if (start != null) header(
                                HttpHeaders.Range,
                                start?.let { "bytes=$it-" })
                        }
                    }
                    this.onDownload { read, total ->
                        val progressStart = start ?: 0L
                        val progressTotal = progressStart + total

                        if (Thread.interrupted()) {
                            throw InterruptedException()
                        }
                        CoroutineScope(Dispatchers.IO).launch {
                            callback?.invoke(progressStart + read, progressTotal)
                        }
                    }
                }.execute { response ->
                    when {
                        response.status == HttpStatusCode.NotModified                                                        -> {
                            retries.remove(url)
                            Result(response.status, lastModified, entityTag)
                        }

                        response.status.isSuccess()                                                                          -> {
                            val append = start != null && response.headers["Content-Range"] != null
                            val channel = response.bodyAsChannel().toInputStream()

                            channel.use { input ->
                                val outputStream = FileOutputStream(target, append)
                                outputStream.use { output ->
                                    input.copyTo(output)
                                    output.fd.sync()
                                }
                            }

                            retries.remove(url)
                            Result(response)
                        }

                        response.status == HttpStatusCode.RequestedRangeNotSatisfiable                                       -> {
                            Log.w(
                                this.javaClass.name,
                                "Failed to download file ($url) with Range: ${start?.let { "bytes=$it-" }}."
                            )
                            target.delete()
                            download(url, target, lastModified, entityTag, authentication, callback)
                        }

                        response.status == HttpStatusCode.GatewayTimeout || response.status == HttpStatusCode.RequestTimeout -> {
                            download(url, target, lastModified, entityTag, authentication, callback)
                        }

                        else                                                                                                 -> {
                            Log.w(
                                this.javaClass.name,
                                "Failed to download file ($url). Response code ${response.status.value}:${response.status.description}."
                            )
                            throw Exception("Failed to download file. Response code ${response.status.value}:${response.status.description}")
                        }
                    }
                }
            } catch (e: Exception) {
                val leftRetries = retries.getOrPut(url) { AtomicInteger(5) }
                Log.w(
                    this.javaClass.name,
                    "Download ($url) faced exception. Tries left: $leftRetries. Exception: ${e.message}.\nStack trace: ${e.stackTrace}."
                )
                if (leftRetries.decrementAndGet() > 0) {
                    retries[url] = leftRetries
                    download(url, target, lastModified, entityTag, authentication, callback)
                } else throw e
            }
        }
    }

    suspend fun dmDownload(
        context: Context,
        task: DownloadTask,
        target: File,
        callback: (suspend (read: Long, total: Long?) -> Unit)?,
    ): Result = coroutineScope {
        val start = if (target.exists()) target.length().coerceAtLeast(0L)
        else null
        val progressStart = start ?: 0L

        val request = DownloadManager.Request(task.url.toUri())
            .setTitle("${task.name} (${task.release.version})")
            .setDescription(task.packageName)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
            .setDestinationUri(target.toUri())
            .apply {
                addRequestHeader(HttpHeaders.Authorization, task.authentication)
                addRequestHeader(HttpHeaders.AcceptEncoding, "gzip, deflate")
            }
        val downloadManager =
            ContextCompat.getSystemService(context, DownloadManager::class.java)
        val downloadID = downloadManager?.enqueue(request) ?: -1L

        var isDownloading = true
        var downloadStatus: Int
        var progressRead: Long
        var progressTotal: Long?
        var response: HttpStatusCode
        var lastModified: Long?
        var cursor: Cursor

        do {
            val downloadQuery = DownloadManager.Query()
            downloadQuery.setFilterById(downloadID)
            cursor = downloadManager!!.query(downloadQuery)
            cursor.moveToFirst()
            progressRead = cursor.getIntOrNull(
                cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
            )?.toLong() ?: 0L
            progressTotal = cursor.getIntOrNull(
                cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
            )?.toLong()
            lastModified = cursor.getLongOrNull(
                cursor.getColumnIndex(DownloadManager.COLUMN_LAST_MODIFIED_TIMESTAMP)
            )
            val responseStatus = cursor.getIntOrNull(
                cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
            )
            downloadStatus =
                cursor.getIntOrNull(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                    ?: DownloadManager.STATUS_PENDING

            response = responseStatus?.dmReasonToHttpResponse() ?: HttpStatusCode.OK

            CoroutineScope(Dispatchers.IO).launch {
                callback?.invoke(progressStart + progressRead, progressTotal)
            }

            when (downloadStatus) {
                DownloadManager.STATUS_SUCCESSFUL,
                DownloadManager.STATUS_FAILED,
                -> isDownloading = false
            }

            cursor.close()
        } while (isDownloading)
        Result(response, lastModified?.formatDateTime() ?: "", "")
    }
}

private fun initDownloadClient(): HttpClient = HttpClient(OkHttp) {
    engine {
        pipelining = true
        config {
            connectionPool(
                ConnectionPool(
                    POOL_DEFAULT_MAX_IDLE_CONNECTIONS,
                    POOL_DEFAULT_KEEP_ALIVE_DURATION_M,
                    TimeUnit.MINUTES
                )
            )
            retryOnConnectionFailure(true)
            followRedirects(true)
            followSslRedirects(true)
            connectionSpecs(
                listOf(
                    ConnectionSpec.RESTRICTED_TLS,
                    ConnectionSpec.MODERN_TLS,
                    ConnectionSpec.CLEARTEXT
                )
            )
        }
    }
    install(Logging) {
        logger = Logger.ANDROID
        level = LogLevel.ALL
    }
    install(UserAgent) {
        agent = "${BuildConfig.APPLICATION_ID}-${BuildConfig.VERSION_CODE}"
    }
    install(HttpTimeout) {
        connectTimeoutMillis = CLIENT_CONNECT_TIMEOUT_MS
        socketTimeoutMillis = CLIENT_CONNECT_TIMEOUT_MS
        requestTimeoutMillis = CLIENT_CONNECT_TIMEOUT_MS
    }
    install(BodyProgress)
    install(HttpRequestRetry) {
        maxRetries = 5
        delayMillis { retryNr -> (2.0.pow(retryNr) * 1000).toLong() }
        retryOnException(5, true)
    }
}

val downloadClientModule = module {
    single { initDownloadClient() }
}
