package com.machiav3lli.fdroid.data.entity

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.ConcurrentHashMap

class StateHolderFlow<T> {
    private val map = ConcurrentHashMap<String, T>()
    val flow: StateFlow<Map<String, T>>
        private field = MutableStateFlow<Map<String, T>>(emptyMap())

    fun updateState(key: String, state: T?) {
        if (state == null) {
            map.remove(key)
        } else {
            Log.d(this::class.java.simpleName, "Updating $key state to ${state::class.simpleName}")
            map[key] = state
        }

        flow.update { map.toMap() }
    }

    fun getState(packageName: String): T? {
        return map[packageName]
    }

    fun isHeld(packageName: String): Boolean {
        return map.contains(packageName)
    }
}