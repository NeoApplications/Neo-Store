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

import com.android.launcher3.model.data.AppInfo;
import com.saggitt.omega.model.AppCountInfo;

import java.util.Comparator;
import java.util.List;

public class MostUsedComparator implements Comparator<AppInfo> {
    private List<AppCountInfo> mApps;

    public MostUsedComparator(List<AppCountInfo> apps) {
        mApps = apps;
    }

    @Override
    public int compare(AppInfo app1, AppInfo app2) {
        int item1 = 0;
        int item2 = 0;

        for (int i = 0; i < mApps.size(); i++) {
            if (mApps.get(i).getPackageName().equals(app1.componentName.getPackageName())) {
                item1 = mApps.get(i).getCount();
            }
            if (mApps.get(i).getPackageName().equals(app2.componentName.getPackageName())) {
                item2 = mApps.get(i).getCount();
            }
        }

        if (item1 < item2) {
            return 1;
        } else if (item2 < item1) {
            return -1;
        } else {
            return 0;
        }
    }

}