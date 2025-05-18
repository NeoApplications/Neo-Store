package com.machiav3lli.fdroid.manager.network

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.machiav3lli.fdroid.CLIENT_CONNECT_TIMEOUT
import com.machiav3lli.fdroid.CLIENT_READ_TIMEOUT
import com.machiav3lli.fdroid.CLIENT_USER_AGENT
import com.machiav3lli.fdroid.CLIENT_WRITE_TIMEOUT
import com.machiav3lli.fdroid.HOST_ICON
import com.machiav3lli.fdroid.HOST_SCREENSHOT
import com.machiav3lli.fdroid.POOL_DEFAULT_KEEP_ALIVE_DURATION_M
import com.machiav3lli.fdroid.POOL_DEFAULT_MAX_IDLE_CONNECTIONS
import com.machiav3lli.fdroid.QUERY_ADDRESS
import com.machiav3lli.fdroid.QUERY_AUTHENTICATION
import com.machiav3lli.fdroid.QUERY_DEVICE
import com.machiav3lli.fdroid.QUERY_DPI
import com.machiav3lli.fdroid.QUERY_ICON
import com.machiav3lli.fdroid.QUERY_LOCALE
import com.machiav3lli.fdroid.QUERY_METADATA_ICON
import com.machiav3lli.fdroid.QUERY_PACKAGE_NAME
import com.machiav3lli.fdroid.QUERY_SCREENSHOT
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.data.database.entity.Repository
import com.machiav3lli.fdroid.utils.extension.text.nullIfEmpty
import io.ktor.http.HttpHeaders
import okhttp3.Cache
import okhttp3.Call
import okhttp3.ConnectionPool
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.net.InetSocketAddress
import java.net.Proxy
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager


object CoilDownloader {

    private val clients = ConcurrentHashMap<String, OkHttpClient>()
    private val connectionPools = ConcurrentHashMap<String, ConnectionPool>()
    var proxy: Proxy? = null
        set(value) {
            Log.i(this.javaClass.name, "updating coil proxies.")
            if (field != value) {
                field = value
                clients.keys.removeAll { !it.endsWith(".onion") }
            }
        }

    private fun getProxy(onion: Boolean) =
        if (onion) Proxy(Proxy.Type.SOCKS, InetSocketAddress("localhost", 9050))
        else proxy

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
        .apply {
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
        .build()

    private fun updateClient(hostUrl: String, cache: Cache?): OkHttpClient {
        return synchronized(clients) {
            clients.getOrPut(hostUrl) {
                val isOnion = hostUrl.endsWith(".onion")
                val connectionPool = connectionPools.getOrPut(hostUrl) {
                    ConnectionPool(
                        POOL_DEFAULT_MAX_IDLE_CONNECTIONS,
                        POOL_DEFAULT_KEEP_ALIVE_DURATION_M,
                        TimeUnit.MINUTES
                    )
                }
                createClient(connectionPool, getProxy(isOnion), cache)
            }
        }
    }

    fun createCall(request: Request.Builder, authentication: String, cache: Cache?): Call {
        val newRequest = request.removeHeader(HttpHeaders.UserAgent)
            .addHeader(HttpHeaders.UserAgent, CLIENT_USER_AGENT)
            .apply {
                if (authentication.isNotEmpty()) {
                    request.addHeader("Authorization", authentication)
                }
            }
            .build()
        val client = updateClient(newRequest.url.host, cache)
        return client.newCall(newRequest)
    }

    class Factory(cacheDir: File) : Call.Factory {
        private val cache = Cache(cacheDir, 50_000_000L)

        override fun newCall(request: Request): Call {
            return when (request.url.host) {
                HOST_ICON       -> {
                    val address = request.url.queryParameter(QUERY_ADDRESS)?.nullIfEmpty()
                    val authentication = request.url.queryParameter(QUERY_AUTHENTICATION)
                    val path = run {
                        val packageName =
                            request.url.queryParameter(QUERY_PACKAGE_NAME)?.nullIfEmpty()
                        val icon = request.url.queryParameter(QUERY_ICON)?.nullIfEmpty()
                        val metadataIcon =
                            request.url.queryParameter(QUERY_METADATA_ICON)?.nullIfEmpty()
                        val dpi = request.url.queryParameter(QUERY_DPI)?.nullIfEmpty()
                        when {
                            icon != null
                                 -> "${if (dpi != null) "icons-$dpi" else "icons"}/$icon"

                            packageName != null && metadataIcon != null
                                 -> "$packageName/$metadataIcon"

                            else -> null
                        }
                    }
                    if (address == null || path == null) {
                        createCall(Request.Builder(), "", null)
                    } else {
                        createCall(
                            request.newBuilder().url(
                                address.toHttpUrl()
                                    .newBuilder()
                                    .addPathSegments(path)
                                    .build()
                            ), authentication.orEmpty(), cache
                        )
                    }
                }

                HOST_SCREENSHOT -> {
                    val address = request.url.queryParameter(QUERY_ADDRESS)
                    val authentication = request.url.queryParameter(QUERY_AUTHENTICATION)
                    val packageName = request.url.queryParameter(QUERY_PACKAGE_NAME)
                    val locale = request.url.queryParameter(QUERY_LOCALE)
                    val device = request.url.queryParameter(QUERY_DEVICE)
                    val screenshot = request.url.queryParameter(QUERY_SCREENSHOT)
                    if (screenshot.isNullOrEmpty() || address.isNullOrEmpty()) {
                        createCall(Request.Builder(), "", null)
                    } else {
                        createCall(
                            request.newBuilder().url(
                                address.toHttpUrl()
                                    .newBuilder()
                                    .addPathSegment(packageName.orEmpty())
                                    .addPathSegment(locale.orEmpty())
                                    .addPathSegment(device.orEmpty())
                                    .addPathSegment(screenshot).build()
                            ),
                            authentication.orEmpty(), cache
                        )
                    }
                }

                else            -> {
                    createCall(request.newBuilder(), "", null)
                }
            }
        }
    }
}

fun createScreenshotUri(
    repository: Repository,
    screenshot: String,
): Uri = (repository.address + screenshot).toUri()

fun createIconUri(icon: String, address: String?, auth: String?): Uri =
    (address + icon).toUri().let {
        if (auth.isNullOrEmpty()) it
        else it.buildUpon().appendQueryParameter(QUERY_AUTHENTICATION, auth).build()
    }