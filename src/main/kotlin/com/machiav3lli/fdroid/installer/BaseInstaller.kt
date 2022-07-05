package com.machiav3lli.fdroid.installer

import android.content.Context

abstract class BaseInstaller(val context: Context) : InstallationEvents {
    companion object {
        const val ROOT_INSTALL_PACKAGE = "cat %s | pm install -i %s --user %s -t -r -S %s"
        const val ROOT_INSTALL_PACKAGE_SESSION_CREATE = "pm install-create -i %s --user %s -r -S %s"
        const val ROOT_INSTALL_PACKAGE_SESSION_WRITE = "cat %s | pm install-write -S %s %s %s"
        const val ROOT_INSTALL_PACKAGE_SESSION_COMMIT = "pm install-commit %s"
        const val ROOT_UNINSTALL_PACKAGE = "pm uninstall --user %s %s"
        const val DELETE_PACKAGE = "%s rm %s"
    }
}