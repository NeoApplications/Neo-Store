package com.looker.droidify.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.looker.droidify.database.DatabaseX
import com.looker.droidify.utility.Utils.toInstalledItem
import com.looker.droidify.utility.extension.android.Android
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PackageChangeReciever : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val db = DatabaseX.getInstance(context)
        val packageName =
            intent.data?.let { if (it.scheme == "package") it.schemeSpecificPart else null }
        if (packageName != null) {
            when (intent.action.orEmpty()) {
                Intent.ACTION_PACKAGE_ADDED,
                Intent.ACTION_PACKAGE_REMOVED,
                -> {
                    val packageInfo = try {
                        context.packageManager.getPackageInfo(
                            packageName,
                            Android.PackageManager.signaturesFlag
                        )
                    } catch (e: Exception) {
                        null
                    }
                    GlobalScope.launch(Dispatchers.IO) {
                        if (packageInfo != null) db.installedDao.put(packageInfo.toInstalledItem())
                        else db.installedDao.delete(packageName)
                    }
                }
            }
        }
    }
}