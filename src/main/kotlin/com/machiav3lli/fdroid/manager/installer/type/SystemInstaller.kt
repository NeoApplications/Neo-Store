package com.machiav3lli.fdroid.manager.installer.type

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller.SessionParams
import android.util.Log
import com.machiav3lli.fdroid.BUFFER_SIZE
import com.machiav3lli.fdroid.data.content.Cache.getReleaseFileUri
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.data.entity.InstallState
import com.machiav3lli.fdroid.manager.installer.InstallationError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import kotlinx.io.files.FileNotFoundException
import java.io.File

class SystemInstaller(context: Context) : BaseInstaller(context) {

    companion object {
        private const val TAG: String = "SystemInstaller"

        // TODO consider needing other flags
        private val flags = PendingIntent.FLAG_IMMUTABLE

        // TODO consider requiring extra params
        private val params = SessionParams(SessionParams.MODE_FULL_INSTALL)
    }

    private val packageInstaller = context.packageManager.packageInstaller

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
            runCatching {
                val packageUri = apkFile.getReleaseFileUri(context)
                val sessionId = packageInstaller.createSession(params)

                // Open session and write APK file
                packageInstaller.openSession(sessionId).use { session ->
                    Log.d(TAG, "Installing: " + apkFile.name)
                    Log.d(TAG, "packageUri: $packageUri")

                    session.openWrite("APPINSTALL", 0, -1)
                        .use { out ->
                            context.contentResolver.openInputStream(packageUri)
                                .use { input ->
                                    if (input == null) {
                                        Log.wtf(TAG, "package file provided can not be resolved")
                                        return@withContext
                                    }

                                    // TODO consider adding listener to track progress
                                    input.copyTo(out, BUFFER_SIZE)
                                    session.fsync(out)
                                }
                        }

                    //TODO: Do we need to receive status somehow?
                    // Consider using Intent(context, InstallerReceiver::class.java) instead
                    val intent = Intent()
                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        sessionId,
                        intent,
                        flags
                    )
                    val statusReceiver = pendingIntent.intentSender

                    emitProgress(InstallState.Installing(0.95f), packageName)
                    session.commit(statusReceiver)
                    if (Preferences[Preferences.Key.ReleasesCacheRetention] == 0) apkFile.delete()
                    // TODO add receiver to track status
                    reportSuccess(packageName)
                }
            }.onFailure { e ->
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
                        if (packageInstaller.mySessions.size == 50) {
                            packageInstaller.mySessions
                                .filter { it.isActive && it.appPackageName == context.packageName }
                                .forEach {
                                    packageInstaller.abandonSession(it.sessionId)
                                }
                            installPackage(packageName, apkFile)
                            return@withContext
                        } else throw e
                    }

                    else                     -> {
                        throw e
                    }
                }

                reportFailure(InstallationError.Unknown(errorMessage), packageName)
            }
        }
    }
}