package com.machiav3lli.fdroid.manager.network

import android.util.Log
import com.machiav3lli.fdroid.BuildConfig
import com.machiav3lli.fdroid.CLIENT_CONNECT_TIMEOUT
import com.machiav3lli.fdroid.CLIENT_READ_TIMEOUT
import com.machiav3lli.fdroid.CLIENT_WRITE_TIMEOUT
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.data.database.entity.ExodusData
import com.machiav3lli.fdroid.data.database.entity.Trackers
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit

class RExodusAPI {

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

    companion object {
        const val URL_BASE = "https://reports.exodus-privacy.eu.org/api"
        const val AUTHENTICATION = "Token ${BuildConfig.KEY_API_EXODUS}"
    }

    suspend fun getTrackers(): Trackers {
        val request = HttpRequestBuilder().apply {
            url("$URL_BASE/trackers")
            headers {
                append(
                    HttpHeaders.IfModifiedSince,
                    Preferences[Preferences.Key.TrackersLastModified]
                )
                append(HttpHeaders.Authorization, AUTHENTICATION)
            }
        }

        val result = client.get(request)
        if (!result.status.isSuccess())
            Log.w(this::javaClass.name, "getTrackers() failed: ${result.status}")

        return when {
            result.status.isSuccess() -> {
                Preferences[Preferences.Key.TrackersLastModified] =
                    result.headers["Last-Modified"].orEmpty()
                Trackers.fromJson(result.bodyAsText()) ?: Trackers()
            }

            else                      -> Trackers()
        }
    }

    suspend fun getExodusInfo(packageName: String): List<ExodusData> {
        val request = HttpRequestBuilder().apply {
            url("$URL_BASE/search/$packageName/details")
            header("Authorization", AUTHENTICATION)
        }

        val result = client.get(request)
        if (!result.status.isSuccess())
            Log.w(this::javaClass.name, "getExodusInfo() failed: ${result.status}")

        return when {
            result.status.isSuccess() -> ExodusData.listFromJson(result.bodyAsText())
            else                      -> emptyList()
        }
    }
}