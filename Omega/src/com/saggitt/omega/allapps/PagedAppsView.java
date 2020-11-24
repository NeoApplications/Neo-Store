/*
 * Copyright (c) 2020 Omega Launcher
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
 */

package com.saggitt.omega.allapps;

import android.content.Context;

import com.android.launcher3.InvariantDeviceProfile;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.allapps.AllAppsStore;

public class PagedAppsView {
    private int numCols;
    private int numRows;
    private int numPages;
    private int appsPerPage;
    private InvariantDeviceProfile idp;
    private Context mContext;
    private final AllAppsStore mAllAppsStore = new AllAppsStore();

    public PagedAppsView(Context context) {
        idp = LauncherAppState.getIDP(context);
        numCols = idp.numColsDrawer;
        numRows = idp.numRows;
        appsPerPage = numCols * numRows;
        mContext = context;
        numPages = mAllAppsStore.getApps().length / appsPerPage;
    }

}
