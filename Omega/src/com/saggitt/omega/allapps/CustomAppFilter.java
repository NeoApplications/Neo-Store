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

import android.content.ComponentName;
import android.content.Context;
import android.os.UserHandle;

import com.android.launcher3.Utilities;
import com.android.launcher3.util.ComponentKey;

import java.util.HashSet;
import java.util.Set;

public class CustomAppFilter extends OmegaAppFilter {
    private final Context mContext;

    public CustomAppFilter(Context context) {
        super(context);
        mContext = context;
    }

    public static void setComponentNameState(Context context, ComponentKey key, boolean hidden) {
        String comp = key.toString();
        Set<String> hiddenApps = getHiddenApps(context);
        while (hiddenApps.contains(comp)) {
            hiddenApps.remove(comp);
        }
        if (hidden) {
            hiddenApps.add(comp);
        }
        setHiddenApps(context, hiddenApps);
    }

    public static boolean isHiddenApp(Context context, ComponentKey key) {
        return getHiddenApps(context).contains(key.toString());
    }

    @SuppressWarnings("ConstantConditions") // This can't be null anyway
    public static Set<String> getHiddenApps(Context context) {
        return new HashSet<>(Utilities.getOmegaPrefs(context).getHiddenAppSet());
    }

    public static void setHiddenApps(Context context, Set<String> hiddenApps) {
        Utilities.getOmegaPrefs(context).setHiddenAppSet(hiddenApps);
    }

    @Override
    public boolean shouldShowApp(ComponentName componentName, UserHandle user) {
        return super.shouldShowApp(componentName, user)
                && (user == null || !isHiddenApp(mContext, new ComponentKey(componentName, user)));
    }
}