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
import com.machiav3lli.fdroid.BuildConfig
import com.machiav3lli.fdroid.NeoApp
import com.machiav3lli.fdroid.data.content.Cache
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.data.database.entity.ExodusData
import com.machiav3lli.fdroid.data.database.entity.ExodusInfo
import com.machiav3lli.fdroid.data.database.entity.Tracker
import com.machiav3lli.fdroid.data.database.entity.Trackers
import com.machiav3lli.fdroid.data.repository.PrivacyRepository
import com.machiav3lli.fdroid.manager.network.Downloader
import org.koin.android.annotation.KoinWorker
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinWorker
class ExodusWorker(
    private val context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params), KoinComponent {
    private val privacyRepository: PrivacyRepository by inject()

    override suspend fun doWork(): Result {
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

        return Result.success()
    }

    private suspend fun fetchTrackers() {
        if (Preferences[Preferences.Key.ShowTrackers]) {
            runCatching {
                val url = "${EXODUS_API_BASE}/trackers"
                val tempFile = Cache.getTemporaryFile(context)
                try {
                    val result = Downloader.download(
                        url = url,
                        target = tempFile,
                        lastModified = Preferences[Preferences.Key.TrackersLastModified],
                        entityTag = "",
                        authentication = EXODUS_AUTHENTICATION,
                        callback = { _, _, _ -> }
                    )

                    when {
                        result.isNotModified -> {
                            Log.i(TAG, "Trackers not modified, skipping update")
                        }

                        result.success       -> {
                            if (!result.isNotModified) {
                                val trackerList = Trackers.fromStream(tempFile.inputStream())

                                // Update last modified timestamp
                                Preferences[Preferences.Key.TrackersLastModified] =
                                    result.lastModified

                                // Update DB with the trackers
                                privacyRepository.upsertTracker(
                                    trackerList.trackers
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
                                        }
                                )
                            } else {
                            }
                        }

                        else                 -> {
                            Log.w(TAG, "Failed to fetch trackers: ${result.statusCode}")
                        }
                    }
                } finally {
                    tempFile.delete()
                }
            }.onFailure { e ->
                Log.e(TAG, "Failed fetching exodus trackers", e)
            }
        }
    }

    private suspend fun fetchExodusData(packageName: String, versionCode: Long) {
        if (Preferences[Preferences.Key.ShowTrackers]) {
            runCatching {
                val url = "${EXODUS_API_BASE}/search/$packageName/details"
                val tempFile = Cache.getTemporaryFile(context)

                try {
                    val result = Downloader.download(
                        url = url,
                        target = tempFile,
                        lastModified = "",
                        entityTag = "",
                        authentication = EXODUS_AUTHENTICATION,
                        callback = { _, _, _ -> }
                    )

                    if (result.success) {
                        val exodusDataList = ExodusData.listFromStream(tempFile.inputStream())

                        val sourceFiltered = exodusDataList.let {
                            it.fastFilter { info -> info.source == "fdroid" }
                                .ifEmpty { it }
                        }
                        val latestExodusApp = sourceFiltered
                            .fastFilter { it.version_code.toLong() == versionCode }
                            .firstOrNull()
                            ?: sourceFiltered.maxByOrNull { it.version_code.toLong() }
                            ?: ExodusInfo()

                        val exodusInfo = latestExodusApp.toExodusInfo(packageName)
                        privacyRepository.upsertExodusInfo(exodusInfo)
                    } else {
                        Log.w(
                            TAG,
                            "Failed to fetch exodus data for $packageName: ${result.statusCode}"
                        )
                    }
                } finally {
                    tempFile.delete()
                }
            }.onFailure { e ->
                Log.e(TAG, "Failed fetching exodus info", e)
            }
        }
    }

    enum class WorkType { TRACKERS, DATA }

    companion object {
        private const val TAG = "ExodusWorker"
        private const val EXODUS_API_BASE = "https://reports.exodus-privacy.eu.org/api"
        private const val EXODUS_AUTHENTICATION = "Token ${BuildConfig.KEY_API_EXODUS}"

        fun fetchTrackers() {
            val data = workDataOf(
                ARG_WORK_TYPE to WorkType.TRACKERS.ordinal,
            )

            NeoApp.wm.enqueueUniqueWork(
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

            NeoApp.wm.enqueueUniqueWork(
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