/*
 * Neo Store: An open-source modern F-Droid client.
 * Copyright (C) 2022  Antonios Hazim
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
package com.machiav3lli.fdroid.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.machiav3lli.fdroid.NAV_MAIN
import com.machiav3lli.fdroid.NAV_PREFS

@Composable
fun BottomNavBar(page: Int = NAV_MAIN, navController: NavController) {
    val items = when (page) {
        NAV_PREFS -> listOf(
            NavItem.PersonalPrefs,
            NavItem.UpdatesPrefs,
            NavItem.ReposPrefs,
            NavItem.OtherPrefs,
        )
        else -> listOf(
            NavItem.Explore,
            NavItem.Latest,
            NavItem.Installed,
        )
    }

    NavigationBar(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination?.route

        items.forEach { item ->
            val selected = currentDestination?.contains(item.destination) ?: false

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = stringResource(id = item.title),
                        modifier = Modifier
                            .background(
                                if (selected) MaterialTheme.colorScheme.surfaceColorAtElevation(48.dp)
                                else Color.Transparent,
                                CircleShape
                            )
                            .padding(8.dp)
                            .size(if (selected) 36.dp else 26.dp),
                    )
                },
                label = {
                    if (!selected)
                        Text(
                            text = stringResource(id = item.title),
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.background,
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                ),
                alwaysShowLabel = true,
                selected = selected,
                onClick = {
                    navController.navigate(item.destination) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = page == NAV_MAIN
                    }
                }
            )
        }
    }
}