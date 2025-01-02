package com.machiav3lli.fdroid.work

import androidx.work.Data
import androidx.work.WorkInfo
import com.machiav3lli.fdroid.service.worker.DownloadWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap

class WorkStateHolder<T> {
    private val stateMap = ConcurrentHashMap<String, T>()
    private val stateFlow = MutableStateFlow<Map<String, T>>(emptyMap())

    fun updateState(key: String, state: T?) {
        if (state == null) {
            stateMap.remove(key)
        } else {
            stateMap[key] = state
        }
        stateFlow.value = stateMap.toMap()
    }

    fun getState(key: String): T? = stateMap[key]
    fun getAllStates(): Map<String, T> = stateMap.toMap()
    fun observeStates() = stateFlow.asStateFlow()
}

/**
 * @return if this is a new state we haven't processed
 */
class WorkTracker {
    private val activeWorks =
        ConcurrentHashMap<String, Pair<WorkInfo.State, DownloadWorker.Progress>>()

    fun trackWork(workInfo: WorkInfo, data: Data): Boolean {
        val previousState = activeWorks[workInfo.id.toString()]?.first
        val previousProgress = activeWorks[workInfo.id.toString()]?.second
        val currentState = workInfo.state
        val currentProgress = DownloadWorker.getProgress(data)

        activeWorks[workInfo.id.toString()] = Pair(currentState, currentProgress)

        if (currentState.isFinished) {
            activeWorks.remove(workInfo.id.toString())
        }

        return previousState != currentState ||
                (currentState == WorkInfo.State.RUNNING && previousProgress != currentProgress)
    }
}