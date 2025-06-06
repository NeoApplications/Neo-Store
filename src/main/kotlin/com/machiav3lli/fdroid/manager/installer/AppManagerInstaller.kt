package com.machiav3lli.fdroid.manager.installer

import android.content.Context
import android.content.Intent
import com.machiav3lli.fdroid.data.content.Cache.getReleaseFileUri
import com.machiav3lli.fdroid.data.entity.InstallState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class AppManagerInstaller(context: Context) : BaseInstaller(context) {

    override suspend fun processNextInstallation() {
        val task = installQueue.getCurrentTask() ?: return
        emitProgress(InstallState.Preparing, task.packageName)

        val apkFile = getApkFile(task.cacheFileName) ?: run {
            installQueue.onInstallationComplete(
                Result.failure(InstallationError.Unknown("Installation failed: Failed to get APK file"))
            )
            return
        }

        val packageName = extractPackageNameFromApk(apkFile) ?: task.packageName

        withContext(Dispatchers.IO) {
            runCatching {
                installPackage(packageName, apkFile)
            }.onFailure { e ->
                installQueue.onInstallationComplete(
                    Result.failure(InstallationError.Unknown("Installation failed: ${e.message}"))
                )
            }
        }
    }

    private suspend fun installPackage(packageName: String, apkFile: File) {
        withContext(Dispatchers.IO) {
            runCatching {
                val intent = Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
                    data = apkFile.getReleaseFileUri(context)
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                            Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
                            Intent.FLAG_ACTIVITY_NEW_TASK
                    // TODO to be tested
                    putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
                    putExtra(Intent.EXTRA_RETURN_RESULT, true)
                    putExtra(
                        "android.content.pm.extra.LEGACY_STATUS", // PackageInstaller.EXTRA_LEGACY_STATUS
                        false
                    )
                }

                // Can't track actual progress
                emitProgress(InstallState.Installing(0.1f), packageName)
                context.startActivity(intent)
                reportSuccess(packageName)
            }
        }.onFailure { e ->
            reportFailure(
                InstallationError.Unknown("Installation failed: Failed to start installation: ${e.message}"),
                packageName
            )
        }
    }
}