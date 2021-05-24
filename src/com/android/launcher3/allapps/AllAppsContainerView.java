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

import static com.android.launcher3.logging.LoggerUtils.newContainerTarget;
import static com.android.launcher3.model.BgDataModel.Callbacks.FLAG_HAS_SHORTCUT_PERMISSION;
import static com.android.launcher3.model.BgDataModel.Callbacks.FLAG_QUIET_MODE_CHANGE_PERMISSION;
import static com.android.launcher3.model.BgDataModel.Callbacks.FLAG_QUIET_MODE_ENABLED;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Process;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.launcher3.DeviceProfile;
import com.android.launcher3.DeviceProfile.OnDeviceProfileChangeListener;
import com.android.launcher3.DragSource;
import com.android.launcher3.DropTarget.DragObject;
import com.android.launcher3.Insettable;
import com.android.launcher3.InsettableFrameLayout;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.config.FeatureFlags;
import com.android.launcher3.keyboard.FocusedItemDecorator;
import com.android.launcher3.model.data.AppInfo;
import com.android.launcher3.model.data.ItemInfo;
import com.android.launcher3.userevent.nano.LauncherLogProto.ContainerType;
import com.android.launcher3.userevent.nano.LauncherLogProto.Target;
import com.android.launcher3.util.ItemInfoMatcher;
import com.android.launcher3.util.MultiValueAlpha;
import com.android.launcher3.util.MultiValueAlpha.AlphaProperty;
import com.android.launcher3.util.Themes;
import com.android.launcher3.views.RecyclerViewFastScroller;
import com.android.launcher3.views.SpringRelativeLayout;
import com.google.android.apps.nexuslauncher.qsb.AllAppsQsbLayout;
import com.saggitt.omega.OmegaPreferences;
import com.saggitt.omega.allapps.AllAppsTabs;
import com.saggitt.omega.allapps.AllAppsTabsController;
import com.saggitt.omega.util.OmegaUtilsKt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The all apps view container.
 */
public class AllAppsContainerView extends SpringRelativeLayout implements DragSource,
        Insettable, OnDeviceProfileChangeListener {

    private static final float FLING_VELOCITY_MULTIPLIER = 135f;
    // Starts the springs after at least 55% of the animation has passed.
    private static final float FLING_ANIMATION_THRESHOLD = 0.55f;
    private static final int ALPHA_CHANNEL_COUNT = 2;

    protected final Launcher mLauncher;
    protected final Point mFastScrollerOffset = new Point();
    private final ItemInfoMatcher mPersonalMatcher = ItemInfoMatcher.ofUser(Process.myUserHandle());
    private final ItemInfoMatcher mWorkMatcher = ItemInfoMatcher.not(mPersonalMatcher);
    private final AllAppsStore mAllAppsStore = new AllAppsStore();

    private final Paint mNavBarScrimPaint;
    private int mNavBarScrimHeight = 0;
    protected AdapterHolder[] mAH;
    private View mSearchContainer;
    private AllAppsPagedView mViewPager;

    private FloatingHeaderView mHeader;
    protected SearchUiManager mSearchUiManager;

    private SpannableStringBuilder mSearchQueryBuilder = null;
    protected boolean mUsingTabs;
    private boolean mSearchModeWhileUsingTabs = false;

    protected RecyclerViewFastScroller mTouchHandler;
    Rect mInsets = new Rect();
    private WorkModeSwitch mWorkModeSwitch;
    private final MultiValueAlpha mMultiValueAlpha;
    private AllAppsTabsController mTabsController;

    public AllAppsContainerView(Context context) {
        this(context, null);
    }

    public AllAppsContainerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AllAppsContainerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mLauncher = Launcher.getLauncher(context);
        mLauncher.addOnDeviceProfileChangeListener(this);

        mSearchQueryBuilder = new SpannableStringBuilder();
        Selection.setSelection(mSearchQueryBuilder, 0);

        AllAppsTabs allAppsTabs = new AllAppsTabs(context);
        mTabsController = new AllAppsTabsController(allAppsTabs, this);
        createHolders();

        /*
        mAH = new AdapterHolder[2];
        mAH[AdapterHolder.MAIN] = new AdapterHolder(false);
        mAH[AdapterHolder.WORK] = new AdapterHolder(true );
        */

        mNavBarScrimPaint = new Paint();
        mNavBarScrimPaint.setColor(Themes.getAttrColor(context, R.attr.allAppsNavBarScrimColor));

        mAllAppsStore.addUpdateListener(this::onAppsUpdated);

        addSpringView(R.id.all_apps_header);
        addSpringView(R.id.apps_list_view);
        addSpringView(R.id.all_apps_tabs_view_pager);

        mMultiValueAlpha = new MultiValueAlpha(this, ALPHA_CHANNEL_COUNT);
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

    public AlphaProperty getAlphaProperty(int index) {
        return mMultiValueAlpha.getProperty(index);
    }

    public WorkModeSwitch getWorkModeSwitch() {
        return mWorkModeSwitch;
    }

    @Override
    protected void setDampedScrollShift(float shift) {
        // Bound the shift amount to avoid content from drawing on top (Y-val) of the QSB.
        float maxShift = getSearchView().getHeight() / 2f;
        super.setDampedScrollShift(Utilities.boundToRange(shift, -maxShift, maxShift));
    }

    @Override
    public void onDeviceProfileChanged(DeviceProfile dp) {
        for (AdapterHolder holder : mAH) {
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
        if (FeatureFlags.ALL_APPS_TABS_ENABLED && Utilities.getOmegaPrefs(getContext()).getSeparateWorkApps()) {
            boolean hasWorkApps = false;
            for (AppInfo app : mAllAppsStore.getApps()) {
                if (mWorkMatcher.matches(app, null)) {
                    hasWorkApps = true;
                    break;
                }
            }
            rebindAdapters(hasWorkApps);
            if (hasWorkApps && mWorkModeSwitch != null) {
                resetWorkProfile();
            }
            AllAppsTabs allAppsTabs = mTabsController.getTabs();
            force = allAppsTabs.getHasWorkApps() != hasWorkApps;
            allAppsTabs.setHasWorkApps(hasWorkApps);
        }
        rebindAdapters(mTabsController.getShouldShowTabs(), force);
    }

    private void resetWorkProfile() {
        mWorkModeSwitch.update(!mAllAppsStore.hasModelFlag(FLAG_QUIET_MODE_ENABLED));
        for (AdapterHolder adapterHolder : mAH) {
            if (adapterHolder.mIsWork) {
                adapterHolder.setupOverlay();
                adapterHolder.applyPadding();
            }
        }
        //mAH[AdapterHolder.WORK].setupOverlay();
        //mAH[AdapterHolder.WORK].applyPadding();
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
        }
        // Reset the search bar and base recycler view after transitioning home
        mSearchUiManager.resetSearch();
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
        mSearchUiManager.initialize(this);
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
    public void fillInLogContainerData(ItemInfo childInfo, Target child,
                                       ArrayList<Target> parents) {
        parents.add(newContainerTarget(
                getApps().hasFilter() ? ContainerType.SEARCHRESULT : ContainerType.ALLAPPS));
    }

    @Override
    public void setInsets(Rect insets) {
        //mInsets.set(insets);
        DeviceProfile grid = mLauncher.getDeviceProfile();
        int leftRightPadding = grid.desiredWorkspaceLeftRightMarginPx
                + grid.cellLayoutPaddingLeftRightPx;

        mTabsController.setPadding(leftRightPadding, insets.bottom);
        /*for (AdapterHolder adapterHolder : mAH) {
            adapterHolder.padding.bottom = insets.bottom;
            adapterHolder.padding.left = adapterHolder.padding.right = leftRightPadding;
            adapterHolder.applyPadding();
            adapterHolder.setupOverlay();
        }*/

        ViewGroup.MarginLayoutParams mlp = (MarginLayoutParams) getLayoutParams();
        if (grid.isVerticalBarLayout()) {
            mlp.leftMargin = insets.left;
            mlp.rightMargin = insets.right;
            setPadding(grid.workspacePadding.left, 0, grid.workspacePadding.right, 0);
        } else {
            mlp.leftMargin = mlp.rightMargin = 0;
            setPadding(0, 0, 0, 0);
        }
        if (!OmegaPreferences.Companion.getInstance(getContext()).getAllAppsSearch()) {
            AllAppsQsbLayout qsb = (AllAppsQsbLayout) mSearchContainer;
            mlp.topMargin = -(qsb.getTopMargin(insets) + qsb.getLayoutParams().height);
        }
        setLayoutParams(mlp);
        InsettableFrameLayout.dispatchInsets(this, insets);
        mLauncher.getAllAppsController()
                .setScrollRangeDelta(mSearchUiManager.getScrollRangeDelta(insets));
    }

    @Override
    public WindowInsets dispatchApplyWindowInsets(WindowInsets insets) {
        if (Utilities.ATLEAST_Q) {
            mNavBarScrimHeight = insets.getTappableElementInsets().bottom;
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

    @Override
    public int getCanvasClipTopForOverscroll() {
        // Do not clip if the QSB is attached to the spring, otherwise the QSB will get clipped.
        return mSpringViews.get(getSearchView().getId()) ? 0 : mHeader.getTop();
    }

    public void reloadTabs() {
        mTabsController.reloadTabs();
        rebindAdapters(mTabsController.getShouldShowTabs(), true);
    }


    private void rebindAdapters(boolean showTabs) {
        rebindAdapters(showTabs, false /* force */);
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

        if (mUsingTabs) {
            mTabsController.setup(mViewPager);
            PersonalWorkSlidingTabStrip tabStrip = findViewById(R.id.tabs);
            tabStrip.inflateButtons(mTabsController.getTabs());
            if (currentTab == 0) {
                tabStrip.setScroll(0, 1);
            }
            onTabChanged(currentTab);
            /*if (mTabsController.getTabs().getHasWorkApps()) {
                setupWorkToggle();
            }*/
        } else {
            mTabsController.setup((View) findViewById(R.id.apps_list_view));
            AllAppsRecyclerView recyclerView = mAH[AdapterHolder.MAIN].recyclerView;
            if (recyclerView != null) {
                OmegaUtilsKt.runOnAttached(recyclerView, () ->
                        recyclerView.setScrollbarColor(Utilities.getOmegaPrefs(getContext()).getAccentColor()));
            }
            if (mWorkModeSwitch != null) {
                ((ViewGroup) mWorkModeSwitch.getParent()).removeView(mWorkModeSwitch);
                mWorkModeSwitch = null;
            }
        }
        setupHeader();

        mTabsController.registerIconContainers(mAllAppsStore);
        if (mViewPager != null) {
            mViewPager.snapToPage(Math.min(mTabsController.getTabsCount() - 1, currentTab), 0);
        }
    }

    private void setupWorkToggle() {
        if (Utilities.ATLEAST_P) {
            mWorkModeSwitch = (WorkModeSwitch) mLauncher.getLayoutInflater().inflate(
                    R.layout.work_mode_switch, this, false);
            mWorkModeSwitch.setVisibility(View.GONE);
            this.addView(mWorkModeSwitch);
            mWorkModeSwitch.setInsets(mInsets);
            //mWorkModeSwitch.post(() -> mAH[AdapterHolder.WORK].applyPadding());
            for (int i = 0; i < mAH.length; i++) {
                if (mAH[i].mIsWork) {
                    int x = i;
                    mWorkModeSwitch.post(() -> mAH[x].applyPadding());
                }
            }
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        for (AdapterHolder adapterHolder : mAH) {
            if (adapterHolder.mIsWork) {
                View overlay = mAH[AdapterHolder.WORK].getOverlayView();
                int v = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE ? GONE : VISIBLE;
                overlay.findViewById(R.id.work_apps_paused_title).setVisibility(v);
                overlay.findViewById(R.id.work_apps_paused_content).setVisibility(v);
            }
        }
    }

    private void replaceRVContainer(boolean showTabs) {
        for (int i = 0; i < mAH.length; i++) {
            if (mAH[i].recyclerView != null) {
                mAH[i].recyclerView.setLayoutManager(null);
            }
        }
        View oldView = getRecyclerViewContainer();
        int index = indexOfChild(oldView);
        removeView(oldView);
        int layout = showTabs ? R.layout.all_apps_tabs : R.layout.all_apps_rv_layout;
        View newView = getLayoutInflater().inflate(layout, this, false);
        addView(newView, index);
        if (showTabs) {
            mViewPager = (AllAppsPagedView) newView;
            mViewPager.addTabs(mTabsController.getTabsCount());
            mViewPager.initParentViews(this);
            mViewPager.getPageIndicator().setContainerView(this);
        } else {
            mViewPager = null;
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
        if (mWorkModeSwitch != null && mAH[pos].mIsWork) {
            mWorkModeSwitch.setWorkTabVisible(pos == AdapterHolder.WORK
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

    /*
    // Used by tests only
    public boolean isPersonalTabVisible() {
        return isDescendantViewVisible(R.id.tab_personal);
    }

    // Used by tests only
    public boolean isWorkTabVisible() {
        return isDescendantViewVisible(R.id.tab_work);
    }*/

    public AlphabeticalAppsList getApps() {
        return mAH[AdapterHolder.MAIN].appsList;
    }

    public Collection<AlphabeticalAppsList> getAppsLists() {
        List<AlphabeticalAppsList> results = new ArrayList<>();
        for (AdapterHolder holder : mAH) {
            results.add(holder.appsList);
        }
        return results;
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

    public RecyclerViewFastScroller getScrollBar() {
        AllAppsRecyclerView rv = getActiveRecyclerView();
        return rv == null ? null : rv.getScrollbar();
    }

    public void setupHeader() {
        mHeader.setVisibility(View.VISIBLE);
        mHeader.setup(mAH, !mUsingTabs);

        int padding = mHeader.getMaxTranslation();
        for (AdapterHolder adapterHolder : mAH) {
            adapterHolder.padding.top = padding;
            adapterHolder.applyPadding();
        }
    }

    public void setLastSearchQuery(String query) {
        for (int i = 0; i < mAH.length; i++) {
            mAH[i].adapter.setLastSearchQuery(query);
        }
        if (mUsingTabs) {
            mSearchModeWhileUsingTabs = true;
            rebindAdapters(false); // hide tabs
        }
    }

    public void onClearSearchResult() {
        if (mSearchModeWhileUsingTabs) {
            rebindAdapters(true); // show tabs
            mSearchModeWhileUsingTabs = false;
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

    public void onScrollUpEnd() {
        highlightWorkTabIfNecessary();
    }

    void highlightWorkTabIfNecessary() {
        if (mUsingTabs) {
            ((PersonalWorkSlidingTabStrip) findViewById(R.id.tabs))
                    .highlightWorkTabIfNecessary();
        }
    }

    /**
     * Adds an update listener to {@param animator} that adds springs to the animation.
     */
    public void addSpringFromFlingUpdateListener(ValueAnimator animator, float velocity) {
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            boolean shouldSpring = true;

            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (shouldSpring
                        && valueAnimator.getAnimatedFraction() >= FLING_ANIMATION_THRESHOLD) {
                    int searchViewId = getSearchView().getId();
                    addSpringView(searchViewId);
                    finishWithShiftAndVelocity(1, velocity * FLING_VELOCITY_MULTIPLIER,
                            (anim, canceled, value, velocity) -> removeSpringView(searchViewId));

                    shouldSpring = false;
                }
            }
        });
    }

    @Override
    public void getDrawingRect(Rect outRect) {
        super.getDrawingRect(outRect);
        outRect.offset(0, (int) getTranslationY());
    }

    public AdapterHolder createHolder(boolean isWork) {
        return new AdapterHolder(isWork);
    }

    public class AdapterHolder {
        public static final int MAIN = 0;
        public static final int WORK = 1;

        public Rect padding = new Rect();
        boolean mWorkDisabled;
        public final AllAppsGridAdapter adapter;
        final LinearLayoutManager layoutManager;
        final AlphabeticalAppsList appsList;
        private ItemInfoMatcher mInfoMatcher;
        public AllAppsRecyclerView recyclerView;
        boolean verticalFadingEdge;
        private boolean mIsWork;
        private View mOverlay;

        AdapterHolder(boolean isWork) {
            mIsWork = isWork;
            appsList = new AlphabeticalAppsList(mLauncher, mAllAppsStore, isWork);
            adapter = new AllAppsGridAdapter(mLauncher, getLayoutInflater(), appsList);
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
            FocusedItemDecorator focusedItemDecorator = new FocusedItemDecorator(recyclerView);
            recyclerView.addItemDecoration(focusedItemDecorator);
            adapter.setIconFocusListener(focusedItemDecorator.getFocusListener());
            applyVerticalFadingEdgeEnabled(verticalFadingEdge);
            applyPadding();
            setupOverlay();
        }

        void setupOverlay() {
            if (!mIsWork || recyclerView == null) return;
            boolean workDisabled = mAllAppsStore.hasModelFlag(FLAG_QUIET_MODE_ENABLED);
            if (mWorkDisabled == workDisabled) return;
            recyclerView.setContentDescription(workDisabled ? mLauncher.getString(
                    R.string.work_apps_paused_content_description) : null);
            View overlayView = getOverlayView();
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            if (workDisabled) {
                overlayView.setAlpha(0);
                recyclerView.addAutoSizedOverlay(overlayView);
                overlayView.animate().alpha(1).withEndAction(
                        () -> {
                            appsList.updateItemFilter((info, cn) -> false);
                            recyclerView.setItemAnimator(null);
                        }).start();
            } else if (mInfoMatcher != null) {
                appsList.updateItemFilter(mInfoMatcher);
                overlayView.animate().alpha(0).withEndAction(() -> {
                    recyclerView.setItemAnimator(null);
                    recyclerView.clearAutoSizedOverlays();
                }).start();
            }
            mWorkDisabled = workDisabled;
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

        private View getOverlayView() {
            if (mOverlay == null) {
                mOverlay = mLauncher.getLayoutInflater().inflate(R.layout.work_apps_paused, null);
            }
            return mOverlay;
        }

        public void setIsWork(boolean isWork) {
            this.mIsWork = isWork;
            appsList.setIsWork(isWork);
        }

        public boolean isWork() {
            return mIsWork;
        }
    }
}
