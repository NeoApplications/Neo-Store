package com.machiav3lli.fdroid.installer

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AndroidRuntimeException
import com.machiav3lli.fdroid.MainApplication
import com.machiav3lli.fdroid.content.Cache
import com.machiav3lli.fdroid.content.Cache.getPackageArchiveInfo
import com.machiav3lli.fdroid.content.Cache.getReleaseFileUri
import com.machiav3lli.fdroid.utility.extension.android.Android
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

// TODO: Use this for MIUI device instead of guiding new users
class LegacyInstaller(context: Context) : BaseInstaller(context) {

    override suspend fun install(packageLabel: String, cacheFileName: String) {
        val cacheFile = Cache.getReleaseFile(context, cacheFileName)
        val packageInfo = context.getPackageArchiveInfo(cacheFile)
        val packageName = packageInfo?.packageName ?: "unknown-package"

        mOldDefaultInstaller(packageName, cacheFile)
    }

    override suspend fun isInstalling(packageName: String): Boolean =
        MainApplication.enqueuedInstalls.contains(packageName)

    override suspend fun uninstall(packageName: String) = mOldDefaultUninstaller(packageName)

    private suspend fun mOldDefaultInstaller(packageName: String, cacheFile: File) =
        withContext(Dispatchers.IO) {
            MainApplication.enqueuedInstalls.add(packageName)
            val (uri, flags) = Pair(
                Cache.getReleaseFile(context, cacheFile.name).getReleaseFileUri(context),
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            @Suppress("DEPRECATION")
            try {
                context.startActivity(
                    Intent(Intent.ACTION_INSTALL_PACKAGE)
                        .setDataAndType(uri, "application/vnd.android.package-archive")
                        .setFlags(flags)
                )
            } catch (e: AndroidRuntimeException) {
                context.startActivity(
                    Intent(Intent.ACTION_INSTALL_PACKAGE)
                        .setDataAndType(uri, "application/vnd.android.package-archive")
                        .setFlags(flags or Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
            MainApplication.db.installTaskDao.delete(packageName)
            MainApplication.enqueuedInstalls.remove(packageName)
        }

    private suspend fun mOldDefaultUninstaller(packageName: String) = withContext(Dispatchers.IO) {
        val uri = Uri.fromParts("package", packageName, null)
        val intent = Intent()
        intent.data = uri

        @Suppress("DEPRECATION")
        if (Android.sdk(28)) {
            intent.action = Intent.ACTION_DELETE
        } else {
            intent.action = Intent.ACTION_UNINSTALL_PACKAGE
            intent.putExtra(Intent.EXTRA_RETURN_RESULT, true)
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }
}