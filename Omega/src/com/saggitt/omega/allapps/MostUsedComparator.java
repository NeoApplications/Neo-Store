/*
 *  Copyright (c) 2020 Omega Launcher
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.saggitt.omega.allapps;

import com.android.launcher3.AppInfo;
import com.saggitt.omega.model.AppCountInfo;

import java.util.Comparator;
import java.util.List;

public class MostUsedComparator implements Comparator<AppInfo> {
    private String TAG = "MostUsedComparator";
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