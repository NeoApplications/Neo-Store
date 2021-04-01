/*
 *  This file is part of Omega Launcher
 *  Copyright (c) 2021   Saul Henriquez
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saggitt.omega.search

import android.content.Context
import android.util.AttributeSet
import androidx.preference.DialogPreference
import com.android.launcher3.R
import com.saggitt.omega.OmegaPreferences
import com.saggitt.omega.util.omegaPrefs

class SearchProviderPreference(context: Context, attrs: AttributeSet? = null) :
        DialogPreference(context, attrs), OmegaPreferences.OnPreferenceChangeListener {
    private val prefs = context.omegaPrefs
    private val mProviders = SearchProviderController.getSearchProviders(context)
    private val current
        get() = mProviders.firstOrNull { it::class.java.name == prefs.searchProvider }
                ?: mProviders[0]

    init {
        layoutResource = R.layout.preference_preview_icon
        dialogLayoutResource = R.layout.pref_dialog_icon_pack
    }

    override fun onAttached() {
        super.onAttached()
        context.omegaPrefs.addOnPreferenceChangeListener("pref_globalSearchProvider", this)
    }

    override fun onDetached() {
        super.onDetached()
        context.omegaPrefs.removeOnPreferenceChangeListener("pref_globalSearchProvider", this)
    }

    override fun onValueChanged(key: String, prefs: OmegaPreferences, force: Boolean) {
        updateSummaryAndIcon()
    }

    private fun updateSummaryAndIcon() {
        icon = current.getIcon()
        summary = current.name
    }
}