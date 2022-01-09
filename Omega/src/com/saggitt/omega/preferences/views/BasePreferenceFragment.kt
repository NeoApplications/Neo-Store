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

import androidx.fragment.app.DialogFragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.saggitt.omega.preferences.custom.CustomDialogPreference

abstract class BasePreferenceFragment : PreferenceFragmentCompat() {

    override fun onDisplayPreferenceDialog(preference: Preference) {
        val f: DialogFragment
        parentFragmentManager.let {
            f = if (preference is CustomDialogPreference) {
                PreferenceDialogFragment.newInstance(preference)
            } else {
                super.onDisplayPreferenceDialog(preference)
                return
            }
            f.setTargetFragment(this, 0)
            f.show(it, "android.support.v7.preference.PreferenceFragment.DIALOG")
        }
    }
}