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
