package com.machiav3lli.fdroid.utils

import android.content.Context
import android.os.Build
import android.util.Log
import com.machiav3lli.fdroid.BuildConfig
import com.machiav3lli.fdroid.NeoApp
import com.machiav3lli.fdroid.data.content.Cache
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.data.database.entity.InstallTask
import com.machiav3lli.fdroid.data.entity.InstallerType
import com.machiav3lli.fdroid.data.repository.InstallsRepository
import com.machiav3lli.fdroid.manager.installer.AppInstaller
import com.machiav3lli.fdroid.manager.work.InstallWorker
import com.machiav3lli.fdroid.utils.extension.android.Android
import com.machiav3lli.fdroid.utils.extension.isInstalled
import com.machiav3lli.fdroid.utils.extension.isNSPackageUpdateOwner
import kotlinx.coroutines.delay
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.AtomicLong
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
object InstallUtils : KoinComponent {
    private const val TAG = "InstallUtils"
    private const val MIN_RESTART_INTERVAL_MS = 30_000L

    private var lastRestartAttempt = AtomicLong(0L)
    private var restartCount = AtomicInt(0)
    private val installsRepository: InstallsRepository by inject()
    private val installer: AppInstaller by inject()

    suspend fun verifyAndEnqueueInstallTask(task: InstallTask) {
        try {
            // Check if the APK file exists
            val apkFile = Cache.getReleaseFile(NeoApp.context, task.cacheFileName)
            if (!apkFile.exists() || !apkFile.canRead()
                // Fixes: Trying to re-install NS after update
                || (task.packageName == BuildConfig.APPLICATION_ID && task.versionCode.toInt() == BuildConfig.VERSION_CODE)
            ) {
                Log.w(
                    TAG,
                    "APK file missing or unreadable for ${task.packageName}: ${task.cacheFileName}"
                )
                // Delete the invalid task
                installsRepository.delete(task.packageName)
                return
            }

            InstallWorker.enqueue(
                packageName = task.packageName,
                label = task.label,
                fileName = task.cacheFileName,
                enforce = true
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error verifying install task for ${task.packageName}: ${e.message}")
        }
    }

    suspend fun restartOrphanedInstallTasks(): Boolean {
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastRestartAttempt.load() < MIN_RESTART_INTERVAL_MS) {
            Log.d(
                TAG,
                "Skipping installs restart: Called too recently, ${currentTime - lastRestartAttempt.load()}ms ago."
            )
            return false
        }

        return try {
            lastRestartAttempt.store(currentTime)
            restartCount.addAndFetch(1)

            val queueCleaned = installer.checkQueueHealth()
            if (queueCleaned) {
                Log.d(
                    TAG,
                    "Queue health check performed cleanup before restarting orphaned tasks"
                )
                delay(500)
            }

            val tasks = installsRepository.loadAll()
            Log.d(TAG, "Found ${tasks.size} install tasks to verify and re-enqueue")
            tasks.forEach { task -> verifyAndEnqueueInstallTask(task) }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for orphaned install tasks: ${e.message}")
            false
        }
    }

    fun canInstallSilently(
        context: Context,
        packageName: String,
        packageTargetSdk: Int
    ): Boolean {
        return when (Preferences[Preferences.Key.Installer].installer) {
            InstallerType.ROOT    -> shellIsRoot
            InstallerType.SYSTEM  -> context.getHasSystemInstallPermission()

            InstallerType.SHIZUKU -> context.hasShizukuOrSui && hasShizukuPermission()

            InstallerType.AM, // No API from AM for this
            InstallerType.LEGACY  -> false

            InstallerType.DEFAULT -> {
                if (
                    !context.packageManager.isInstalled(packageName) || // not installed
                    !context.packageManager.isNSPackageUpdateOwner(packageName) // updates not owned
                ) return false

                // https://developer.android.com/reference/android/content/pm/PackageInstaller.SessionParams#setRequireUserAction(int)
                Android.sdk(
                    mapOf(
                        // TODO add as soon as new SDK version is published
                        Build.VERSION_CODES.BAKLAVA to { packageTargetSdk >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE },
                        Build.VERSION_CODES.VANILLA_ICE_CREAM to { packageTargetSdk >= Build.VERSION_CODES.TIRAMISU },
                        Build.VERSION_CODES.UPSIDE_DOWN_CAKE to { packageTargetSdk >= Build.VERSION_CODES.S },
                        Build.VERSION_CODES.TIRAMISU to { packageTargetSdk >= Build.VERSION_CODES.R },
                        Build.VERSION_CODES.S to { packageTargetSdk >= Build.VERSION_CODES.Q },
                    )
                )
            }
        }
    }
}
