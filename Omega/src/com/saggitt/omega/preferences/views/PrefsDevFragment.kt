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

import android.os.Bundle
import androidx.preference.Preference
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.farmerbb.taskbar.lib.Taskbar
import com.saggitt.omega.PREFS_DESKTOP_MODE_SETTINGS
import com.saggitt.omega.PREFS_KILL
import com.saggitt.omega.compose.ComposeActivity
import com.saggitt.omega.compose.navigation.Routes
import com.saggitt.omega.theme.ThemeOverride
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PrefsDevFragment :
    BasePreferenceFragment(R.xml.preferences_dev, R.string.developer_options_title) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        findPreference<Preference>(PREFS_KILL)?.setOnPreferenceClickListener {
            Utilities.killLauncher()
            false
        }
        findPreference<Preference>(PREFS_DESKTOP_MODE_SETTINGS)?.setOnPreferenceClickListener {
            Taskbar.openSettings(
                    requireContext(),
                    ThemeOverride.Settings().getTheme(requireContext())
            )
            true
        }

        findPreference<Preference>("pref_gesture_selector")?.setOnPreferenceClickListener {
            val scope = CoroutineScope(Dispatchers.Main)
            scope.launch {
                requireContext().startActivity(
                        ComposeActivity.createIntent(
                                requireContext(),
                                "${Routes.GESTURE_SELECTOR}/"
                        )
                )
            }
            true
        }

        findPreference<Preference>(Routes.PREFS_MAIN)?.setOnPreferenceClickListener {
            val scope = CoroutineScope(Dispatchers.Main)
            scope.launch {
                requireContext().startActivity(
                    ComposeActivity.createIntent(
                        requireContext(),
                        "${Routes.PREFS_MAIN}/"
                    )
                )
            }
            true
        }
    }
}