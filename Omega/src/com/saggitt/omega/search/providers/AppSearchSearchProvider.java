/*
 *  This file is part of Omega Launcher.
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

package com.saggitt.omega.search.providers;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;

import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.LauncherState;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.saggitt.omega.search.SearchProvider;

import org.jetbrains.annotations.NotNull;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class AppSearchSearchProvider extends SearchProvider {
    private Context mContext;

    public AppSearchSearchProvider(Context context) {
        super(context);
        mContext = context;

    }

    @Override
    public void startSearch(@NotNull Function1<? super Intent, Unit> callback) {
        Launcher launcher = LauncherAppState.getInstanceNoCreate().getLauncher();
        launcher.getStateManager().goToState(LauncherState.ALL_APPS, true, (Runnable) (new Runnable() {
            public final void run() {
                launcher.getAppsView().getSearchUiManager().startSearch();
            }
        }));
    }

    @NotNull
    @Override
    public Drawable getIcon() {
        Drawable icon = mContext.getDrawable(R.drawable.ic_allapps_search).mutate();
        icon.setTint(Utilities.getOmegaPrefs(mContext).getAccentColor());
        return icon;
    }

    @NotNull
    @Override
    public String getName() {
        return mContext.getString(R.string.search_provider_appsearch);
    }

    @Override
    public boolean getSupportsAssistant() {
        return false;
    }

    @Override
    public boolean getSupportsFeed() {
        return false;
    }

    @Override
    public boolean getSupportsVoiceSearch() {
        return false;
    }
}
