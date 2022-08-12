/*
 * This file is part of Neo Launcher
 * Copyright (c) 2022   Neo Launcher Team
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
package com.saggitt.omega.compose.components.preferences

import androidx.compose.runtime.Composable
import com.saggitt.omega.preferences.BasePreferences

val PreferenceBuilder = @Composable { pref: Any, onDialogPref: (Any) -> Unit ->
    when (pref) {
        is BasePreferences.BooleanPref -> SwitchPreference(pref = pref)
        is BasePreferences.FloatPref -> SeekBarPreference(pref = pref)
        is BasePreferences.ColorIntPref -> ColorIntPreference(pref = pref)
        is BasePreferences.IdpIntPref -> IntSeekBarPreference(pref = pref)
        is BasePreferences.IntSelectionPref ->
            IntSelectionPreference(pref = pref) { onDialogPref(pref) }
    }
}