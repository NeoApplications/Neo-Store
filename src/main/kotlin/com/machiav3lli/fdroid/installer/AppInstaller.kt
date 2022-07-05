package com.machiav3lli.fdroid.installer

import android.content.Context
import com.machiav3lli.fdroid.utility.Utils.rootInstallerEnabled

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
                                    return when (rootInstallerEnabled) {
                                        false -> DefaultInstaller(it)
                                        true -> RootInstaller(it)
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
