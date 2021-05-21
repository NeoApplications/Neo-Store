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

package com.saggitt.omega.preferences

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Switch
import androidx.preference.PreferenceViewHolder
import androidx.preference.SwitchPreference
import com.android.launcher3.Utilities
import com.saggitt.omega.OmegaPreferences
import com.saggitt.omega.settings.search.SearchIndex
import com.saggitt.omega.util.applyColor
import com.saggitt.omega.util.omegaPrefs

open class StyledSwitchPreferenceCompat(context: Context, attrs: AttributeSet? = null) :
    SwitchPreference(context, attrs),
    ControlledPreference by ControlledPreference.Delegate(context, attrs) {

    protected var checkableView: View? = null
        private set

    override fun onBindViewHolder(holder: PreferenceViewHolder?) {
        super.onBindViewHolder(holder)
        checkableView = holder?.findViewById(android.R.id.switch_widget)
        (checkableView as Switch).applyColor(Utilities.getOmegaPrefs(context).accentColor)
    }


    open class SwitchSlice(context: Context, attrs: AttributeSet) :
        SearchIndex.Slice(context, attrs) {

        private val defaultValue: Boolean

        init {
            val ta = context.obtainStyledAttributes(attrs, intArrayOf(android.R.attr.defaultValue))
            defaultValue = ta.getBoolean(0, false)
            ta.recycle()
        }

        override fun createSliceView(): View {
            return SwitchSliceView(context, key, defaultValue)
        }
    }

    class SwitchSliceView(context: Context, private val key: String, private val defaultValue: Boolean) :
        Switch(context), OmegaPreferences.OnPreferenceChangeListener {

        init {
            applyColor(context.omegaPrefs.accentColor)
            setOnCheckedChangeListener { _, isChecked ->
                context.omegaPrefs.sharedPrefs.edit().putBoolean(key, isChecked).apply()
            }
        }

        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            context.omegaPrefs.addOnPreferenceChangeListener(key, this)
        }

        override fun onDetachedFromWindow() {
            super.onDetachedFromWindow()
            context.omegaPrefs.removeOnPreferenceChangeListener(key, this)
        }

        override fun onValueChanged(key: String, prefs: OmegaPreferences, force: Boolean) {
            isChecked = prefs.sharedPrefs.getBoolean(key, defaultValue)
        }
    }
}