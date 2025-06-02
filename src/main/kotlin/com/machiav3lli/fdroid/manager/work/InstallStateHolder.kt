package com.machiav3lli.fdroid.manager.work

import android.util.Log
import com.machiav3lli.fdroid.data.entity.InstallState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.ConcurrentHashMap

class InstallStateHolder() {
    private val _installStates = ConcurrentHashMap<String, InstallState>()
    val statesFlow: StateFlow<Map<String, InstallState>>
        private field = MutableStateFlow<Map<String, InstallState>>(emptyMap())

    fun updateState(packageName: String, state: InstallState?) {
        if (state == null) {
            _installStates.remove(packageName)
        } else {
            Log.d(TAG, "Updating install state for $packageName to ${state::class.simpleName}")
            _installStates[packageName] = state
        }

        statesFlow.update { _installStates.toMap() }
    }

    fun getState(packageName: String): InstallState? {
        return _installStates[packageName]
    }

    fun isInstalling(packageName: String): Boolean {
        val state = _installStates[packageName]
        return state != null && state.isActive()
    }

    companion object {
        private const val TAG = "InstallStateHolder"
    }
}
