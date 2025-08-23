package com.machiav3lli.fdroid.manager.network

import android.util.Log
import com.machiav3lli.fdroid.CLIENT_CONNECT_TIMEOUT
import com.machiav3lli.fdroid.CLIENT_READ_TIMEOUT
import com.machiav3lli.fdroid.CLIENT_WRITE_TIMEOUT
import com.machiav3lli.fdroid.data.content.Preferences
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
import io.ktor.http.isSuccess
import io.ktor.utils.io.jvm.javaio.toInputStream
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit

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

    suspend fun getIndex(): Map<String, Map<String, ClientCounts>> {
        val request = HttpRequestBuilder().apply {
            // TODO update when stats server is stable
            url("https://codeberg.org/IzzyOnDroid/iod-stats-collector/raw/branch/pages/stats/upstream/yearly/_all.json")
            headers {
                append(
                    HttpHeaders.IfModifiedSince,
                    Preferences[Preferences.Key.DownloadStatsLastModified]
                )
            }
        }

        val result = client.get(request)
        if (!result.status.isSuccess())
            Log.w(this::javaClass.name, "getIndex() failed: ${result.status}")

        return when {
            result.status.isSuccess() -> {
                Preferences[Preferences.Key.DownloadStatsLastModified] =
                    result.headers["Last-Modified"].orEmpty()
                DownloadStatsData.fromStream(result.bodyAsChannel().toInputStream())
            }

            else                      -> emptyMap()
        }
    }
}
