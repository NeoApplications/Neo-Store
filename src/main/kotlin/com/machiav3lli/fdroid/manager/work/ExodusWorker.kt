package com.machiav3lli.fdroid.manager.work

import android.content.Context
import android.util.Log
import androidx.compose.ui.util.fastFilter
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.machiav3lli.fdroid.ARG_PACKAGE_NAME
import com.machiav3lli.fdroid.ARG_VERSION_CODE
import com.machiav3lli.fdroid.ARG_WORK_TYPE
import com.machiav3lli.fdroid.NeoApp
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.data.database.entity.ExodusInfo
import com.machiav3lli.fdroid.data.database.entity.Tracker
import com.machiav3lli.fdroid.manager.network.RExodusAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.android.annotation.KoinWorker
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinWorker
class ExodusWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params), KoinComponent {
    private val repoExodusAPI: RExodusAPI by inject()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val type = WorkType.entries[
            inputData.getInt(ARG_WORK_TYPE, 0)
        ]

        when (type) {
            WorkType.TRACKERS -> fetchTrackers()
            WorkType.DATA     -> fetchExodusData(
                inputData.getString(ARG_PACKAGE_NAME)!!,
                inputData.getLong(ARG_VERSION_CODE, -1)
            )
        }

        Result.success()
    }

    private suspend fun fetchTrackers() {
        if (Preferences[Preferences.Key.ShowTrackers]) {
            withContext(Dispatchers.IO) {
                try {
                    val trackerList = repoExodusAPI.getTrackers()
                    // TODO **conditionally** update DB with the trackers
                    NeoApp.db.getTrackerDao().upsert(
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

    private suspend fun fetchExodusData(packageName: String, versionCode: Long) {
        if (Preferences[Preferences.Key.ShowTrackers]) {
            withContext(Dispatchers.IO) {
                try {
                    val sourceFiltered = repoExodusAPI.getExodusInfo(packageName).let {
                        it.fastFilter { info -> info.source == "fdroid" }
                            .ifEmpty { it }
                    }
                    val latestExodusApp = sourceFiltered
                        .fastFilter { it.version_code.toLong() == versionCode }
                        .firstOrNull()
                        ?: sourceFiltered.maxByOrNull { it.version_code.toLong() }
                        ?: ExodusInfo()

                    val exodusInfo = latestExodusApp.toExodusInfo(packageName)
                    NeoApp.db.getExodusInfoDao().upsert(exodusInfo)
                } catch (e: Exception) {
                    Log.e(this::javaClass.name, "Failed fetching exodus info", e)
                }
            }
        }
    }

    enum class WorkType { TRACKERS, DATA }

    companion object {
        fun fetchTrackers() {
            val data = workDataOf(
                ARG_WORK_TYPE to WorkType.TRACKERS.ordinal,
            )

            NeoApp.wm.workManager.enqueueUniqueWork(
                WorkType.TRACKERS.name,
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<ExodusWorker>()
                    .setInputData(data)
                    .addTag(WorkType.TRACKERS.toString())
                    .build()
            )
        }

        fun fetchExodusInfo(packageName: String, versionCode: Long) {
            val data = workDataOf(
                ARG_WORK_TYPE to WorkType.DATA.ordinal,
                ARG_PACKAGE_NAME to packageName,
                ARG_VERSION_CODE to versionCode,
            )

            NeoApp.wm.workManager.enqueueUniqueWork(
                WorkType.TRACKERS.name,
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<ExodusWorker>()
                    .setInputData(data)
                    .addTag(WorkType.TRACKERS.toString())
                    .build()
            )
        }
    }
}