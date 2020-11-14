package com.saggitt.omega.search.providers

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.annotation.Keep
import com.android.launcher3.R
import com.saggitt.omega.OmegaLauncher
import com.saggitt.omega.search.SearchProvider
import com.saggitt.omega.util.Config


@Keep
class GoogleSearchProvider(context: Context) : SearchProvider(context) {

    override val name = context.getString(R.string.google_app)
    override val supportsVoiceSearch = true
    override val supportsAssistant = true
    override val supportsFeed = true
    override val settingsIntent: Intent
        get() = Intent("com.google.android.googlequicksearchbox.TEXT_ASSIST")
                .setPackage(Config.GOOGLE_QSB).addFlags(268435456)
    override val isBroadcast: Boolean
        get() = true


    override fun startSearch(callback: (intent: Intent) -> Unit) =
            callback(Intent().setClassName(Config.GOOGLE_QSB, Config.GOOGLE_QSB + "SearchActivity"))

    override fun startVoiceSearch(callback: (intent: Intent) -> Unit) =
            callback(Intent("android.intent.action.VOICE_ASSIST").setPackage(Config.GOOGLE_QSB))

    override fun startAssistant(callback: (intent: Intent) -> Unit) =
            callback(Intent(Intent.ACTION_VOICE_COMMAND).setPackage(Config.GOOGLE_QSB))

    override fun startFeed(callback: (intent: Intent) -> Unit) {
        val launcher = OmegaLauncher.getLauncher(context)
        if (launcher.googleNow != null) {
            //TODO OPEN GOOGLE FEED.
            launcher.googleNow!!.showOverlay(true)
        } else {
            callback(Intent(Intent.ACTION_MAIN).setClassName(Config.GOOGLE_QSB, Config.GOOGLE_QSB + ".SearchActivity"))
        }
    }

    override fun getIcon(): Drawable = context.getDrawable(R.drawable.ic_qsb_logo)!!

    override fun getVoiceIcon(): Drawable = context.getDrawable(R.drawable.ic_qsb_mic)!!

    override fun getAssistantIcon(): Drawable = context.getDrawable(R.drawable.ic_qsb_assist)!!
}
