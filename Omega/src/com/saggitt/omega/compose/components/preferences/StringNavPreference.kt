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
import androidx.compose.ui.Modifier
import com.saggitt.omega.compose.navigation.LocalNavController
import com.saggitt.omega.compose.navigation.subRoute
import com.saggitt.omega.preferences.BasePreferences

@Composable
fun StringNavPreference(
        modifier: Modifier = Modifier,
        pref: BasePreferences.StringNavPref,
        isEnabled: Boolean = true,
        onValueChange: ((Float) -> Unit) = {}
) {
    val navController = LocalNavController.current
    val route=subRoute(pref.navRoute)
    BasePreference(
        modifier = modifier,
        titleId = pref.titleId,
        summaryId = pref.summaryId,
        isEnabled = isEnabled,
        onClick = {
            navController.navigate(route)
        }
    )
}