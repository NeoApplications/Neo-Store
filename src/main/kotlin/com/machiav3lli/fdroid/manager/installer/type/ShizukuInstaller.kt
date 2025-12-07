package com.machiav3lli.fdroid.manager.installer.type

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.IPackageInstaller
import android.content.pm.IPackageInstallerSession
import android.content.pm.IPackageManager
import android.content.pm.PackageInstaller
import android.content.pm.PackageInstallerHidden
import android.content.pm.PackageManagerHidden
import android.os.Build
import android.os.IBinder
import android.os.IInterface
import android.util.Log
import com.machiav3lli.fdroid.BUFFER_SIZE
import com.machiav3lli.fdroid.data.entity.InstallState
import com.machiav3lli.fdroid.manager.installer.InstallationError
import com.machiav3lli.fdroid.manager.service.InstallerReceiver
import com.machiav3lli.fdroid.utils.extension.android.Android
import dev.rikka.tools.refine.Refine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.files.FileNotFoundException
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class ShizukuInstaller(context: Context) : BaseInstaller(context) {

    companion object {
        const val TAG = "ShizukuInstaller"

        private val flags by lazy {
            when {
                Android.sdk(Build.VERSION_CODES.UPSIDE_DOWN_CAKE) ->
                    // For Android 14+, use FLAG_IMMUTABLE for implicit intents for security
                    PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT or
                            PendingIntent.FLAG_ALLOW_UNSAFE_IMPLICIT_INTENT


                Android.sdk(Build.VERSION_CODES.S)                ->
                    // For Android 12+, but below 14, can use FLAG_MUTABLE
                    PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT

                else                                              -> PendingIntent.FLAG_UPDATE_CURRENT
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

        private fun IBinder.shizukuWrapper() = ShizukuBinderWrapper(this)
        private fun IInterface.shizukuBinder() = ShizukuBinderWrapper(this.asBinder())
    }

    private fun createInstallerIntent(packageName: String): Intent {
        return Intent(context, InstallerReceiver::class.java).apply {
            putExtra(PackageInstaller.EXTRA_PACKAGE_NAME, packageName)
        }
    }

    private val iPackageManager: IPackageManager by lazy {
        IPackageManager.Stub.asInterface(
            SystemServiceHelper.getSystemService("package").shizukuWrapper()
        )
    }

    private val _packageInstaller: IPackageInstaller by lazy {
        IPackageInstaller.Stub.asInterface(iPackageManager.packageInstaller.shizukuBinder())
    }

    // TODO add pref for installerPackageName
    private val packageInstaller: PackageInstaller? by lazy {
        when {
            Android.sdk(Build.VERSION_CODES.S) -> {
                Refine.unsafeCast<PackageInstaller>(
                    PackageInstallerHidden(_packageInstaller, context.packageName, null, 0)
                )
            }

            Android.sdk(Build.VERSION_CODES.O) -> {
                Refine.unsafeCast<PackageInstaller>(
                    PackageInstallerHidden(_packageInstaller, context.packageName, 0)
                )
            }

            else                               -> null
        }
    }

    fun getSession(sessionId: Int): PackageInstaller.Session {
        val iSession = IPackageInstallerSession.Stub.asInterface(
            _packageInstaller.openSession(sessionId).shizukuBinder()
        )
        return Refine.unsafeCast(
            PackageInstallerHidden.SessionHidden(iSession)
        )
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

            // Create installation session
            val installer = packageInstaller
                ?: throw IllegalStateException("PackageInstaller not available")

            runCatching {
                val params = PackageInstaller.SessionParams(
                    PackageInstaller.SessionParams.MODE_FULL_INSTALL
                ).apply {
                    setAppPackageName(packageName)
                    Refine.unsafeCast<PackageInstallerHidden.SessionParamsHidden>(this).apply {
                        installFlags = installFlags or PackageManagerHidden.INSTALL_REPLACE_EXISTING
                    }
                }

                sessionId = installer.createSession(params)
                Log.d(TAG, "Created session $sessionId for $packageName")
            }.onFailure { e ->
                Log.e(TAG, "Failed to create session: ${e.message}")
                sessionId = -1
                throw e
            }

            getSession(sessionId).use { session ->
                writeApkToSession(session, apkFile, packageName)
                commitSession(session, sessionId, packageName)
                cleanupApkIfNeeded(apkFile)
            }
        } catch (e: IllegalStateException) {
            if ((packageInstaller?.mySessions?.size ?: 0) >= 50 && attemptsRemaining > 1) {
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
        // Write the APK to the session
        val writeMode = "$packageName-${System.currentTimeMillis()}"
        session.openWrite(writeMode, 0, apkFile.length()).use { out ->
            apkFile.inputStream().use { input ->
                copyWithProgress(input, out, apkFile.length(), packageName)
            }

            // Ensure the stream is flushed and synced
            out.flush()
            session.fsync(out)
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
                            // copying is considered 90% of the process
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
        val installer = packageInstaller ?: return

        Log.d(
            TAG,
            "Cleaning up stale sessions. Total sessions: ${installer.mySessions.size}"
        )

        installer.mySessions
            .filter { it.installerPackageName == context.packageName }
            .forEach { session ->
                try {
                    installer.abandonSession(session.sessionId)
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
            packageInstaller?.abandonSession(sessionId)
            Log.d(TAG, "Abandoned session $sessionId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to abandon session $sessionId: ${e.message}")
        }
    }

    // TODO add uninstall()
}