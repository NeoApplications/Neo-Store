package com.machiav3lli.fdroid.manager.installer

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.content.pm.PackageInstaller.SessionParams
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.machiav3lli.fdroid.BUFFER_SIZE
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.data.entity.InstallState
import com.machiav3lli.fdroid.manager.service.InstallerReceiver
import com.machiav3lli.fdroid.utils.extension.android.Android
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.files.FileNotFoundException
import java.io.File
import java.io.IOException

class SessionInstaller(context: Context) : BaseInstaller(context) {

    companion object {
        private val flags = when {
            Android.sdk(Build.VERSION_CODES.UPSIDE_DOWN_CAKE) ->
                // For Android 14+, use FLAG_IMMUTABLE for implicit intents for security
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_ALLOW_UNSAFE_IMPLICIT_INTENT


            Android.sdk(Build.VERSION_CODES.S)                ->
                // For Android 12+, but below 14, can use FLAG_MUTABLE
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT

            else                                              -> PendingIntent.FLAG_UPDATE_CURRENT
        }
        private val params = SessionParams(SessionParams.MODE_FULL_INSTALL).apply {
            if (Android.sdk(Build.VERSION_CODES.O)) {
                setInstallReason(PackageManager.INSTALL_REASON_USER)
            }
            if (Android.sdk(Build.VERSION_CODES.TIRAMISU)) {
                setRequireUserAction(SessionParams.USER_ACTION_NOT_REQUIRED)
                setPackageSource(PackageInstaller.PACKAGE_SOURCE_STORE)
            }
        }
    }

    private val sessionInstaller = context.packageManager.packageInstaller
    private val intent = Intent(context, InstallerReceiver::class.java)

    override suspend fun processNextInstallation() {
        // Get the current task (if any)
        val task = installQueue.getCurrentTask() ?: return
        emitProgress(InstallState.Preparing)

        val apkFile = getApkFile(task.cacheFileName) ?: run {
            installQueue.onInstallationComplete(
                Result.failure(InstallationError.Unknown("Installation failed: Failed to get APK file"))
            )
            return
        }

        val packageName = extractPackageNameFromApk(apkFile) ?: task.packageName

        withContext(Dispatchers.IO) {
            try {
                installPackage(packageName, apkFile)
            } catch (e: Exception) {
                installQueue.onInstallationComplete(
                    Result.failure(InstallationError.Unknown("Installation failed: ${e.message}"))
                )
            }
        }
    }

    private suspend fun installPackage(packageName: String, apkFile: File) {
        withContext(Dispatchers.IO) {
            try {
                val sessionId = sessionInstaller.createSession(params)

                // Open session and write APK file
                sessionInstaller.openSession(sessionId).use { session ->
                    // Write the APK to the session
                    session.openWrite("package-${System.currentTimeMillis()}", 0, apkFile.length())
                        .use { out ->
                            apkFile.inputStream().use { input ->
                                val buffer = ByteArray(BUFFER_SIZE)
                                var length: Int
                                var lastProgress = 0f
                                var bytesWritten = 0L
                                val totalBytes = apkFile.length()

                                while (input.read(buffer).also { length = it } > 0) {
                                    out.write(buffer, 0, length)
                                    bytesWritten += length

                                    // Calculate and emit progress
                                    val progress = bytesWritten.toFloat() / totalBytes
                                    if (progress - lastProgress >= 0.05f) { // Update progress in 5% increments
                                        emitProgress(InstallState.Installing(progress))
                                        lastProgress = progress
                                    }
                                }

                                // Make sure the stream is flushed
                                out.flush()
                            }
                        }

                    // Create a pending intent for the install status
                    intent.apply {
                        putExtra(PackageInstaller.EXTRA_PACKAGE_NAME, packageName)
                    }

                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        sessionId,
                        intent,
                        flags,
                    )

                    emitProgress(InstallState.Installing(0.98f))
                    // Commit the session
                    session.commit(pendingIntent.intentSender)
                    if (Preferences[Preferences.Key.ReleasesCacheRetention] == 0) apkFile.delete()
                }
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is SecurityException     -> {
                        Log.w(
                            "SessionInstaller",
                            "Installation failed: Attempted to use a destroyed or sealed session when installing.\n${e.message}"
                        )
                        "Installation failed: Attempted to use a destroyed or sealed session when installing.\n${e.message}"
                    }

                    is FileNotFoundException -> {
                        Log.w(
                            "SessionInstaller",
                            "Installation failed: Cache file does not seem to exist.\n${e.message}"
                        )
                        "Installation failed: Cache file does not seem to exist.\n${e.message}"
                    }

                    is IOException           -> {
                        Log.w(
                            "SessionInstaller",
                            "Installation failed: Due to a bad pipe.\n${e.message}"
                        )
                        "Installation failed: Due to a bad pipe.\n${e.message}"
                    }

                    is IllegalStateException -> {
                        Log.w(
                            "SessionInstaller",
                            "Installation failed: ${e.message}"
                        )
                        if (sessionInstaller.mySessions.size >= 50) {
                            sessionInstaller.mySessions
                                .filter { it.isActive && it.appPackageName == context.packageName }
                                .forEach {
                                    sessionInstaller.abandonSession(it.sessionId)
                                }
                            installPackage(packageName, apkFile)
                            return@withContext
                        } else throw e
                    }

                    else                     -> {
                        throw e
                    }
                }
                installQueue.onInstallationComplete(
                    Result.failure(InstallationError.Unknown(errorMessage))
                )
            }
        }
    }

    override suspend fun uninstall(packageName: String) {
        withContext(Dispatchers.IO) {
            intent.putExtra(InstallerReceiver.KEY_ACTION, InstallerReceiver.ACTION_UNINSTALL)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                -1,
                intent,
                flags,
            )

            withContext(Dispatchers.Default) {
                sessionInstaller.uninstall(packageName, pendingIntent.intentSender)
            }
        }
    }
}