package com.machiav3lli.fdroid.manager.installer.type

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.content.pm.PackageInstaller.SessionParams
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.machiav3lli.fdroid.BUFFER_SIZE
import com.machiav3lli.fdroid.data.entity.InstallState
import com.machiav3lli.fdroid.manager.installer.InstallationError
import com.machiav3lli.fdroid.manager.service.InstallerReceiver
import com.machiav3lli.fdroid.utils.extension.android.Android
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.files.FileNotFoundException
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class SessionInstaller(context: Context) : BaseInstaller(context) {

    companion object {
        private const val TAG = "SessionInstaller"

        private val flags = when {
            // For Android 14+, use FLAG_IMMUTABLE for implicit intents for security
            Android.sdk(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
                 -> PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_ALLOW_UNSAFE_IMPLICIT_INTENT

            // For Android 12+, but below 14, can use FLAG_MUTABLE
            Android.sdk(Build.VERSION_CODES.S)
                 -> PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT

            else -> PendingIntent.FLAG_UPDATE_CURRENT
        }
        private val params = SessionParams(SessionParams.MODE_FULL_INSTALL).apply {
            if (Android.sdk(Build.VERSION_CODES.O)) {
                setInstallReason(PackageManager.INSTALL_REASON_USER)
            }
            if (Android.sdk(Build.VERSION_CODES.S)) {
                setRequireUserAction(SessionParams.USER_ACTION_NOT_REQUIRED)
            }
            if (Android.sdk(Build.VERSION_CODES.TIRAMISU)) {
                setPackageSource(PackageInstaller.PACKAGE_SOURCE_STORE)
            }
            if (Android.sdk(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)) {
                setRequestUpdateOwnership(true)
                setApplicationEnabledSettingPersistent()
            }
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

    private val sessionInstaller by lazy {
        context.packageManager.packageInstaller
    }

    private fun createInstallerIntent(packageName: String): Intent {
        return Intent(context, InstallerReceiver::class.java).apply {
            putExtra(PackageInstaller.EXTRA_PACKAGE_NAME, packageName)
        }
    }

    private fun createUninstallerIntent(): Intent {
        return Intent(context, InstallerReceiver::class.java).apply {
            putExtra(InstallerReceiver.KEY_ACTION, InstallerReceiver.ACTION_UNINSTALL)
        }
    }

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

            // Open session and write APK, then commit
            sessionInstaller.openSession(sessionId).use { session ->
                writeApkToSession(session, apkFile, packageName)
                commitSession(session, sessionId, packageName)
                cleanupApkIfNeeded(apkFile)
            }
        } catch (e: IllegalStateException) {
            if (sessionInstaller.mySessions.size >= 50 && attemptsRemaining > 1) {
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
        session: PackageInstaller.Session,
        apkFile: File,
        packageName: String
    ) {
        val buffer = bufferPool.acquire()
        try {
            // Write the APK to the session
            val writeMode = "package-${System.currentTimeMillis()}"
            session.openWrite(writeMode, 0, apkFile.length()).use { out ->
                apkFile.inputStream().use { input ->
                    copyWithProgress(input, out, apkFile.length(), packageName)
                }

                // Make sure the stream is flushed
                out.flush()
            }
        } finally {
            bufferPool.release(buffer)
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

                if (bytesWritten - lastEmittedBytes >= progressThreshold) {
                    val progress = bytesWritten.toFloat() / totalBytes
                    // non-blocking progress report
                    launch(Dispatchers.Default) {
                        installQueue.emitProgress(
                            InstallState.Installing(progress),
                            packageName
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
        session: PackageInstaller.Session,
        sessionId: Int,
        packageName: String
    ) {
        val intent = createInstallerIntent(packageName)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            sessionId,
            intent,
            flags,
        )

        installQueue.emitProgress(InstallState.Installing(0.98f), packageName)
        session.commit(pendingIntent.intentSender)
        Log.d(TAG, "Committed session $sessionId for $packageName")
    }

    private suspend fun handleInstallationException(e: Exception, packageName: String) {
        val errorMessage = when (e) {
            is SecurityException     -> {
                Log.w(TAG, "Security exception: ${e.message}")
                "Installation failed: Security exception. Session may be destroyed or sealed.\n${e.message}"
            }

            is FileNotFoundException -> {
                Log.w(TAG, "File not found: ${e.message}")
                "Installation failed: Cache file does not exist.\n${e.message}"
            }

            is IOException           -> {
                Log.w(TAG, "IO error: ${e.message}")
                "Installation failed: I/O error during installation.\n${e.message}"
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
            "Cleaning up stale sessions. Total sessions: ${sessionInstaller.mySessions.size}"
        )

        sessionInstaller.mySessions
            .filter { it.installerPackageName == context.packageName }
            .forEach { session ->
                try {
                    sessionInstaller.abandonSession(session.sessionId)
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
            sessionInstaller.abandonSession(sessionId)
            Log.d(TAG, "Abandoned session $sessionId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to abandon session $sessionId: ${e.message}")
        }
    }

    override suspend fun uninstall(packageName: String) {
        runCatching {
            val intent = createUninstallerIntent()
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                -1,
                intent,
                flags,
            )

            sessionInstaller.uninstall(packageName, pendingIntent.intentSender)
            Log.d(TAG, "Initiated uninstall for $packageName")
        }.onFailure { e ->
            Log.e(TAG, "Failed to uninstall $packageName: ${e.message}")
        }
    }
}