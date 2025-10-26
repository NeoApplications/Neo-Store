package com.machiav3lli.fdroid.manager.installer.type

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.IPackageInstaller
import android.content.pm.IPackageInstallerSession
import android.content.pm.IPackageManager
import android.content.pm.PackageInstaller
import android.content.pm.PackageInstallerHidden
import android.os.Build
import android.os.IBinder
import android.os.IInterface
import android.util.Log
import com.machiav3lli.fdroid.data.entity.InstallState
import com.machiav3lli.fdroid.manager.installer.InstallationError
import com.machiav3lli.fdroid.manager.service.InstallerReceiver
import com.machiav3lli.fdroid.utils.extension.android.Android
import dev.rikka.tools.refine.Refine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.io.files.FileNotFoundException
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper
import java.io.File
import java.io.IOException

class ShizukuInstaller(context: Context) : BaseInstaller(context) {

    companion object {
        const val TAG = "ShizukuInstaller"

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

        private fun IBinder.shizukuWrapper() = ShizukuBinderWrapper(this)
        private fun IInterface.shizukuBinder() = ShizukuBinderWrapper(this.asBinder())
    }

    private val intent by lazy {
        Intent(context, InstallerReceiver::class.java)
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
        withContext(Dispatchers.IO) {
            var sessionId = -1
            runCatching {
                // Check if APK file exists and is readable
                if (!apkFile.exists() || !apkFile.canRead()) {
                    throw FileNotFoundException("APK file does not exist or is not readable: ${apkFile.absolutePath}")
                }

                // Create installation session
                val installer = packageInstaller!!
                val params = PackageInstaller.SessionParams(
                    PackageInstaller.SessionParams.MODE_FULL_INSTALL
                ).apply {
                    setAppPackageName(packageName)
                }

                runCatching {
                    sessionId = installer.createSession(params)
                    Log.d(TAG, "Created session $sessionId for $packageName")
                }.onFailure { e ->
                    Log.e(TAG, "Failed to create session: ${e.message}")
                    sessionId = -1
                    throw e
                }

                var session: PackageInstaller.Session? = null
                runCatching {
                    session = getSession(sessionId)

                    // Write APK to session
                    session.openWrite(
                        "$packageName-${System.currentTimeMillis()}",
                        0,
                        apkFile.length()
                    )
                        .use { out ->
                            apkFile.inputStream().use { input ->
                                input.copyTo(out)
                            }
                            session.fsync(out)
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

                    installQueue.emitProgress(InstallState.Installing(0.98f), packageName)
                    session?.commit(pendingIntent.intentSender)
                }.onFailure { e ->
                    Log.e(SessionInstaller.Companion.TAG, "Error writing to session: ${e.message}")
                    try {
                        session?.abandon()
                        Log.d(
                            SessionInstaller.Companion.TAG,
                            "Abandoned session $sessionId after error"
                        )
                    } catch (abandonException: Exception) {
                        Log.e(
                            SessionInstaller.Companion.TAG,
                            "Failed to abandon session: ${abandonException.message}"
                        )
                    }
                    throw e
                }
            }.onFailure { e ->
                // Clean up session if there was an error
                if (sessionId != -1) {
                    runCatching {
                        packageInstaller?.abandonSession(sessionId)
                        Log.d(
                            SessionInstaller.Companion.TAG,
                            "Installation failed: Abandoned session $sessionId after exception"
                        )
                    }.onFailure { abandonException ->
                        Log.e(
                            SessionInstaller.Companion.TAG,
                            "Installation failed: Failed to abandon session after exception: ${abandonException.message}"
                        )
                    }
                }

                val errorMessage = when (e) {
                    is SecurityException     -> {
                        Log.w(
                            SessionInstaller.Companion.TAG,
                            "Installation failed: Attempted to use a destroyed or sealed session when installing.\n${e.message}"
                        )
                        "Installation failed: Attempted to use a destroyed or sealed session when installing.\n${e.message}"
                    }

                    is FileNotFoundException -> {
                        Log.w(
                            SessionInstaller.Companion.TAG,
                            "Installation failed: Cache file does not seem to exist.\n${e.message}"
                        )
                        "Installation failed: Cache file does not seem to exist.\n${e.message}"
                    }

                    is IOException           -> {
                        Log.w(
                            SessionInstaller.Companion.TAG,
                            "Installation failed: I/O error due to a bad pipe.\n${e.message}"
                        )
                        "Installation failed: I/O error due to a bad pipe.\n${e.message}"
                    }

                    is IllegalStateException -> {
                        Log.w(SessionInstaller.Companion.TAG, "Installation failed: ${e.message}")
                        if (packageInstaller != null && packageInstaller!!.mySessions.size >= 50) {
                            (packageInstaller?.mySessions ?: emptyList())
                                .filter { it.installerPackageName == context.packageName }
                                .forEach {
                                    try {
                                        packageInstaller?.abandonSession(it.sessionId)
                                    } catch (abandonException: Exception) {
                                        Log.e(
                                            SessionInstaller.Companion.TAG,
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
                            SessionInstaller.Companion.TAG,
                            "Unexpected error during installation: ${e.javaClass.simpleName} - ${e.message}"
                        )
                        "Installation failed: ${e.javaClass.simpleName}\n${e.message}"
                    }
                }

                reportFailure(InstallationError.Unknown(errorMessage), packageName)
            }
        }
    }

    // TODO add uninstall()
}