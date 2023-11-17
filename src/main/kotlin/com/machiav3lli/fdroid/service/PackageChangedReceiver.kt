package com.machiav3lli.fdroid.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.machiav3lli.fdroid.MainApplication
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.entity.Order
import com.machiav3lli.fdroid.entity.Section
import com.machiav3lli.fdroid.utility.Utils.toInstalledItem
import com.machiav3lli.fdroid.utility.displayUpdatesNotification
import com.machiav3lli.fdroid.utility.extension.android.Android
import com.machiav3lli.fdroid.utility.getLaunchActivities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PackageChangedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val packageName =
            intent.data?.let { if (it.scheme == "package") it.schemeSpecificPart else null }
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
                        if (packageInfo != null) MainApplication.db.getInstalledDao().insertReplace(
                            packageInfo.toInstalledItem(launcherActivities)
                        )
                        else MainApplication.db.getInstalledDao().delete(packageName)

                        // Update updates notification
                        if (Preferences[Preferences.Key.UpdateNotify]) context.displayUpdatesNotification(
                            MainApplication.db.getProductDao()
                                .queryObject(
                                    installed = true,
                                    updates = true,
                                    section = Section.All,
                                    order = Order.NAME,
                                    ascending = true,
                                ).map { it.toItem() }
                        )
                    }
                }
            }
        }
    }
}