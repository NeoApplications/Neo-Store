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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.machiav3lli.fdroid.NAV_MAIN
import com.machiav3lli.fdroid.NAV_PREFS
import com.machiav3lli.fdroid.content.Preferences

@Composable
fun BottomNavBar(page: Int = NAV_MAIN, navController: NavHostController) {
    val items = when (page) {
        NAV_PREFS -> listOf(
            NavItem.PersonalPrefs,
            NavItem.UpdatesPrefs,
            NavItem.ReposPrefs,
            NavItem.OtherPrefs,
        )

        else      -> listOf(
            NavItem.Latest,
            NavItem.Explore,
            NavItem.Installed,
            NavItem.Search,
        )
    }

    NavigationBar(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination?.route

        items.forEach { item ->
            val selected = currentDestination?.contains(item.destination) ?: false

            if (Preferences[Preferences.Key.AltNavBarItem])
                AltNavBarItem(
                    modifier = Modifier.weight(1f),
                    icon = item.icon,
                    labelId = item.title,
                    selected = selected,
                    onClick = {
                        navController.navigate(item.destination) {
                            navBackStackEntry?.destination?.let {
                                popUpTo(it.route.orEmpty()) {
                                    inclusive = true
                                    saveState = true
                                }
                            }
                            launchSingleTop = true
                            restoreState = page == NAV_MAIN
                        }
                    }
                )
            else NavBarItem(
                modifier = Modifier.weight(if (selected) 2f else 1f),
                icon = item.icon,
                labelId = item.title,
                selected = selected,
                onClick = {
                    navController.navigate(item.destination) {
                        navBackStackEntry?.destination?.let {
                            popUpTo(it.route.orEmpty()) {
                                inclusive = true
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                        restoreState = page == NAV_MAIN
                    }
                }
            )
        }
    }
}

@Composable
fun RowScope.AltNavBarItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    labelId: Int,
    selected: Boolean,
    onClick: () -> Unit = {},
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.surfaceColorAtElevation(12.dp)
        else Color.Transparent,
        label = "backgroundColor",
    )
    val iconSize by animateDpAsState(
        targetValue = if (selected) 32.dp else 24.dp,
        label = "iconSize",
    )
    val iconColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurface,
        label = "iconColor",
    )

    Row(
        modifier = Modifier
            .clickable { onClick() }
            .weight(1f),
        horizontalArrangement = Arrangement.Center,
    ) {
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = stringResource(id = labelId),
                modifier = Modifier
                    .background(backgroundColor, CircleShape)
                    .padding(8.dp)
                    .size(iconSize),
                tint = iconColor,
            )
            AnimatedVisibility(
                visible = !selected,
                enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
            ) {
                Text(
                    text = stringResource(id = labelId),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
fun RowScope.NavBarItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    labelId: Int,
    selected: Boolean,
    onClick: () -> Unit = {},
) {
    val background by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.surfaceColorAtElevation(12.dp)
        else Color.Transparent, label = "backgroundColor"
    )
    val iconColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurface,
        label = "iconColor",
    )

    Row(
        modifier = modifier
            .padding(vertical = 8.dp)
            .clip(MaterialTheme.shapes.extraLarge)
            .clickable { onClick() }
            .background(
                background,
                MaterialTheme.shapes.extraLarge
            )
            .padding(8.dp)
            .weight(1f),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = stringResource(id = labelId),
            modifier = Modifier.size(24.dp),
            tint = iconColor,
        )
        AnimatedVisibility(
            visible = selected,
            enter = fadeIn() + expandHorizontally(expandFrom = Alignment.Start),
        ) {
            Text(
                text = stringResource(id = labelId),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
