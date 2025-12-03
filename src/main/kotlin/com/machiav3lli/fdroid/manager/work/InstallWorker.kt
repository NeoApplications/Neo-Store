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
import com.machiav3lli.fdroid.data.repository.InstallsRepository
import com.machiav3lli.fdroid.manager.installer.AppInstaller
import com.machiav3lli.fdroid.manager.installer.InstallationError
import com.machiav3lli.fdroid.utils.extension.android.Android
import com.machiav3lli.fdroid.utils.installNotificationBuilder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
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
    private val installer: AppInstaller by inject()
    private val installsRepository: InstallsRepository by inject()
    private val langContext = ContextWrapperX.wrap(applicationContext)

    @Deprecated("")
    override val coroutineContext: CoroutineDispatcher
        get() = Dispatchers.IO

    override suspend fun doWork(): Result {
        val label = inputData.getString(ARG_NAME) ?: ""
        val fileName = inputData.getString(ARG_FILE_NAME) ?: ""
        val packageName = inputData.getString(ARG_PACKAGE_NAME) ?: ""
        val maxRetries = 3
        var attemptCount = 0

        Log.d(TAG, "Starting installation task for $packageName ($fileName)")

        while (attemptCount < maxRetries) {
            try {
                attemptCount++
                Log.d(TAG, "Installation attempt $attemptCount for $packageName")
                // Make sure the task is not queued causing a loop
                installer.cancelInstall(packageName)

                val result = withTimeout(INSTALL_TIMEOUT) {
                    handleInstall(label, fileName)
                }

                if (result == Result.success()) {
                    Log.d(
                        TAG,
                        "Installation successful for $packageName after $attemptCount attempts"
                    )
                    return result
                }

                if (attemptCount < maxRetries) {
                    (INITIAL_BACKOFF_MILLIS * (1 shl (attemptCount - 1))).let { delayTime ->
                        Log.w(TAG, "Retrying installation of $packageName in ${delayTime}ms")
                        delay(delayTime)
                    }
                }
            } catch (e: TimeoutCancellationException) {
                Log.w(
                    TAG,
                    "Installation timed out for $packageName (attempt $attemptCount): ${e.message}"
                )
                installer.cancelInstall(packageName)
                if (attemptCount >= maxRetries) return Result.failure()
                delay(INITIAL_BACKOFF_MILLIS * (1 shl (attemptCount - 1)))
            } catch (e: CancellationException) {
                Log.w(TAG, "Installation cancelled for $packageName: ${e.message}")
                installer.cancelInstall(packageName)
                return Result.failure()
            } catch (e: Exception) {
                Log.e(
                    TAG,
                    "Installation failed for $packageName (attempt $attemptCount): ${e.message}",
                    e
                )
                installer.cancelInstall(packageName)
                if (attemptCount >= maxRetries) return Result.failure()
                delay(INITIAL_BACKOFF_MILLIS * (1 shl (attemptCount - 1)))
            }
        }

        Log.e(TAG, "Installation failed for $packageName after $maxRetries attempts")
        return Result.failure()
    }

    private suspend fun handleInstall(label: String, fileName: String): Result = coroutineScope {
        var installResult: Result = Result.failure()
        var installLaunched = false

        try {
            var attemptsCount = 0
            val maxRetries = 3
            currentTask = installsRepository.load(fileName) ?: run {
                Log.e(TAG, "No install task found for $fileName")
                return@coroutineScope Result.failure()
            }

            val queueCleaned = installer.checkQueueHealth()
            if (queueCleaned) {
                Log.d(
                    TAG,
                    "Queue cleanup performed before starting installation of ${currentTask.packageName}"
                )
                delay(500)
            }

            var waitingForQueueTime = 0L

            while (installState.value !is InstallState.Success && installState.value !is InstallState.Failed && attemptsCount < maxRetries) {
                when (installState.value) {
                    is InstallState.Preparing -> {
                        if (!installer.isEnqueued(currentTask.packageName)) {
                            waitingForQueueTime = 0
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
                                installLaunched = true
                                if (installer.isLegacy() && NeoApp.mainActivity != null) {
                                    NeoApp.mainActivity?.withResumed {
                                        launch {
                                            installer.install(label, fileName, installCallback)
                                        }
                                    }
                                } else {
                                    installer.install(label, fileName, installCallback)
                                }
                            } catch (e: Exception) {
                                Log.e(
                                    TAG,
                                    "Error launching installer for ${currentTask.packageName}: ${e.message}"
                                )
                                installState.value = InstallState.Failed(e)
                                installResult = Result.failure()
                                attemptsCount++
                                delay(1000) // Brief delay before potential retry
                            }
                        } else {
                            Log.d(
                                TAG,
                                "${currentTask.packageName} is already enqueued, waiting..."
                            )
                            delay(1000)
                            waitingForQueueTime += 1000

                            if (waitingForQueueTime >= PREPARATION_TIMEOUT) {
                                Log.w(
                                    TAG,
                                    "Queue wait timeout for ${currentTask.packageName}, forcing cleanup"
                                )
                                installer.cancelInstall(currentTask.packageName)
                                delay(1000) // Give time for cleanup
                                attemptsCount++
                                waitingForQueueTime = 0
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

        } catch (e: Exception) {
            Log.e(
                TAG,
                "Install failed while in handleInstall for ${currentTask.packageName}: ${e.message}",
                e
            )
            installResult = Result.failure()
        } finally {
            // Clean up the installation task
            runCatching {
                if (this@InstallWorker::currentTask.isInitialized) {
                    Log.d(TAG, "Cleaning up install task for ${currentTask.packageName}")
                    if (installResult == Result.success() || installLaunched) {
                        installsRepository.delete(currentTask.packageName)
                    }
                }
            }.onFailure { e ->
                Log.e(TAG, "Error during cleanup: ${e.message}", e)
            }
        }
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
        private const val PREPARATION_TIMEOUT = 60 * 1000L // 1 minute
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
            Log.d(
                TAG,
                "Enqueued installation task for $packageName ($fileName) with enforce=$enforce"
            )
        }
    }
}
