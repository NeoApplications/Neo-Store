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
import com.saggitt.omega.search.SearchProviderController
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class SearchProviderPreference(context: Context, attrs: AttributeSet? = null) :
    ListPreference(context, attrs) {
    private val prefs = Utilities.getOmegaPrefs(context)
    private val mProviders = SearchProviderController.getSearchProviders(context)
    private val current
        get() = mProviders.firstOrNull { it::class.java.name == prefs.searchProvider }
            ?: mProviders[0]

    init {
        entries = mProviders.map { it.name }.toTypedArray()
        entryValues = mProviders.map { it::class.java.name }.toTypedArray()
        updateSummary()
    }

    private fun updateSummary() {
        summary = current.name
        icon = current.icon
    }

    override fun callChangeListener(newValue: Any?): Boolean {
        MainScope().launch { updateSummary() }
        return super.callChangeListener(newValue)
    }
}