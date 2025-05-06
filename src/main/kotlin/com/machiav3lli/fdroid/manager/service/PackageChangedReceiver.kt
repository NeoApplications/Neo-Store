package com.machiav3lli.fdroid.manager.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.machiav3lli.fdroid.ContextWrapperX
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.data.repository.InstalledRepository
import com.machiav3lli.fdroid.utils.Utils.toInstalledItem
import com.machiav3lli.fdroid.utils.displayUpdatesNotification
import com.machiav3lli.fdroid.utils.extension.android.Android
import com.machiav3lli.fdroid.utils.getLaunchActivities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PackageChangedReceiver : BroadcastReceiver(), KoinComponent {
    private val installedRepo: InstalledRepository by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val packageName =
            intent.data?.let { if (it.scheme == "package") it.schemeSpecificPart else null }
        val langContext = ContextWrapperX.wrap(context)
        if (packageName != null) {
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
                    val launcherActivities = context.packageManager.getLaunchActivities(packageName)
                    CoroutineScope(Dispatchers.IO).launch {
                        if (packageInfo != null) installedRepo.upsert(
                            packageInfo.toInstalledItem(launcherActivities)
                        )
                        else installedRepo.delete(packageName)

                        // Update updates notification
                        if (Preferences[Preferences.Key.UpdateNotify]) langContext.displayUpdatesNotification(
                            installedRepo.loadInstalledProducts().map { it.toItem() }
                        )
                    }
                }
            }
        }
    }
}