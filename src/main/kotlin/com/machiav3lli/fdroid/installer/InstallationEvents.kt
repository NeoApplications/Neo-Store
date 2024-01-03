package com.machiav3lli.fdroid.installer

interface InstallationEvents {
    suspend fun install(packageLabel: String, cacheFileName: String)
    suspend fun isInstalling(packageName: String): Boolean
    suspend fun uninstall(packageName: String)
}