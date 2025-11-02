package com.machiav3lli.fdroid.manager.network

import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.database.getIntOrNull
import androidx.core.database.getLongOrNull
import androidx.core.net.toUri
import com.machiav3lli.fdroid.BUFFER_SIZE
import com.machiav3lli.fdroid.CLIENT_CONNECT_TIMEOUT_MS
import com.machiav3lli.fdroid.CLIENT_USER_AGENT
import com.machiav3lli.fdroid.POOL_DEFAULT_KEEP_ALIVE_DURATION_M
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.data.entity.DownloadTask
import com.machiav3lli.fdroid.utils.dmReasonToHttpResponse
import com.machiav3lli.fdroid.utils.extension.text.formatDateTime
import io.ktor.client.HttpClient
import io.ktor.client.engine.ProxyBuilder
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.engine.resolveAddress
import io.ktor.client.plugins.BodyProgress
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.compression.ContentEncoding
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
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import okhttp3.ConnectionPool
import okhttp3.ConnectionSpec
import org.koin.dsl.module
import org.koin.mp.KoinPlatform.getKoin
import java.io.File
import java.io.FileOutputStream
import java.net.Proxy
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.pow

object Downloader {
    private const val TAG = "Downloader"
    private val client: HttpClient by getKoin().inject()
    private val retries = ConcurrentHashMap<String, AtomicInteger>()
    private val downloadSemaphore = Semaphore(Preferences[Preferences.Key.MaxParallelDownloads])

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
        callback: suspend (read: Long, total: Long?, downloadID: Long) -> Unit,
    ): Result = downloadSemaphore.withPermit {
        Log.i(
            TAG,
            "Entering download of $url.\nPermissions left for parallel downloads: ${downloadSemaphore.availablePermits}"
        )
        permittedDownload(url, target, lastModified, entityTag, authentication, callback)
    }

    private suspend fun permittedDownload(
        url: String,
        target: File,
        lastModified: String,
        entityTag: String,
        authentication: String,
        callback: suspend (read: Long, total: Long?, downloadID: Long) -> Unit,
    ): Result = coroutineScope {
        ensureActive()

        var start = if (target.exists()) target.length().coerceAtLeast(0L)
        else null
        Log.i(TAG, "download start byte = $start")

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
                    //append(HttpHeaders.AcceptEncoding, "gzip, deflate")
                    append(HttpHeaders.CacheControl, CacheControl.MaxAge(60).toString())
                }
                ifNoneMatch(entityTag)
                if (start != null) header(HttpHeaders.Range, start.let { "bytes=$it-" })
                retry {
                    modifyRequest {
                        ensureActive()
                        start = if (target.exists()) target.length().coerceAtLeast(0L)
                        else null
                        if (start != null) header(
                            HttpHeaders.Range,
                            start?.let { "bytes=$it-" })
                    }
                }
                this.onDownload { read, total ->
                    ensureActive()

                    val progressStart = start ?: 0L
                    val progressTotal = total?.let { progressStart + total }

                    // Check if downloaded size exceeds total size
                    if (total != null && total > 0 && progressStart + read > total) {
                        throw DownloadSizeException("Downloaded size exceeds expected total size")
                    }

                    this@coroutineScope.launch {
                        callback.invoke(progressStart + read, progressTotal, -1L)
                    }
                }
            }.execute { response ->
                when {
                    response.status == HttpStatusCode.NotModified
                        -> {
                        retries.remove(url)
                        Result(response.status, lastModified, entityTag)
                    }

                    response.status.isSuccess()
                        -> {
                        val append = start != null && response.headers["Content-Range"] != null
                        val channel = response.bodyAsChannel().toInputStream()

                        channel.use { input ->
                            val outputStream = FileOutputStream(target, append)
                            outputStream.use { output ->
                                input.copyTo(output, BUFFER_SIZE)
                                output.fd.sync()
                            }
                        }

                        retries.remove(url)
                        Result(response)
                    }

                    response.status == HttpStatusCode.RequestedRangeNotSatisfiable
                        -> {
                        Log.w(
                            TAG,
                            "Failed to download file ($url) with Range: ${start?.let { "bytes=$it-" }}."
                        )
                        target.delete()
                        download(url, target, lastModified, entityTag, authentication, callback)
                    }

                    response.status == HttpStatusCode.GatewayTimeout || response.status == HttpStatusCode.RequestTimeout
                        -> download(
                        url,
                        target,
                        lastModified,
                        entityTag,
                        authentication,
                        callback
                    )

                    response.status == HttpStatusCode.NotFound -> {
                        Result(response)
                    }

                    else -> {
                        Log.w(
                            TAG,
                            "Failed to download file ($url). Response code ${response.status.value}:${response.status.description}."
                        )
                        throw Exception("Failed to download file. Response code ${response.status.value}:${response.status.description}")
                    }
                }
            }
        } catch (e: CancellationException) {
            retries.remove(url)
            throw e
        } catch (e: Exception) {
            val leftRetries = retries.getOrPut(url) { AtomicInteger(10) }
            Log.w(
                TAG,
                "Download ($url) faced exception. Tries left: $leftRetries. Exception: ${e.message}.\nStack trace: ${e.stackTrace}."
            )
            if (leftRetries.decrementAndGet() > 0) {
                retries[url] = leftRetries
                download(url, target, lastModified, entityTag, authentication, callback)
            } else throw e
        }
    }

    suspend fun dmDownload(
        context: Context,
        task: DownloadTask,
        target: File,
        callback: suspend (read: Long, total: Long?, downloadID: Long) -> Unit,
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
                addRequestHeader(HttpHeaders.UserAgent, CLIENT_USER_AGENT)
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

            // Check if downloaded size exceeds total size
            if (progressTotal != null && progressTotal != -1L && progressStart + progressRead > progressTotal) {
                downloadManager.remove(downloadID)
                throw DownloadSizeException("Downloaded size exceeds expected total size")
            }

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

            this.launch {
                callback.invoke(progressStart + progressRead, progressTotal, downloadID)
            }

            when (downloadStatus) {
                DownloadManager.STATUS_SUCCESSFUL,
                DownloadManager.STATUS_FAILED,
                    -> isDownloading = false
            }

            cursor.close()
            if (isDownloading) delay(100L) // avoid busy-waiting
        } while (isDownloading)

        // Final check after download completion
        if (progressTotal != null && progressTotal != -1L && target.length() > progressTotal) {
            downloadManager.remove(downloadID)
            throw DownloadSizeException("Downloaded size exceeds expected total size")
        }

        Result(response, lastModified?.formatDateTime() ?: "", "")
    }
}

private fun initDownloadClient(): HttpClient = HttpClient(OkHttp) {
    engine {
        pipelining = true
        config {
            connectionPool(
                ConnectionPool(
                    Preferences[Preferences.Key.MaxIdleConnections],
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
            if (Preferences[Preferences.Key.DisableCertificateValidation]) {
                val trustAllCerts = object : X509TrustManager {
                    override fun checkClientTrusted(
                        chain: Array<out X509Certificate>?,
                        authType: String?
                    ) {
                    }

                    override fun checkServerTrusted(
                        chain: Array<out X509Certificate>?,
                        authType: String?
                    ) {
                    }

                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                }
                val sslContext = SSLContext.getInstance("TLS")
                sslContext.init(null, arrayOf(trustAllCerts), SecureRandom())
                val sslSocketFactory = sslContext.socketFactory
                sslSocketFactory(sslSocketFactory, trustAllCerts)
                hostnameVerifier { _, _ -> true }
            }
        }
    }
    install(ContentEncoding) {
        gzip()
        deflate()
    }
    install(Logging) {
        logger = Logger.ANDROID
        level = LogLevel.INFO
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
    install(UserAgent) {
        agent = CLIENT_USER_AGENT
    }
}

val downloadClientModule = module {
    single { initDownloadClient() }
}

class DownloadSizeException(message: String, cause: Throwable? = null) : Exception(message, cause)