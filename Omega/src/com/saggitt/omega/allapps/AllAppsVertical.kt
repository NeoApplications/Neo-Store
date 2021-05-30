/*
 *  This file is part of Omega Launcher
 *  Copyright (c) 2021   Saul Henriquez
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

package com.saggitt.omega.allapps

import com.android.launcher3.Launcher
import com.android.launcher3.LauncherAppState
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.saggitt.omega.OmegaPreferences

class AllAppsVertical(launcher: Launcher) : DrawerBaseLayout() {
    val prefs: OmegaPreferences by lazy { Utilities.getOmegaPrefs(launcher.applicationContext) }
    val mLauncher by lazy { launcher }

    override val iconLayout = R.layout.all_apps_icon
    override fun numRows(default: Int) = -1
    override fun numColumns(default: Int) =
        LauncherAppState.getIDP(mLauncher.applicationContext).numColsDrawer

    override val tabsEnabled = prefs.drawerTabs.isEnabled
    override fun iconHeight(default: Int): Int {
        return mLauncher.deviceProfile.allAppsCellHeightPx
    }
}