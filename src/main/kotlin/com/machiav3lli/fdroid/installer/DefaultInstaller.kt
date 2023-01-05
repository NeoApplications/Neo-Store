package com.machiav3lli.fdroid.installer

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller.SessionParams
import android.content.pm.PackageManager.PackageInfoFlags
import android.os.Build
import android.util.Log
import com.machiav3lli.fdroid.content.Cache
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.utility.extension.android.Android
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

class DefaultInstaller(context: Context) : BaseInstaller(context) {

    private val packageManager = context.packageManager
    private val sessionInstaller = packageManager.packageInstaller
    private val intent = Intent(context, InstallerService::class.java)

    companion object {
        val flags = if (Android.sdk(31)) PendingIntent.FLAG_MUTABLE else 0
        val sessionParams = SessionParams(SessionParams.MODE_FULL_INSTALL).apply {
            if (Android.sdk(31)) {
                setRequireUserAction(SessionParams.USER_ACTION_NOT_REQUIRED)
            }
        }
    }

    override suspend fun install(cacheFileName: String) {
        val cacheFile = Cache.getReleaseFile(context, cacheFileName)
        mDefaultInstaller(cacheFile)
    }

    override suspend fun install(packageName: String, cacheFileName: String) {
        val cacheFile = Cache.getReleaseFile(context, cacheFileName)
        // using packageName to store the app's name for the notification later down the line
        intent.putExtra(InstallerService.KEY_APP_NAME, packageName)
        mDefaultInstaller(cacheFile)
    }

    override suspend fun install(packageName: String, cacheFile: File) {
        intent.putExtra(InstallerService.KEY_APP_NAME, packageName)
        mDefaultInstaller(cacheFile)
    }

    override suspend fun uninstall(packageName: String) = mDefaultUninstaller(packageName)

    private fun mDefaultInstaller(cacheFile: File) {
        // clean up inactive sessions
        sessionInstaller.mySessions
            .filter { session -> !session.isActive }
            .forEach { session ->
                try {
                    sessionInstaller.abandonSession(session.sessionId)
                } catch (_: SecurityException) {
                    Log.w(
                        "DefaultInstaller",
                        "Attempted to abandon a session we do not own."
                    )
                }
            }

        // start new session
        val id = sessionInstaller.createSession(sessionParams)
        val session = sessionInstaller.openSession(id)

        // get package name
        val packageInfo =
            if (Android.sdk(Build.VERSION_CODES.TIRAMISU)) packageManager.getPackageArchiveInfo(
                cacheFile.absolutePath,
                PackageInfoFlags.of(0)
            ) else packageManager.getPackageArchiveInfo(cacheFile.absolutePath, 0)
        val packageName = packageInfo?.packageName ?: "unknown-package"

        // error flags
        var hasErrors = false

        session.use { activeSession ->
            try {
                val sessionOutputStream = activeSession.openWrite(packageName, 0, -1)
                val packageInputStream = cacheFile.inputStream()
                val buffer = ByteArray(4096)

                var n: Int?
                while (packageInputStream.read(buffer).also { n = it } > 0) {
                    sessionOutputStream.write(buffer, 0, n ?: 0)
                }

                packageInputStream.close()
                sessionOutputStream.flush()
                sessionOutputStream.close()
            } catch (e: FileNotFoundException) {
                Log.w(
                    "DefaultInstaller",
                    "Cache file does not seem to exist.\n${e.message}"
                )
                hasErrors = true
            } catch (e: SecurityException) {
                Log.w(
                    "DefaultInstaller",
                    "Attempted to use a destroyed or sealed session when installing.\n${e.message}"
                )
                hasErrors = true
            } catch (e: IOException) {
                Log.w(
                    "DefaultInstaller",
                    "Failed to perform cache to package copy due to a bad pipe.\n${e.message}"
                )
                hasErrors = true
            }
        }

        if (!hasErrors) {
            session.commit(PendingIntent.getService(context, id, intent, flags).intentSender)
            if (Preferences[Preferences.Key.ReleasesCacheRetention] == 0) cacheFile.delete()
        }
    }

    private suspend fun mDefaultUninstaller(packageName: String) {
        intent.putExtra(InstallerService.KEY_ACTION, InstallerService.ACTION_UNINSTALL)

        val pendingIntent = PendingIntent.getService(context, -1, intent, flags)

        withContext(Dispatchers.Default) {
            sessionInstaller.uninstall(packageName, pendingIntent.intentSender)
        }
    }
}