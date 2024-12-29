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
import com.machiav3lli.fdroid.MainApplication
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.pages.ExplorePage
import com.machiav3lli.fdroid.pages.InstalledPage
import com.machiav3lli.fdroid.pages.LatestPage
import com.machiav3lli.fdroid.pages.PrefsOtherPage
import com.machiav3lli.fdroid.pages.PrefsPersonalPage
import com.machiav3lli.fdroid.pages.PrefsReposPage
import com.machiav3lli.fdroid.pages.PrefsUpdatesPage
import com.machiav3lli.fdroid.pages.SearchPage
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CircleWavyWarning
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Compass
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.DotsThreeOutline
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Download
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Graph
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.House
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.MagnifyingGlass
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.UserGear
import kotlinx.serialization.Serializable

sealed class NavItem(
    val title: Int,
    val icon: ImageVector,
    val destination: String,
    val content: @Composable () -> Unit,
) {
    data object Explore :
        NavItem(R.string.explore, Phosphor.Compass, "main_explore", {
            ExplorePage()
        })

    data object Latest :
        NavItem(R.string.latest, Phosphor.CircleWavyWarning, "main_latest", {
            LatestPage()
        })

    data object Installed :
        NavItem(R.string.installed, Phosphor.House, "main_installed", {
            InstalledPage()
        })

    data object Search :
        NavItem(R.string.search, Phosphor.MagnifyingGlass, "main_search", {
            SearchPage()
        })

    data object PersonalPrefs :
        NavItem(R.string.prefs_personalization, Phosphor.UserGear, "prefs_personal", {
            PrefsPersonalPage()
        })

    data object UpdatesPrefs :
        NavItem(R.string.updates, Phosphor.Download, "prefs_updates", {
            PrefsUpdatesPage()
        })

    data object ReposPrefs :
        NavItem(R.string.repositories, Phosphor.Graph, "prefs_repos", {
            PrefsReposPage()
        })

    data object OtherPrefs :
        NavItem(R.string.other, Phosphor.DotsThreeOutline, "prefs_other", {
            PrefsOtherPage()
        })
}

@Serializable
open class NavRoute {
    @Serializable
    data object Permissions : NavRoute()

    @Serializable
    data class Main(val page: Int = Preferences[Preferences.Key.DefaultTab].index) :
        NavRoute()

    @Serializable
    data class Prefs(val page: Int = 0) : NavRoute()
}