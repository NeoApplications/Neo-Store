package com.machiav3lli.fdroid.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.machiav3lli.fdroid.ARG_PACKAGE_NAME
import com.machiav3lli.fdroid.ARG_REPOSITORY_ID
import com.machiav3lli.fdroid.MainApplication

class ActionReceiver : BroadcastReceiver() {

    companion object {
        const val COMMAND_CANCEL_SYNC = "cancel_sync"
        const val COMMAND_CANCEL_SYNC_ALL = "cancel_sync_all"
        const val COMMAND_CANCEL_DOWNLOAD = "cancel_download"
        const val COMMAND_CANCEL_DOWNLOAD_ALL = "cancel_download_all"
        const val COMMAND_CANCEL_INSTALL = "cancel_install"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        when (intent.action) {
            COMMAND_CANCEL_DOWNLOAD     -> {
                val packageName = intent.getStringExtra(ARG_PACKAGE_NAME)
                MainApplication.wm.cancelDownload(packageName)
            }

            COMMAND_CANCEL_DOWNLOAD_ALL -> {
                MainApplication.wm.cancelDownloadAll()
            }

            COMMAND_CANCEL_SYNC         -> {
                val repoId = intent.getLongExtra(ARG_REPOSITORY_ID, -1)
                MainApplication.wm.cancelSync(repoId)
            }

            COMMAND_CANCEL_SYNC_ALL     -> {
                MainApplication.wm.cancelSyncAll()
            }

            COMMAND_CANCEL_INSTALL      -> {
                intent.getStringExtra(ARG_PACKAGE_NAME)
                    ?.let { packageName -> MainApplication.wm.cancelInstall(packageName) }
            }

            else                        -> {}
        }
    }
}