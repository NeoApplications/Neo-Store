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

import com.machiav3lli.fdroid.R

sealed class NavItem(var title: Int, var icon: Int, var destination: String) {
    object Explore :
        NavItem(R.string.explore, R.drawable.ic_public, "main_explore")

    object Latest :
        NavItem(R.string.latest, R.drawable.ic_new_releases, "main_latest")

    object Installed :
        NavItem(R.string.installed, R.drawable.ic_launch, "main_installed")

    object PersonalPrefs :
        NavItem(R.string.prefs_personalization, R.drawable.ic_person, "prefs_personal")

    object UpdatesPrefs :
        NavItem(R.string.updates, R.drawable.ic_download, "prefs_updates")

    object ReposPrefs :
        NavItem(R.string.repositories, R.drawable.ic_repos, "prefs_repos")

    object OtherPrefs :
        NavItem(R.string.other, R.drawable.ic_tune, "prefs_other")
}