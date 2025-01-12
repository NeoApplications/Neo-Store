package com.machiav3lli.fdroid.work

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
