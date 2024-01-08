package com.machiav3lli.fdroid.installer

import android.content.Context
import com.machiav3lli.fdroid.BuildConfig
import com.machiav3lli.fdroid.MainApplication
import com.machiav3lli.fdroid.content.Cache
import com.machiav3lli.fdroid.content.Cache.getPackageArchiveInfo
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.utility.Utils.quotePath
import com.machiav3lli.fdroid.utility.extension.android.Android
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.regex.Pattern

class RootInstaller(context: Context) : BaseInstaller(context) {

    companion object {
        private val getCurrentUserState: String =
            if (Android.sdk(25)) Shell.cmd("am get-current-user").exec().out[0]
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
            "-i", BuildConfig.APPLICATION_ID,
            "--user", getCurrentUserState,
            "-t",
            "-r",
            "-S", length(),
        ).joinToString(" ")

        fun File.sessionInstallCreate(): String = listOfNotNull(
            "pm", "install-create",
            "-i", BuildConfig.APPLICATION_ID,
            "--user", getCurrentUserState,
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

    override suspend fun install(packageLabel: String, cacheFileName: String) {
        val cacheFile = Cache.getReleaseFile(context, cacheFileName)
        val packageInfo = context.getPackageArchiveInfo(cacheFile)
        val packageName = packageInfo?.packageName ?: "unknown-package"

        mRootInstaller(packageName, cacheFile)
    }

    override suspend fun isInstalling(packageName: String): Boolean =
        MainApplication.enqueuedInstalls.contains(packageName)

    override suspend fun uninstall(packageName: String) = mRootUninstaller(packageName)

    private suspend fun mRootInstaller(packageName: String, cacheFile: File) =
        withContext(Dispatchers.Default) {
            MainApplication.enqueuedInstalls.add(packageName)
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
                                    if (result.isSuccess) Shell.cmd(cacheFile.deletePackage())
                                        .submit()
                                }
                        }
                    }

            } else {
                Shell.cmd(cacheFile.legacyInstall())
                    .submit {
                        if (it.isSuccess && Preferences[Preferences.Key.ReleasesCacheRetention] == 0)
                            Shell.cmd(cacheFile.deletePackage()).submit()
                    }
            }
            MainApplication.db.getInstallTaskDao().delete(packageName)
            MainApplication.enqueuedInstalls.remove(packageName)
        }

    private suspend fun mRootUninstaller(packageName: String) = withContext(Dispatchers.Default) {
        Shell.cmd(packageName.uninstall).submit()
    }
}