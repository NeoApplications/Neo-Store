package com.machiav3lli.fdroid.service.worker

import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import com.machiav3lli.fdroid.MainApplication
import com.machiav3lli.fdroid.database.entity.InstallTask
import com.machiav3lli.fdroid.installer.AppInstaller
import com.machiav3lli.fdroid.installer.LegacyInstaller
import com.machiav3lli.fdroid.utility.extension.android.Android
import com.machiav3lli.fdroid.utility.installNotificationBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex

class InstallWorker(
    val context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    companion object {
        fun enqueue(packageName: String, label: String, fileName: String) {
            val data = workDataOf(
                ARG_NAME to label,
                ARG_FILE_NAME to fileName,
            )

            val installerRequest = OneTimeWorkRequestBuilder<InstallWorker>()
                .setInputData(data)
                .addTag("installer")
                .build()

            MainApplication.wm.workManager
                .beginUniqueWork(
                    "Installer_$packageName",
                    ExistingWorkPolicy.KEEP,
                    installerRequest,
                ).enqueue()
        }
    }

    private val scope = CoroutineScope(Dispatchers.Default)

    private lateinit var currentTask: InstallTask

    override suspend fun doWork(): Result {
        val lock = Mutex()
        val label = inputData.getString(ARG_NAME) ?: ""
        val fileName = inputData.getString(ARG_FILE_NAME) ?: ""
        var task = MainApplication.db.getInstallTaskDao().get(fileName)
        val installerInstance = AppInstaller.getInstance(context)

        try {
            while (task != null) {
                if (!lock.isLocked && installerInstance?.defaultInstaller?.isInstalling(task.packageName) != true) {
                    currentTask = task
                    val installer = suspend { // TODO add sort of notification
                        installerInstance?.defaultInstaller?.install(
                            label,
                            fileName
                        )
                        lock.unlock()
                    }

                    lock.lock()
                    try {
                        if (MainApplication.mainActivity != null && installerInstance?.defaultInstaller is LegacyInstaller) {
                            MainApplication.mainActivity?.withResumed {
                                scope.launch { installer() }
                            }
                        } else {
                            scope.launch { installer() }
                        }
                    } catch (e: Exception) {
                        return Result.failure()
                    }
                } else delay(5000)
                task = MainApplication.db.getInstallTaskDao().get(fileName)
            }
        } catch (e: Exception) {
            return Result.failure()
        }
        return Result.success()
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            currentTask.key.hashCode(),
            stateNotificationBuilder.build(),
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

    private var stateNotificationBuilder by mutableStateOf(
        applicationContext.installNotificationBuilder()
    )
}
