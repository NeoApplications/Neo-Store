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

import android.content.pm.PackageManager
import com.android.launcher3.model.data.AppInfo
import java.util.*

class InstallTimeComparator(private val mPackageManager: PackageManager) : Comparator<AppInfo> {
    override fun compare(app1: AppInfo, app2: AppInfo): Int = try {
        val app1InstallTime =
            mPackageManager.getPackageInfo(app1.componentName.packageName, 0).firstInstallTime
        val app2InstallTime =
            mPackageManager.getPackageInfo(app2.componentName.packageName, 0).firstInstallTime
        when {
            app1InstallTime < app2InstallTime -> 1
            app2InstallTime < app1InstallTime -> -1
            else -> 0
        }
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        0
    }
}