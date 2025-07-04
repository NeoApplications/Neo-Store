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
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.io.files.FileNotFoundException
import java.io.File
import java.io.IOException

class SessionInstaller(context: Context) : BaseInstaller(context) {

    companion object {
        const val TAG = "SessionInstaller"

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
            var sessionId = -1
            runCatching {
                // Check if APK file exists and is readable
                if (!apkFile.exists() || !apkFile.canRead()) {
                    throw FileNotFoundException("APK file does not exist or is not readable: ${apkFile.absolutePath}")
                }

                try {
                    sessionId = sessionInstaller.createSession(params)
                    Log.d(TAG, "Created session $sessionId for $packageName")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to create session: ${e.message}")
                    throw e
                }

                // Open session and write APK file
                sessionInstaller.openSession(sessionId).use { session ->
                    runCatching {
                        // Write the APK to the session
                        val writeMode = "package-${System.currentTimeMillis()}"
                        session.openWrite(writeMode, 0, apkFile.length()).use { out ->
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
                                        emitProgress(InstallState.Installing(progress), packageName)
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

                        emitProgress(InstallState.Installing(0.98f), packageName)
                        // Commit the session
                        session.commit(pendingIntent.intentSender)
                        if (Preferences[Preferences.Key.ReleasesCacheRetention] == 0) {
                            if (apkFile.delete())
                                Log.d(TAG, "Deleted APK file after commit: ${apkFile.absolutePath}")
                            else Log.w(TAG, "Failed to delete APK file: ${apkFile.absolutePath}")
                        }
                    }.onFailure { e ->
                        Log.e(TAG, "Error writing to session: ${e.message}")
                        try {
                            session.abandon()
                            Log.d(TAG, "Abandoned session $sessionId after error")
                        } catch (abandonException: Exception) {
                            Log.e(TAG, "Failed to abandon session: ${abandonException.message}")
                        }
                        throw e
                    }
                }
            }.onFailure { e ->
                // Clean up session if there was an error
                if (sessionId != -1) {
                    runCatching {
                        sessionInstaller.abandonSession(sessionId)
                        Log.d(
                            TAG,
                            "Installation failed: Abandoned session $sessionId after exception"
                        )
                    }.onFailure { abandonException ->
                        Log.e(
                            TAG,
                            "Installation failed: Failed to abandon session after exception: ${abandonException.message}"
                        )
                    }
                }

                val errorMessage = when (e) {
                    is SecurityException     -> {
                        Log.w(
                            TAG,
                            "Installation failed: Attempted to use a destroyed or sealed session when installing.\n${e.message}"
                        )
                        "Installation failed: Attempted to use a destroyed or sealed session when installing.\n${e.message}"
                    }

                    is FileNotFoundException -> {
                        Log.w(
                            TAG,
                            "Installation failed: Cache file does not seem to exist.\n${e.message}"
                        )
                        "Installation failed: Cache file does not seem to exist.\n${e.message}"
                    }

                    is IOException           -> {
                        Log.w(
                            TAG,
                            "Installation failed: I/O error due to a bad pipe.\n${e.message}"
                        )
                        "Installation failed: I/O error due to a bad pipe.\n${e.message}"
                    }

                    is IllegalStateException -> {
                        Log.w(TAG, "Installation failed: ${e.message}")
                        if (sessionInstaller.mySessions.size >= 50) {
                            sessionInstaller.mySessions
                                .filter { it.installerPackageName == context.packageName }
                                .forEach {
                                    try {
                                        sessionInstaller.abandonSession(it.sessionId)
                                    } catch (abandonException: Exception) {
                                        Log.e(
                                            TAG,
                                            "Failed to abandon session during cleanup: ${abandonException.message}"
                                        )
                                    }
                                }
                            delay(500)
                            installPackage(packageName, apkFile)
                            return@withContext
                        } else {
                            "Installation failed: Invalid state\n${e.message}"
                        }
                    }

                    else                     -> {
                        Log.e(
                            TAG,
                            "Unexpected error during installation: ${e.javaClass.simpleName} - ${e.message}"
                        )
                        "Installation failed: ${e.javaClass.simpleName}\n${e.message}"
                    }
                }

                reportFailure(InstallationError.Unknown(errorMessage), packageName)
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