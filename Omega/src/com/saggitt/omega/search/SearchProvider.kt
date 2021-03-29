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

package com.saggitt.omega.search

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import com.android.launcher3.R
import com.android.launcher3.graphics.ShadowDrawable
import com.saggitt.omega.settings.search.SettingsSearchActivity

abstract class SearchProvider(protected val context: Context) {
    abstract val name: String
    abstract val supportsVoiceSearch: Boolean
    abstract val supportsAssistant: Boolean
    abstract val supportsFeed: Boolean
    open val settingsIntent get() = Intent().setClass(context, SettingsSearchActivity::class.java)

    abstract val packageName: String

    /**
     * Whether the settings intent needs to be sent as broadcast
     */
    open val isBroadcast = false
    open val isAvailable: Boolean = true

    abstract fun startSearch(callback: (intent: Intent) -> Unit = {})
    open fun startVoiceSearch(callback: (intent: Intent) -> Unit = {}) {
        if (supportsVoiceSearch) throw RuntimeException("Voice search supported but not implemented")
    }
    open fun startAssistant(callback: (intent: Intent) -> Unit = {}) {
        if (supportsAssistant) throw RuntimeException("Assistant supported but not implemented")
    }

    open fun startFeed(callback: (intent: Intent) -> Unit = {}) {
        if (supportsFeed) throw RuntimeException("Feed supported but not implemented")
    }

    protected fun wrapInShadowDrawable(d: Drawable?): Drawable {
        return ShadowDrawable.wrap(context, d, R.color.qsb_icon_shadow_color,
                4f, R.color.qsb_dark_icon_tint).apply { applyTheme(context.theme) }
    }

    fun getIcon(colored: Boolean) = if (colored) getIcon() else getShadowIcon()

    abstract fun getIcon(): Drawable?
    open fun getShadowIcon(): Drawable? {
        return wrapInShadowDrawable(getIcon())
    }

    fun getVoiceIcon(colored: Boolean) = if (colored) getVoiceIcon() else getShadowVoiceIcon()

    open fun getVoiceIcon(): Drawable? = if (supportsVoiceSearch)
        throw RuntimeException("Voice search supported but not implemented")
    else null

    open fun getShadowVoiceIcon() = getVoiceIcon()?.let { wrapInShadowDrawable(it) }

    fun getAssistantIcon(colored: Boolean) = if (colored) getAssistantIcon() else getShadowAssistantIcon()

    open fun getAssistantIcon(): Drawable? = if (supportsAssistant)
        throw RuntimeException("Assistant supported but not implemented")
    else null
    open fun getShadowAssistantIcon() = getAssistantIcon()?.let { wrapInShadowDrawable(it) }

    override fun toString(): String = this::class.java.name
}
