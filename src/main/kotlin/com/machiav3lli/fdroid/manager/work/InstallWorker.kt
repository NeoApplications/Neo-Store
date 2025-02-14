package com.machiav3lli.fdroid.manager.work

import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.lifecycle.withResumed
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.common.util.concurrent.ListenableFuture
import com.machiav3lli.fdroid.ARG_FILE_NAME
import com.machiav3lli.fdroid.ARG_NAME
import com.machiav3lli.fdroid.ARG_PACKAGE_NAME
import com.machiav3lli.fdroid.ARG_REPOSITORY_ID
import com.machiav3lli.fdroid.NeoApp
import com.machiav3lli.fdroid.data.database.entity.InstallTask
import com.machiav3lli.fdroid.data.entity.InstallState
import com.machiav3lli.fdroid.manager.installer.LegacyInstaller
import com.machiav3lli.fdroid.utils.extension.android.Android
import com.machiav3lli.fdroid.utils.installNotificationBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.cancellation.CancellationException

class InstallWorker(
    val context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    private lateinit var currentTask: InstallTask
    private val installState = MutableStateFlow<InstallState>(InstallState.Pending)
    private val installerInstance = NeoApp.installer
    private val installJob = Job()

    override suspend fun doWork(): Result = withContext(Dispatchers.Default + installJob) {
        val label = inputData.getString(ARG_NAME) ?: ""
        val fileName = inputData.getString(ARG_FILE_NAME) ?: ""

        try {
            withTimeoutOrNull(INSTALL_TIMEOUT) {
                handleInstall(label, fileName)
            } ?: run {
                installState.value = InstallState.Failed("Installation timed out")
                Result.failure()
            }
        } catch (e: CancellationException) {
            installState.value = InstallState.Failed("Installation cancelled: ${e.message}")
            Result.failure()
        }
    }

    private suspend fun handleInstall(label: String, fileName: String): Result? = coroutineScope {
        var result: Result? = null
        currentTask = NeoApp.db.getInstallTaskDao()
            .get(fileName) ?: return@coroutineScope null
        while (installState.value !is InstallState.Completed && installState.value !is InstallState.Failed) {
            when (installState.value) {
                InstallState.Pending    -> {
                    if (!installerInstance.isInstalling(currentTask.packageName)) {
                        installState.value = InstallState.Installing
                        try {
                            if (NeoApp.mainActivity != null && installerInstance is LegacyInstaller) {
                                NeoApp.mainActivity?.withResumed {
                                    launch {
                                        installerInstance.install(label, fileName) {
                                            installState.value = InstallState.Completed
                                            result = Result.success()
                                        }
                                    }
                                }
                            } else {
                                installerInstance.install(label, fileName) {
                                    installState.value = InstallState.Completed
                                    result = Result.success()
                                }
                            }
                        } catch (e: Exception) {
                            installState.value =
                                InstallState.Failed(e.message ?: "Unknown error")
                            result = Result.failure()
                        }
                    }
                }

                InstallState.Installing -> {
                    // Wait for installation to complete
                    delay(2000)
                }

                InstallState.Completed  -> {
                    result = Result.success()
                    break
                }

                is InstallState.Failed  -> {
                    result = Result.failure()
                    break
                }
            }
        }
        result
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val notificationBuilder = applicationContext.installNotificationBuilder()
        return ForegroundInfo(
            currentTask.key.hashCode(),
            notificationBuilder.build(),
            if (Android.sdk(Build.VERSION_CODES.Q)) ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            else 0
        )
    }

    override fun setProgressAsync(data: Data): ListenableFuture<Void> {
        return super.setProgressAsync(
            Data.Builder()
                .putAll(data)
                .putString(ARG_PACKAGE_NAME, currentTask.packageName)
                .putString(ARG_NAME, currentTask.label)
                .putLong(ARG_REPOSITORY_ID, currentTask.repositoryId)
                .build()
        )
    }

    companion object {
        private const val INSTALL_TIMEOUT = 10 * 60 * 1000L // 10 minutes
        private const val INITIAL_BACKOFF_MILLIS = 1000L

        fun enqueue(
            packageName: String,
            label: String,
            fileName: String,
            enforce: Boolean = false,
        ) {
            val data = workDataOf(
                ARG_NAME to label,
                ARG_FILE_NAME to fileName,
            )

            val installerRequest = OneTimeWorkRequestBuilder<InstallWorker>()
                .setInputData(data)
                /*.setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    INITIAL_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )*/
                .addTag("installer")
                .build()

            NeoApp.wm.workManager
                .enqueueUniqueWork(
                    "Installer_$packageName",
                    if (enforce) ExistingWorkPolicy.REPLACE
                    else ExistingWorkPolicy.KEEP,
                    installerRequest,
                )
        }
    }
}
