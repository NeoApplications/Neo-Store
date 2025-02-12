package com.machiav3lli.fdroid.manager.service.worker

sealed class InstallState {
    data object Pending : InstallState()
    data object Installing : InstallState()
    data object Completed : InstallState()
    data class Failed(val error: String) : InstallState()
}