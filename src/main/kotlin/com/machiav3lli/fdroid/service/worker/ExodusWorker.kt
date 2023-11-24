package com.machiav3lli.fdroid.service.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.machiav3lli.fdroid.ARG_PACKAGE_NAME
import com.machiav3lli.fdroid.ARG_WORK_TYPE
import com.machiav3lli.fdroid.MainApplication
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.database.entity.ExodusInfo
import com.machiav3lli.fdroid.database.entity.Tracker
import com.machiav3lli.fdroid.network.RExodusAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinWorker
import org.koin.core.component.KoinComponent

@KoinWorker
class ExodusWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params), KoinComponent {

    val scope = CoroutineScope(Dispatchers.Default)
    private val repoExodusAPI: RExodusAPI by getKoin().inject()

    enum class WorkType { TRACKERS, DATA }

    companion object {

        fun fetchTrackers() {
            val data = workDataOf(
                ARG_WORK_TYPE to WorkType.TRACKERS.ordinal,
            )

            MainApplication.wm.workManager.enqueueUniqueWork(
                WorkType.TRACKERS.name,
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<ExodusWorker>()
                    .setInputData(data)
                    .addTag(WorkType.TRACKERS.toString())
                    .build()
            )
        }

        fun fetchExodusInfo(packageName: String) {
            val data = workDataOf(
                ARG_WORK_TYPE to WorkType.DATA.ordinal,
                ARG_PACKAGE_NAME to packageName
            )

            MainApplication.wm.workManager.enqueueUniqueWork(
                WorkType.TRACKERS.name,
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<ExodusWorker>()
                    .setInputData(data)
                    .addTag(WorkType.TRACKERS.toString())
                    .build()
            )
        }
    }

    override suspend fun doWork(): Result {
        val type = WorkType.values()[
            inputData.getInt(ARG_WORK_TYPE, 0)
        ]

        when (type) {
            WorkType.TRACKERS -> fetchTrackers()
            WorkType.DATA     -> fetchExodusData(inputData.getString(ARG_PACKAGE_NAME)!!)
        }

        return Result.success()
    }


    private fun fetchTrackers() {
        if (Preferences[Preferences.Key.ShowTrackers]) {
            scope.launch {
                try {
                    val trackerList = repoExodusAPI.getTrackers()
                    // TODO **conditionally** update DB with the trackers
                    MainApplication.db.getTrackerDao().upsert(
                        *trackerList.trackers
                            .map { (key, value) ->
                                Tracker(
                                    key.toInt(),
                                    value.name,
                                    value.network_signature,
                                    value.code_signature,
                                    value.creation_date,
                                    value.website,
                                    value.description,
                                    value.categories
                                )
                            }.toTypedArray()
                    )
                } catch (e: Exception) {
                    Log.e(this::javaClass.name, "Failed fetching exodus trackers", e)
                }
            }
        }
    }

    private fun fetchExodusData(packageName: String) {
        if (Preferences[Preferences.Key.ShowTrackers]) {
            scope.launch {
                try {
                    val exodusDataList = repoExodusAPI.getExodusInfo(packageName)
                    val latestExodusApp = exodusDataList.maxByOrNull { it.version_code.toLong() }
                        ?: ExodusInfo()

                    val exodusInfo = latestExodusApp.toExodusInfo(packageName)
                    Log.e(this::javaClass.name, exodusInfo.toString())
                    MainApplication.db.getExodusInfoDao().upsert(exodusInfo)
                } catch (e: Exception) {
                    Log.e(this::javaClass.name, "Failed fetching exodus info", e)
                }
            }
        }
    }
}