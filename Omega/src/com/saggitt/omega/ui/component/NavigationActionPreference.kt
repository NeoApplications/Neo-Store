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

package com.saggitt.omega.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun NavigationActionPreference(
    label: String,
    subtitle: String? = null,
    destination: String,
    endWidget: (@Composable () -> Unit)? = null
) {
    //val navController = LocalNavController.current

    PreferenceItem(
        modifier = Modifier.clickable { /*navController.navigate(route = destination)*/ },
        title = { Text(text = label) },
        description = { subtitle?.let { Text(text = it) } },
        endWidget = endWidget,
        showDivider = false
    )
}

val LocalNavController = staticCompositionLocalOf<NavController> {
    error("CompositionLocal LocalNavController not present")
}
