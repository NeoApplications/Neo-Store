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

package com.saggitt.omega.qsb;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;

import com.android.launcher3.ExtendedEditText;
import com.android.launcher3.Launcher;
import com.android.launcher3.allapps.AllAppsContainerView;
import com.android.launcher3.allapps.AllAppsStore.OnUpdateListener;
import com.android.launcher3.allapps.AlphabeticalAppsList;
import com.android.launcher3.allapps.FloatingHeaderView;
import com.android.launcher3.allapps.search.AllAppsSearchBarController;
import com.android.launcher3.allapps.search.AllAppsSearchBarController.Callbacks;
import com.android.launcher3.util.ComponentKey;

import java.util.ArrayList;
import java.util.List;

public class FallbackAppsSearchView extends ExtendedEditText implements OnUpdateListener, Callbacks {
    AllAppsSearchBarController mSearchBarController;
    AllAppsQsbLayout allAppsQsbLayout;
    AlphabeticalAppsList mApps;
    AllAppsContainerView mAppsView;

    public FallbackAppsSearchView(Context context) {
        this(context, null);
    }

    public FallbackAppsSearchView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public FallbackAppsSearchView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        mSearchBarController = new AllAppsSearchBarController();
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Launcher.getLauncher(getContext()).getAppsView().getAppsStore().addUpdateListener(this);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Launcher.getLauncher(getContext()).getAppsView().getAppsStore().removeUpdateListener(this);
    }

    @Override
    public boolean onSubmitSearch() {
        if (mApps.hasNoFilteredResults()) {
            return false;
        }
        Intent i = mApps.getFilteredApps().get(0).getIntent();
        getContext().startActivity(i);
        return true;
    }

    @Override
    public void onSearchResult(String query, ArrayList<ComponentKey> apps, List<String> suggestions) {
        if (getParent() != null) {
            if (apps != null) {
                mApps.setOrderedFilter(apps);
            }
            if (suggestions != null) {
                mApps.setSearchSuggestions(suggestions);
            }
            if (apps != null || suggestions != null) {
                notifyResultChanged();
                hidePredictionRowView(true);
                mAppsView.setLastSearchQuery(query);
            }
        }
    }

    @Override
    public final void clearSearchResult() {
        if (getParent() != null) {
            if (mApps.setOrderedFilter(null) || mApps.setSearchSuggestions(null)) {
                notifyResultChanged();
            }
            hidePredictionRowView(false);
            allAppsQsbLayout.mDoNotRemoveFallback = true;
            mAppsView.onClearSearchResult();
            allAppsQsbLayout.mDoNotRemoveFallback = false;
        }
    }


    public void onAppsUpdated() {
        mSearchBarController.refreshSearchResult();
    }

    private void hidePredictionRowView(boolean z) {
        FloatingHeaderView predictionsFloatingHeader = mAppsView.getFloatingHeaderView();
        predictionsFloatingHeader.setCollapsed(z);
    }

    private void notifyResultChanged() {
        allAppsQsbLayout.setShadowAlpha(0);
        mAppsView.onSearchResultsChanged();
    }
}
