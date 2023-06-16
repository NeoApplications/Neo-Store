package com.machiav3lli.fdroid.network

import android.util.Log
import com.machiav3lli.fdroid.BuildConfig
import com.machiav3lli.fdroid.CLIENT_CONNECT_TIMEOUT
import com.machiav3lli.fdroid.CLIENT_READ_TIMEOUT
import com.machiav3lli.fdroid.CLIENT_WRITE_TIMEOUT
import com.machiav3lli.fdroid.database.entity.ExodusData
import com.machiav3lli.fdroid.database.entity.Trackers
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import okhttp3.OkHttpClient
import okhttp3.Request
import org.koin.dsl.module
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

    val client = OkHttpClient.Builder()
        .connectTimeout(CLIENT_CONNECT_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(CLIENT_READ_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(CLIENT_WRITE_TIMEOUT, TimeUnit.SECONDS)
        .proxy(getProxy(false))
        .retryOnConnectionFailure(true)
        .build()

    companion object {
        const val URL_BASE = "https://reports.exodus-privacy.eu.org/api"
        const val AUTHENTICATION = "Token ${BuildConfig.KEY_API_EXODUS}"
    }

    fun getTrackers(): Trackers {
        val request = Request.Builder()
            .url("$URL_BASE/trackers")
            .header("Authorization", AUTHENTICATION)
            .build()

        val result = client.newCall(request).execute()
        if (!result.isSuccessful)
            Log.w(this::javaClass.name, "getTrackers() failed: Response code  ${result.code}")

        val moshi = Moshi.Builder().build()
        val adapter = moshi.adapter(Trackers::class.java)

        return when {
            result.isSuccessful -> adapter.fromJson(result.body.string()) ?: Trackers()
            else                -> Trackers()
        }
    }

    fun getExodusInfo(packageName: String): List<ExodusData> {
        val request = Request.Builder()
            .url("$URL_BASE/search/$packageName/details")
            .header("Authorization", AUTHENTICATION)
            .build()

        val result = client.newCall(request).execute()
        if (!result.isSuccessful)
            Log.w(this::javaClass.name, "getExodusInfo() failed: Response code ${result.code}")

        val moshi = Moshi.Builder().build()
        val exodusDataListType =
            Types.newParameterizedType(List::class.java, ExodusData::class.java)
        val adapter = moshi.adapter<List<ExodusData>>(exodusDataListType)

        return when {
            result.isSuccessful -> adapter.fromJson(result.body.string()) ?: emptyList()
            else                -> emptyList()
        }
    }
}

val exodusModule = module {
    single { RExodusAPI() }
}