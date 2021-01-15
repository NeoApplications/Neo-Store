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