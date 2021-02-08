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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.launcher3.BaseRecyclerView;
import com.android.launcher3.DeviceProfile;
import com.android.launcher3.InvariantDeviceProfile;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.allapps.AllAppsContainerView;
import com.android.launcher3.allapps.SearchUiManager;
import com.android.launcher3.anim.PropertySetter;
import com.android.launcher3.util.PackageManagerHelper;
import com.saggitt.omega.OmegaPreferences;
import com.saggitt.omega.search.SearchProvider;
import com.saggitt.omega.search.SearchProviderController;
import com.saggitt.omega.search.SearchThread;
import com.saggitt.omega.search.providers.AppSearchSearchProvider;
import com.saggitt.omega.search.webproviders.WebSearchProvider;

import static com.android.launcher3.InvariantDeviceProfile.CHANGE_FLAG_ICON_PARAMS;
import static com.android.launcher3.LauncherState.ALL_APPS_CONTENT;
import static com.android.launcher3.LauncherState.ALL_APPS_HEADER;
import static com.android.launcher3.LauncherState.HOTSEAT_SEARCH_BOX;
import static com.android.launcher3.anim.Interpolators.LINEAR;
import static com.android.launcher3.anim.PropertySetter.NO_ANIM_PROPERTY_SETTER;

public class AllAppsQsbLayout extends AbstractQsbLayout implements SearchUiManager,
        QsbChangeListener, InvariantDeviceProfile.OnIDPChangeListener {

    private final QsbConfiguration qsbConfiguration;
    private final int mTopAdjusting;
    public float mVerticalOffset;
    public boolean mDoNotRemoveFallback;
    private final boolean mLowPerformanceMode;
    private int mShadowAlpha;
    private boolean mUseFallbackSearch;
    private FallbackAppsSearchView mFallback;
    private TextView mHint;
    private AllAppsContainerView mAppsView;
    private final OmegaPreferences prefs;

    // This value was used to position the QSB. We store it here for translationY animations.
    private final float mFixedTranslationY;
    private final float mMarginTopAdjusting;

    private boolean widgetMode;

    public AllAppsQsbLayout(Context context) {
        this(context, null);
    }

    public AllAppsQsbLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public AllAppsQsbLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        mShadowAlpha = 0;
        setOnClickListener(this);
        qsbConfiguration = QsbConfiguration.getInstance(context);
        mTopAdjusting = getResources().getDimensionPixelSize(R.dimen.qsb_margin_top_adjusting);
        mVerticalOffset = getResources().getDimensionPixelSize(R.dimen.all_apps_search_vertical_offset);
        setClipToPadding(false);
        prefs = OmegaPreferences.Companion.getInstanceNoCreate();

        mLowPerformanceMode = prefs.getLowPerformanceMode();

        mFixedTranslationY = getTranslationY();
        mMarginTopAdjusting = mFixedTranslationY - getPaddingTop();
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        mHint = findViewById(R.id.qsb_hint);
    }

    public void setWidgetMode(boolean enable) {
        widgetMode = enable;
        loadIcons();
        setContentVisibility(HOTSEAT_SEARCH_BOX, NO_ANIM_PROPERTY_SETTER, LINEAR);
    }

    public void setInsets(Rect insets) {
        removeFallBack();
        MarginLayoutParams mlp = (MarginLayoutParams) getLayoutParams();
        mlp.topMargin = Math.round(Math.max(-mFixedTranslationY, insets.top - mMarginTopAdjusting));
        requestLayout();
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        updateConfiguration();
        qsbConfiguration.addListener(this);
    }

    @Override
    public void onIdpChanged(int changeFlags, InvariantDeviceProfile profile) {
        if ((changeFlags & CHANGE_FLAG_ICON_PARAMS) != 0) {
            mAllAppsShadowBitmap = mHotseatShadowBitmap = mBubbleShadowBitmap = mClearBitmap = null;
            addOrUpdateSearchRipple();
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        super.onSharedPreferenceChanged(sharedPreferences, key);
        if (key.equals("pref_allAppsGoogleSearch")) {
            loadPreferences(sharedPreferences);
        }
    }

    @Override
    protected Drawable getIcon(boolean colored) {
        if (prefs.getAllAppsGlobalSearch()) {
            return super.getIcon(colored);
        } else {
            return new AppSearchSearchProvider(getContext()).getIcon(colored);
        }
    }

    @Override
    protected boolean logoCanOpenFeed() {
        return super.logoCanOpenFeed() && prefs.getAllAppsGlobalSearch();
    }

    @Override
    protected Drawable getMicIcon(boolean colored) {
        if (prefs.getAllAppsGlobalSearch()) {
            mMicIconView.setVisibility(View.VISIBLE);
            return super.getMicIcon(colored);
        } else {
            mMicIconView.setVisibility(View.GONE);
            return null;
        }
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        qsbConfiguration.removeListener(this);
    }

    public final void initialize(AllAppsContainerView allAppsContainerView) {
        mAppsView = allAppsContainerView;
        mAppsView.addElevationController(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                setShadowAlpha(((BaseRecyclerView) recyclerView).getCurrentScrollY());
            }
        });
        mAppsView.setRecyclerViewVerticalFadingEdgeEnabled(!mLowPerformanceMode);
    }

    public final void onChange() {
        updateConfiguration();
        invalidate();
    }

    private void updateConfiguration() {
        az(mColor);
        addOrUpdateSearchPaint(qsbConfiguration.micStrokeWidth());
        showHintAssitant = qsbConfiguration.hintIsForAssistant();
        setHintText(qsbConfiguration.hintTextValue(), mHint);
        addOrUpdateSearchRipple();
    }

    public void onClick(View view) {
        super.onClick(view);
        startSearch("", mResult);
    }

    @Override
    public void startSearch() {
        post(() -> startSearch("", mResult));
    }

    @Override
    public final void startSearch(String str, int i) {
        if (mHotseatProgress < 0.5) {
            startDrawerSearch(str, i);
        } else {
            startHotseatSearch();
        }
    }

    private void startDrawerSearch(String str, int i) {
        SearchProviderController controller = SearchProviderController.Companion
                .getInstance(getContext());
        SearchProvider provider = controller.getSearchProvider();
        if (shouldUseFallbackSearch(provider)) {
            searchFallback(str);
        } else if (controller.isGoogle()) {
            final ConfigBuilder f = new ConfigBuilder(this, true);
            if (!getLauncher().getGoogleNow().startSearch(f.build(), f.getExtras())) {
                searchFallback(str);
                if (mFallback != null) {
                    mFallback.setHint(null);
                }
            }
        } else {
            provider.startSearch(intent -> {
                getLauncher().startActivity(intent);
                return null;
            });
        }
    }

    private void startHotseatSearch() {
        SearchProviderController controller = SearchProviderController.Companion.getInstance(getContext());
        if (controller.isGoogle()) {
            startGoogleSearch();
        } else {
            controller.getSearchProvider().startSearch(intent -> {
                getLauncher().openQsb();
                getContext().startActivity(intent, ActivityOptionsCompat
                        .makeClipRevealAnimation(this, 0, 0, getWidth(), getHeight()).toBundle());
                return null;
            });
        }
    }

    private void startGoogleSearch() {
        final ConfigBuilder f = new ConfigBuilder(this, false);
        if (!forceFallbackSearch() && getLauncher().getGoogleNow()
                .startSearch(f.build(), f.getExtras())) {
            SharedPreferences devicePrefs = Utilities.getDevicePrefs(getContext());
            devicePrefs.edit().putInt("key_hotseat_qsb_tap_count",
                    devicePrefs.getInt("key_hotseat_qsb_tap_count", 0) + 1).apply();
            getLauncher().playQsbAnimation();
        } else {
            getContext().sendOrderedBroadcast(getSearchIntent(), null,
                    new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            if (getResultCode() == 0) {
                                fallbackSearch(
                                        "com.google.android.googlequicksearchbox.TEXT_ASSIST");
                            } else {
                                getLauncher().playQsbAnimation();
                            }
                        }
                    }, null, 0, null, null);
        }
    }

    private boolean forceFallbackSearch() {
        return !PackageManagerHelper.isAppEnabled(getContext().getPackageManager(),
                "com.google.android.apps.nexuslauncher", 0);
    }

    private Intent getSearchIntent() {
        int[] array = new int[2];
        getLocationInWindow(array);
        Rect rect = new Rect(0, 0, getWidth(), getHeight());
        rect.offset(array[0], array[1]);
        rect.inset(getPaddingLeft(), getPaddingTop());
        return ConfigBuilder.getSearchIntent(rect, findViewById(R.id.g_icon), mMicIconView);
    }

    private boolean shouldUseFallbackSearch() {
        SearchProviderController controller = SearchProviderController.Companion.getInstance(mContext);
        SearchProvider provider = controller.getSearchProvider();
        return shouldUseFallbackSearch(provider);
    }

    private boolean shouldUseFallbackSearch(SearchProvider provider) {
        return !Utilities
                .getOmegaPrefs(getContext()).getAllAppsGlobalSearch()
                || provider instanceof AppSearchSearchProvider
                || provider instanceof WebSearchProvider;
    }

    public void searchFallback(String query) {
        ensureFallbackView();
        mFallback.setText(query);
        mFallback.showKeyboard();
    }

    public final void resetSearch() {
        setShadowAlpha(0);
        if (mUseFallbackSearch) {
            resetFallbackView();
        } else if (!mDoNotRemoveFallback) {
            removeFallbackView();
        }
    }

    @Override
    public void preDispatchKeyEvent(KeyEvent event) {
    }

    private void ensureFallbackView() {
        if (mFallback == null) {
            setOnClickListener(null);
            mFallback = (FallbackAppsSearchView) getLauncher().getLayoutInflater()
                    .inflate(R.layout.all_apps_google_search_fallback, this, false);
            AllAppsContainerView allAppsContainerView = this.mAppsView;
            mFallback.allAppsQsbLayout = this;
            mFallback.mApps = allAppsContainerView.getApps();
            mFallback.mAppsView = allAppsContainerView;
            mFallback.mSearchBarController.initialize(new SearchThread(mFallback.getContext()), mFallback,
                    Launcher.getLauncher(mFallback.getContext()), mFallback);
            addView(mFallback);
        }
    }

    private void removeFallbackView() {
        if (mFallback != null) {
            mFallback.clearSearchResult();
            setOnClickListener(this);
            removeView(mFallback);
            mFallback = null;
        }
    }

    private void resetFallbackView() {
        if (mFallback != null) {
            mFallback.reset();
            mFallback.clearSearchResult();
        }
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        View parent = (View) getParent();
        setTranslationX((float) ((parent.getPaddingLeft() + (
                (((parent.getWidth() - parent.getPaddingLeft()) - parent.getPaddingRight()) - (right - left))
                        / 2)) - left));
        int containerTopMargin;
        if (!prefs.getAllAppsSearch()) {
            MarginLayoutParams mlp = (MarginLayoutParams) getLayoutParams();
            containerTopMargin = -(mlp.topMargin + mlp.height - 125);
            offsetTopAndBottom((int) mVerticalOffset - containerTopMargin);
        }

    }

    final void setShadowAlpha(int i) {
        i = Utilities.boundToRange(i, 0, 255);
        if (mShadowAlpha != i) {
            mShadowAlpha = i;
            invalidate();
        }
    }

    protected final boolean dK() {
        if (mFallback != null) {
            return false;
        }
        return super.dK();
    }

    protected final void removeFallBack() {
        if (mUseFallbackSearch) {
            removeFallbackView();
            mUseFallbackSearch = false;
        }
    }

    public boolean hasOverlappingRendering() {
        return false;
    }

    //Used when search bar is disabled
    public int getTopMargin(Rect rect) {
        return Math.max(Math.round(-mVerticalOffset), rect.top - mTopAdjusting);
    }

    @Override
    public float getScrollRangeDelta(Rect insets) {
        DeviceProfile wallpaperDeviceProfile = mActivity.getWallpaperDeviceProfile();
        int i = (wallpaperDeviceProfile.hotseatBarSizePx - wallpaperDeviceProfile.hotseatCellHeightPx) - getLayoutParams().height;
        int bottom = insets.bottom;
        return ((getLayoutParams().height + Math.max(-mVerticalOffset, insets.top - mTopAdjusting)) +
                mVerticalOffset) + (bottom + (i - bottom) * 0.45f);
    }

    @Override
    public void setContentVisibility(int visibleElements, PropertySetter setter, Interpolator interpolator) {
        OmegaPreferences prefs = Utilities.getOmegaPrefs(getContext());
        boolean hotseatQsbEnabled = prefs.getDockSearchBar() || widgetMode;
        boolean hotseatQsbVisible = (visibleElements & HOTSEAT_SEARCH_BOX) != 0;
        boolean drawerQsbEnabled = prefs.getAllAppsSearch();
        boolean drawerQsbVisible = (visibleElements & ALL_APPS_HEADER) != 0;
        boolean qsbVisible = (hotseatQsbEnabled && hotseatQsbVisible) || (drawerQsbEnabled && drawerQsbVisible);
        float hotseatProgress, micProgress;
        if (!hotseatQsbEnabled) {
            hotseatProgress = 0;
        } else if (!drawerQsbEnabled) {
            hotseatProgress = 1;
        } else {
            hotseatProgress = (visibleElements & ALL_APPS_CONTENT) != 0 ? 0 : 1;
        }
        if (prefs.getAllAppsGlobalSearch()) {
            micProgress = 1f;
        } else {
            micProgress = hotseatProgress;
        }
        setter.setFloat(this, HOTSEAT_PROGRESS, hotseatProgress, LINEAR);
        setter.setViewAlpha(this, qsbVisible ? 1 : 0, interpolator);
        setter.setViewAlpha(mLogoIconView, 1 - hotseatProgress, interpolator);
        setter.setViewAlpha(mHotseatLogoIconView, hotseatProgress, interpolator);
        setter.setViewAlpha(mMicIconView, micProgress, interpolator);
        if (mMicIconView != null) {
            mMicIconView.setVisibility(micProgress > 0 ? View.VISIBLE : View.INVISIBLE);
        }
    }

    @Nullable
    @Override
    protected String getClipboardText() {
        return shouldUseFallbackSearch() ? super.getClipboardText() : null;
    }

    @Override
    protected void clearMainPillBg(Canvas canvas) {
        if (!mLowPerformanceMode && mClearBitmap != null) {
            drawPill(mClearShadowHelper, mClearBitmap, canvas);
        }
    }

    @Override
    protected void clearPillBg(Canvas canvas, int left, int top, int right) {
        if (!mLowPerformanceMode && mClearBitmap != null) {
            mClearShadowHelper.draw(mClearBitmap, canvas, left, top, right);
        }
    }
}