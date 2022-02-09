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
import android.text.TextUtils
import android.util.AttributeSet
import android.widget.TextView
import androidx.preference.DialogPreference
import androidx.preference.PreferenceViewHolder
import com.android.launcher3.R
import com.android.launcher3.R.string
import com.saggitt.omega.omegaApp
import com.saggitt.omega.preferences.OmegaPreferences
import com.saggitt.omega.smartspace.OmegaSmartSpaceController
import com.saggitt.omega.util.omegaPrefs
import com.saggitt.omega.util.runOnMainThread

class EventProvidersPreference(context: Context, attrs: AttributeSet?) :
    DialogPreference(context, attrs),
    OmegaPreferences.MutableListPrefChangeListener {

    private val providersPref = context.omegaPrefs.eventProviders

    init {
        updateSummary()
    }

    fun setProviders(providers: List<String>) {
        context.omegaPrefs.eventProviders.setAll(providers)
        context.omegaApp.smartspace.onProviderChanged()
    }

    private fun updateSummary() {
        val providerNames = providersPref.getAll()
            .map { OmegaSmartSpaceController.getDisplayName(context, it) }
        if (providerNames.isNotEmpty()) {
            summary = TextUtils.join(", ", providerNames)
        } else {
            setSummary(string.title_disabled)
        }
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        val summaryView = holder.findViewById(android.R.id.summary) as TextView
        summaryView.maxLines = 1
        summaryView.ellipsize = TextUtils.TruncateAt.END
    }

    override fun onAttached() {
        super.onAttached()
        providersPref.addListener(this)
    }

    override fun onDetached() {
        super.onDetached()
        providersPref.removeListener(this)
    }

    override fun onListPrefChanged(key: String) {
        runOnMainThread {
            updateSummary()
        }
    }

    override fun getDialogLayoutResource() = R.layout.dialog_preference_recyclerview
}
