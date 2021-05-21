package com.saggitt.omega.search.providers

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.annotation.Keep
import androidx.core.content.res.ResourcesCompat
import com.android.launcher3.R
import com.android.launcher3.util.PackageManagerHelper
import com.saggitt.omega.search.SearchProvider

@Keep
class BaiduSearchProvider(context: Context) : SearchProvider(context) {
    override val name = context.getString(R.string.search_provider_baidu)
    override val supportsVoiceSearch = true
    override val supportsAssistant = false
    override val supportsFeed = true
    override val packageName: String
        get() = "com.baidu.searchbox"

    override val isAvailable: Boolean
        get() = PackageManagerHelper.isAppEnabled(context.packageManager, packageName, 0)

    override fun startSearch(callback: (intent: Intent) -> Unit) =
        callback(
            Intent(Intent.ACTION_ASSIST)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).setPackage(packageName)
        )

    override fun startVoiceSearch(callback: (intent: Intent) -> Unit) =
        callback(
            Intent(Intent.ACTION_SEARCH_LONG_PRESS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).setPackage(packageName)
        )

    override fun startFeed(callback: (intent: Intent) -> Unit) =
        callback(
            Intent("$packageName.action.HOME")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).setPackage(packageName)
        )

    override val icon: Drawable
        get() = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_baidu, null)!!

    override val voiceIcon: Drawable
        get() = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_qsb_mic, null)!!
            .mutate()
            .apply {
                setTint(Color.rgb(0x2d, 0x03, 0xe4))
            }
}
