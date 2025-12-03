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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import java.io.File
import java.util.regex.Pattern

class RootInstaller(context: Context) : BaseInstaller(context) {

    companion object {
        private val sessionIdPattern = Pattern.compile("(\\d+)")

        private fun String.quote() =
            "\"${this.replace(Regex("""[\\$"`]""")) { c -> "\\${c.value}" }}\""

        private val getCurrentUserState: String by lazy {
            if (Android.sdk(Build.VERSION_CODES.N_MR1)) Shell.cmd("am get-current-user")
                .exec().out[0]
            else Shell.cmd("dumpsys activity | grep -E \"mUserLru\"")
                .exec().out[0].trim()
                .removePrefix("mUserLru: [").removeSuffix("]")
        }

        private val getUtilBoxPath: String by lazy {
            listOf("toybox", "busybox").forEach {
                val shellResult = Shell.cmd("which $it").exec()
                if (shellResult.out.isNotEmpty()) {
                    val utilBoxPath = shellResult.out.joinToString("")
                    if (utilBoxPath.isNotEmpty()) utilBoxPath.quote()
                }
            }
            ""
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
            absolutePath.quote()
        ).joinToString(" ")
    }

    override suspend fun installPackage(packageName: String, apkFile: File) {
        try {
            if (Preferences[Preferences.Key.RootSessionInstaller])
                installWithSession(packageName, apkFile)
            else installLegacy(packageName, apkFile)
        } catch (e: IOException) {
            reportFailure(
                InstallationError.Unknown("Installation failed: $e.message"),
                packageName
            )
        }
    }

    private suspend fun installWithSession(packageName: String, apkFile: File) {
        val createResult = Shell.cmd(apkFile.sessionInstallCreate()).exec()
        val sessionIdMatcher = if (createResult.out.isEmpty()) null
        else sessionIdPattern.matcher(createResult.out[0])
        val found = sessionIdMatcher?.find() == true
        installQueue.emitProgress(InstallState.Installing(0.15f), packageName)

        if (found) {
            val sessionId = sessionIdMatcher?.group(1)?.toInt() ?: -1
            val writeResult = Shell.cmd(apkFile.sessionInstallWrite(sessionId)).exec()
            installQueue.emitProgress(
                InstallState.Installing(0.75f),
                packageName
            )
            val commitResult = Shell.cmd(sessionInstallCommit(sessionId)).exec()
            installQueue.emitProgress(
                InstallState.Installing(0.95f),
                packageName
            )

            if (commitResult.isSuccess) {
                reportSuccess(packageName)
                if (Preferences[Preferences.Key.ReleasesCacheRetention] == 0)
                    Shell.cmd(apkFile.deletePackage()).submit()
            } else reportFailure(
                InstallationError.Unknown(commitResult.err.joinToString("\n")),
                packageName
            )
            withContext(Dispatchers.Default) {
                notifyFinishedInstall(context, packageName)
            }
        }

    }

    private suspend fun installLegacy(packageName: String, apkFile: File) {
        val installResult = Shell.cmd(apkFile.legacyInstall()).exec()
        installQueue.emitProgress(
            InstallState.Installing(0.95f),
            packageName
        )

        if (installResult.isSuccess) {
            reportSuccess(packageName)
            if (Preferences[Preferences.Key.ReleasesCacheRetention] == 0) {
                Shell.cmd(apkFile.deletePackage()).exec()
            }
        } else reportFailure(
            InstallationError.Unknown(installResult.err.joinToString("\n")),
            packageName
        )
        withContext(Dispatchers.Default) {
            notifyFinishedInstall(context, packageName)
        }
    }

    override suspend fun uninstall(packageName: String) {
        Shell.cmd(packageName.uninstall).submit()
    }
}