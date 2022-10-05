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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.saggitt.omega.util.App
import com.saggitt.omega.util.appComparator
import com.saggitt.omega.util.comparing

@Composable
fun HiddenAppsPage() {
    val context = LocalContext.current
    val prefs = Utilities.getOmegaPrefs(context)
    val hiddenApps by remember {
        mutableStateOf(prefs.drawerHiddenApps)
    }
    val title = if (hiddenApps.isEmpty()) stringResource(id = R.string.title__drawer_hide_apps)
    else stringResource(id = R.string.hide_app_selected, hiddenApps.size)

    AppSelectionPage(
        pageTitle = title,
        selectedApps = hiddenApps,
        pluralTitleId = R.string.hide_app_selected
    ) { selectedApps ->
        prefs.drawerHiddenApps = selectedApps
    }
}

@Composable
fun hiddenAppsComparator(hiddenApps: Set<String>): Comparator<App> = remember {
    comparing<App, Int> {
        if (hiddenApps.contains(it.key.toString())) 0 else 1
    }.then(appComparator)
}