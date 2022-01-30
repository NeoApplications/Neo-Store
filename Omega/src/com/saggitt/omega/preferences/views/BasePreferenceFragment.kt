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

package com.saggitt.omega.preferences.views

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.saggitt.omega.preferences.custom.CustomDialogPreference
import com.saggitt.omega.preferences.custom.SearchProviderPreference
import com.saggitt.omega.search.SelectSearchProviderFragment

abstract class BasePreferenceFragment(val layoutId: Int, val titleId: Int = -1) :
    PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(layoutId, rootKey)
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        val f: DialogFragment
        parentFragmentManager.let {
            f = when (preference) {
                is CustomDialogPreference -> {
                    PreferenceDialogFragment.newInstance(preference)
                }
                is SearchProviderPreference -> {
                    SelectSearchProviderFragment.newInstance(preference)
                }
                else -> {
                    super.onDisplayPreferenceDialog(preference)
                    return
                }
            }
            f.setTargetFragment(this, 0)
            f.show(it, "android.support.v7.preference.PreferenceFragment.DIALOG")
        }
    }

    override fun onResume() {
        super.onResume()
        if (titleId != -1) requireActivity().title = requireActivity().getString(titleId)
    }
}