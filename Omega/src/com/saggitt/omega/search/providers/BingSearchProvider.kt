/*
 *  This file is part of Omega Launcher
 *  Copyright (c) 2021   Saul Henriquez
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saggitt.omega.search.providers

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.annotation.Keep
import androidx.core.content.res.ResourcesCompat
import com.android.launcher3.R
import com.saggitt.omega.search.SearchProvider
import com.saggitt.omega.util.isAppEnabled

@Keep
class BingSearchProvider(context: Context) : SearchProvider(context) {

    private val cortanaInstalled: Boolean
        get() = context.packageManager.isAppEnabled(PACKAGE_CORTANA, 0)
    private val alexaInstalled: Boolean
        get() = context.packageManager.isAppEnabled(PACKAGE_ALEXA, 0)

    override val name: String = context.getString(R.string.search_provider_bing)
    override val supportsVoiceSearch: Boolean
        get() = true
    override val supportsAssistant: Boolean
        get() = cortanaInstalled || alexaInstalled
    override val supportsFeed = false
    override val packageName: String
        get() = PACKAGE

    override val isAvailable: Boolean
        get() = context.packageManager.isAppEnabled(PACKAGE, 0)

    override fun startSearch(callback: (intent: Intent) -> Unit) = callback(
        Intent().setClassName(
            PACKAGE,
            "com.microsoft.sapphire.app.main.SplashActivity"
        ).setPackage(PACKAGE)
    )

    override fun startVoiceSearch(callback: (intent: Intent) -> Unit) =
        callback(
            Intent().setClassName(PACKAGE, "com.microsoft.clients.bing.voice.VoiceActivity")
                .setPackage(PACKAGE)
        )

    override fun startAssistant(callback: (intent: Intent) -> Unit) = callback(
        if (cortanaInstalled) {
            Intent().setClassName(
                PACKAGE_CORTANA,
                "com.microsoft.bing.dss.assist.AssistProxyActivity"
            ).setPackage(PACKAGE_CORTANA)
        } else {
            Intent(Intent.ACTION_ASSIST).setPackage(PACKAGE_ALEXA)
        }
    )

    override val iconRes: Int
        get() = R.drawable.ic_bing
    override val icon: Drawable
        get() = ResourcesCompat.getDrawable(context.resources, iconRes, null)!!

    override val voiceIcon: Drawable
        get() = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_mic, null)!!
            .mutate().apply { setTint(Color.rgb(0x00, 0x89, 0x7B)) }

    override val assistantIcon: Drawable?
        get() = ResourcesCompat.getDrawable(
            context.resources,
            if (cortanaInstalled) R.drawable.ic_cortana
            else R.drawable.ic_alexa, null
        )!!

    companion object {
        private const val PACKAGE = "com.microsoft.bing"
        private const val PACKAGE_CORTANA = "com.microsoft.cortana"
        private const val PACKAGE_ALEXA = "com.amazon.dee.app"
    }
}
