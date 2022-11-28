package com.machiav3lli.fdroid.network

import com.machiav3lli.fdroid.database.entity.ExodusData
import com.machiav3lli.fdroid.database.entity.Trackers
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface IExodusAPI {

    @GET("trackers")
    suspend fun getTrackers(): Response<Trackers>

    @GET("search/{packageName}/details")
    suspend fun getExodusData(
        @Path("packageName") packageName: String
    ): Response<List<ExodusData>>

    companion object {
        const val URL_BASE = "https://reports.exodus-privacy.eu.org/api/"
    }
}