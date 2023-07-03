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

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import com.machiav3lli.fdroid.MainApplication
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.pages.ExplorePage
import com.machiav3lli.fdroid.pages.InstalledPage
import com.machiav3lli.fdroid.pages.LatestPage
import com.machiav3lli.fdroid.pages.PermissionsPage
import com.machiav3lli.fdroid.pages.PrefsOtherPage
import com.machiav3lli.fdroid.pages.PrefsPersonalPage
import com.machiav3lli.fdroid.pages.PrefsReposPage
import com.machiav3lli.fdroid.pages.PrefsUpdatesPage
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CircleWavyWarning
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Compass
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.DotsThreeOutline
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Download
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.GearSix
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Graph
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.House
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ShieldStar
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.UserGear

sealed class NavItem(var title: Int, var icon: ImageVector, var destination: String) {
    object Main :
        NavItem(R.string.explore, Phosphor.House, "main")

    object Permissions :
        NavItem(R.string.permissions, Phosphor.ShieldStar, "main_permissions")

    object Explore :
        NavItem(R.string.explore, Phosphor.Compass, "main_explore")

    object Latest :
        NavItem(R.string.latest, Phosphor.CircleWavyWarning, "main_latest")

    object Installed :
        NavItem(R.string.installed, Phosphor.House, "main_installed")

    object Prefs :
        NavItem(R.string.settings, Phosphor.GearSix, "prefs")

    object PersonalPrefs :
        NavItem(R.string.prefs_personalization, Phosphor.UserGear, "prefs_personal")

    object UpdatesPrefs :
        NavItem(R.string.updates, Phosphor.Download, "prefs_updates")

    object ReposPrefs :
        NavItem(R.string.repositories, Phosphor.Graph, "prefs_repos")

    object OtherPrefs :
        NavItem(R.string.other, Phosphor.DotsThreeOutline, "prefs_other")

    @Composable
    fun ComposablePage(navController: NavHostController) {
        when (destination) {
            Permissions.destination   -> {
                PermissionsPage {
                    navController.navigate(Main.destination)
                }
            }

            Explore.destination       -> {
                MainApplication.mainActivity?.exploreViewModel?.let {
                    ExplorePage(it)
                }
            }

            Latest.destination        -> {
                MainApplication.mainActivity?.latestViewModel?.let {
                    LatestPage(it)
                }
            }

            Installed.destination     -> {
                MainApplication.mainActivity?.installedViewModel?.let {
                    InstalledPage(it)
                }
            }

            PersonalPrefs.destination -> {
                PrefsPersonalPage()
            }

            UpdatesPrefs.destination  -> {
                PrefsUpdatesPage()
            }

            OtherPrefs.destination    -> {
                MainApplication.prefsActivity?.prefsViewModel?.let {
                    PrefsOtherPage(it)
                }
            }

            ReposPrefs.destination    -> {
                MainApplication.prefsActivity?.prefsViewModel?.let {
                    PrefsReposPage(it)
                }
            }
        }
    }
}