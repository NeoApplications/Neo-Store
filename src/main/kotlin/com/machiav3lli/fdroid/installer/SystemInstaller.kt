package com.machiav3lli.fdroid.installer

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Build
import android.util.Log
import com.machiav3lli.fdroid.MainApplication
import com.machiav3lli.fdroid.content.Cache
import com.machiav3lli.fdroid.content.Cache.getPackageArchiveInfo
import com.machiav3lli.fdroid.content.Cache.getReleaseFileUri
import com.machiav3lli.fdroid.utility.extension.android.Android
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.OutputStream

class SystemInstaller(context: Context) : BaseInstaller(context) {
    companion object{
        private const val TAG: String = "SystemInstaller";
    }
    private suspend fun mSystemInstaller(
        packageName: String,
        cacheFile: File,
        postInstall: () -> Unit
    ) =
        withContext(Dispatchers.IO) {
            MainApplication.enqueuedInstalls.add(packageName)

            val packageInstaller = context.packageManager.packageInstaller
            val packageUri = Cache.getReleaseFile(context, cacheFile.name).getReleaseFileUri(context)
            val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
            val sessionId = packageInstaller.createSession(params)
            val session = packageInstaller.openSession(sessionId)

            Log.d(TAG, "Installing: " + cacheFile.name)
            Log.d(TAG, "packageUri: " + packageUri)

            val out: OutputStream = session.openWrite("APPINSTALL", 0, -1)
            val `in` = context.contentResolver.openInputStream(packageUri)

            if (`in` == null){
                Log.wtf(TAG, "package file provided can not be resolved")
                return@withContext
            }

            `in`.copyTo(out)
            session.fsync(out)
            `in`.close()
            out.close()

            //TODO: Do we need to receive status somehow?
            val intent = Intent()
            val pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_IMMUTABLE)
            val statusReceiver = pendingIntent.intentSender

            session.commit(statusReceiver)

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


    override suspend fun install(
        packageLabel: String,
        cacheFileName: String,
        postInstall: () -> Unit
    ) {
        val cacheFile = Cache.getReleaseFile(context, cacheFileName)
        val packageInfo = context.getPackageArchiveInfo(cacheFile)
        val packageName = packageInfo?.packageName ?: "unknown-package"

        mSystemInstaller(packageName, cacheFile, postInstall)
    }

    override suspend fun isInstalling(packageName: String): Boolean {
        return MainApplication.enqueuedInstalls.contains(packageName)
    }

    override suspend fun uninstall(packageName: String) {
        mDefaultUninstaller(packageName)
    }
}