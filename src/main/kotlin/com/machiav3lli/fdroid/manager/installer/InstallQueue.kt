package com.machiav3lli.fdroid.manager.installer

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Manages the queue of installation tasks to ensure they run sequentially
 */
class InstallQueue {
    private val mutex = Mutex()
    private val queue = ConcurrentLinkedQueue<InstallTask>()
    private val qcc = Dispatchers.IO + SupervisorJob()
    val isProcessing: StateFlow<Boolean>
        private field = MutableStateFlow(false)
    val inUserInteraction: StateFlow<String>
        private field = MutableStateFlow("")

    private var currentTask: InstallTask? = null

    data class InstallTask(
        val packageName: String,
        val packageLabel: String,
        val cacheFileName: String,
        val callback: (Result<String>) -> Unit
    )

    /**
     * Enqueues an installation task and starts processing if not already running
     */
    suspend fun enqueue(
        packageName: String,
        packageLabel: String,
        cacheFileName: String,
        callback: (Result<String>) -> Unit
    ) {
        val task = InstallTask(packageName, packageLabel, cacheFileName, callback)

        withContext(qcc) {
            queue.add(task)
            processNextIfNeeded()
        }
    }

    /**
     * Register starting user interaction for a specific package
     */
    suspend fun startUserInteraction(packageName: String) {
        withContext(qcc) {
            inUserInteraction.update { packageName }
        }
    }

    /**
     * Checks if a package is currently in the installation queue
     */
    fun isEnqueued(packageName: String): Boolean {
        return currentTask?.packageName == packageName ||
                queue.any { it.packageName == packageName }
    }

    /**
     * Checks if a package is currently in user interaction
     */
    fun isInUserInteraction(packageName: String?): Boolean {
        return inUserInteraction.value == packageName
    }

    /**
     * Cancels all pending installation tasks for a specific package
     */
    suspend fun cancel(packageName: String) {
        withContext(qcc) {
            mutex.withLock {
                queue.removeIf { it.packageName == packageName }

                // If current task is for this package, report cancellation
                if (currentTask?.packageName == packageName) {
                    currentTask?.callback?.invoke(Result.failure(InstallationError.UserCancelled()))
                    currentTask = null
                    isProcessing.update { false }
                    inUserInteraction.update { "" }
                }
            }
        }
    }

    /**
     * Processes the next task in the queue if not already processing
     */
    suspend fun processNextIfNeeded() {
        withContext(qcc) {
            mutex.withLock {
                if (isProcessing.value || queue.isEmpty()) return@withLock

                currentTask = queue.poll()
                isProcessing.update { true }
            }
        }
    }

    /**
     * Called by installers when an installation completes (success or failure)
     */
    suspend fun onInstallationComplete(result: Result<String>) {
        withContext(qcc) {
            mutex.withLock {
                try {
                    val currentPackage = currentTask?.packageName

                    result.fold(
                        onSuccess = {
                            Log.d(
                                TAG,
                                "Installation completed successfully for $currentPackage"
                            )
                        },
                        onFailure = { error ->
                            // Only retry on specific errors
                            val shouldRetry = error !is InstallationError.UserCancelled &&
                                    error !is InstallationError.ConflictingSignature &&
                                    error !is InstallationError.Downgrade &&
                                    error !is InstallationError.Incompatible

                            Log.w(
                                TAG,
                                "Installation failed for $currentPackage: ${error.message}, shouldRetry=$shouldRetry"
                            )

                            if (shouldRetry && currentTask != null) {
                                val retryTask = currentTask
                                // TODO add a retry counter
                                if (retryTask != null) {
                                    Log.d(
                                        TAG,
                                        "Re-enqueueing installation task for $currentPackage"
                                    )
                                    queue.add(retryTask)
                                }
                            }
                        }
                    )

                    currentTask?.callback?.invoke(result)
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing installation completion: ${e.message}")
                } finally {
                    currentTask = null
                    isProcessing.update { false }
                    inUserInteraction.update { "" }

                    if (queue.isEmpty()) {
                        Log.d(TAG, "No more installation tasks in queue")
                    } else {
                        Log.d(TAG, "Processing next installation task, ${queue.size} remaining")
                        currentTask = queue.poll()
                        isProcessing.update { true }
                    }
                }
            }
        }
    }

    /**
     * Returns the current task being processed, if any
     */
    suspend fun getCurrentTask(): InstallTask? {
        return withContext(qcc) {
            mutex.withLock {
                currentTask
            }
        }
    }

    /**
     * Clears all pending tasks
     */
    suspend fun clear() {
        withContext(qcc) {
            mutex.withLock {
                queue.clear()
                if (currentTask != null) {
                    currentTask?.callback?.invoke(Result.failure(InstallationError.UserCancelled()))
                    currentTask = null
                }
                isProcessing.update { false }
                inUserInteraction.update { "" }
            }
        }
    }

    companion object {
        private const val TAG = "InstallQueue"
    }
}