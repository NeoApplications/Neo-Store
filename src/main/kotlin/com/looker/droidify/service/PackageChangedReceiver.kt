package com.looker.droidify.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.looker.droidify.database.DatabaseX
import com.looker.droidify.utility.Utils.toInstalledItem
import com.looker.droidify.utility.extension.android.Android
import com.looker.droidify.utility.getLaunchActivities
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
                Intent.ACTION_PACKAGE_REPLACED
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
                        if (packageInfo != null) db.installedDao
                            .insertReplace(packageInfo.toInstalledItem(launcherActivities))
                        else db.installedDao.delete(packageName)
                    }
                }
            }
        }
    }
}