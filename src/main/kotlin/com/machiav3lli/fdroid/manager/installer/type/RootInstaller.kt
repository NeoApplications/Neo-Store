package com.machiav3lli.fdroid.manager.installer.type

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.machiav3lli.fdroid.BuildConfig
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.data.entity.InstallState
import com.machiav3lli.fdroid.manager.installer.InstallationError
import com.machiav3lli.fdroid.utils.Utils.quotePath
import com.machiav3lli.fdroid.utils.extension.android.Android
import com.machiav3lli.fdroid.utils.notifyFinishedInstall
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import java.io.File
import java.util.regex.Pattern

class RootInstaller(context: Context) : BaseInstaller(context) {

    companion object {
        private val getCurrentUserState: String =
            if (Android.sdk(Build.VERSION_CODES.N_MR1)) Shell.cmd("am get-current-user")
                .exec().out[0]
            else Shell.cmd("dumpsys activity | grep -E \"mUserLru\"")
                .exec().out[0].trim()
                .removePrefix("mUserLru: [").removeSuffix("]")

        private val String.quote
            get() = "\"${this.replace(Regex("""[\\$"`]""")) { c -> "\\${c.value}" }}\""

        private val getUtilBoxPath: String
            get() {
                listOf("toybox", "busybox").forEach {
                    val shellResult = Shell.cmd("which $it").exec()
                    if (shellResult.out.isNotEmpty()) {
                        val utilBoxPath = shellResult.out.joinToString("")
                        if (utilBoxPath.isNotEmpty()) return utilBoxPath.quote
                    }
                }
                return ""
            }

        fun File.legacyInstall() = listOfNotNull(
            "cat", quotePath(absolutePath),
            "|",
            "pm", "install",
            if (Preferences[Preferences.Key.RootAllowDowngrades]) "-d" else null,
            if (Preferences[Preferences.Key.RootAllowInstallingOldApps]) "--bypass-low-target-sdk-block" else null,
            "-i", BuildConfig.APPLICATION_ID,
            "--user", getCurrentUserState,
            if (Android.sdk(Build.VERSION_CODES.O)) "--install-reason ${PackageManager.INSTALL_REASON_USER}" else null,
            "-t",
            "-r",
            "-S", length(),
        ).joinToString(" ")

        fun File.sessionInstallCreate(): String = listOfNotNull(
            "pm", "install-create",
            if (Preferences[Preferences.Key.RootAllowDowngrades]) "-d" else null,
            if (Preferences[Preferences.Key.RootAllowInstallingOldApps]) "--bypass-low-target-sdk-block" else null,
            "-i", BuildConfig.APPLICATION_ID,
            "--user", getCurrentUserState,
            if (Android.sdk(Build.VERSION_CODES.O)) "--install-reason ${PackageManager.INSTALL_REASON_USER}" else null,
            "-t",
            "-r",
            "-S", length(),
        ).joinToString(" ")

        fun File.sessionInstallWrite(sessionId: Int) = listOfNotNull(
            "cat", quotePath(absolutePath),
            "|",
            "pm", "install-write",
            "-S", length(),
            sessionId,
            name,
        ).joinToString(" ")

        fun sessionInstallCommit(sessionId: Int) = listOfNotNull(
            "pm", "install-commit", sessionId
        ).joinToString(" ")

        val String.uninstall
            get() = listOfNotNull(
                "pm", "uninstall",
                "--user", getCurrentUserState,
                this
            ).joinToString(" ")

        fun File.deletePackage(): String = listOfNotNull(
            getUtilBoxPath,
            "rm",
            absolutePath.quote
        ).joinToString(" ")
    }

    override suspend fun installPackage(packageName: String, apkFile: File) {
        withContext(Dispatchers.IO) {
            try {
                if (Preferences[Preferences.Key.RootSessionInstaller]) {
                    Shell.cmd(apkFile.sessionInstallCreate())
                        .submit {
                            val sessionIdPattern = Pattern.compile("(\\d+)")
                            val sessionIdMatcher =
                                if (it.out.isNotEmpty()) sessionIdPattern.matcher(it.out[0])
                                else null
                            val found = sessionIdMatcher?.find() == true
                            installQueue.emitProgress(InstallState.Installing(0.15f), packageName)

                            if (found) {
                                val sessionId = sessionIdMatcher?.group(1)?.toInt() ?: -1
                                Shell.cmd(apkFile.sessionInstallWrite(sessionId))
                                    .submit {
                                        installQueue.emitProgress(
                                            InstallState.Installing(0.75f),
                                            packageName
                                        )
                                        Shell.cmd(sessionInstallCommit(sessionId))
                                            .submit { result ->
                                                installQueue.emitProgress(
                                                    InstallState.Installing(0.95f),
                                                    packageName
                                                )
                                                runBlocking {
                                                    if (result.isSuccess) reportSuccess(packageName)
                                                    else reportFailure(
                                                        InstallationError.Unknown(
                                                            result.err.joinToString("\n")
                                                        ),
                                                        packageName
                                                    )
                                                }
                                                if (result.isSuccess && Preferences[Preferences.Key.ReleasesCacheRetention] == 0)
                                                    Shell.cmd(apkFile.deletePackage()).submit()
                                                CoroutineScope(Dispatchers.Default).launch {
                                                    notifyFinishedInstall(context, packageName)
                                                }
                                            }
                                    }
                            }
                        }
                } else {
                    Shell.cmd(apkFile.legacyInstall())
                        .submit { result ->
                            if (result.isSuccess && Preferences[Preferences.Key.ReleasesCacheRetention] == 0)
                                Shell.cmd(apkFile.deletePackage()).submit { result ->
                                    installQueue.emitProgress(
                                        InstallState.Installing(0.95f),
                                        packageName
                                    )
                                    runBlocking {
                                        if (result.isSuccess) reportSuccess(packageName)
                                        else reportFailure(
                                            InstallationError.Unknown(result.err.joinToString("\n")),
                                            packageName
                                        )
                                    }
                                    CoroutineScope(Dispatchers.Default).launch {
                                        notifyFinishedInstall(context, packageName)
                                    }
                                }
                        }
                }
            } catch (e: IOException) {
                reportFailure(
                    InstallationError.Unknown("Installation failed: $e.message"),
                    packageName
                )
            }
        }
    }

    override suspend fun uninstall(packageName: String) {
        withContext(Dispatchers.IO) {
            Shell.cmd(packageName.uninstall).submit()
        }
    }
}