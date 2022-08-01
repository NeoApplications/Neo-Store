/*
 *  This file is part of Omega Launcher
 *  Copyright (c) 2021   Omega Launcher Team
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

package com.saggitt.omega.preferences.views

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import androidx.preference.SwitchPreference
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.saggitt.omega.PREFS_PROTECTED_APPS
import com.saggitt.omega.PREFS_TRUST_APPS
import com.saggitt.omega.util.Config
import com.saggitt.omega.util.omegaPrefs

class PrefsDrawerFragment :
    BasePreferenceFragment(R.xml.preferences_drawer, R.string.title__general_drawer) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findPreference<SwitchPreference>(PREFS_PROTECTED_APPS)?.apply {
            onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any ->
                    requireActivity().omegaPrefs.drawerEnableProtectedApps.onSetValue(newValue as Boolean)
                    true
                }

            isVisible = Utilities.ATLEAST_R
        }

        findPreference<Preference>(PREFS_TRUST_APPS)?.apply {
            onPreferenceClickListener = Preference.OnPreferenceClickListener {
                if (
                    Utilities.getOmegaPrefs(requireContext()).drawerEnableProtectedApps.onGetValue() &&
                    Utilities.ATLEAST_R
                ) {
                    Config.showLockScreen(
                        requireContext(),
                        getString(R.string.trust_apps_manager_name)
                    ) {
                        val fragment = "com.saggitt.omega.preferences.views.HiddenAppsFragment"
                        PreferencesActivity.startFragment(
                            context,
                            fragment,
                            context.resources.getString(R.string.title__drawer_hide_apps)
                        )
                    }
                } else {
                    val fragment = "com.saggitt.omega.preferences.views.HiddenAppsFragment"
                    PreferencesActivity.startFragment(
                        context,
                        fragment,
                        context.resources.getString(R.string.title__drawer_hide_apps)
                    )
                }
                false
            }
        }

        findPreference<Preference>("pref_suggestions")?.apply {
            isVisible = false
            //isVisible = isDspEnabled(context)
        }
    }

    private fun isDspEnabled(context: Context): Boolean {
        return try {
            context.packageManager.getApplicationInfo(Config.DPS_PACKAGE, 0).enabled
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
}