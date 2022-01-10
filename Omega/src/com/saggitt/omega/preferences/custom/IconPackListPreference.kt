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
import com.saggitt.omega.iconpack.IconPackProvider
import com.saggitt.omega.preferences.OmegaPreferences

class IconPackListPreference(context: Context, attrs: AttributeSet? = null) :
        ListPreference(context, attrs), OmegaPreferences.OnPreferenceChangeListener {
    private val prefs = Utilities.getOmegaPrefs(context)
    val packs = IconPackProvider.INSTANCE.get(context).getIconPackList()

    init {
        entries = packs.map { it.name }.toTypedArray()
        entryValues = packs.map { it.packageName }.toTypedArray()
        updateSummary()
    }

    private fun updateSummary() {
        val current = packs.firstOrNull { it.packageName == prefs.iconPackPackage }
                ?: packs[0]
        summary = current.name
        icon = current.icon
    }

    override fun onAttached() {
        super.onAttached()
        Utilities.getOmegaPrefs(context).addOnPreferenceChangeListener("pref_icon_pack_package", this)
    }

    override fun onDetached() {
        super.onAttached()
        Utilities.getOmegaPrefs(context).removeOnPreferenceChangeListener("pref_icon_pack_package", this)
    }

    override fun onValueChanged(key: String, prefs: OmegaPreferences, force: Boolean) {
        updateSummary()
    }
}