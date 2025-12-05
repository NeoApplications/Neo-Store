package com.machiav3lli.fdroid.manager.installer.type

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AndroidRuntimeException
import com.machiav3lli.fdroid.data.content.Cache.getReleaseFileUri
import com.machiav3lli.fdroid.data.entity.InstallState
import com.machiav3lli.fdroid.manager.installer.InstallationError
import java.io.File

// TODO: Use this for MIUI device instead of guiding new users
class LegacyInstaller(context: Context) : BaseInstaller(context) {

    override suspend fun installPackage(packageName: String, apkFile: File) {
        val fileUri = apkFile.getReleaseFileUri(context)

        runCatching {
            startInstallation(packageName, fileUri, false)
        }.onFailure { tr ->
            when (tr) {
                is AndroidRuntimeException
                     -> runCatching {
                    // Retry with NEW_TASK flag for contexts without activity
                    startInstallation(packageName, fileUri, true)
                }.onFailure { e ->
                    reportFailure(
                        InstallationError.Unknown("Installation failed: Failed to start installation: ${e.message}"),
                        packageName
                    )
                }

                else -> reportFailure(
                    InstallationError.Unknown("Installation failed: Failed to start installation: ${tr.message}"),
                    packageName
                )
            }
        }
        reportSuccess(packageName)
    }

    private suspend fun startInstallation(
        packageName: String,
        fileUri: Uri,
        addNewTaskFlag: Boolean,
    ) {
        val intent = createInstallIntent(fileUri, addNewTaskFlag)
        // Can't track actual progress with legacy installer
        installQueue.emitProgress(InstallState.Installing(0.1f), packageName)
        context.startActivity(intent)
        reportSuccess(packageName)
    }

    private fun createInstallIntent(fileUri: Uri, addNewTaskFlag: Boolean): Intent {
        return Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
            data = fileUri
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION.let { baseFlags ->
                if (addNewTaskFlag) baseFlags or Intent.FLAG_ACTIVITY_NEW_TASK else baseFlags
            }
            // TODO to be tested
            putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
            putExtra(Intent.EXTRA_INSTALLER_PACKAGE_NAME, context.packageName)
            putExtra(Intent.EXTRA_RETURN_RESULT, true)
            putExtra(
                "android.content.pm.extra.LEGACY_STATUS", // PackageInstaller.EXTRA_LEGACY_STATUS
                false
            )
        }
    }
}