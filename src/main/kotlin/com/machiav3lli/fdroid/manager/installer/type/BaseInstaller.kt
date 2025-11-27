package com.machiav3lli.fdroid.manager.installer.type

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import com.machiav3lli.fdroid.data.content.Cache
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.data.entity.InstallState
import com.machiav3lli.fdroid.manager.installer.InstallQueue
import com.machiav3lli.fdroid.manager.installer.InstallQueue.Companion.InstallTask
import com.machiav3lli.fdroid.manager.installer.InstallationError
import com.machiav3lli.fdroid.manager.installer.InstallationEvents
import com.machiav3lli.fdroid.utils.extension.android.Android
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

abstract class BaseInstaller(val context: Context) : InstallationEvents, KoinComponent {
    protected val installQueue: InstallQueue by inject()

    override suspend fun install(
        packageLabel: String,
        cacheFileName: String,
        postInstall: (Result<String>) -> Unit
    ) {
        val apkFile = getApkFile(cacheFileName)
        if (apkFile == null) {
            postInstall(Result.failure(InstallationError.Unknown("Installation failed: Unable to access APK file")))
            return
        }

        val packageName = extractPackageNameFromApk(apkFile) ?: packageLabel

        installQueue.enqueue(packageName, packageLabel, cacheFileName, postInstall)
    }

    suspend fun runInstall(task: InstallTask, onFailure: suspend (Throwable) -> Unit) {
        withContext(Dispatchers.IO) {
            val apkFile = getApkFile(task.cacheFileName)
            if (apkFile == null) {
                installQueue.onInstallationComplete(
                    Result.failure(InstallationError.Unknown("Installation failed: Failed to get cached APK file ${task.cacheFileName}"))
                )
                return@withContext
            }

            val packageName = extractPackageNameFromApk(apkFile) ?: task.packageName

            runCatching {
                installPackage(packageName, apkFile)
            }.onFailure { e ->
                onFailure(e)
            }
        }
    }

    protected abstract suspend fun installPackage(packageName: String, apkFile: File)

    suspend fun checkQueueHealth(): Boolean = installQueue.checkQueueHealth()

    override suspend fun isEnqueued(packageName: String): Boolean =
        installQueue.isEnqueued(packageName)

    fun isInUserInteraction(packageName: String?): Boolean =
        installQueue.isInUserInteraction(packageName)

    // TODO reconsider where to use this
    override suspend fun cancelInstall(packageName: String) {
        installQueue.cancel(packageName)
    }

    suspend fun reportFailure(error: InstallationError, packageName: String? = null) {
        val taskPackageName = packageName ?: installQueue.getCurrentTask()?.packageName
        installQueue.emitProgress(InstallState.Failed(error), taskPackageName)

        val currentTask = installQueue.getCurrentTask()
        if (currentTask != null) {
            Log.w(TAG, "Installation failed for ${currentTask.packageName}: ${error.message}")
            installQueue.onInstallationComplete(Result.failure(error))
        } else if (packageName != null) {
            Log.w(TAG, "Installation failed for $packageName but no current task found")
        }
    }

    suspend fun reportSuccess(packageName: String) {
        installQueue.emitProgress(InstallState.Success, packageName)

        val currentTask = installQueue.getCurrentTask()
        if (currentTask != null) {
            Log.d(TAG, "Installation succeeded for $packageName")
            installQueue.onInstallationComplete(Result.success(packageName))
        } else {
            Log.w(TAG, "Installation succeeded for $packageName but no current task found")
        }
    }

    fun reportUserInteraction(packageName: String?) {
        installQueue.startUserInteraction(packageName.orEmpty())
        installQueue.emitProgress(InstallState.Pending, packageName)
    }

    // Default; used in Legacy, System & AppManager
    override suspend fun uninstall(packageName: String) {
        withContext(Dispatchers.IO) {
            runCatching {
                val uri = Uri.fromParts("package", packageName, null)
                val intent = Intent()
                intent.data = uri

                @Suppress("DEPRECATION")
                if (Android.sdk(Build.VERSION_CODES.P)) {
                    intent.action = Intent.ACTION_DELETE
                } else {
                    intent.action = Intent.ACTION_UNINSTALL_PACKAGE
                    intent.putExtra(Intent.EXTRA_RETURN_RESULT, true)
                }
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            } // TODO add reporting failure
        }
    }

    protected fun getApkFile(fileName: String): File? {
        return try {
            Cache.getReleaseFile(context, fileName).takeIf {
                it.exists() && it.canRead()
            }
        } catch (e: Exception) {
            installQueue.emitProgress(InstallState.Failed(InstallationError.Unknown("Installation failed: Error getting apk-file: ${e.message}")))
            null
        }
    }

    protected suspend fun cleanupApkIfNeeded(apkFile: File) {
        if (Preferences[Preferences.Key.ReleasesCacheRetention] == 0) {
            withContext(Dispatchers.IO) {
                if (apkFile.delete()) {
                    Log.d(TAG, "Deleted APK file: ${apkFile.absolutePath}")
                } else {
                    Log.w(TAG, "Failed to delete APK file: ${apkFile.absolutePath}")
                }
            }
        }
    }

    protected fun extractPackageNameFromApk(apkFile: File): String? {
        return try {
            val packageInfo = if (Android.sdk(Build.VERSION_CODES.TIRAMISU))
                context.packageManager.getPackageArchiveInfo(
                    apkFile.absolutePath,
                    PackageManager.PackageInfoFlags.of(0)
                )
            else context.packageManager.getPackageArchiveInfo(apkFile.absolutePath, 0)
            packageInfo?.packageName
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        const val TAG = "BaseInstaller"

        fun translatePackageInstallerError(status: Int): InstallationError {
            return when (status) {
                PackageInstaller.STATUS_FAILURE_ABORTED      -> InstallationError.UserCancelled()
                PackageInstaller.STATUS_FAILURE_BLOCKED      -> InstallationError.Blocked()
                PackageInstaller.STATUS_FAILURE_CONFLICT     -> InstallationError.ConflictingSignature()
                PackageInstaller.STATUS_FAILURE_INCOMPATIBLE -> InstallationError.Incompatible()
                PackageInstaller.STATUS_FAILURE_INVALID      -> InstallationError.PackageInvalid()
                PackageInstaller.STATUS_FAILURE_STORAGE      -> InstallationError.InsufficientStorage()
                else                                         -> InstallationError.Unknown("Installation failed: $status")
            }
        }
    }
}
