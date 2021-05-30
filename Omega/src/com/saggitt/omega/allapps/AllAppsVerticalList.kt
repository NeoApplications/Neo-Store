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
import com.android.launcher3.R

class AllAppsVerticalList(launcher: Launcher) : DrawerBaseLayout() {
    val mLauncher by lazy { launcher }

    override val iconLayout = R.layout.all_apps_icon_vertical
    override fun numRows(default: Int) = -1
    override fun numColumns(default: Int) = 1
    override val tabsEnabled = false
    override fun iconHeight(default: Int): Int {
        return mLauncher.deviceProfile.allAppsIconSizePx
    }
}