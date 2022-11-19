package com.machiav3lli.fdroid.installer

import android.content.Context
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.entity.InstallerType

abstract class AppInstaller {
    abstract val defaultInstaller: BaseInstaller?

    companion object {
        @Volatile
        private var INSTANCE: AppInstaller? = null
        fun getInstance(context: Context?): AppInstaller? {
            if (INSTANCE == null) {
                synchronized(AppInstaller::class.java) {
                    context?.let {
                        INSTANCE = object : AppInstaller() {
                            override val defaultInstaller: BaseInstaller
                                get() {
                                    return when (Preferences[Preferences.Key.Installer].installer) {
                                        InstallerType.ROOT -> RootInstaller(it)
                                        InstallerType.LEGACY -> LegacyInstaller(it)
                                        else -> DefaultInstaller(it)
                                    }
                                }
                        }
                    }
                }
            }
            return INSTANCE
        }
    }
}
