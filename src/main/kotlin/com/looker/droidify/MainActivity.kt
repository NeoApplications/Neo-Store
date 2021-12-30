package com.looker.droidify

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.net.Uri
import com.looker.droidify.ContextWrapperX.Companion.wrap
import com.looker.droidify.installer.InstallerService
import com.looker.droidify.screen.ScreenActivity
import com.looker.droidify.utility.extension.android.Android
import kotlinx.coroutines.withContext

class MainActivity : ScreenActivity() {
    companion object {
        const val ACTION_UPDATES = "${BuildConfig.APPLICATION_ID}.intent.action.UPDATES"
        const val ACTION_INSTALL = "${BuildConfig.APPLICATION_ID}.intent.action.INSTALL"
        const val EXTRA_CACHE_FILE_NAME =
            "${BuildConfig.APPLICATION_ID}.intent.extra.CACHE_FILE_NAME"
    }

    override fun handleIntent(intent: Intent?) {
        when (intent?.action) {
            ACTION_UPDATES -> handleSpecialIntent(SpecialIntent.Updates)
            ACTION_INSTALL -> {
                // continue install prompt
                val promptIntent: Intent? = intent.getParcelableExtra(Intent.EXTRA_INTENT)

                promptIntent?.let {
                    it.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
                    it.putExtra(Intent.EXTRA_INSTALLER_PACKAGE_NAME, BuildConfig.APPLICATION_ID)
                    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK )

                    startActivity(it)
                }

                // TODO: send this back to the InstallerService to free up the UI
                // prepare prompt intent
//                val name = intent.getStringExtra(PackageInstaller.EXTRA_PACKAGE_NAME)
//
//                val pending = PendingIntent.getService(
//                    this,
//                    0,
//                    Intent(this, InstallerService::class.java)
//                        .setData(Uri.parse("package:$name"))
//                        .putExtra(Intent.EXTRA_INTENT, promptIntent)
//                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
//                    if (Android.sdk(23)) PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//                    else PendingIntent.FLAG_UPDATE_CURRENT
//                )
//
//                pending.send()
            }
            else -> super.handleIntent(intent)
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(wrap(newBase))
    }
}
