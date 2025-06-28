package com.machiav3lli.fdroid.manager.network

import android.util.Log
import com.machiav3lli.fdroid.CLIENT_CONNECT_TIMEOUT
import com.machiav3lli.fdroid.CLIENT_READ_TIMEOUT
import com.machiav3lli.fdroid.CLIENT_WRITE_TIMEOUT
import com.machiav3lli.fdroid.data.database.entity.RBData
import com.machiav3lli.fdroid.data.database.entity.RBLog
import com.machiav3lli.fdroid.data.database.entity.RBLogs
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit

class RBAPI {
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
        // TODO make an option in Preferences
        const val URL_BASE = "https://codeberg.org/IzzyOnDroid/rbtlog/raw/branch/izzy/log"
    }

    suspend fun getIndex(): Map<String, List<RBData>> {
        val request = HttpRequestBuilder().apply {
            url("$URL_BASE/index.json")
        }

        val result = client.get(request)
        if (!result.status.isSuccess())
            Log.w(this::javaClass.name, "getIndex() failed: ${result.status}")

        return when {
            result.status.isSuccess() -> RBLogs.fromJson(result.bodyAsText())
            else                      -> emptyMap()
        }
    }
}

fun RBData.toLog(hash: String): RBLog = RBLog(
    hash = hash,
    repository = repository,
    apk_url = apk_url,
    appid = appid,
    version_code = version_code,
    version_name = version_name,
    tag = tag,
    commit = commit,
    timestamp = timestamp,
    reproducible = reproducible,
    error = error
)

fun Map<String, List<RBData>>.toLogs(): List<RBLog> {
    return this.flatMap { (hash, data) ->
        data.map { it.toLog(hash) }
    }
}