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

abstract class SearchProvider(protected val context: Context) {
    abstract val name: String
    abstract val supportsVoiceSearch: Boolean
    abstract val supportsAssistant: Boolean
    abstract val supportsFeed: Boolean

    abstract val packageName: String
    abstract val icon: Drawable
    open val voiceIcon: Drawable?
        get() = if (supportsVoiceSearch)
            throw RuntimeException("Voice search supported but not implemented")
        else null
    open val assistantIcon: Drawable?
        get() = if (supportsVoiceSearch)
            throw RuntimeException("Assistant supported but not implemented")
        else null

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

    fun getIcon(colored: Boolean) = icon // maybe consider coloring

    fun getVoiceIcon(colored: Boolean) = voiceIcon // maybe consider coloring

    fun getAssistantIcon(colored: Boolean) = assistantIcon // maybe consider coloring

    override fun toString(): String = this::class.java.name
}
