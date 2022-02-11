/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.launcher3.allapps;

import static com.android.launcher3.model.BgDataModel.Callbacks.FLAG_HAS_SHORTCUT_PERMISSION;
import static com.android.launcher3.model.BgDataModel.Callbacks.FLAG_QUIET_MODE_CHANGE_PERMISSION;
import static com.android.launcher3.model.BgDataModel.Callbacks.FLAG_QUIET_MODE_ENABLED;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.Process;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.VisibleForTesting;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.launcher3.BaseDraggingActivity;
import com.android.launcher3.DeviceProfile;
import com.android.launcher3.DeviceProfile.OnDeviceProfileChangeListener;
import com.android.launcher3.DragSource;
import com.android.launcher3.DropTarget.DragObject;
import com.android.launcher3.Insettable;
import com.android.launcher3.InsettableFrameLayout;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.allapps.search.SearchAdapterProvider;
import com.android.launcher3.config.FeatureFlags;
import com.android.launcher3.keyboard.FocusedItemDecorator;
import com.android.launcher3.model.data.AppInfo;
import com.android.launcher3.testing.TestProtocol;
import com.android.launcher3.util.ItemInfoMatcher;
import com.android.launcher3.util.Themes;
import com.android.launcher3.views.RecyclerViewFastScroller;
import com.android.launcher3.views.ScrimView;
import com.android.launcher3.views.SpringRelativeLayout;
import com.android.launcher3.workprofile.PersonalWorkSlidingTabStrip;
import com.saggitt.omega.allapps.AllAppsTabItem;
import com.saggitt.omega.allapps.AllAppsTabs;
import com.saggitt.omega.allapps.AllAppsTabsController;
import com.saggitt.omega.util.OmegaUtilsKt;

/**
 * The all apps view container.
 */
public class AllAppsContainerView extends SpringRelativeLayout implements DragSource,
        Insettable, OnDeviceProfileChangeListener, PersonalWorkSlidingTabStrip.OnActivePageChangedListener,
        ScrimView.ScrimDrawingController {

    private static final String BUNDLE_KEY_CURRENT_PAGE = "launcher.allapps.current_page";

    public static final float PULL_MULTIPLIER = .02f;
    public static final float FLING_VELOCITY_MULTIPLIER = 1200f;

    private final Paint mHeaderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    protected final BaseDraggingActivity mLauncher;
    protected AdapterHolder[] mAH;
    private final ItemInfoMatcher mPersonalMatcher = ItemInfoMatcher.ofUser(Process.myUserHandle());
    private final ItemInfoMatcher mWorkMatcher = mPersonalMatcher.negate();
    private final AllAppsStore mAllAppsStore = new AllAppsStore();

    private final RecyclerView.OnScrollListener mScrollListener =
            new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    updateHeaderScroll(((AllAppsRecyclerView) recyclerView).getCurrentScrollY());
                }
            };

    private final Paint mNavBarScrimPaint;
    private int mNavBarScrimHeight = 0;

    protected SearchUiManager mSearchUiManager;
    private View mSearchContainer;
    private AllAppsPagedView mViewPager;

    protected FloatingHeaderView mHeader;
    private float mHeaderTop;
    private WorkModeSwitch mWorkModeSwitch;

    private SpannableStringBuilder mSearchQueryBuilder = null;

    protected boolean mUsingTabs;
    private boolean mIsSearching;
    private boolean mHasWorkApps;

    protected RecyclerViewFastScroller mTouchHandler;
    protected final Point mFastScrollerOffset = new Point();

    private Rect mInsets = new Rect();

    private SearchAdapterProvider mSearchAdapterProvider;
    private WorkAdapterProvider mWorkAdapterProvider;
    private final int mScrimColor;
    private final int mHeaderProtectionColor;
    private final float mHeaderThreshold;
    private ScrimView mScrimView;
    private int mHeaderColor;
    private int mTabsProtectionAlpha;
    private AllAppsTabsController mTabsController;

    public AllAppsContainerView(Context context) {
        this(context, null);
    }

    public AllAppsContainerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AllAppsContainerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mLauncher = BaseDraggingActivity.fromContext(context);

        mScrimColor = Themes.getAttrColor(context, R.attr.allAppsScrimColor);
        mHeaderThreshold = getResources().getDimensionPixelSize(
                R.dimen.dynamic_grid_cell_border_spacing);
        mHeaderProtectionColor = Themes.getAttrColor(context, R.attr.allappsHeaderProtectionColor);

        mLauncher.addOnDeviceProfileChangeListener(this);

        mSearchAdapterProvider = mLauncher.createSearchAdapterProvider(this);
        mSearchQueryBuilder = new SpannableStringBuilder();
        Selection.setSelection(mSearchQueryBuilder, 0);

        AllAppsTabs allAppsTabs = new AllAppsTabs(context);
        mTabsController = new AllAppsTabsController(allAppsTabs, this);
        createHolders();

        mWorkAdapterProvider = new WorkAdapterProvider(mLauncher, () -> {
            for (AdapterHolder holder : mAH) {
                if (holder.mIsWork) {
                    holder.appsList.updateAdapterItems();
                }
            }
        });

        mNavBarScrimPaint = new Paint();
        mNavBarScrimPaint.setColor(Themes.getAttrColor(context, R.attr.allAppsNavBarScrimColor));

        mAllAppsStore.addUpdateListener(this::onAppsUpdated);
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> sparseArray) {
        try {
            // Many slice view id is not properly assigned, and hence throws null
            // pointer exception in the underneath method. Catching the exception
            // simply doesn't restore these slice views. This doesn't have any
            // user visible effect because because we query them again.
            super.dispatchRestoreInstanceState(sparseArray);
        } catch (Exception e) {
            Log.e("AllAppsContainerView", "restoreInstanceState viewId = 0", e);
        }

        Bundle state = (Bundle) sparseArray.get(R.id.work_tab_state_id, null);
        if (state != null) {
            int currentPage = state.getInt(BUNDLE_KEY_CURRENT_PAGE, 0);
            if (currentPage != 0 && mViewPager != null) {
                mViewPager.setCurrentPage(currentPage);
                rebindAdapters(false);
            } else {
                reset(true);
            }
        }

    }

    @Override
    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        super.dispatchSaveInstanceState(container);
        Bundle state = new Bundle();
        state.putInt(BUNDLE_KEY_CURRENT_PAGE, getCurrentPage());
        container.put(R.id.work_tab_state_id, state);
    }

    /**
     * Sets the long click listener for icons
     */
    public void setOnIconLongClickListener(OnLongClickListener listener) {
        for (AdapterHolder holder : mAH) {
            holder.adapter.setOnIconLongClickListener(listener);
        }
    }

    private void createHolders() {
        mAH = mTabsController.createHolders();
    }

    public AllAppsStore getAppsStore() {
        return mAllAppsStore;
    }

    public WorkModeSwitch getWorkModeSwitch() {
        return mWorkModeSwitch;
    }

    @Override
    public void onDeviceProfileChanged(DeviceProfile dp) {
        for (AdapterHolder holder : mAH) {
            holder.adapter.setAppsPerRow(dp.numShownAllAppsColumns);
            if (holder.recyclerView != null) {
                // Remove all views and clear the pool, while keeping the data same. After this
                // call, all the viewHolders will be recreated.
                holder.recyclerView.swapAdapter(holder.recyclerView.getAdapter(), true);
                holder.recyclerView.getRecycledViewPool().clear();
            }
        }
    }

    private void onAppsUpdated() {
        boolean force = false;
        boolean hasWorkApps = false;

        if (Utilities.getOmegaPrefs(getContext()).getSeparateWorkApps()) {
            for (AppInfo app : mAllAppsStore.getApps()) {
                if (mWorkMatcher.matches(app, null)) {
                    hasWorkApps = true;
                    break;
                }
            }

            rebindAdapters(mHasWorkApps);
            if (mHasWorkApps && mWorkModeSwitch != null) {
                resetWorkProfile();
            }
            AllAppsTabs allAppsTabs = mTabsController.getTabs();
            force = allAppsTabs.getHasWorkApps() != hasWorkApps;
            allAppsTabs.setHasWorkApps(hasWorkApps);
        }
        rebindAdapters(mTabsController.getShouldShowTabs(), force);
    }

    private void resetWorkProfile() {
        boolean isEnabled = !mAllAppsStore.hasModelFlag(FLAG_QUIET_MODE_ENABLED);
        for (AdapterHolder adapterHolder : mAH) {
            if (adapterHolder.mIsWork) {
                mWorkModeSwitch.updateCurrentState(isEnabled);
                adapterHolder.applyPadding();
            }
        }
        mWorkAdapterProvider.updateCurrentState(isEnabled);
    }

    /**
     * Returns whether the view itself will handle the touch event or not.
     */
    public boolean shouldContainerScroll(MotionEvent ev) {
        // IF the MotionEvent is inside the search box, and the container keeps on receiving
        // touch input, container should move down.
        if (mLauncher.getDragLayer().isEventOverView(mSearchContainer, ev)) {
            return true;
        }
        AllAppsRecyclerView rv = getActiveRecyclerView();
        if (rv == null) {
            return true;
        }
        if (rv.getScrollbar().getThumbOffsetY() >= 0 &&
                mLauncher.getDragLayer().isEventOverView(rv.getScrollbar(), ev)) {
            return false;
        }
        return rv.shouldContainerScroll(ev, mLauncher.getDragLayer());
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            AllAppsRecyclerView rv = getActiveRecyclerView();
            if (rv != null &&
                    rv.getScrollbar().isHitInParent(ev.getX(), ev.getY(), mFastScrollerOffset)) {
                mTouchHandler = rv.getScrollbar();
            } else {
                mTouchHandler = null;
            }
        }
        if (mTouchHandler != null) {
            return mTouchHandler.handleTouchEvent(ev, mFastScrollerOffset);
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            AllAppsRecyclerView rv = getActiveRecyclerView();
            if (rv != null && rv.getScrollbar().isHitInParent(ev.getX(), ev.getY(),
                    mFastScrollerOffset)) {
                mTouchHandler = rv.getScrollbar();
            } else {
                mTouchHandler = null;

            }
        }
        if (mTouchHandler != null) {
            mTouchHandler.handleTouchEvent(ev, mFastScrollerOffset);
            return true;
        }
        return false;
    }

    public String getDescription() {
        @StringRes int descriptionRes;
        if (mUsingTabs) {
            descriptionRes =
                    mViewPager.getNextPage() == 0
                            ? R.string.all_apps_button_personal_label
                            : R.string.all_apps_button_work_label;
        } else if (mIsSearching) {
            descriptionRes = R.string.all_apps_search_results;
        } else {
            descriptionRes = R.string.all_apps_button_label;
        }
        return getContext().getString(descriptionRes);
    }

    public AllAppsRecyclerView getActiveRecyclerView() {
        if (!mUsingTabs || mViewPager.getNextPage() == 0) {
            return mAH[AdapterHolder.MAIN].recyclerView;
        } else {
            return mAH[AdapterHolder.WORK].recyclerView;
        }
    }

    public LayoutInflater getLayoutInflater() {
        return LayoutInflater.from(getContext());
    }

    /**
     * Resets the state of AllApps.
     */
    public void reset(boolean animate) {
        reset(animate, false);
    }

    public void reset(boolean animate, boolean force) {
        if (force || !Utilities.getOmegaPrefs(getContext()).getSaveScrollPosition()) {
            for (AdapterHolder adapterHolder : mAH) {
                if (adapterHolder.recyclerView != null) {
                    adapterHolder.recyclerView.scrollToTop();
                }
            }
            if (isHeaderVisible()) {
                mHeader.reset(animate);
            }
            // Reset the search bar and base recycler view after transitioning home
            mSearchUiManager.resetSearch();
            updateHeaderScroll(0);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        // This is a focus listener that proxies focus from a view into the list view.  This is to
        // work around the search box from getting first focus and showing the cursor.
        setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && getActiveRecyclerView() != null) {
                getActiveRecyclerView().requestFocus();
            }
        });

        mHeader = findViewById(R.id.all_apps_header);
        rebindAdapters(mUsingTabs, true /* force */);
        mSearchContainer = findViewById(R.id.search_container_all_apps);
        mSearchUiManager = (SearchUiManager) mSearchContainer;
        mSearchUiManager.initializeSearch(this);
    }

    public SearchUiManager getSearchUiManager() {
        return mSearchUiManager;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        mSearchUiManager.preDispatchKeyEvent(event);
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onDropCompleted(View target, DragObject d, boolean success) {
    }

    @Override
    public void setInsets(Rect insets) {
        mInsets.set(insets);
        DeviceProfile grid = mLauncher.getDeviceProfile();
        int leftRightPadding = grid.desiredWorkspaceLeftRightMarginPx
                + grid.cellLayoutPaddingLeftRightPx;

        /*for (int i = 0; i < mAH.length; i++) {
            mAH[i].padding.bottom = insets.bottom;
            mAH[i].padding.left = mAH[i].padding.right = leftRightPadding;
            mAH[i].applyPadding();
        }*/
        mTabsController.setPadding(leftRightPadding, insets.bottom);

        ViewGroup.MarginLayoutParams mlp = (MarginLayoutParams) getLayoutParams();
        mlp.leftMargin = insets.left;
        mlp.rightMargin = insets.right;
        setLayoutParams(mlp);

        if (grid.isVerticalBarLayout()) {
            setPadding(grid.workspacePadding.left, 0, grid.workspacePadding.right, 0);
        } else {
            setPadding(0, 0, 0, 0);
        }

        InsettableFrameLayout.dispatchInsets(this, insets);
    }

    @Override
    public WindowInsets dispatchApplyWindowInsets(WindowInsets insets) {
        if (Utilities.ATLEAST_Q) {
            mNavBarScrimHeight = insets.getTappableElementInsets().bottom
                    - mLauncher.getDeviceProfile().nonOverlappingTaskbarInset;
        } else {
            mNavBarScrimHeight = insets.getStableInsetBottom();
        }
        return super.dispatchApplyWindowInsets(insets);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        if (mNavBarScrimHeight > 0) {
            canvas.drawRect(0, getHeight() - mNavBarScrimHeight, getWidth(), getHeight(),
                    mNavBarScrimPaint);
        }
    }

    public void reloadTabs() {
        mTabsController.reloadTabs();
        rebindAdapters(mTabsController.getShouldShowTabs(), true);
    }

    private void rebindAdapters(boolean showTabs) {
        rebindAdapters(showTabs, false);
    }

    protected void rebindAdapters(boolean showTabs, boolean force) {
        if (showTabs == mUsingTabs && !force) {
            return;
        }

        int currentTab = mViewPager != null ? mViewPager.getNextPage() : 0;
        mTabsController.unregisterIconContainers(mAllAppsStore);

        createHolders();
        replaceRVContainer(showTabs);
        mUsingTabs = showTabs;

        if (mTabsController.getTabs().getHasWorkApps())
            setupWorkToggle();

        //mAllAppsStore.unregisterIconContainer(mAH[AdapterHolder.MAIN].recyclerView);
        //mAllAppsStore.unregisterIconContainer(mAH[AdapterHolder.WORK].recyclerView);

        if (mUsingTabs) {
            //mAH[AdapterHolder.MAIN].setup(mViewPager.getChildAt(0), mPersonalMatcher);
            ///mAH[AdapterHolder.WORK].setup(mViewPager.getChildAt(1), mWorkMatcher);
            //mAH[AdapterHolder.WORK].recyclerView.setId(R.id.apps_list_view_work);
            /*mViewPager.getPageIndicator().setActiveMarker(AdapterHolder.MAIN);
            findViewById(R.id.tab_personal)
                    .setOnClickListener((View view) -> {
                        if (mViewPager.snapToPage(AdapterHolder.MAIN)) {
                            mLauncher.getStatsLogManager().logger()
                                    .log(LAUNCHER_ALLAPPS_TAP_ON_PERSONAL_TAB);
                        }
                    });
            findViewById(R.id.tab_work)
                    .setOnClickListener((View view) -> {
                        if (mViewPager.snapToPage(AdapterHolder.WORK)) {
                            mLauncher.getStatsLogManager().logger()
                                    .log(LAUNCHER_ALLAPPS_TAP_ON_WORK_TAB);
                        }
                    });
            onActivePageChanged(mViewPager.getNextPage());*/

            mTabsController.setup(mViewPager);
            AllAppsTabItem tabStrip = findViewById(R.id.tabs);
            tabStrip.inflateButtons(mTabsController.getTabs());
            if (currentTab == 0) {
                tabStrip.setScroll(0, 1);
            }
            onTabChanged(currentTab);
        } else {
            mTabsController.setup((View) findViewById(R.id.apps_list_view));
            AllAppsRecyclerView recyclerView = mAH[AdapterHolder.MAIN].recyclerView;
            if (recyclerView != null) {
                OmegaUtilsKt.runOnAttached(recyclerView, () ->
                        recyclerView.setScrollbarColor(Utilities.getOmegaPrefs(getContext()).getAccentColor()));
            }
        }
        setupHeader();

        mTabsController.registerIconContainers(mAllAppsStore);
        if (mViewPager != null) {
            mViewPager.snapToPage(Math.min(mTabsController.getTabsCount() - 1, currentTab), 0);
        }
    }

    private void setupWorkToggle() {
        removeWorkToggle();
        if (Utilities.ATLEAST_P) {
            mWorkModeSwitch = (WorkModeSwitch) mLauncher.getLayoutInflater().inflate(
                    R.layout.work_mode_fab, this, false);
            this.addView(mWorkModeSwitch);
            mWorkModeSwitch.setInsets(mInsets);
            mWorkModeSwitch.post(() -> {
                for (int i = 0; i < mAH.length; i++) {
                    if (mAH[i].mIsWork) {
                        mAH[i].applyPadding();
                    }
                }

                resetWorkProfile();
            });
        }
    }

    private void removeWorkToggle() {
        if (mWorkModeSwitch == null) return;
        if (mWorkModeSwitch.getParent() == this) {
            this.removeView(mWorkModeSwitch);
        }
        mWorkModeSwitch = null;
    }

    private void replaceRVContainer(boolean showTabs) {
        for (int i = 0; i < mAH.length; i++) {
            AllAppsRecyclerView rv = mAH[i].recyclerView;
            if (rv != null) {
                rv.setLayoutManager(null);
                rv.setAdapter(null);
            }
        }
        View oldView = getRecyclerViewContainer();
        int index = indexOfChild(oldView);
        removeView(oldView);
        int layout = showTabs ? R.layout.all_apps_tabs : R.layout.all_apps_rv_layout;
        View newView = getLayoutInflater().inflate(layout, this, false);
        addView(newView, index);
        if (TestProtocol.sDebugTracing) {
            Log.d(TestProtocol.WORK_PROFILE_REMOVED, "should show tabs:" + showTabs,
                    new Exception());
        }
        if (showTabs) {
            mViewPager = (AllAppsPagedView) newView;
            mViewPager.addTabs(mTabsController.getTabsCount());
            mViewPager.initParentViews(this);
            mViewPager.getPageIndicator().setOnActivePageChangedListener(this);
            //setupWorkToggle();
        } else {
            mViewPager = null;
            removeWorkToggle();
        }
    }

    public View getRecyclerViewContainer() {
        return mViewPager != null ? mViewPager : findViewById(R.id.apps_list_view);
    }

    public void onTabChanged(int pos) {
        pos = Utilities.boundToRange(pos, 0, mTabsController.getTabsCount() - 1);
        mHeader.setCurrentActive(pos);
        if (mAH[pos].recyclerView != null) {
            mAH[pos].recyclerView.bindFastScrollbar();
            mAH[pos].recyclerView.setScrollbarColor(Utilities.getOmegaPrefs(getContext()).getAccentColor());
            mTabsController.bindButtons(findViewById(R.id.tabs), mViewPager);
        }
        reset(true /* animate */, true);
    }

    @Override
    public void onActivePageChanged(int currentActivePage) {
        mHeader.setCurrentActive(currentActivePage);
        if (mAH[currentActivePage].recyclerView != null) {
            mAH[currentActivePage].recyclerView.bindFastScrollbar();
        }
        reset(true /* animate */);
        if (mWorkModeSwitch != null) {
            mWorkModeSwitch.setWorkTabVisible(currentActivePage == AdapterHolder.WORK
                    && mAllAppsStore.hasModelFlag(
                    FLAG_HAS_SHORTCUT_PERMISSION | FLAG_QUIET_MODE_CHANGE_PERMISSION));
        }
    }

    // Used by tests only
    private boolean isDescendantViewVisible(int viewId) {
        final View view = findViewById(viewId);
        if (view == null) return false;

        if (!view.isShown()) return false;

        return view.getGlobalVisibleRect(new Rect());
    }

    @VisibleForTesting
    public boolean isPersonalTabVisible() {
        return isDescendantViewVisible(R.id.tab_personal);
    }

    // Used by tests only
    public boolean isWorkTabVisible() {
        return isDescendantViewVisible(R.id.tab_work);
    }

    public AlphabeticalAppsList getApps() {
        return mAH[AdapterHolder.MAIN].appsList;
    }

    public FloatingHeaderView getFloatingHeaderView() {
        return mHeader;
    }

    public View getSearchView() {
        return mSearchContainer;
    }

    public View getContentView() {
        return mViewPager == null ? getActiveRecyclerView() : mViewPager;
    }

    public int getCurrentPage() {
        return mViewPager != null ? mViewPager.getCurrentPage() : AdapterHolder.MAIN;
    }

    /**
     * Handles selection on focused view and returns success
     */
    public boolean launchHighlightedItem() {
        if (mSearchAdapterProvider == null) return false;
        return mSearchAdapterProvider.launchHighlightedItem();
    }

    public SearchAdapterProvider getSearchAdapterProvider() {
        return mSearchAdapterProvider;
    }

    public RecyclerViewFastScroller getScrollBar() {
        AllAppsRecyclerView rv = getActiveRecyclerView();
        return rv == null ? null : rv.getScrollbar();
    }

    public void setupHeader() {
        mHeader.setVisibility(View.VISIBLE);
        mHeader.setup(mAH, !mUsingTabs);
        int padding = mHeader.getMaxTranslation();
        for (int i = 0; i < mAH.length; i++) {
            mAH[i].padding.top = padding;
            mAH[i].applyPadding();
        }
        mHeaderTop = mHeader.getTop();
    }

    public void setLastSearchQuery(String query) {
        for (int i = 0; i < mAH.length; i++) {
            mAH[i].adapter.setLastSearchQuery(query);
        }

        if (mUsingTabs) {
            mIsSearching = true;
            rebindAdapters(false); // hide tabs
        }
        mHeader.setCollapsed(true);
    }

    public void onClearSearchResult() {
        if ((mIsSearching)) {
            mIsSearching = false;
            rebindAdapters(true); // show tabs
            getActiveRecyclerView().scrollToTop();
        }
    }

    public void onSearchResultsChanged() {
        for (int i = 0; i < mAH.length; i++) {
            if (mAH[i].recyclerView != null) {
                mAH[i].recyclerView.onSearchResultsChanged();
            }
        }
    }

    public void setRecyclerViewVerticalFadingEdgeEnabled(boolean enabled) {
        for (int i = 0; i < mAH.length; i++) {
            mAH[i].applyVerticalFadingEdgeEnabled(enabled);
        }
    }

    public void addElevationController(RecyclerView.OnScrollListener scrollListener) {
        if (!mUsingTabs) {
            mAH[AdapterHolder.MAIN].recyclerView.addOnScrollListener(scrollListener);
        }
    }

    public boolean isHeaderVisible() {
        return mHeader != null && mHeader.getVisibility() == View.VISIBLE;
    }

    /**
     * Adds an update listener to {@param animator} that adds springs to the animation.
     */
    public void addSpringFromFlingUpdateListener(ValueAnimator animator,
                                                 float velocity /* release velocity */,
                                                 float progress /* portion of the distance to travel*/) {
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animator) {
                float distance = (float) ((1 - progress) * getHeight()); // px
                float settleVelocity = Math.min(0, distance
                        / (AllAppsTransitionController.INTERP_COEFF * animator.getDuration())
                        + velocity);
                absorbSwipeUpVelocity(Math.max(1000, Math.abs(
                        Math.round(settleVelocity * FLING_VELOCITY_MULTIPLIER))));
            }
        });
    }

    public void onPull(float deltaDistance, float displacement) {
        absorbPullDeltaDistance(PULL_MULTIPLIER * deltaDistance, PULL_MULTIPLIER * displacement);
        // Current motion spec is to actually push and not pull
        // on this surface. However, until EdgeEffect.onPush (b/190612804) is
        // implemented at view level, we will simply pull
    }

    @Override
    public void getDrawingRect(Rect outRect) {
        super.getDrawingRect(outRect);
        outRect.offset(0, (int) getTranslationY());
    }

    @Override
    public void setTranslationY(float translationY) {
        super.setTranslationY(translationY);
        invalidateHeader();
    }

    public void setScrimView(ScrimView scrimView) {
        mScrimView = scrimView;
    }

    @Override
    public void drawOnScrim(Canvas canvas) {
        mHeaderPaint.setColor(mHeaderColor);
        mHeaderPaint.setAlpha((int) (getAlpha() * Color.alpha(mHeaderColor)));
        if (mHeaderPaint.getColor() != mScrimColor && mHeaderPaint.getColor() != 0) {
            int bottom = (int) (mSearchContainer.getBottom() + getTranslationY());
            canvas.drawRect(0, 0, canvas.getWidth(), bottom, mHeaderPaint);
            int tabsHeight = getFloatingHeaderView().getPeripheralProtectionHeight();
            if (mTabsProtectionAlpha > 0 && tabsHeight != 0) {
                mHeaderPaint.setAlpha((int) (getAlpha() * mTabsProtectionAlpha));
                canvas.drawRect(0, bottom, canvas.getWidth(), bottom + tabsHeight, mHeaderPaint);
            }
        }
    }

    public AdapterHolder createHolder(boolean isWork) {
        return new AdapterHolder(isWork);
    }

    public class AdapterHolder {
        public static final int MAIN = 0;
        public static final int WORK = 1;

        private ItemInfoMatcher mInfoMatcher;
        private boolean mIsWork;
        public final AllAppsGridAdapter adapter;
        final LinearLayoutManager layoutManager;
        final AlphabeticalAppsList appsList;
        public final Rect padding = new Rect();
        public AllAppsRecyclerView recyclerView;
        boolean verticalFadingEdge;
        private View mOverlay;

        boolean mWorkDisabled;

        AdapterHolder(boolean isWork) {
            mIsWork = isWork;
            appsList = new AlphabeticalAppsList(mLauncher, mAllAppsStore,
                    isWork ? mWorkAdapterProvider : null);

            BaseAdapterProvider[] adapterProviders =
                    isWork ? new BaseAdapterProvider[]{mSearchAdapterProvider, mWorkAdapterProvider}
                            : new BaseAdapterProvider[]{mSearchAdapterProvider};

            adapter = new AllAppsGridAdapter(mLauncher, getLayoutInflater(), appsList,
                    adapterProviders);
            appsList.setAdapter(adapter);
            layoutManager = adapter.getLayoutManager();
        }

        public void setup(@NonNull View rv, @Nullable ItemInfoMatcher matcher) {
            mInfoMatcher = matcher;
            appsList.updateItemFilter(matcher);
            recyclerView = (AllAppsRecyclerView) rv;
            recyclerView.setEdgeEffectFactory(createEdgeEffectFactory());
            recyclerView.setApps(appsList, mUsingTabs);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(adapter);
            recyclerView.setHasFixedSize(true);
            // No animations will occur when changes occur to the items in this RecyclerView.
            recyclerView.setItemAnimator(null);
            recyclerView.addOnScrollListener(mScrollListener);
            FocusedItemDecorator focusedItemDecorator = new FocusedItemDecorator(recyclerView);
            recyclerView.addItemDecoration(focusedItemDecorator);
            adapter.setIconFocusListener(focusedItemDecorator.getFocusListener());
            applyVerticalFadingEdgeEnabled(verticalFadingEdge);
            applyPadding();
            if (FeatureFlags.ENABLE_DEVICE_SEARCH.get()) {
                recyclerView.addItemDecoration(mSearchAdapterProvider.getDecorator());
            }
        }

        public void applyPadding() {
            if (recyclerView != null) {
                Resources res = getResources();
                int switchH = res.getDimensionPixelSize(R.dimen.work_profile_footer_padding) * 2
                        + mInsets.bottom + Utilities.calculateTextHeight(
                        res.getDimension(R.dimen.work_profile_footer_text_size));

                int bottomOffset = mWorkModeSwitch != null && mIsWork ? switchH : 0;
                recyclerView.setPadding(padding.left, padding.top, padding.right,
                        padding.bottom + bottomOffset);
            }
        }

        public void applyVerticalFadingEdgeEnabled(boolean enabled) {
            verticalFadingEdge = enabled;
            mAH[AdapterHolder.MAIN].recyclerView.setVerticalFadingEdgeEnabled(!mUsingTabs
                    && verticalFadingEdge);
        }

        public void setIsWork(boolean isWork) {
            this.mIsWork = isWork;
        }

        public boolean isWork() {
            return mIsWork;
        }
    }


    protected void updateHeaderScroll(int scrolledOffset) {

        /*
        float prog = Utilities.boundToRange((float) scrolledOffset / mHeaderThreshold, 0f, 1f);
        int viewBG = ColorUtils.blendARGB(mScrimColor, mHeaderProtectionColor, prog);
        int headerColor = ColorUtils.setAlphaComponent(viewBG,
                (int) (getSearchView().getAlpha() * 255));
                int tabsAlpha = mHeader.getPeripheralProtectionHeight() == 0 ? 0
                : (int) (Utilities.boundToRange(
                        (scrolledOffset + mHeader.mSnappedScrolledY) / mHeaderThreshold, 0f, 1f)
                        * 255);
        if (headerColor != mHeaderColor || mTabsProtectionAlpha != tabsAlpha) {
            mHeaderColor = headerColor;
             mTabsProtectionAlpha = tabsAlpha;
            invalidateHeader();

        }
        if (mSearchUiManager.getEditText() != null) {
            ExtendedEditText editText = mSearchUiManager.getEditText();
            boolean bgVisible = editText.getBackgroundVisibility();
            if (scrolledOffset == 0 && !mIsSearching) {
                bgVisible = true;
            } else if (scrolledOffset > mHeaderThreshold) {
                bgVisible = false;
            }
            editText.setBackgroundVisibility(bgVisible, 1 - prog);
                */

    }

    /**
     * redraws header protection
     */
    public void invalidateHeader() {
        if (mScrimView != null && mHeader.isHeaderProtectionSupported()) {
            mScrimView.invalidate();
        }
    }
}
