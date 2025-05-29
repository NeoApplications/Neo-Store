package com.machiav3lli.fdroid.manager.service

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Build
import android.util.Log
import com.machiav3lli.fdroid.ARG_PACKAGE_NAME
import com.machiav3lli.fdroid.NeoActivity
import com.machiav3lli.fdroid.manager.installer.BaseInstaller
import com.machiav3lli.fdroid.manager.installer.BaseInstaller.Companion.translatePackageInstallerError
import com.machiav3lli.fdroid.utils.Utils
import com.machiav3lli.fdroid.utils.extension.android.Android
import com.machiav3lli.fdroid.utils.notifyStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Runs during or after a PackageInstaller session in order to handle completion, failure, or
 * interruptions requiring user intervention, such as the package installer prompt.
 */
/**
 * Runs during or after a PackageInstaller session in order to handle completion, failure, or
 * interruptions requiring user intervention, such as the package installer prompt.
 */
class InstallerReceiver : BroadcastReceiver(), KoinComponent {
    companion object {
        private const val TAG = "InstallerReceiver"
        const val KEY_ACTION = "installerAction"
        const val KEY_PACKAGE_LABEL = "packageLabel"
        const val ACTION_UNINSTALL = "uninstall"
        const val INSTALLED_NOTIFICATION_TIMEOUT: Long = 5000
        const val NOTIFICATION_TAG_PREFIX = "install-"
    }

    private val receiveJob = Job()

    override fun onReceive(context: Context, intent: Intent?) {
        val status = intent?.getIntExtra(PackageInstaller.EXTRA_STATUS, -1)
        val sessionId = intent?.getIntExtra(PackageInstaller.EXTRA_SESSION_ID, -1) ?: 0
        val installer: BaseInstaller by inject()

        // get package information from session
        val sessionInstaller = context.packageManager.packageInstaller
        val session = if (sessionId > 0) sessionInstaller.getSessionInfo(sessionId) else null

        val packageName =
            session?.appPackageName ?: intent?.getStringExtra(PackageInstaller.EXTRA_PACKAGE_NAME)

        Log.i(TAG, "Status: $status, Package: $packageName")
        // only trigger a prompt if in foreground, otherwise make notification
        runBlocking(Dispatchers.IO + receiveJob) {
            when (status) {
                PackageInstaller.STATUS_SUCCESS,
                    -> packageName?.let { installer.reportSuccess(it) }

                PackageInstaller.STATUS_PENDING_USER_ACTION,
                    -> {
                    val isNotInUserInteraction = !installer.isInUserInteraction(packageName) &&
                            !(Android.sdk(Build.VERSION_CODES.R) && session?.isStagedSessionActive == true)
                    if (Utils.inForeground() && isNotInUserInteraction) {
                        installer.reportUserInteraction(packageName)
                        // Triggers the installer prompt and "unknown apps" prompt if needed
                        val promptIntent: Intent? = intent.getParcelableExtra(Intent.EXTRA_INTENT)

                        promptIntent?.let {
                            it.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
                            it.putExtra(Intent.EXTRA_INSTALLER_PACKAGE_NAME, "com.android.vending")
                            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                            Log.i(TAG, "Initiating install dialog for Package: $packageName")
                            context.startActivity(it)
                        }
                    }
                }

                PackageInstaller.STATUS_FAILURE_ABORTED,
                PackageInstaller.STATUS_FAILURE_CONFLICT,
                PackageInstaller.STATUS_FAILURE_INCOMPATIBLE,
                PackageInstaller.STATUS_FAILURE_INVALID,
                PackageInstaller.STATUS_FAILURE_STORAGE,
                    -> {
                    val cancelIntent = Intent(context, ActionReceiver::class.java).apply {
                        this.action = ActionReceiver.COMMAND_CANCEL_INSTALL
                        putExtra(ARG_PACKAGE_NAME, packageName)
                    }
                    context.sendBroadcast(cancelIntent)
                    installer.reportFailure(translatePackageInstallerError(status))
                }
            }
            if (!(Utils.inForeground() && status == PackageInstaller.STATUS_PENDING_USER_ACTION))
                notifyStatus(context, intent)
        }
    }
}

/**
 * Generates an intent that provides the specified activity information necessary to trigger
 * the package manager's prompt, thus completing a staged installation requiring user
 * intervention.
 *
 * @param intent the intent provided by PackageInstaller to the callback target passed to
 * PackageInstaller.Session.commit().
 * @return a pending intent that can be attached to a background-accessible entry point such as
 * a notification
 */
fun installIntent(context: Context, intent: Intent): PendingIntent {
    // prepare prompt intent
    val promptIntent: Intent? = intent.getParcelableExtra(Intent.EXTRA_INTENT)
    val name = intent.getStringExtra(PackageInstaller.EXTRA_PACKAGE_NAME)
    val cacheFileName = intent.getStringExtra(NeoActivity.EXTRA_CACHE_FILE_NAME)

    return PendingIntent.getActivity(
        context,
        0,
        Intent(context, NeoActivity::class.java)
            .setAction(NeoActivity.ACTION_INSTALL)
            .setData(Uri.parse("package:$name"))
            .putExtra(Intent.EXTRA_INTENT, promptIntent)
            .putExtra(NeoActivity.EXTRA_CACHE_FILE_NAME, cacheFileName)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}