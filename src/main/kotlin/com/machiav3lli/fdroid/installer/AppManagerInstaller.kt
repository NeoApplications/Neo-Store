package com.machiav3lli.fdroid.installer

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.machiav3lli.fdroid.MainApplication
import com.machiav3lli.fdroid.content.Cache
import com.machiav3lli.fdroid.content.Cache.getPackageArchiveInfo
import com.machiav3lli.fdroid.content.Cache.getReleaseFileUri
import com.machiav3lli.fdroid.utility.extension.android.Android
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class AppManagerInstaller(context: Context) : BaseInstaller(context) {

    override suspend fun install(
        packageLabel: String,
        cacheFileName: String,
        postInstall: () -> Unit
    ) {
        val cacheFile = Cache.getReleaseFile(context, cacheFileName)
        val packageInfo = context.getPackageArchiveInfo(cacheFile)
        val packageName = packageInfo?.packageName ?: "unknown-package"

        mAppManagerInstaller(packageName, cacheFile, postInstall)
    }

    override suspend fun isInstalling(packageName: String): Boolean =
        MainApplication.enqueuedInstalls.contains(packageName)

    override suspend fun uninstall(packageName: String) = mDefaultUninstaller(packageName)

    private suspend fun mAppManagerInstaller(
        packageName: String,
        cacheFile: File,
        postInstall: () -> Unit
    ) =
        withContext(Dispatchers.IO) {
            MainApplication.enqueuedInstalls.add(packageName)
            val (uri, flags) = Pair(
                Cache.getReleaseFile(context, cacheFile.name).getReleaseFileUri(context),
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            )

            @Suppress("DEPRECATION")
            context.startActivity(
                Intent(Intent.ACTION_INSTALL_PACKAGE)
                    .setDataAndType(uri, "application/octet-stream")
                    .setFlags(flags)
            )
            MainApplication.db.getInstallTaskDao().delete(packageName)
            MainApplication.enqueuedInstalls.remove(packageName)
            postInstall()
        }

    private suspend fun mDefaultUninstaller(packageName: String) = withContext(Dispatchers.IO) {
        val uri = Uri.fromParts("package", packageName, null)
        val intent = Intent()
        intent.data = uri

        @Suppress("DEPRECATION")
        when {
            Android.sdk(Build.VERSION_CODES.P) -> {
                intent.action = Intent.ACTION_DELETE
            }

            else                               -> {
                intent.action = Intent.ACTION_UNINSTALL_PACKAGE
                intent.putExtra(Intent.EXTRA_RETURN_RESULT, true)
            }
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        context.startActivity(intent)
    }
}