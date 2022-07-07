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
package com.saggitt.omega.gestures.ui

import android.content.Context
import android.util.AttributeSet
import androidx.preference.ListPreference
import com.saggitt.omega.gestures.BlankGestureHandler
import com.saggitt.omega.gestures.GestureController
import com.saggitt.omega.gestures.GestureHandler

class LauncherGesturePreference(context: Context, attrs: AttributeSet?) :
    ListPreference(context, attrs) {

    var defaultValue = BlankGestureHandler::class.java.name
    lateinit var onSelectHandler: (GestureHandler) -> Unit
    private val mContext = context
    private val blankGestureHandler = BlankGestureHandler(mContext, null)
    val handlers = GestureController.getGestureHandlers(context, isSwipeUp = false, hasBlank = true)
    private val handler
        get() = GestureController.createGestureHandler(
            mContext,
            value,
            blankGestureHandler
        )

    init {
        entries = handlers.map { it.displayName }.toTypedArray()
        entryValues = handlers.map { it.displayName }.toTypedArray()
        setOnPreferenceChangeListener { _, newValue ->
            onSelectHandler(handlers.find { it.displayName == newValue } ?: blankGestureHandler)
            summary = handler.displayName
            true
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onSetInitialValue(restorePersistedValue: Boolean, defaultValue: Any?) {
        value = if (restorePersistedValue) {
            getPersistedString(defaultValue as String?) ?: ""
        } else {
            defaultValue as String? ?: ""
        }
    }
}
