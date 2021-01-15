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
import com.saggitt.omega.search.SearchProvider

@Keep
class GoogleGoSearchProvider(context: Context) : SearchProvider(context) {

    override val name = context.getString(R.string.search_provider_google_go)
    override val supportsVoiceSearch = true
    override val supportsAssistant = false
    override val supportsFeed = true
    override val isAvailable: Boolean
        get() = PackageManagerHelper.isAppEnabled(context.packageManager, PACKAGE, 0)

    override fun startSearch(callback: (intent: Intent) -> Unit) = callback(Intent("$PACKAGE.SEARCH").putExtra("showKeyboard", true).putExtra("$PACKAGE.SKIP_BYPASS_AND_ONBOARDING", true).setPackage(PACKAGE))
    override fun startVoiceSearch(callback: (intent: Intent) -> Unit) = callback(Intent("$PACKAGE.SEARCH").putExtra("openMic", true).putExtra("$PACKAGE.SKIP_BYPASS_AND_ONBOARDING", true).setPackage(PACKAGE))
    override fun startFeed(callback: (intent: Intent) -> Unit) = callback(Intent("$PACKAGE.SEARCH").putExtra("$PACKAGE.SKIP_BYPASS_AND_ONBOARDING", true).setPackage(PACKAGE))

    override fun getIcon(): Drawable = context.getDrawable(R.drawable.ic_qsb_logo)!!

    override fun getVoiceIcon(): Drawable = context.getDrawable(R.drawable.ic_qsb_mic)!!

    companion object {
        private const val PACKAGE = "com.google.android.apps.searchlite"
    }
}