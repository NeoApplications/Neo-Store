/*
 *     This file is part of Lawnchair Launcher.
 *
 *     Lawnchair Launcher is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Lawnchair Launcher is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Lawnchair Launcher.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saggitt.omega.search.providers

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.annotation.Keep
import com.android.launcher3.R
import com.android.launcher3.util.PackageManagerHelper
import com.saggitt.omega.OmegaLauncher
import com.saggitt.omega.search.SearchProvider
import com.saggitt.omega.util.Config

@Keep
class GoogleSearchProvider(context: Context) : SearchProvider(context) {

    override val name = context.getString(R.string.google_app)
    override val supportsVoiceSearch = true
    override val supportsAssistant = true
    override val isAvailable: Boolean
        get() = PackageManagerHelper.isAppEnabled(context.packageManager, Config.GOOGLE_QSB, 0)
    override val supportsFeed = true
    override val settingsIntent: Intent
        get() = Intent("com.google.android.apps.gsa.nowoverlayservice.PIXEL_DOODLE_QSB_SETTINGS")
                .setPackage(Config.GOOGLE_QSB).addFlags(268435456)
    override val isBroadcast: Boolean
        get() = true

    override fun startSearch(callback: (intent: Intent) -> Unit) =
            callback(Intent().setClassName(Config.GOOGLE_QSB, "${Config.GOOGLE_QSB}.SearchActivity"))

    override fun startVoiceSearch(callback: (intent: Intent) -> Unit) =
            callback(Intent("android.intent.action.VOICE_ASSIST").setPackage(Config.GOOGLE_QSB))

    override fun startAssistant(callback: (intent: Intent) -> Unit) =
            callback(Intent(Intent.ACTION_VOICE_COMMAND).setPackage(Config.GOOGLE_QSB))

    override fun startFeed(callback: (intent: Intent) -> Unit) {
        val launcher = OmegaLauncher.getLauncher(context)
        if (launcher.googleNow != null) {
            launcher.googleNow?.showOverlay(true)
        } else {
            callback(Intent(Intent.ACTION_MAIN).setClassName(Config.GOOGLE_QSB, "${Config.GOOGLE_QSB}.SearchActivity"))
        }
    }

    override fun getIcon(): Drawable = context.getDrawable(R.drawable.ic_qsb_logo)!!

    override fun getVoiceIcon(): Drawable = context.getDrawable(R.drawable.ic_qsb_mic)!!

    override fun getAssistantIcon(): Drawable = context.getDrawable(R.drawable.ic_qsb_assist)!!
}
