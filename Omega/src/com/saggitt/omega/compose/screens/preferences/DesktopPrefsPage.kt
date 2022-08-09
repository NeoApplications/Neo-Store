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
package com.saggitt.omega.compose.screens.preferences

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.saggitt.omega.compose.components.preferences.SeekBarPreference
import com.saggitt.omega.compose.components.preferences.SwitchPreference
import com.saggitt.omega.compose.components.preferences.PreferenceGroup
import com.saggitt.omega.preferences.BasePreferences
import com.saggitt.omega.theme.OmegaAppTheme

@Composable
fun DesktopPrefsPage() {
    val context = LocalContext.current
    val prefs = Utilities.getOmegaPrefs(context)
    val iconPrefs = listOf(
        prefs.desktopIconScale,
        prefs.desktopHideAppLabels,
        prefs.desktopMultilineLabel,
        prefs.desktopTextScale,
        //prefs.desktopUsePopupMenuView, TODO
    )
    val gridPrefs = listOf(
        prefs.desktopGridSize, // TODO
        // TODO Missing add app icon on install
        // TODO Missing allow desktop rotation
        prefs.desktopAllowFullWidthWidgets,
        prefs.desktopAllowEmptyScreens
    )
    val folderPrefs = listOf(
        prefs.desktopFolderRadius,
        prefs.desktopFolderColumns,
        prefs.desktopFolderRows
    )
    val otherPrefs = listOf(
        prefs.desktopHideStatusBar,
        prefs.desktopLock
    )

    val composer = @Composable { pref: Any ->
        when (pref) {
            is BasePreferences.BooleanPref -> SwitchPreference(pref = pref)
            is BasePreferences.FloatPref -> SeekBarPreference(pref = pref)
        }
    }

    OmegaAppTheme {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                PreferenceGroup(stringResource(id = R.string.cat_drawer_icons)) {
                    iconPrefs.forEach { composer(it) }
                }
            }
            item {
                PreferenceGroup(stringResource(id = R.string.cat_desktop_grid)) {
                    gridPrefs.forEach { composer(it) }
                }
            }
            item {
                PreferenceGroup(stringResource(id = R.string.app_categorization_folders)) {
                    folderPrefs.forEach { composer(it) }
                }
            }
            item {
                PreferenceGroup(stringResource(id = R.string.pref_category__others)) {
                    otherPrefs.forEach { composer(it) }
                }
            }
        }
    }
}