package com.machiav3lli.fdroid.network

import com.machiav3lli.fdroid.utility.ProgressInputStream
import com.machiav3lli.fdroid.utility.getBaseUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Cache
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Streaming
import retrofit2.http.Url
import java.io.File
import java.io.FileOutputStream
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit

object Downloader {
    private val clients = mutableMapOf<ClientConfiguration, OkHttpClient>()
    private val onionProxy = Proxy(Proxy.Type.SOCKS, InetSocketAddress("127.0.0.1", 9050))
    var proxy: Proxy? = null
        set(value) {
            if (field != value) {
                synchronized(clients) {
                    field = value
                    clients.keys.removeAll { !it.onion }
                }
            }
        }

    private var client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30L, TimeUnit.SECONDS)
        .readTimeout(15L, TimeUnit.SECONDS)
        .writeTimeout(15L, TimeUnit.SECONDS)
        .build()

    private var retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("http://PLACEHOLDER/") // TODO replace with the appropriate base URL?
        .client(client)
        .build()

    private val apiService: ApiService = retrofit.create(ApiService::class.java)

    private data class ClientConfiguration(val cache: Cache?, val onion: Boolean)
    interface ApiService {
        @GET
        @Streaming
        suspend fun downloadFile(
            @Url url: String,
            @Header("If-Modified-Since") lastModified: String?,
            @Header("If-None-Match") entityTag: String?,
            @Header("Range") range: String?,
            @Header("Authorization") authentication: String,
        ): Response<ResponseBody>
    }

    class Result(val code: Int, val lastModified: String, val entityTag: String) {
        val success: Boolean
            get() = code == 200 || code == 206

        val isNotChanged: Boolean
            get() = code == 304

        constructor(response: Response<ResponseBody>) : this(
            response.code(),
            response.headers()["Last-Modified"].orEmpty(),
            response.headers()["ETag"].orEmpty()
        )
    }

    private fun createClient(proxy: Proxy?, cache: Cache?): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30L, TimeUnit.SECONDS)
        .readTimeout(15L, TimeUnit.SECONDS)
        .writeTimeout(15L, TimeUnit.SECONDS)
        .proxy(proxy).cache(cache).build()

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
        val isOnion = hostUrl.endsWith(".onion")
        return synchronized(clients) {
            val proxy = if (isOnion) onionProxy else proxy
            val clientConfiguration = ClientConfiguration(cache, isOnion)
            clients[clientConfiguration] ?: run {
                val client = createClient(proxy, cache)
                clients[clientConfiguration] = client
                client
            }
        }
    }

    suspend fun download(
        url: String,
        target: File,
        lastModified: String,
        entityTag: String,
        authentication: String,
        callback: ((read: Long, total: Long?) -> Unit)?,
    ): Result {
        return withContext(Dispatchers.IO) {
            val start =
                if (target.exists()) target.length().let { if (it > 0L) it else null } else null
            val rangeHeader = start?.let { "bytes=$start-" }
            val baseUrl = getBaseUrl(url)
            client = updateClient(baseUrl, null)
            retrofit = retrofit.newBuilder().baseUrl(baseUrl).client(client).build()
            val response =
                apiService.downloadFile(url, lastModified, entityTag, rangeHeader, authentication)

            val result = response.body().use { responseBody ->
                if (response.code() == 304) {
                    Result(response.code(), lastModified, entityTag)
                } else if (response.isSuccessful) {
                    val append = start != null && response.headers()["Content-Range"] != null
                    val progressStart = if (append && start != null) start else 0L
                    val progressTotal =
                        responseBody?.contentLength()?.let { len -> if (len >= 0L) len else null }
                            ?.let { len -> progressStart + len }

                    val inputStream = ProgressInputStream(responseBody!!.byteStream()) {
                        if (Thread.interrupted()) {
                            throw InterruptedException()
                        }
                        callback?.invoke(progressStart + it, progressTotal)
                    }

                    inputStream.use { input ->
                        val outputStream =
                            if (append) FileOutputStream(target, true) else FileOutputStream(target)
                        outputStream.use { output ->
                            input.copyTo(output)
                            output.fd.sync()
                        }
                    }

                    Result(response)
                } else {
                    throw Exception("Failed to download file. Response code: ${response.code()}")
                }
            }

            result
        }
    }
}
