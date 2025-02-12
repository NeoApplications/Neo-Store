package com.machiav3lli.fdroid.manager.installer

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.machiav3lli.fdroid.BuildConfig
import com.machiav3lli.fdroid.NeoApp
import com.machiav3lli.fdroid.data.content.Cache
import com.machiav3lli.fdroid.data.content.Cache.getPackageArchiveInfo
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.utils.Utils.quotePath
import com.machiav3lli.fdroid.utils.extension.android.Android
import com.machiav3lli.fdroid.utils.notifyFinishedInstall
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    override suspend fun install(
        packageLabel: String,
        cacheFileName: String,
        postInstall: () -> Unit
    ) {
        val cacheFile = Cache.getReleaseFile(context, cacheFileName)
        val packageInfo = context.getPackageArchiveInfo(cacheFile)
        val packageName = packageInfo?.packageName ?: "unknown-package"

        mRootInstaller(packageName, cacheFile, postInstall)
    }

    override suspend fun isInstalling(packageName: String): Boolean =
        NeoApp.enqueuedInstalls.contains(packageName)

    override suspend fun uninstall(packageName: String) = mRootUninstaller(packageName)

    private suspend fun mRootInstaller(
        packageName: String,
        cacheFile: File,
        postInstall: () -> Unit
    ) =
        withContext(Dispatchers.Default) {
            NeoApp.enqueuedInstalls.add(packageName)
            if (Preferences[Preferences.Key.RootSessionInstaller]) {
                Shell.cmd(cacheFile.sessionInstallCreate())
                    .submit {
                        val sessionIdPattern = Pattern.compile("(\\d+)")
                        val sessionIdMatcher =
                            if (it.out.isNotEmpty()) sessionIdPattern.matcher(it.out[0])
                            else null
                        val found = sessionIdMatcher?.find() ?: false

                        if (found) {
                            val sessionId = sessionIdMatcher?.group(1)?.toInt() ?: -1
                            Shell.cmd(cacheFile.sessionInstallWrite(sessionId))
                                .submit { result ->
                                    Shell.cmd(sessionInstallCommit(sessionId)).exec()
                                    if (result.isSuccess)
                                        Shell.cmd(cacheFile.deletePackage()).submit()
                                    CoroutineScope(Dispatchers.Default).launch {
                                        NeoApp.db.getInstallTaskDao().delete(packageName)
                                        NeoApp.enqueuedInstalls.remove(packageName)
                                        notifyFinishedInstall(context, packageName)
                                    }
                                    postInstall()
                                }
                        }
                    }
            } else {
                Shell.cmd(cacheFile.legacyInstall())
                    .submit { result ->
                        if (result.isSuccess && Preferences[Preferences.Key.ReleasesCacheRetention] == 0)
                            Shell.cmd(cacheFile.deletePackage()).submit()
                        CoroutineScope(Dispatchers.Default).launch {
                            NeoApp.db.getInstallTaskDao().delete(packageName)
                            NeoApp.enqueuedInstalls.remove(packageName)
                            notifyFinishedInstall(context, packageName)
                        }
                        postInstall()
                    }
            }
        }

    private suspend fun mRootUninstaller(packageName: String) = withContext(Dispatchers.Default) {
        Shell.cmd(packageName.uninstall).submit()
    }
}