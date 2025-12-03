package com.machiav3lli.fdroid.manager.installer.type

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.machiav3lli.fdroid.AM_PACKAGENAME
import com.machiav3lli.fdroid.data.content.Cache.getReleaseFileUri
import com.machiav3lli.fdroid.data.entity.InstallState
import com.machiav3lli.fdroid.manager.installer.InstallationError
import com.machiav3lli.fdroid.utils.installedAM
import java.io.File

class AppManagerInstaller(context: Context) : BaseInstaller(context) {

    override suspend fun installPackage(packageName: String, apkFile: File) {
        runCatching {
            val intent = Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
                data = apkFile.getReleaseFileUri(context)
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                        Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
                        Intent.FLAG_ACTIVITY_NEW_TASK
                val amPackage = context.installedAM?.`package` ?: AM_PACKAGENAME
                // TODO need to be updated should AM change class
                component = ComponentName(
                    amPackage,
                    "$amPackage.apk.installer.PackageInstallerActivity"
                )
                // TODO to be tested
                putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
                putExtra(Intent.EXTRA_RETURN_RESULT, true)
                putExtra(
                    "android.content.pm.extra.LEGACY_STATUS", // PackageInstaller.EXTRA_LEGACY_STATUS
                    false
                )
            }

            // Can't track actual progress
            installQueue.emitProgress(InstallState.Installing(0.1f), packageName)
            context.startActivity(intent)
            reportSuccess(packageName)
        }.onFailure { e ->
            reportFailure(
                InstallationError.Unknown("Installation failed: Failed to start installation: ${e.message}"),
                packageName
            )
        }
    }
}