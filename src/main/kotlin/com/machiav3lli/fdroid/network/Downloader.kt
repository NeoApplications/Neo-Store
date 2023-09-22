package com.machiav3lli.fdroid.network

import android.util.Log
import com.machiav3lli.fdroid.CLIENT_CONNECT_TIMEOUT
import com.machiav3lli.fdroid.CLIENT_READ_TIMEOUT
import com.machiav3lli.fdroid.CLIENT_WRITE_TIMEOUT
import com.machiav3lli.fdroid.POOL_DEFAULT_KEEP_ALIVE_DURATION_MS
import com.machiav3lli.fdroid.POOL_DEFAULT_MAX_IDLE_CONNECTIONS
import com.machiav3lli.fdroid.utility.ProgressInputStream
import com.machiav3lli.fdroid.utility.getBaseUrl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.Call
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

object Downloader {
    class Result(val code: Int, val lastModified: String, val entityTag: String) {
        val success: Boolean
            get() = code == 200 || code == 206

        val isNotChanged: Boolean
            get() = code == 304

        constructor(response: Response) : this(
            response.code,
            response.headers["Last-Modified"].orEmpty(),
            response.headers["ETag"].orEmpty()
        )
    }

    private val clients = ConcurrentHashMap<String, OkHttpClient>()
    private val connectionPools = ConcurrentHashMap<String, ConnectionPool>()
    private val onionProxy = Proxy(Proxy.Type.SOCKS, InetSocketAddress("127.0.0.1", 9050))
    var proxy: Proxy? = null
        set(value) {
            if (field != value) {
                synchronized(clients) {
                    field = value
                    clients.keys.removeAll { !it.endsWith(".onion") }
                }
            }
        }

    private fun getProxy(onion: Boolean) = if (onion) onionProxy else proxy

    private fun buildRequest(
        url: String,
        lastModified: String,
        entityTag: String,
        authentication: String,
        range: String?,
    ): Request.Builder = Request.Builder()
        .url(url)
        .header("If-Modified-Since", lastModified)
        .header("If-None-Match", entityTag)
        .header("Authorization", authentication)
        .apply {
            if (range != null) header("Range", range)
        }

    private fun createClient(
        connectionPool: ConnectionPool,
        proxy: Proxy?,
        cache: Cache?,
    ): OkHttpClient = OkHttpClient.Builder()
        .connectionPool(connectionPool)
        .connectTimeout(CLIENT_CONNECT_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(CLIENT_READ_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(CLIENT_WRITE_TIMEOUT, TimeUnit.SECONDS)
        .proxy(proxy)
        .cache(cache)
        .followRedirects(true)
        .followSslRedirects(true)
        .retryOnConnectionFailure(true)
        .build()

    fun createCall(request: Request.Builder, authentication: String, cache: Cache?): Call {
        val newRequest = if (authentication.isNotEmpty()) {
            request.addHeader("Authorization", authentication).build()
        } else {
            request.build()
        }
        val client = updateClient(newRequest.url.host, cache)
        return client.newCall(newRequest)
    }

    private fun updateClient(hostUrl: String, cache: Cache?): OkHttpClient {
        return synchronized(clients) {
            clients.getOrPut(hostUrl) {
                val isOnion = hostUrl.endsWith(".onion")
                val connectionPool = connectionPools.getOrPut(hostUrl) {
                    ConnectionPool(
                        POOL_DEFAULT_MAX_IDLE_CONNECTIONS,
                        POOL_DEFAULT_KEEP_ALIVE_DURATION_MS,
                        TimeUnit.MILLISECONDS
                    )
                }
                createClient(connectionPool, getProxy(isOnion), cache)
            }
        }
    }

    suspend fun download(
        url: String,
        target: File,
        lastModified: String,
        entityTag: String,
        authentication: String,
        callback: (suspend (read: Long, total: Long?) -> Unit)?,
    ): Result {
        return withContext(Dispatchers.IO) {
            val start = if (target.exists()) target.length().coerceAtLeast(0L)
            else null
            Log.i(this.javaClass.name, "download start byte = $start")
            val rangeHeader = start?.let { "bytes=$it-" }
            val baseUrl = getBaseUrl(url)

            val connectionPool = connectionPools.getOrPut(baseUrl) {
                ConnectionPool(
                    POOL_DEFAULT_MAX_IDLE_CONNECTIONS,
                    POOL_DEFAULT_KEEP_ALIVE_DURATION_MS,
                    TimeUnit.MILLISECONDS
                )
            }

            val request: Request = buildRequest(
                url,
                lastModified,
                entityTag,
                authentication,
                rangeHeader,
            ).apply {
                val cacheControl = CacheControl.Builder()
                    .maxAge(1, TimeUnit.MINUTES)
                    .build()
                cacheControl(cacheControl)
                header("Accept-Encoding", "gzip, deflate")
            }.build()

            val client = clients.getOrPut(baseUrl) {
                OkHttpClient.Builder()
                    .connectionPool(connectionPool)
                    .connectTimeout(CLIENT_CONNECT_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(CLIENT_READ_TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(CLIENT_WRITE_TIMEOUT, TimeUnit.SECONDS)
                    .followRedirects(true)
                    .followSslRedirects(true)
                    .retryOnConnectionFailure(true)
                    .build()
            }

            val call = client.newCall(request)
            val response = call.execute()

            val result = response.body.use { responseBody ->
                if (response.code == 304) {
                    Result(response.code, lastModified, entityTag)
                } else if (response.isSuccessful) {
                    val append = start != null && response.headers["Content-Range"] != null
                    val progressStart = if (append && start != null) start else 0L
                    val contentLength = responseBody.contentLength().coerceAtLeast(0L)
                    val progressTotal = if (append) progressStart + contentLength
                    else contentLength

                    withContext(Dispatchers.IO) {
                        val inputStream = ProgressInputStream(responseBody.byteStream()) {
                            if (Thread.interrupted()) {
                                throw InterruptedException()
                            }
                            CoroutineScope(Dispatchers.IO).launch {
                                callback?.invoke(progressStart + it, progressTotal)
                            }
                        }

                        inputStream.use { input ->
                            val outputStream = FileOutputStream(target, append)
                            outputStream.use { output ->
                                input.copyTo(output)
                                output.fd.sync()
                            }
                        }
                    }

                    Result(response)
                } else if (response.code == 416) {
                    // Error code 416 means the requested range is invalid → retry from start
                    Log.w(
                        this.javaClass.name,
                        "Failed to download file ($url) with Range $rangeHeader."
                    )
                    target.delete()
                    download(url, target, lastModified, entityTag, authentication, callback)
                } else {
                    throw Exception("Failed to download file. Response code ${response.code}:${response.message}")
                }
            }

            result
        }
    }
}
