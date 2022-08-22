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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.saggitt.omega.compose.components.BaseDialog
import com.saggitt.omega.compose.components.ViewWithActionBar
import com.saggitt.omega.compose.components.preferences.IntSelectionPrefDialogUI
import com.saggitt.omega.compose.components.preferences.PreferenceGroup
import com.saggitt.omega.compose.components.preferences.StringMultiSelectionPrefDialogUI
import com.saggitt.omega.compose.components.preferences.StringSelectionPrefDialogUI
import com.saggitt.omega.preferences.BasePreferences
import com.saggitt.omega.theme.OmegaAppTheme

@Composable
fun WidgetsPrefsPage() {
    val context = LocalContext.current
    val prefs = Utilities.getOmegaPrefs(context)
    val openDialog = remember { mutableStateOf(false) }
    var dialogPref by remember { mutableStateOf<Any?>(null) }
    val onPrefDialog = { pref: Any ->
        dialogPref = pref
        openDialog.value = true
    }
    val smartspacePrefs = listOf(
        prefs.smartspaceEnable,
        prefs.smartspaceDate,
        prefs.smartspaceTime,
        prefs.smartspaceTimeAbove,
        prefs.smartspaceTime24H,
        prefs.smartspaceUsePillQsb,
        prefs.smartspaceWeatherProvider, // TODO
        prefs.smartspaceWeatherUnit, // TODO
        prefs.smartspaceEventProviders // TODO
    )
    val notificationsPrefs = listOf(
        // TODO Missing enable notification badge
        prefs.notificationCount,
        prefs.notificationCustomColor,
        prefs.notificationBackground
    )

    OmegaAppTheme {
        ViewWithActionBar(
            title = stringResource(R.string.title__general_widgets_notifications)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // TODO Add Smartspace preview
                item {
                    PreferenceGroup(
                        stringResource(id = R.string.title__general_smartspace),
                        prefs = smartspacePrefs,
                        onPrefDialog = onPrefDialog
                    )
                }
                item {
                    PreferenceGroup(
                        stringResource(id = R.string.pref_category__notifications),
                        prefs = notificationsPrefs,
                        onPrefDialog = onPrefDialog
                    )
                }
            }

            if (openDialog.value) {
                BaseDialog(openDialogCustom = openDialog) {
                    when (dialogPref) {
                        is BasePreferences.IntSelectionPref -> IntSelectionPrefDialogUI(
                            pref = dialogPref as BasePreferences.IntSelectionPref,
                            openDialogCustom = openDialog
                        )
                        is BasePreferences.StringSelectionPref -> StringSelectionPrefDialogUI(
                            pref = dialogPref as BasePreferences.StringSelectionPref,
                            openDialogCustom = openDialog
                        )
                        is BasePreferences.StringMultiSelectionPref -> StringMultiSelectionPrefDialogUI(
                            pref = dialogPref as BasePreferences.StringMultiSelectionPref,
                            openDialogCustom = openDialog
                        )
                    }
                }
            }
        }
    }
}