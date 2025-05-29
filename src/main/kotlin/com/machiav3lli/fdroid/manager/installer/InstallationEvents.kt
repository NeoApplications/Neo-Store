package com.machiav3lli.fdroid.manager.installer

/**
 * Interface defining core installation operations
 * Implementations should provide robust installation handling
 */
interface InstallationEvents {
    /**
     * Initiates an installation process for an app
     *
     * @param packageLabel The display name of the app to be installed
     * @param cacheFileName The cache file name containing the APK
     * @param postInstall Callback to be invoked after the installation completes (success or failure)
     */
    suspend fun install(packageLabel: String, cacheFileName: String, postInstall: (Result<String>) -> Unit = {})
    
    /**
     * Checks if a package is already enqueued for installation
     *
     * @param packageName The package name to check
     * @return True if the package is in the installation queue, false otherwise
     */
    suspend fun isEnqueued(packageName: String): Boolean
    
    /**
     * Cancels any ongoing installation
     */
    suspend fun cancelInstall()
    
    /**
     * Initiates an uninstallation process for an app
     *
     * @param packageName The package name of the app to be uninstalled
     */
    suspend fun uninstall(packageName: String)
}