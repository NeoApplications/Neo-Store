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
import androidx.core.content.res.ResourcesCompat
import com.android.launcher3.R
import com.saggitt.omega.search.SearchProvider
import com.saggitt.omega.util.isAppEnabled

@Keep
class YandexSearchProvider(context: Context) : SearchProvider(context) {

    override val name = context.getString(R.string.search_provider_yandex)
    override val supportsVoiceSearch = true
    override val supportsAssistant = true
    override val supportsFeed = true
    override val packageName: String
        get() = "ru.yandex.searchplugin"
    override val isAvailable: Boolean
        get() = context.packageManager.isAppEnabled(packageName, 0)

    override fun startSearch(callback: (intent: Intent) -> Unit) =
            callback(Intent(Intent.ACTION_WEB_SEARCH).setPackage(packageName))

    override fun startVoiceSearch(callback: (intent: Intent) -> Unit) =
            callback(Intent(Intent.ACTION_ASSIST).setPackage(packageName))

    override fun startAssistant(callback: (intent: Intent) -> Unit) = startVoiceSearch(callback)
    override fun startFeed(callback: (intent: Intent) -> Unit) =
            callback(Intent().setClassName(packageName, "$packageName.MainActivity"))


    override val icon: Drawable
        get() = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_yandex, null)!!

    override val voiceIcon: Drawable
        get() = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_alisa_yandex, null)!!

    override val assistantIcon: Drawable
        get() = voiceIcon
}
