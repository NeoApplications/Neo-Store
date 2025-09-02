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
import com.machiav3lli.fdroid.data.repository.PrivacyRepository
import com.machiav3lli.fdroid.manager.network.RBAPI
import com.machiav3lli.fdroid.manager.network.toLogs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.android.annotation.KoinWorker
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinWorker
class RBWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params), KoinComponent {
    private val rbAPI: RBAPI by inject()
    private val privacyRepository: PrivacyRepository by inject()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        runCatching {
            fetchLogs()
        }.fold(
            onSuccess = {
                Result.success()
            },
            onFailure = {
                Log.e(this::javaClass.name, "Failed fetching exodus trackers", it)
                Result.failure(workDataOf(ARG_EXCEPTION to it.message))
            }
        )
    }

    private suspend fun fetchLogs() {
        withContext(Dispatchers.IO) {
            val logs = rbAPI.getIndex()
            privacyRepository.upsertRBLogs(logs.toLogs())
        }
    }

    companion object {
        fun fetchRBLogs() {
            NeoApp.wm.enqueueUniqueWork(
                "rb_index",
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<RBWorker>()
                    .addTag(TAG_SYNC_PERIODIC)
                    .build()
            )
        }
    }
}