package com.machiav3lli.fdroid.manager.installer

import kotlinx.coroutines.Dispatchers
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

        withContext(Dispatchers.IO) {
            queue.add(task)
            processNextIfNeeded()
        }
    }

    /**
     * Register starting user interaction for a specific package
     */
    suspend fun startUserInteraction(packageName: String) {
        withContext(Dispatchers.IO) {
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
        withContext(Dispatchers.IO) {
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
        withContext(Dispatchers.IO) {
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
        withContext(Dispatchers.IO) {
            mutex.withLock {
                currentTask?.callback?.invoke(result)
                currentTask = null
                isProcessing.update { false }
                inUserInteraction.update { "" }
                if (queue.isNotEmpty()) {
                    if (isProcessing.value || queue.isEmpty()) return@withLock

                    currentTask = queue.poll()
                    isProcessing.update { true }
                }
            }
        }
    }

    /**
     * Returns the current task being processed, if any
     */
    suspend fun getCurrentTask(): InstallTask? {
        return withContext(Dispatchers.IO) {
            mutex.withLock {
                currentTask
            }
        }
    }

    /**
     * Clears all pending tasks
     */
    suspend fun clear() {
        withContext(Dispatchers.IO) {
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
}