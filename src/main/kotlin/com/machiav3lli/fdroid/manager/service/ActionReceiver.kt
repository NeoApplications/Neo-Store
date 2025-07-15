package com.machiav3lli.fdroid.manager.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.machiav3lli.fdroid.ARG_PACKAGE_NAME
import com.machiav3lli.fdroid.ARG_PACKAGE_NAMES
import com.machiav3lli.fdroid.ARG_REPOSITORY_ID
import com.machiav3lli.fdroid.ARG_REPOSITORY_IDS
import com.machiav3lli.fdroid.manager.installer.BaseInstaller
import com.machiav3lli.fdroid.manager.work.WorkerManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ActionReceiver : BroadcastReceiver(), KoinComponent {

    companion object {
        const val COMMAND_CANCEL_SYNC = "cancel_sync"
        const val COMMAND_CANCEL_SYNC_ALL = "cancel_sync_all"
        const val COMMAND_CANCEL_DOWNLOAD = "cancel_download"
        const val COMMAND_CANCEL_DOWNLOAD_ALL = "cancel_download_all"
        const val COMMAND_CANCEL_INSTALL = "cancel_install"
        const val COMMAND_BATCH_UPDATE = "batch_update"
    }

    private val receiveJob = Job()

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        val installer: BaseInstaller by inject()
        val wm: WorkerManager by inject()

        runBlocking(Dispatchers.IO + receiveJob) {
            when (intent.action) {
                COMMAND_CANCEL_DOWNLOAD     -> {
                    val packageName = intent.getStringExtra(ARG_PACKAGE_NAME)
                    wm.cancelDownload(packageName)
                }

                COMMAND_BATCH_UPDATE        -> {
                    val packageNames: Array<String> =
                        intent.getStringArrayExtra(ARG_PACKAGE_NAMES) ?: emptyArray()
                    val repoIds: Array<Long> =
                        intent.getLongArrayExtra(ARG_REPOSITORY_IDS)?.toTypedArray() ?: emptyArray()
                    wm.update(*packageNames.zip(repoIds).toTypedArray())
                }

                COMMAND_CANCEL_DOWNLOAD_ALL -> {
                    wm.cancelDownloadAll()
                }

                COMMAND_CANCEL_SYNC         -> {
                    val repoId = intent.getLongExtra(ARG_REPOSITORY_ID, -1)
                    wm.cancelSync(repoId)
                }

                COMMAND_CANCEL_SYNC_ALL     -> {
                    wm.cancelSyncAll()
                }

                COMMAND_CANCEL_INSTALL      -> {
                    intent.getStringExtra(ARG_PACKAGE_NAME)
                        ?.let { packageName ->
                            wm.cancelInstall(packageName)
                            installer.cancelInstall(packageName)
                        }
                }

                else                        -> {}
            }
        }
    }
}