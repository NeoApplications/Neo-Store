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

import android.content.pm.PackageManager;

import com.android.launcher3.AppInfo;

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
