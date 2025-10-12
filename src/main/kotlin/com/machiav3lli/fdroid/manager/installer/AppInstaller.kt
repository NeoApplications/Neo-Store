package com.machiav3lli.fdroid.manager.installer

import android.content.Context
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.data.entity.InstallerType
import com.machiav3lli.fdroid.manager.installer.type.AppManagerInstaller
import com.machiav3lli.fdroid.manager.installer.type.BaseInstaller
import com.machiav3lli.fdroid.manager.installer.type.LegacyInstaller
import com.machiav3lli.fdroid.manager.installer.type.RootInstaller
import com.machiav3lli.fdroid.manager.installer.type.SessionInstaller
import com.machiav3lli.fdroid.manager.installer.type.SystemInstaller
import com.machiav3lli.fdroid.utils.amInstalled
import com.machiav3lli.fdroid.utils.getHasSystemInstallPermission
import com.machiav3lli.fdroid.utils.shellIsRoot
import org.koin.dsl.module

/**
 * Factory class for creating the appropriate installer based on user preferences
 * and device capabilities.
 */
object AppInstaller {
    fun create(context: Context): BaseInstaller {
        val preferredInstaller = Preferences[Preferences.Key.Installer].installer

        return when (preferredInstaller) {
            InstallerType.SYSTEM
                 -> if (context.getHasSystemInstallPermission()) SystemInstaller(context)
            // Fall back to SESSION if NS doesn't have system permission
            else SessionInstaller(context)

            InstallerType.ROOT
                 -> if (shellIsRoot) RootInstaller(context)
            // Fall back to SESSION if root is not available
            else SessionInstaller(context)

            InstallerType.LEGACY
                 -> LegacyInstaller(context)

            InstallerType.AM
                 -> if (context.amInstalled) AppManagerInstaller(context)
            // Fall back to SESSION if AM is not installed
            else SessionInstaller(context)

            else ->
                // Default to SESSION installer
                SessionInstaller(context)
        }
    }
}


val installerModule = module {
    factory { AppInstaller.create(get()) }
    single { InstallQueue() }
}
