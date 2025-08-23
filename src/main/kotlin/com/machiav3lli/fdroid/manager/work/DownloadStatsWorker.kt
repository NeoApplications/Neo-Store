package com.machiav3lli.fdroid.manager.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.machiav3lli.fdroid.ARG_EXCEPTION
import com.machiav3lli.fdroid.NeoApp
import com.machiav3lli.fdroid.TAG_SYNC_PERIODIC
import com.machiav3lli.fdroid.data.database.entity.toDownloadStats
import com.machiav3lli.fdroid.data.repository.PrivacyRepository
import com.machiav3lli.fdroid.manager.network.DownloadStatsAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.android.annotation.KoinWorker
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinWorker
class DownloadStatsWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params), KoinComponent {
    private val dsAPI: DownloadStatsAPI by inject()
    private val privacyRepository: PrivacyRepository by inject()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        runCatching {
            fetchData()
        }.fold(
            onSuccess = {
                Result.success()
            },
            onFailure = {
                Log.e(this::javaClass.name, "Failed fetching download stats", it)
                Result.failure(workDataOf(ARG_EXCEPTION to it.message))
            }
        )
    }

    private suspend fun fetchData() {
        withContext(Dispatchers.IO) {
            val data = dsAPI.getIndex()
            privacyRepository.upsertDownloadStats(*data.toDownloadStats().toTypedArray())
        }
    }

    companion object {
        fun fetchDownloadStats() {
            NeoApp.wm.enqueueUniqueWork(
                "download_stats",
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<DownloadStatsWorker>()
                    .addTag(TAG_SYNC_PERIODIC)
                    .build()
            )
        }
    }
}