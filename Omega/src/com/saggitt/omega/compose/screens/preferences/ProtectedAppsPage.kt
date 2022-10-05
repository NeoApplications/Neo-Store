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

@Composable
fun ProtectedAppsPage() {
    val context = LocalContext.current
    val prefs = Utilities.getOmegaPrefs(context)
    val protectedApps by remember {
        mutableStateOf(prefs.drawerProtectedApps)
    }
    val title = if (protectedApps.isEmpty()) stringResource(id = R.string.protected_apps)
    else stringResource(id = R.string.protected_app_selected, protectedApps.size)

    AppSelectionPage(
        pageTitle = title,
        selectedApps = protectedApps,
        pluralTitleId = R.string.protected_app_selected
    ) { selectedApps ->
        prefs.drawerProtectedApps = selectedApps
    }
}