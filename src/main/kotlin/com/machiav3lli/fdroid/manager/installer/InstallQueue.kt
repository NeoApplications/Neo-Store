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
    private var processingStartTime: Long = 0
    val isProcessing: StateFlow<Boolean>
        private field = MutableStateFlow(false)
    val inUserInteraction: StateFlow<String>
        private field = MutableStateFlow("")

    private var currentTask: InstallTask? = null

    data class InstallTask(
        val packageName: String,
        val packageLabel: String,
        val cacheFileName: String,
        val retryCount: Int = 0,
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
        val task = InstallTask(packageName, packageLabel, cacheFileName, 0, callback)

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
        return (currentTask?.packageName == packageName ||
                queue.any { it.packageName == packageName }).apply {
            // TODO remove when more queueing logic is in place
            if (this) Log.d(
                "InstallQueue",
                "$packageName is ${if (currentTask?.packageName == packageName) "the current task" else "already in the queue: ${queue.toArray()}"}"
            )
        }
    }

    /**
     * Checks the health of the installation queue and cleans up any stale tasks
     */
    suspend fun checkQueueHealth(): Boolean {
        return withContext(qcc) {
            mutex.withLock {
                try {
                    var cleanupPerformed = false

                    // If there's a current task but processing is false, we have an inconsistency
                    if (currentTask != null && !isProcessing.value) {
                        Log.w(
                            TAG,
                            "Queue health check: Inconsistent state detected for ${currentTask?.packageName}, cleaning up"
                        )
                        currentTask?.callback?.invoke(Result.failure(InstallationError.Unknown("Queue cleanup: inconsistent state")))
                        currentTask = null
                        inUserInteraction.update { "" }
                        cleanupPerformed = true
                    }

                    // Check for stuck processing based on time
                    if (isProcessing.value && currentTask != null && processingStartTime > 0) {
                        val processingTime = System.currentTimeMillis() - processingStartTime
                        if (processingTime > MAX_PROCESSING_TIME) {
                            Log.w(
                                TAG,
                                "Queue health check: Task ${currentTask?.packageName} has been processing for ${processingTime}ms, forcing cleanup"
                            )
                            currentTask?.callback?.invoke(Result.failure(InstallationError.Unknown("Installation timeout")))
                            currentTask = null
                            isProcessing.update { false }
                            inUserInteraction.update { "" }
                            processingStartTime = 0
                            cleanupPerformed = true
                        }
                    }

                    // Check for empty queue but still processing
                    if (isProcessing.value && currentTask == null && queue.isEmpty()) {
                        Log.w(
                            TAG,
                            "Queue health check: Processing flag set but no tasks, resetting"
                        )
                        isProcessing.update { false }
                        inUserInteraction.update { "" }
                        cleanupPerformed = true
                    }

                    Log.d(
                        TAG,
                        "Queue health check completed. Queue size: ${queue.size}, Processing: ${isProcessing.value}, Current task: ${currentTask?.packageName}"
                    )
                    cleanupPerformed
                } catch (e: Exception) {
                    Log.e(TAG, "Error checking queue health: ${e.message}")
                    false
                }
            }
        }
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
                                if (retryTask != null && retryTask.retryCount < MAX_RETRIES) { // Max 3 retries
                                    Log.d(
                                        TAG,
                                        "Re-enqueueing installation task for $currentPackage"
                                    )
                                    queue.add(retryTask.copy(retryCount = retryTask.retryCount + 1))
                                } else {
                                    Log.w(
                                        TAG,
                                        "Max retries reached for $currentPackage, not re-enqueueing"
                                    )
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
        private const val MAX_RETRIES = 3
        private const val MAX_PROCESSING_TIME = 5 * 60 * 1000L // 5 minutes
    }
}