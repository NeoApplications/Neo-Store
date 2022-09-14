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
import com.saggitt.omega.compose.objects.PageItem
import com.saggitt.omega.preferences.BasePreferences
import com.saggitt.omega.preferences.custom.GridSize
import com.saggitt.omega.preferences.custom.GridSize2D

val PreferenceBuilder =
    @Composable { pref: Any, onDialogPref: (Any) -> Unit, index: Int, size: Int ->
        when (pref) {
            is BasePreferences.IntentLauncherPref -> IntentLauncherPreference(
                pref = pref,
                index = index,
                groupSize = size
            ) { onDialogPref(pref) }
            is GridSize2D -> GridSize2DPreference(
                pref = pref,
                index = index,
                groupSize = size
            ) { onDialogPref(pref) }
            is GridSize -> GridSizePreference(
                pref = pref,
                index = index,
                groupSize = size
            ) { onDialogPref(pref) }
            is BasePreferences.BooleanPref ->
                SwitchPreference(pref = pref, index = index, groupSize = size)
            is BasePreferences.StringPref ->
                StringPreference(pref = pref, index = index, groupSize = size)
            is BasePreferences.StringSetPref ->
                StringSetPreference(pref = pref, index = index, groupSize = size)
            is BasePreferences.FloatPref ->
                SeekBarPreference(pref = pref, index = index, groupSize = size)
            is BasePreferences.ColorIntPref ->
                ColorIntPreference(pref = pref, index = index, groupSize = size)
            is BasePreferences.IdpIntPref ->
                IntSeekBarPreference(pref = pref, index = index, groupSize = size)
            is BasePreferences.IntSelectionPref ->
                IntSelectionPreference(
                    pref = pref,
                    index = index,
                    groupSize = size
                ) { onDialogPref(pref) }
            is BasePreferences.StringSelectionPref ->
                StringSelectionPreference(
                    pref = pref,
                    index = index,
                    groupSize = size
                ) { onDialogPref(pref) }
            is BasePreferences.StringMultiSelectionPref -> StringMultiSelectionPreference(
                pref = pref,
                index = index,
                groupSize = size
            ) { onDialogPref(pref) }
            is PageItem ->
                PagePreference(
                    titleId = pref.titleId,
                    iconId = pref.iconId,
                    route = pref.route,
                    index = index,
                    groupSize = size
                )
        }
    }
