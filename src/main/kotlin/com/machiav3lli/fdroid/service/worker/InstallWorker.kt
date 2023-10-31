package com.machiav3lli.fdroid.service.worker

import android.content.Context
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
import com.google.common.util.concurrent.ListenableFuture
import com.machiav3lli.fdroid.ARG_NAME
import com.machiav3lli.fdroid.ARG_PACKAGE_NAME
import com.machiav3lli.fdroid.ARG_REPOSITORY_ID
import com.machiav3lli.fdroid.MainApplication
import com.machiav3lli.fdroid.database.entity.InstallTask
import com.machiav3lli.fdroid.installer.AppInstaller
import com.machiav3lli.fdroid.installer.LegacyInstaller
import com.machiav3lli.fdroid.utility.downloadNotificationBuilder
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
        fun launch(packageName: String) {
            val downloadRequest = OneTimeWorkRequestBuilder<InstallWorker>()
                .addTag("installer_$packageName")
                .build()

            MainApplication.wm.workManager
                .beginUniqueWork(
                    "Installer",
                    ExistingWorkPolicy.APPEND,
                    downloadRequest,
                ).enqueue()
        }
    }

    private val scope = CoroutineScope(Dispatchers.Default)

    private lateinit var currentTask: InstallTask

    override suspend fun doWork(): Result {
        val lock = Mutex()
        var tasks = MainApplication.db.getInstallTaskDao().getAll()
        var currentTask: InstallTask? = null

        while (tasks.isNotEmpty()) {
            tasks.first().also { task ->
                if (task != currentTask && !lock.isLocked) {
                    currentTask = task
                    val installer = suspend { // TODO add sort of notification
                        val installerInstance = AppInstaller.getInstance(context)
                        installerInstance?.defaultInstaller?.install(
                            task.label,
                            task.cacheFileName
                        )
                        lock.unlock()
                    }
                    lock.lock()
                    if (MainApplication.mainActivity != null &&
                        AppInstaller.getInstance(context)?.defaultInstaller is LegacyInstaller
                    ) {
                        scope.launch {
                            MainApplication.mainActivity?.withResumed {
                                scope.launch {
                                    installer()
                                }
                            }
                        }
                    } else {
                        scope.launch { installer() }
                    }
                } else delay(200)
                tasks = MainApplication.db.getInstallTaskDao().getAll()
            }
        }

        return Result.success()
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            currentTask.key.hashCode(),
            stateNotificationBuilder.build()
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
        applicationContext.downloadNotificationBuilder()
    )
}
