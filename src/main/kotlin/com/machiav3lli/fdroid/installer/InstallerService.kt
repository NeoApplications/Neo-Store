package com.machiav3lli.fdroid.installer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.IBinder
import com.machiav3lli.fdroid.NOTIFICATION_CHANNEL_INSTALLER
import com.machiav3lli.fdroid.NeoActivity
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.utility.Utils
import com.machiav3lli.fdroid.utility.extension.android.Android
import com.machiav3lli.fdroid.utility.extension.android.notificationManager
import com.machiav3lli.fdroid.utility.notifyStatus

/**
 * Runs during or after a PackageInstaller session in order to handle completion, failure, or
 * interruptions requiring user intervention, such as the package installer prompt.
 */
class InstallerService : Service() {
    companion object {
        const val KEY_ACTION = "installerAction"
        const val KEY_APP_NAME = "appName"
        const val ACTION_UNINSTALL = "uninstall"
        const val INSTALLED_NOTIFICATION_TIMEOUT: Long = 5000
        const val NOTIFICATION_TAG_PREFIX = "install-"
    }

    override fun onCreate() {
        super.onCreate()

        if (Android.sdk(26)) {
            NotificationChannel(
                NOTIFICATION_CHANNEL_INSTALLER,
                getString(R.string.syncing), NotificationManager.IMPORTANCE_LOW
            )
                .let(notificationManager::createNotificationChannel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val status = intent?.getIntExtra(PackageInstaller.EXTRA_STATUS, -1)

        // only trigger a prompt if in foreground, otherwise make notification
        if (Utils.inForeground() && status == PackageInstaller.STATUS_PENDING_USER_ACTION) {
            // Triggers the installer prompt and "unknown apps" prompt if needed
            val promptIntent: Intent? = intent.getParcelableExtra(Intent.EXTRA_INTENT)

            promptIntent?.let {
                it.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
                it.putExtra(Intent.EXTRA_INSTALLER_PACKAGE_NAME, "com.android.vending")
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                startActivity(it)
            }
        } else {
            notifyStatus(intent)
        }

        stopSelf()
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        //super.onBind(intent)
        return null
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
    fun installIntent(intent: Intent): PendingIntent {
        // prepare prompt intent
        val promptIntent: Intent? = intent.getParcelableExtra(Intent.EXTRA_INTENT)
        val name = intent.getStringExtra(PackageInstaller.EXTRA_PACKAGE_NAME)

        return PendingIntent.getActivity(
            this,
            0,
            Intent(this, NeoActivity::class.java)
                .setAction(NeoActivity.ACTION_INSTALL)
                .setData(Uri.parse("package:$name"))
                .putExtra(Intent.EXTRA_INTENT, promptIntent)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}