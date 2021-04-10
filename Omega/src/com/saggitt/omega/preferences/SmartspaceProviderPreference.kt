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
import androidx.preference.ListPreference
import com.android.launcher3.Utilities
import com.saggitt.omega.OmegaPreferences
import com.saggitt.omega.smartspace.BlankDataProvider
import com.saggitt.omega.smartspace.OmegaSmartspaceController
import com.saggitt.omega.smartspace.SmartspaceDataWidget
import com.saggitt.omega.smartspace.weather.FakeDataProvider
import com.saggitt.omega.smartspace.weather.OnePlusWeatherDataProvider
import com.saggitt.omega.smartspace.weather.PEWeatherDataProvider
import com.saggitt.omega.util.buildEntries

class SmartspaceProviderPreference(context: Context, attrs: AttributeSet?)
    : ListPreference(context, attrs), OmegaPreferences.OnPreferenceChangeListener {

    private val prefs = Utilities.getOmegaPrefs(context)
    private val forWeather by lazy { key == "pref_smartspace_widget_provider" }

    init {
        buildEntries {
            getProviders().forEach {
                addEntry(OmegaSmartspaceController.getDisplayName(it), it)
            }
        }
    }

    override fun onSetInitialValue(restoreValue: Boolean, defaultValue: Any?) {
        super.onSetInitialValue(true, defaultValue)
    }

    private fun getProviders(): List<String> {
        return if (forWeather) getWeatherProviders() else SmartspaceEventProvidersAdapter.getEventProviders(context)
    }

    private fun getWeatherProviders(): List<String> {
        val list = ArrayList<String>()
        list.add(BlankDataProvider::class.java.name)
        list.add(SmartspaceDataWidget::class.java.name)
        //if (FeedBridge.getInstance(context).resolveBridge()?.supportsSmartspace == true)
        //    list.add(SmartspacePixelBridge::class.java.name)
        if (PEWeatherDataProvider.isAvailable(context))
            list.add(PEWeatherDataProvider::class.java.name)
        if (OnePlusWeatherDataProvider.isAvailable(context))
            list.add(OnePlusWeatherDataProvider::class.java.name)
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

    private fun getPersistedValue() = prefs.sharedPrefs.getString(key, SmartspaceDataWidget::class.java.name)

    override fun persistString(value: String?): Boolean {
        prefs.sharedPrefs.edit().putString(key, value ?: BlankDataProvider::class.java.name).apply()
        return true
    }
}