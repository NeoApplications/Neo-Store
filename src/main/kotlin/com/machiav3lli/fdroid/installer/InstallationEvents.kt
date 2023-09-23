package com.machiav3lli.fdroid.installer

interface InstallationEvents {
    suspend fun install(packageName: String, cacheFileName: String)

    suspend fun uninstall(packageName: String)
}