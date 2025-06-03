package com.machiav3lli.fdroid.manager.installer

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import com.machiav3lli.fdroid.data.content.Cache
import com.machiav3lli.fdroid.data.entity.InstallState
import com.machiav3lli.fdroid.manager.work.InstallStateHolder
import com.machiav3lli.fdroid.utils.extension.android.Android
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

abstract class BaseInstaller(val context: Context) : InstallationEvents, KoinComponent {
    private val installStateHolder: InstallStateHolder by inject()
    protected val installQueue: InstallQueue by inject()

    protected fun emitProgress(progress: InstallState, packageName: String? = null) {
        if (packageName != null) {
            installStateHolder.updateState(packageName, progress)
        } else {
            runBlocking { installQueue.getCurrentTask() }?.let { currentTask ->
                installStateHolder.updateState(currentTask.packageName, progress)
            }
        }

        Log.d(
            TAG,
            "Installation state updated for ${packageName ?: "current task"}: ${progress::class.simpleName}"
        )
    }

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

        withContext(Dispatchers.IO) {
            processNextInstallation()
        }
    }

    protected abstract suspend fun processNextInstallation()

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
        emitProgress(InstallState.Failed(error), taskPackageName)

        val currentTask = installQueue.getCurrentTask()
        if (currentTask != null) {
            Log.w(TAG, "Installation failed for ${currentTask.packageName}: ${error.message}")
            installQueue.onInstallationComplete(Result.failure(error))
        } else if (packageName != null) {
            Log.w(TAG, "Installation failed for $packageName but no current task found")
        }
    }

    suspend fun reportSuccess(packageName: String) {
        emitProgress(InstallState.Success, packageName)

        val currentTask = installQueue.getCurrentTask()
        if (currentTask != null) {
            Log.d(TAG, "Installation succeeded for $packageName")
            installQueue.onInstallationComplete(Result.success(packageName))
        } else {
            Log.w(TAG, "Installation succeeded for $packageName but no current task found")
        }
    }

    suspend fun reportUserInteraction(packageName: String?) {
        installQueue.startUserInteraction(packageName.orEmpty())
        emitProgress(InstallState.Pending, packageName)
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
            Cache.getReleaseFile(context, fileName)
        } catch (e: Exception) {
            emitProgress(InstallState.Failed(InstallationError.Unknown("Installation failed: Error getting apk-file: ${e.message}")))
            null
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
