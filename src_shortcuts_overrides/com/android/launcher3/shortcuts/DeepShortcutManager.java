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

package com.android.launcher3.shortcuts;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.LauncherApps;
import android.content.pm.LauncherApps.ShortcutQuery;
import android.content.pm.ShortcutInfo;
import android.os.UserHandle;
import android.util.Log;

import com.android.launcher3.model.data.ItemInfo;
import com.android.launcher3.util.ShortcutUtil;
import com.saggitt.omega.override.CustomInfoProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Performs operations related to deep shortcuts, such as querying for them, pinning them, etc.
 */
public class DeepShortcutManager {
    private static final String TAG = "DeepShortcutManager";

    private static DeepShortcutManager sInstance;

    public static DeepShortcutManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DeepShortcutManager(context.getApplicationContext());
        }
        return sInstance;
    }

    private final LauncherApps mLauncherApps;

    private DeepShortcutManager(Context context) {
        mLauncherApps = (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
    }


    private boolean mWasLastCallSuccess;

    public static boolean supportsEdit(ItemInfo info) {
        return CustomInfoProvider.Companion.isEditable(info) || ShortcutUtil.supportsShortcuts(info);
    }

    public boolean wasLastCallSuccess() {
        return mWasLastCallSuccess;
    }

    /**
     * Returns the id's of pinned shortcuts associated with the given package and user.
     * <p>
     * If packageName is null, returns all pinned shortcuts regardless of package.
     */
    public QueryResult queryForPinnedShortcuts(String packageName, UserHandle user) {
        return queryForPinnedShortcuts(packageName, null, user);
    }

    public QueryResult queryForPinnedShortcuts(String packageName, List<String> shortcutIds,
                                               UserHandle user) {
        return query(ShortcutQuery.FLAG_MATCH_PINNED, packageName, null, shortcutIds, user);
    }

    /**
     * Query the system server for all the shortcuts matching the given parameters.
     * If packageName == null, we query for all shortcuts with the passed flags, regardless of app.
     * <p>
     * TODO: Use the cache to optimize this so we don't make an RPC every time.
     */
    private QueryResult query(int flags, String packageName, ComponentName activity,
                              List<String> shortcutIds, UserHandle user) {
        ShortcutQuery q = new ShortcutQuery();
        q.setQueryFlags(flags);
        if (packageName != null) {
            q.setPackage(packageName);
            q.setActivity(activity);
            q.setShortcutIds(shortcutIds);
        }
        try {
            return new QueryResult(mLauncherApps.getShortcuts(q, user));
        } catch (SecurityException | IllegalStateException e) {
            Log.e(TAG, "Failed to query for shortcuts", e);
            return QueryResult.FAILURE;
        }
    }

    public static class QueryResult extends ArrayList<ShortcutInfo> {

        static QueryResult FAILURE = new QueryResult();

        private final boolean mWasSuccess;

        QueryResult(List<ShortcutInfo> result) {
            super(result == null ? Collections.emptyList() : result);
            mWasSuccess = true;
        }

        QueryResult() {
            mWasSuccess = false;
        }


        public boolean wasSuccess() {
            return mWasSuccess;
        }
    }
}
