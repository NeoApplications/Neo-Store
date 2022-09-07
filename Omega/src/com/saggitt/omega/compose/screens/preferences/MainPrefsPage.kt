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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.saggitt.omega.compose.components.OverflowMenu
import com.saggitt.omega.compose.components.ViewWithActionBar
import com.saggitt.omega.compose.components.preferences.PreferenceGroup
import com.saggitt.omega.compose.navigation.BlankScreen
import com.saggitt.omega.compose.navigation.LocalNavController
import com.saggitt.omega.compose.navigation.Routes
import com.saggitt.omega.compose.navigation.preferenceGraph
import com.saggitt.omega.compose.navigation.subRoute
import com.saggitt.omega.compose.objects.PageItem
import com.saggitt.omega.theme.OmegaAppTheme

@Composable
fun MainPrefsPage() {
    val context = LocalContext.current
    val prefs = Utilities.getOmegaPrefs(context)
    val uiPrefs = listOf(
        PageItem.PrefsProfile,
        PageItem.PrefsDesktop,
        PageItem.PrefsDock,
        PageItem.PrefsDrawer
    )
    val featuresPrefs = listOf(
        PageItem.PrefsWidgetsNotifications,
        PageItem.PrefsSearchFeed,
        PageItem.PrefsGesturesDash
    )
    val otherPrefs = listOfNotNull(
        PageItem.PrefsBackup,
        PageItem.PrefsDesktopMode,
        if (prefs.developerOptionsEnabled.onGetValue()) PageItem.PrefsDeveloper
        else null,
        PageItem.PrefsAbout
    )
    val navController = LocalNavController.current
    val destination = subRoute(Routes.PREFS_DEV)

    OmegaAppTheme {
        ViewWithActionBar(
            title = stringResource(R.string.settings_button_text),
            showBackButton = false,
            actions = {
                OverflowMenu {
                    DropdownMenuItem(
                        onClick = {
                            Utilities.killLauncher()
                            hideMenu()
                        },
                        text = { Text(text = stringResource(id = R.string.title__restart_launcher)) }
                    )
                    DropdownMenuItem(
                        onClick = {
                            navController.navigate(destination)
                            hideMenu()
                        },
                        text = { Text(text = stringResource(id = R.string.developer_options_title)) }
                    )
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                contentPadding = paddingValues,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    PreferenceGroup(
                        heading = stringResource(id = R.string.pref_category__interfaces),
                        prefs = uiPrefs
                    )
                }
                item {
                    PreferenceGroup(
                        heading = stringResource(id = R.string.pref_category__features),
                        prefs = featuresPrefs
                    )
                }
                item {
                    PreferenceGroup(
                        heading = stringResource(id = R.string.pref_category__others),
                        prefs = otherPrefs
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

fun NavGraphBuilder.mainPrefsGraph(route: String) {
    preferenceGraph(route, { MainPrefsPage() }) { subRoute ->
        profilePrefsGraph(route = subRoute(Routes.PREFS_PROFILE))
        preferenceGraph(route = subRoute(Routes.PREFS_DESKTOP), { DesktopPrefsPage() })
        preferenceGraph(route = subRoute(Routes.PREFS_DOCK), { DockPrefsPage() })
        drawerPrefsGraph(route = subRoute(Routes.PREFS_DRAWER))
        preferenceGraph(route = subRoute(Routes.PREFS_WIDGETS), { WidgetsPrefsPage() })
        preferenceGraph(route = subRoute(Routes.PREFS_SEARCH), { SearchPrefsPage() })
        preferenceGraph(route = subRoute(Routes.PREFS_BACKUPS), { BackupsPrefPage() })
        preferenceGraph(route = subRoute(Routes.PREFS_DM), { BlankScreen() }) // TODO
        preferenceGraph(route = subRoute(Routes.PREFS_DEV), { DevPrefPage() })
        gesturesPrefGraph(route = subRoute(Routes.PREFS_GESTURES))
        aboutPrefsGraph(route = subRoute(Routes.ABOUT))
    }
}