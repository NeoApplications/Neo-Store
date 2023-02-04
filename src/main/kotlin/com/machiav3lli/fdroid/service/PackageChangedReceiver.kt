package com.machiav3lli.fdroid.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.database.DatabaseX
import com.machiav3lli.fdroid.entity.Order
import com.machiav3lli.fdroid.entity.Section
import com.machiav3lli.fdroid.utility.Utils.toInstalledItem
import com.machiav3lli.fdroid.utility.displayUpdatesNotification
import com.machiav3lli.fdroid.utility.extension.android.Android
import com.machiav3lli.fdroid.utility.getLaunchActivities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PackageChangedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val db = DatabaseX.getInstance(context)
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
                    GlobalScope.launch(Dispatchers.IO) {
                        if (packageInfo != null) db.installedDao.insertReplace(
                            packageInfo.toInstalledItem(launcherActivities)
                        )
                        else db.installedDao.delete(packageName)

                        // Update updates notification
                        if (Preferences[Preferences.Key.UpdateNotify]) context.displayUpdatesNotification(
                            db.productDao
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