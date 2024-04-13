package com.machiav3lli.fdroid.installer

import android.content.Context
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.entity.InstallerType
import com.machiav3lli.fdroid.utility.amInstalled
import com.machiav3lli.fdroid.utility.getHasSystemInstallPermission
import com.machiav3lli.fdroid.utility.shellIsRoot

abstract class AppInstaller {
    abstract val defaultInstaller: BaseInstaller?

    companion object {
        @Volatile
        private var INSTANCE: AppInstaller? = null
        fun getInstance(context: Context?): AppInstaller? {
            return INSTANCE ?: synchronized(this) {
                context?.let { context ->
                    val instance = object : AppInstaller() {
                        override val defaultInstaller: BaseInstaller
                            get() {
                                val installer = Preferences[Preferences.Key.Installer].installer
                                return when {
                                    installer == InstallerType.SYSTEM && context.getHasSystemInstallPermission()
                                         -> SystemInstaller(context)

                                    installer == InstallerType.ROOT && shellIsRoot
                                         -> RootInstaller(context)

                                    installer == InstallerType.LEGACY
                                         -> LegacyInstaller(context)

                                    installer == InstallerType.AM && context.amInstalled
                                         -> AppManagerInstaller(context)

                                    else -> SessionInstaller(context)
                                }
                            }
                    }
                    INSTANCE = instance
                    instance
                }
            }
        }
    }
}
