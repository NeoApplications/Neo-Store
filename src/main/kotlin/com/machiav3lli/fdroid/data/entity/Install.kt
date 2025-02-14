package com.machiav3lli.fdroid.data.entity

sealed class InstallState {
    data object Pending : InstallState()
    data object Installing : InstallState()
    data object Completed : InstallState()
    data class Failed(val error: String) : InstallState()
}