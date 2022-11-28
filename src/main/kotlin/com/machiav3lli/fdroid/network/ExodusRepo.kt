package com.machiav3lli.fdroid.network

import android.util.Log
import com.machiav3lli.fdroid.database.entity.ExodusData
import com.machiav3lli.fdroid.database.entity.Trackers
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

class RExodusAPI @Inject constructor(
    private val exodusAPIInterface: IExodusAPI
) {

    suspend fun getTrackers(): Trackers {
        val result = exodusAPIInterface.getTrackers()
        if (!result.isSuccessful)
            Log.w(this::javaClass.name, "getTrackers() failed: Response code  ${result.code()}")
        return when {
            result.isSuccessful -> result.body() ?: Trackers()
            else -> Trackers()
        }
    }

    suspend fun getExodusInfo(packageName: String): List<ExodusData> {
        val result = exodusAPIInterface.getExodusData(packageName)
        if (!result.isSuccessful)
            Log.w(this::javaClass.name, "getExodusInfo() failed: Response code ${result.code()}")
        return when {
            result.isSuccessful -> result.body() ?: emptyList()
            else -> emptyList()
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
object ExodusModule {

    @Singleton
    @Provides
    fun provideExodusAPIInstance(okHttpClient: OkHttpClient): IExodusAPI {
        return Retrofit.Builder()
            .baseUrl(IExodusAPI.URL_BASE)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(IExodusAPI::class.java)
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(interceptor: Interceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()
    }

    @Singleton
    @Provides
    fun provideInterceptor(): Interceptor {
        return Interceptor { chain ->
            val builder = chain.request().newBuilder().apply {
                header(
                    "Authorization",
                    "Token 81f30e4903bde25023857719e71c94829a41e6a5"
                ) // TODO hide it as a SECRET
            }
            return@Interceptor chain.proceed(builder.build())
        }
    }
}