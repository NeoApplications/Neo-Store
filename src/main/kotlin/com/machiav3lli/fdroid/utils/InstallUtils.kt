package com.machiav3lli.fdroid.utils

import android.util.Log
import com.machiav3lli.fdroid.NeoApp
import com.machiav3lli.fdroid.data.content.Cache
import com.machiav3lli.fdroid.data.database.entity.InstallTask
import com.machiav3lli.fdroid.manager.work.InstallWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.AtomicLong
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
object InstallUtils {
    private const val TAG = "InstallUtils"
    private const val MIN_RESTART_INTERVAL_MS = 30_000L

    private var lastRestartAttempt = AtomicLong(0L)
    private var restartCount = AtomicInt(0)

    suspend fun verifyAndEnqueueInstallTask(task: InstallTask) {
        withContext(Dispatchers.IO) {
            try {
                // Check if the APK file exists
                val apkFile = Cache.getReleaseFile(NeoApp.context, task.cacheFileName)
                if (!apkFile.exists() || !apkFile.canRead()) {
                    Log.w(
                        TAG,
                        "APK file missing or unreadable for ${task.packageName}: ${task.cacheFileName}"
                    )
                    // Delete the invalid task
                    NeoApp.db.getInstallTaskDao().delete(task.packageName)
                    return@withContext
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

        return withContext(Dispatchers.IO) {
            try {
                lastRestartAttempt.store(currentTime)
                restartCount.addAndFetch(1)

                val tasks = NeoApp.db.getInstallTaskDao().getAll()
                Log.d(TAG, "Found ${tasks.size} install tasks to verify and re-enqueue")
                tasks.forEach { task -> verifyAndEnqueueInstallTask(task) }
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error checking for orphaned install tasks: ${e.message}")
                false
            }
        }
    }
}
