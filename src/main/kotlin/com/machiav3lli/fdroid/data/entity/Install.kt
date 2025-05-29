package com.machiav3lli.fdroid.data.entity

sealed class InstallState {
    object Preparing : InstallState()
    object Pending : InstallState()
    data class Installing(val progress: Float = 0f) : InstallState()
    object Success : InstallState()

    // TODO data class Failed(val error: InstallationError) : InstallState()
    data class Failed(val error: Throwable) : InstallState()

    fun isActive(): Boolean = this is Preparing || this is Pending || this is Installing
}