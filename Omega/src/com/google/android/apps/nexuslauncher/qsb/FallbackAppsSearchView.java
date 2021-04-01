package com.google.android.apps.nexuslauncher.qsb;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;

import com.android.launcher3.ExtendedEditText;
import com.android.launcher3.Launcher;
import com.android.launcher3.allapps.AllAppsContainerView;
import com.android.launcher3.allapps.AllAppsStore.OnUpdateListener;
import com.android.launcher3.allapps.AlphabeticalAppsList;
import com.android.launcher3.allapps.search.AllAppsSearchBarController;
import com.android.launcher3.allapps.search.AllAppsSearchBarController.Callbacks;
import com.android.launcher3.util.ComponentKey;
import com.saggitt.omega.search.SearchProvider;
import com.saggitt.omega.search.SearchProviderController;
import com.saggitt.omega.search.webproviders.WebSearchProvider;

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

    @Override
    public boolean onSubmitSearch() {
        SearchProvider provider = getSearchProvider();
        if (provider instanceof WebSearchProvider) {
            ((WebSearchProvider) provider).openResults(getText().toString());
            return true;
        }
        if (mApps.hasNoFilteredResults()) {
            return false;
        }
        Intent i = mApps.getFilteredApps().get(0).getIntent();
        getContext().startActivity(i);
        return true;
    }

    public void onAppsUpdated() {
        mSearchBarController.refreshSearchResult();
    }

    private void hidePredictionRowView(boolean z) {
        mAppsView.getFloatingHeaderView().setCollapsed(z);
    }

    private void notifyResultChanged() {
        allAppsQsbLayout.setShadowAlpha(0);
        mAppsView.onSearchResultsChanged();
    }

    private SearchProvider getSearchProvider() {
        return SearchProviderController.Companion.getInstance(getContext()).getSearchProvider();
    }
}
