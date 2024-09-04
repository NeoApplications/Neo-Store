package com.machiav3lli.fdroid.service

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.net.Uri
import com.machiav3lli.fdroid.ARG_PACKAGE_NAME
import com.machiav3lli.fdroid.NeoActivity
import com.machiav3lli.fdroid.utility.Utils
import com.machiav3lli.fdroid.utility.notifyStatus

/**
 * Runs during or after a PackageInstaller session in order to handle completion, failure, or
 * interruptions requiring user intervention, such as the package installer prompt.
 */
/**
 * Runs during or after a PackageInstaller session in order to handle completion, failure, or
 * interruptions requiring user intervention, such as the package installer prompt.
 */
class InstallerReceiver : BroadcastReceiver() {
    companion object {
        const val KEY_ACTION = "installerAction"
        const val KEY_PACKAGE_LABEL = "packageLabel"
        const val ACTION_UNINSTALL = "uninstall"
        const val INSTALLED_NOTIFICATION_TIMEOUT: Long = 5000
        const val NOTIFICATION_TAG_PREFIX = "install-"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val status = intent?.getIntExtra(PackageInstaller.EXTRA_STATUS, -1)
        val sessionId = intent?.getIntExtra(PackageInstaller.EXTRA_SESSION_ID, -1) ?: 0

        // get package information from session
        val sessionInstaller = context.packageManager.packageInstaller
        val session = if (sessionId > 0) sessionInstaller.getSessionInfo(sessionId) else null

        val packageName =
            session?.appPackageName ?: intent?.getStringExtra(PackageInstaller.EXTRA_PACKAGE_NAME)

        // only trigger a prompt if in foreground, otherwise make notification
        if (Utils.inForeground() && status == PackageInstaller.STATUS_PENDING_USER_ACTION) {
            // Triggers the installer prompt and "unknown apps" prompt if needed
            val promptIntent: Intent? = intent.getParcelableExtra(Intent.EXTRA_INTENT)

            promptIntent?.let {
                it.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
                it.putExtra(Intent.EXTRA_INSTALLER_PACKAGE_NAME, "com.android.vending")
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                context.startActivity(it)
            }
        } else if (
            status == PackageInstaller.STATUS_FAILURE_ABORTED
            || status == PackageInstaller.STATUS_FAILURE_CONFLICT
            || status == PackageInstaller.STATUS_FAILURE_INCOMPATIBLE
            || status == PackageInstaller.STATUS_FAILURE_INVALID
            || status == PackageInstaller.STATUS_FAILURE_STORAGE
        ) {
            val cancelIntent = Intent(context, ActionReceiver::class.java).apply {
                this.action = ActionReceiver.COMMAND_CANCEL_INSTALL
                putExtra(ARG_PACKAGE_NAME, packageName)
            }
            context.sendBroadcast(cancelIntent)
        } else {
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