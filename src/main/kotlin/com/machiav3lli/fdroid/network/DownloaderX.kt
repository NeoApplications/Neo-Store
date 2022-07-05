package com.machiav3lli.fdroid.network

import com.machiav3lli.fdroid.utility.ProgressInputStream
import com.machiav3lli.fdroid.utility.extension.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Cache
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit.SECONDS

object DownloaderX {
    private val client = OkHttpClient()

    private data class ClientConfiguration(val cache: Cache?, val onion: Boolean)

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

    private fun createCall(request: Request.Builder, authentication: String): Call {
        val oldRequest = request.build()
        val newRequest = if (authentication.isNotEmpty()) {
            request.addHeader("Authorization", authentication).build()
        } else {
            request.build()
        }
        val onion = oldRequest.url.host.endsWith(".onion")
        val client = synchronized(clients) {
            val proxy = if (onion) onionProxy else proxy
            val clientConfiguration = ClientConfiguration(null, onion)
            clients[clientConfiguration] ?: run {
                val client = this.client
                    .newBuilder()
                    .fastFallback(true)
                    .connectTimeout(30L, SECONDS)
                    .readTimeout(15L, SECONDS)
                    .writeTimeout(15L, SECONDS)
                    .proxy(proxy).build()
                clients[clientConfiguration] = client
                client
            }
        }
        return client.newCall(newRequest)
    }

    suspend fun startDownload(
        url: String,
        partialFile: File,
        authentication: String
    ): Flow<DownloadResult<Unit>> = flow {
        val scope = currentCoroutineContext()
        val start = if (partialFile.exists()) partialFile.length()
            .let { if (it > 0L) it else null } else null
        val request = Request.Builder().url(url)
            .apply { if (start != null) addHeader("Range", "bytes=$start-") }

        val response = createCall(request, authentication).await()

        response.use { it ->
            if (it.code == 304) emit(DownloadResult.Loading())
            else {
                val body = it.body!!
                val append = start != null && it.header("Content-Range") != null
                val progressStart = if (append && start != null) start else 0L
                val progressTotal =
                    body.contentLength().let { if (it >= 0L) it else null }
                        ?.let { progressStart + it }
                withContext(Dispatchers.IO + scope) {
                    val inputStream = ProgressInputStream(body.byteStream()) {
                        if (Thread.interrupted()) {
                            launch { emit(DownloadResult.Error("Thread Interrupted")) }
                            throw InterruptedException()
                        }
                        launch {
                            emit(
                                DownloadResult.Loading(
                                    progress = progressStart + it,
                                    total = progressTotal
                                )
                            )
                        }
                    }
                    inputStream.use { input ->
                        val outputStream =
                            if (append) FileOutputStream(partialFile, true)
                            else FileOutputStream(partialFile)
                        outputStream.use { output ->
                            input.copyTo(output)
                            output.fd.runCatching { sync() }
                                .onSuccess { emit(DownloadResult.Success(Unit)) }
                                .onFailure { emit(DownloadResult.Error(it.message.toString())) }
                        }
                    }
                }
            }
        }

    }

}