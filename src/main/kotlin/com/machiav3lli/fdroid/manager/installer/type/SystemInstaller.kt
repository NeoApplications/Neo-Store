package com.machiav3lli.fdroid.manager.installer.type

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller.SessionParams
import android.util.Log
import com.machiav3lli.fdroid.BUFFER_SIZE
import com.machiav3lli.fdroid.data.content.Cache.getReleaseFileUri
import com.machiav3lli.fdroid.data.entity.InstallState
import com.machiav3lli.fdroid.manager.installer.InstallationError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import kotlinx.io.files.FileNotFoundException
import java.io.File
import java.io.InputStream
import java.io.OutputStream

class SystemInstaller(context: Context) : BaseInstaller(context) {

    companion object {
        private const val TAG: String = "SystemInstaller"

        // TODO consider needing other flags
        private val flags by lazy {
            PendingIntent.FLAG_IMMUTABLE
        }

        // TODO consider requiring extra params
        private val params by lazy {
            SessionParams(SessionParams.MODE_FULL_INSTALL)
        }

        private val bufferPool = object {
            private val buffers = ArrayDeque<ByteArray>(5)

            @Synchronized
            fun acquire(): ByteArray = buffers.removeFirstOrNull() ?: ByteArray(BUFFER_SIZE)

            @Synchronized
            fun release(buffer: ByteArray) {
                if (buffers.size < 5) {
                    buffers.addLast(buffer)
                }
            }
        }
    }

    private val packageInstaller = context.packageManager.packageInstaller

    override suspend fun installPackage(packageName: String, apkFile: File) {
        installPackageWithRetry(packageName, apkFile, MAX_RETRY_ATTEMPTS)
    }

    private suspend fun installPackageWithRetry(
        packageName: String,
        apkFile: File,
        attemptsRemaining: Int
    ) {
        var sessionId = -1

        try {
            if (!apkFile.exists() || !apkFile.canRead()) {
                throw FileNotFoundException("APK file does not exist or is not readable: ${apkFile.absolutePath}")
            }

            val packageUri = apkFile.getReleaseFileUri(context)
            try {
                sessionId = packageInstaller.createSession(params)
                Log.d(TAG, "Created session $sessionId for $packageName")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create session: ${e.message}")
                throw e
            }

            // Open session and write APK file
            packageInstaller.openSession(sessionId).use { session ->
                Log.d(TAG, "Installing: " + apkFile.name)
                Log.d(TAG, "packageUri: $packageUri")
                writeApkToSession(session, apkFile, packageName)
                commitSession(session, sessionId, packageName)
                cleanupApkIfNeeded(apkFile)
                // TODO add receiver to track status?
                reportSuccess(packageName)
            }
        } catch (e: IllegalStateException) {
            if (packageInstaller.mySessions.size >= 50 && attemptsRemaining > 1) {
                Log.w(
                    TAG,
                    "Session limit exceeded, cleaning up and retrying. Attempts remaining: ${attemptsRemaining - 1}"
                )
                if (sessionId != -1) {
                    abandonSessionSafely(sessionId)
                }
                cleanupStaleSessions()
                delay(500)
                installPackageWithRetry(packageName, apkFile, attemptsRemaining - 1)
            } else {
                if (sessionId != -1) {
                    abandonSessionSafely(sessionId)
                }
                handleInstallationException(e, packageName)
            }
        } catch (e: Exception) {
            if (sessionId != -1) {
                abandonSessionSafely(sessionId)
            }
            handleInstallationException(e, packageName)
        }
    }

    private suspend fun writeApkToSession(
        session: android.content.pm.PackageInstaller.Session,
        apkFile: File,
        packageName: String
    ) {
        val packageUri = apkFile.getReleaseFileUri(context)
        val writeMode = "APPINSTALL-${System.currentTimeMillis()}"
        session.openWrite(writeMode, 0, -1).use { out ->
            context.contentResolver.openInputStream(packageUri).use { input ->
                if (input == null) {
                    Log.wtf(TAG, "package file provided can not be resolved")
                    throw IOException("Failed to open input stream for $packageUri")
                }

                copyWithProgress(input, out, apkFile.length(), packageName)
                session.fsync(out)
            }
        }
    }

    private suspend fun copyWithProgress(
        input: InputStream,
        output: OutputStream,
        totalBytes: Long,
        packageName: String
    ) = withContext(Dispatchers.IO) {
        val buffer = bufferPool.acquire()
        try {
            var bytesWritten = 0L
            var lastEmittedBytes = 0L
            val progressThreshold = (totalBytes * 0.05f).toLong() // 5% in bytes

            var length: Int
            while (input.read(buffer).also { length = it } > 0) {
                output.write(buffer, 0, length)
                bytesWritten += length

                // Emit progress at 5% intervals
                if (bytesWritten - lastEmittedBytes >= progressThreshold) {
                    val progress = bytesWritten.toFloat() / totalBytes
                    // Non-blocking progress report using launch
                    launch(Dispatchers.Default) {
                        installQueue.emitProgress(
                            // Copying is considered 90% of the process
                            InstallState.Installing(progress * 0.9f),
                            packageName,
                        )
                    }
                    lastEmittedBytes = bytesWritten
                }
            }
        } finally {
            bufferPool.release(buffer)
        }
    }

    private fun commitSession(
        session: android.content.pm.PackageInstaller.Session,
        sessionId: Int,
        packageName: String
    ) {
        // TODO: Do we need to receive status somehow?
        // Consider using Intent(context, InstallerReceiver::class.java) instead
        val intent = Intent()
        val pendingIntent = PendingIntent.getActivity(
            context,
            sessionId,
            intent,
            flags
        )

        installQueue.emitProgress(InstallState.Installing(0.95f), packageName)
        session.commit(pendingIntent.intentSender)
        Log.d(TAG, "Committed session $sessionId for $packageName")
    }

    private suspend fun handleInstallationException(e: Exception, packageName: String) {
        val errorMessage = when (e) {
            is SecurityException     -> {
                Log.w(TAG, "Security exception: ${e.message}")
                "Installation failed: Attempted to use a destroyed or sealed session when installing.\n${e.message}"
            }

            is FileNotFoundException -> {
                Log.w(TAG, "File not found: ${e.message}")
                "Installation failed: Cache file does not seem to exist.\n${e.message}"
            }

            is IOException           -> {
                Log.w(TAG, "IO error: ${e.message}")
                "Installation failed: Due to a bad pipe.\n${e.message}"
            }

            is IllegalStateException -> {
                Log.w(TAG, "Illegal state: ${e.message}")
                "Installation failed: Invalid installation state.\n${e.message}"
            }

            else                     -> {
                Log.e(TAG, "Unexpected error: ${e.javaClass.simpleName} - ${e.message}")
                "Installation failed: ${e.javaClass.simpleName}\n${e.message}"
            }
        }

        reportFailure(InstallationError.Unknown(errorMessage), packageName)
    }

    private fun cleanupStaleSessions() {
        Log.d(
            TAG,
            "Cleaning up stale sessions. Total sessions: ${packageInstaller.mySessions.size}"
        )

        packageInstaller.mySessions
            .filter { it.isActive && it.appPackageName == context.packageName }
            .forEach { session ->
                try {
                    packageInstaller.abandonSession(session.sessionId)
                    Log.d(TAG, "Cleaned up session ${session.sessionId}")
                } catch (abandonException: Exception) {
                    Log.e(
                        TAG,
                        "Failed to abandon session ${session.sessionId}: ${abandonException.message}"
                    )
                }
            }
    }

    private fun abandonSessionSafely(sessionId: Int) {
        try {
            packageInstaller.abandonSession(sessionId)
            Log.d(TAG, "Abandoned session $sessionId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to abandon session $sessionId: ${e.message}")
        }
    }
}