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

package com.saggitt.omega.allapps;

import android.content.pm.PackageManager;

import com.android.launcher3.model.data.AppInfo;

import java.util.Comparator;

public class InstallTimeComparator implements Comparator<AppInfo> {
    private final PackageManager mPackageManager;

    public InstallTimeComparator(PackageManager packageManager) {
        mPackageManager = packageManager;
    }

    @Override
    public int compare(AppInfo app1, AppInfo app2) {
        try {
            long app1InstallTime = mPackageManager.getPackageInfo(app1.componentName.getPackageName(), 0).firstInstallTime;
            long app2InstallTime = mPackageManager.getPackageInfo(app2.componentName.getPackageName(), 0).firstInstallTime;
            if (app1InstallTime < app2InstallTime) {
                return 1;
            } else if (app2InstallTime < app1InstallTime) {
                return -1;
            } else {
                return 0;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
