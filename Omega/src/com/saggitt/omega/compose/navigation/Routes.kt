/*
 * This file is part of Omega Launcher
 * Copyright (c) 2022   Omega Launcher Team
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

package com.saggitt.omega.compose.navigation

sealed class Routes(val route: String) {
    object AboutMainScreen : Routes("about")
    object Translators : Routes("about/translators")
    object Changelog : Routes("about/changelog")
    object License : Routes("about/license")

    object EditIconMainScreen : Routes("edit_icon")
    object IconListScreen : Routes("edit_icon/icon_picker")
}