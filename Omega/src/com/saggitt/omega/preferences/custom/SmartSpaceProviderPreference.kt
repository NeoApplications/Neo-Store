/*
 * This file is part of Omega Launcher
 * Copyright (c) 2022   Omega Launcher Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saggitt.omega.preferences.custom

import android.content.Context
import android.util.AttributeSet
import androidx.preference.ListPreference
import com.android.launcher3.Utilities
import com.saggitt.omega.preferences.OmegaPreferences
import com.saggitt.omega.smartspace.BlankDataProvider
import com.saggitt.omega.smartspace.OmegaSmartSpaceController
import com.saggitt.omega.smartspace.SmartSpaceDataWidget
import com.saggitt.omega.smartspace.weather.FakeDataProvider
import com.saggitt.omega.smartspace.weather.PEWeatherDataProvider

class SmartSpaceProviderPreference(context: Context, attrs: AttributeSet?) :
    ListPreference(context, attrs), OmegaPreferences.OnPreferenceChangeListener {

    private val prefs = Utilities.getOmegaPrefs(context)
    private val mProviders = getWeatherProviders()
    init {

        entries =
            mProviders.map { OmegaSmartSpaceController.getDisplayName(context, it) }.toTypedArray()
        entryValues = mProviders.map { it }.toTypedArray()
    }

    private fun getWeatherProviders(): List<String> {
        val list = ArrayList<String>()
        list.add(BlankDataProvider::class.java.name)
        list.add(SmartSpaceDataWidget::class.java.name)
        if (PEWeatherDataProvider.isAvailable(context))
            list.add(PEWeatherDataProvider::class.java.name)
        if (prefs.showDebugInfo)
            list.add(FakeDataProvider::class.java.name)
        return list
    }

    override fun shouldDisableDependents(): Boolean {
        return super.shouldDisableDependents() || value == BlankDataProvider::class.java.name
    }

    override fun onValueChanged(key: String, prefs: OmegaPreferences, force: Boolean) {
        if (value != getPersistedValue()) {
            value = getPersistedValue()
        }
        notifyDependencyChange(shouldDisableDependents())
    }

    override fun onAttached() {
        super.onAttached()

        prefs.addOnPreferenceChangeListener(key, this)
    }

    override fun onDetached() {
        super.onDetached()

        prefs.removeOnPreferenceChangeListener(key, this)
    }

    override fun getPersistedString(defaultReturnValue: String?): String {
        return getPersistedValue()!!
    }

    private fun getPersistedValue() =
        prefs.sharedPrefs.getString(key, SmartSpaceDataWidget::class.java.name)

    override fun persistString(value: String?): Boolean {
        prefs.sharedPrefs.edit().putString(key, value ?: BlankDataProvider::class.java.name).apply()
        return true
    }
}