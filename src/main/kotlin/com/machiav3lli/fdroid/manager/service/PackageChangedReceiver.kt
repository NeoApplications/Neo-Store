package com.machiav3lli.fdroid.manager.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.machiav3lli.fdroid.NeoApp
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.data.repository.InstalledRepository
import com.machiav3lli.fdroid.manager.work.UpdatesNotificationManager
import com.machiav3lli.fdroid.utils.Utils.toInstalledItem
import com.machiav3lli.fdroid.utils.extension.android.Android
import com.machiav3lli.fdroid.utils.getLaunchActivities
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PackageChangedReceiver : BroadcastReceiver(), KoinComponent {
    private val installedRepo: InstalledRepository by inject()
    private val updatesManager: UpdatesNotificationManager by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val packageName =
            intent.data?.let { if (it.scheme == "package") it.schemeSpecificPart else null }

        if (packageName != null) {
            val pending = goAsync()
            val appScope = (context.applicationContext as NeoApp).applicationScope
            appScope.launch {
                try {
                    when (intent.action.orEmpty()) {
                        Intent.ACTION_PACKAGE_ADDED,
                        Intent.ACTION_PACKAGE_REMOVED,
                        Intent.ACTION_PACKAGE_REPLACED,
                            -> {
                            val packageInfo = try {
                                context.packageManager.getPackageInfo(
                                    packageName,
                                    Android.PackageManager.signaturesFlag
                                )
                            } catch (e: Exception) {
                                null
                            }
                            val launcherActivities =
                                context.packageManager.getLaunchActivities(packageName)
                            if (packageInfo != null) installedRepo.upsert(
                                packageInfo.toInstalledItem(launcherActivities)
                            )
                            else installedRepo.delete(packageName)

                            // Update updates notification
                            if (Preferences[Preferences.Key.UpdateNotify]) updatesManager.replaceUpdates(
                                *installedRepo.loadUpdatedProducts().map { it.toItem() }
                                    .toTypedArray()
                            )
                        }
                    }
                } finally {
                    pending.finish()
                }
            }
        }
    }
}