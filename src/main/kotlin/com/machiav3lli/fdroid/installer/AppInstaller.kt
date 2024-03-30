package com.machiav3lli.fdroid.installer

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.entity.InstallerType
import com.machiav3lli.fdroid.utility.amInstalled
import com.machiav3lli.fdroid.utility.shellIsRoot

abstract class AppInstaller {
    abstract val defaultInstaller: BaseInstaller?

    companion object {
        @Volatile
        private var INSTANCE: AppInstaller? = null
        fun getInstance(context: Context?): AppInstaller? {
            val hasSystemInstallPermission = context?.packageManager?.checkPermission(
                Manifest.permission.INSTALL_PACKAGES,
                context.packageName) == PackageManager.PERMISSION_GRANTED;
            return INSTANCE ?: synchronized(this) {
                context?.let { context ->
                    val instance = object : AppInstaller() {
                        override val defaultInstaller: BaseInstaller
                            get() {
                                val installer = Preferences[Preferences.Key.Installer].installer
                                return when {
                                    hasSystemInstallPermission ->
                                        SystemInstaller(context)

                                    installer == InstallerType.ROOT && shellIsRoot       ->
                                        RootInstaller(context)

                                    installer == InstallerType.LEGACY                    ->
                                        LegacyInstaller(context)

                                    installer == InstallerType.AM && context.amInstalled ->
                                        AppManagerInstaller(context)

                                    else                                                 ->
                                        SessionInstaller(context)
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
