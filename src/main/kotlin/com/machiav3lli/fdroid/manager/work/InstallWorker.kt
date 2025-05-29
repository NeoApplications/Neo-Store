package com.machiav3lli.fdroid.manager.work

import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
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
import com.machiav3lli.fdroid.ContextWrapperX
import com.machiav3lli.fdroid.NeoApp
import com.machiav3lli.fdroid.data.database.entity.InstallTask
import com.machiav3lli.fdroid.data.entity.InstallState
import com.machiav3lli.fdroid.manager.installer.BaseInstaller
import com.machiav3lli.fdroid.manager.installer.InstallationError
import com.machiav3lli.fdroid.manager.installer.LegacyInstaller
import com.machiav3lli.fdroid.utils.extension.android.Android
import com.machiav3lli.fdroid.utils.installNotificationBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.coroutines.cancellation.CancellationException

class InstallWorker(
    val context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params), KoinComponent {
    private lateinit var currentTask: InstallTask
    private val installState = MutableStateFlow<InstallState>(InstallState.Preparing)
    private val installer: BaseInstaller by inject()
    private val installJob = Job()
    private val langContext = ContextWrapperX.wrap(applicationContext)

    override suspend fun doWork(): Result = withContext(Dispatchers.IO + installJob) {
        val label = inputData.getString(ARG_NAME) ?: ""
        val fileName = inputData.getString(ARG_FILE_NAME) ?: ""
        val packageName = inputData.getString(ARG_PACKAGE_NAME) ?: ""

        Log.d(TAG, "Starting installation task for $packageName ($fileName)")

        try {
            withTimeout(INSTALL_TIMEOUT) {
                handleInstall(label, fileName)
            }
        } catch (e: TimeoutCancellationException) {
            Log.w(TAG, "Installation timed out for $packageName: ${e.message}")
            Result.failure()
        } catch (e: CancellationException) {
            Log.w(TAG, "Installation cancelled for $packageName: ${e.message}")
            Result.failure()
        } catch (e: Exception) {
            Log.e(TAG, "Installation failed for $packageName: ${e.message}", e)
            Result.failure()
        }
    }

    private suspend fun handleInstall(label: String, fileName: String): Result = coroutineScope {
        var installResult: Result = Result.failure()
        currentTask = NeoApp.db.getInstallTaskDao().get(fileName) ?: run {
            Log.e(TAG, "No install task found for $fileName")
            return@coroutineScope Result.failure()
        }

        while (installState.value !is InstallState.Success && installState.value !is InstallState.Failed) {
            when (installState.value) {
                is InstallState.Preparing -> {
                    if (!installer.isEnqueued(currentTask.packageName)) {
                        installState.value = InstallState.Installing(0.05f)

                        val installCallback = { result: kotlin.Result<String> ->
                            handleInstallResult(result, currentTask.packageName)
                            installState.value = result.fold(
                                onSuccess = { InstallState.Success },
                                onFailure = { InstallState.Failed(it) },
                            )
                            installResult =
                                if (result.isSuccess) Result.success() else Result.failure()
                        }
                        try {
                            if (installer is LegacyInstaller && NeoApp.mainActivity != null) {
                                NeoApp.mainActivity?.withResumed {
                                    launch {
                                        installer.install(label, fileName, installCallback)
                                    }
                                }
                            } else {
                                installer.install(label, fileName, installCallback)
                            }
                        } catch (e: Exception) {
                            installState.value = InstallState.Failed(e)
                            installResult = Result.failure()
                        }
                    }
                }

                is InstallState.Pending,
                is InstallState.Installing,
                                          -> {
                    // Wait for installation to complete
                    delay(2000)
                }

                is InstallState.Success   -> {
                    installResult = Result.success()
                    break
                }

                is InstallState.Failed    -> {
                    installResult = Result.failure()
                    break
                }
            }
        }
        // Clean up the installation task
        NeoApp.db.getInstallTaskDao().delete(currentTask.packageName)
        installResult
    }

    private fun handleInstallResult(result: kotlin.Result<String>, packageName: String) {
        result.fold(
            onSuccess = {
                Log.d(TAG, "Successfully installed $packageName")
                // Additional successful install handling if needed
            },
            onFailure = { throwable ->
                when (throwable) {
                    is InstallationError.InsufficientStorage -> {
                        Log.e(TAG, "Insufficient storage for installing $packageName")
                    }

                    is InstallationError.Downgrade -> {
                        Log.e(TAG, "Attempted downgrade of $packageName")
                    }

                    is InstallationError.ConflictingSignature -> {
                        Log.e(TAG, "Conflicting signature for $packageName")
                    }

                    is InstallationError.RootAccessDenied -> {
                        Log.e(TAG, "Root access denied when installing $packageName")
                    }

                    is InstallationError.UserCancelled -> {
                        Log.w(TAG, "User cancelled installation of $packageName")
                    }

                    else -> {
                        Log.e(TAG, "Error installing $packageName: ${throwable.message}")
                    }
                }
            }
        )
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val notificationBuilder = langContext.installNotificationBuilder()
        return ForegroundInfo(
            currentTask.key.hashCode(),
            notificationBuilder.build(),
            if (Android.sdk(Build.VERSION_CODES.Q)) ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            else 0
        )
    }

    // TODO replace with setProgressData and make use of it
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
        private const val TAG = "InstallWorker"
        private const val INSTALL_TIMEOUT = 10 * 60 * 1000L // 10 minutes
        private const val INITIAL_BACKOFF_MILLIS = 1000L

        /**
         * Enqueues an installation task to be executed by the WorkManager
         */
        fun enqueue(
            packageName: String,
            label: String,
            fileName: String,
            enforce: Boolean = false,
        ) {
            val data = workDataOf(
                ARG_NAME to label,
                ARG_FILE_NAME to fileName,
                ARG_PACKAGE_NAME to packageName,
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

            NeoApp.wm.enqueueUniqueWork(
                "Installer_$packageName",
                if (enforce) ExistingWorkPolicy.REPLACE
                else ExistingWorkPolicy.KEEP,
                installerRequest,
            )
            Log.d(TAG, "Enqueued installation task for $packageName ($fileName)")
        }
    }
}
