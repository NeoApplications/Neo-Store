package com.google.android.apps.nexuslauncher.qsb;

import static com.android.launcher3.InvariantDeviceProfile.CHANGE_FLAG_ICON_PARAMS;
import static com.android.launcher3.LauncherState.ALL_APPS_HEADER;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.android.launcher3.BaseRecyclerView;
import com.android.launcher3.InvariantDeviceProfile;
import com.android.launcher3.InvariantDeviceProfile.OnIDPChangeListener;
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.allapps.AllAppsContainerView;
import com.android.launcher3.allapps.SearchUiManager;
import com.android.launcher3.anim.PropertySetter;
import com.android.launcher3.util.Themes;
import com.google.android.apps.nexuslauncher.search.SearchThread;
import com.saggitt.omega.OmegaPreferences;
import com.saggitt.omega.search.SearchProvider;
import com.saggitt.omega.search.SearchProviderController;
import com.saggitt.omega.search.WebSearchProvider;
import com.saggitt.omega.search.providers.AppsSearchProvider;

public class AllAppsQsbLayout extends AbstractQsbLayout implements
        SearchUiManager, QsbChangeListener, OnIDPChangeListener {

    private QsbConfiguration qsbConfiguration;
    private boolean mLowPerformanceMode;
    private int mTopAdjusting;
    private int mVerticalOffset;
    boolean mDoNotRemoveFallback;
    private int mShadowAlpha;
    private Bitmap shadowBitmap;
    private boolean mUseFallbackSearch;
    private FallbackAppsSearchView mFallback;
    private TextView hintTextView;
    private AllAppsContainerView mAppsView;
    private OmegaPreferences prefs;
    private int mTextColor;

    public AllAppsQsbLayout(Context context) {
        super(context);
        init();
    }

    public AllAppsQsbLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
    }

    public AllAppsQsbLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init();
    }

    private void init() {
        mShadowAlpha = 0;
        setOnClickListener(this);
        qsbConfiguration = QsbConfiguration.getInstance(mContext);
        mTopAdjusting = getResources().getDimensionPixelSize(R.dimen.qsb_margin_top_adjusting);
        mVerticalOffset = getResources().getDimensionPixelSize(R.dimen.all_apps_search_vertical_offset);
        setClipToPadding(false);
        prefs = OmegaPreferences.Companion.getInstanceNoCreate();
        mLowPerformanceMode = prefs.getLowPerformanceMode();
        mTextColor = Themes.getAttrColor(mContext, R.attr.folderTextColor);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        hintTextView = findViewById(R.id.qsb_hint);
    }

    @Override
    public float getScrollRangeDelta(Rect insets) {
        return 0f;
    }

    public void setInsets(Rect rect) {
        removeFallBack();
        MarginLayoutParams mlp = (MarginLayoutParams) getLayoutParams();
        mlp.topMargin = getTopMargin(rect);
        requestLayout();
    }

    public int getTopMargin(Rect rect) {
        return Math.max(Math.round(-mVerticalOffset), rect.top - mTopAdjusting);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        LauncherAppState.getIDP(getContext()).addOnChangeListener(this);
        updateConfiguration();
        qsbConfiguration.addListener(this);
    }

    protected void onDetachedFromWindow() {
        LauncherAppState.getIDP(getContext()).removeOnChangeListener(this);
        qsbConfiguration.removeListener(this);
        super.onDetachedFromWindow();
    }

    @Override
    public void onIdpChanged(int changeFlags, InvariantDeviceProfile profile) {
        if ((changeFlags & CHANGE_FLAG_ICON_PARAMS) != 0) {
            mAllAppsShadowBitmap = mBubbleShadowBitmap = mClearBitmap = null;
            addOrUpdateSearchRipple();
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        super.onSharedPreferenceChanged(sharedPreferences, key);
        if (key.equals("pref_allAppsGlobalSearch")) {
            loadPreferences(sharedPreferences);

        }
    }

    @Override
    public boolean useTwoBubbles() {
        return mMicFrame != null
                && mMicFrame.getVisibility() == View.VISIBLE
                && prefs.getDualBubbleSearch()
                && prefs.getAllAppsGlobalSearch();
    }

    @Override
    protected Drawable getIcon(boolean colored) {
        if (prefs.getAllAppsGlobalSearch()) {
            return super.getIcon(colored);
        } else {
            return new AppsSearchProvider(getContext()).getIcon(colored);
        }
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
        setBubbleBgColor(mAllAppsBgColor);
        addOrUpdateSearchPaint(qsbConfiguration.micStrokeWidth());
        showHintAssitant = qsbConfiguration.hintIsForAssistant();
        setHintText(qsbConfiguration.hintTextValue(), hintTextView);
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
    public final void startSearch(String str, int result) {
        startDrawerSearch(str, result);
    }

    private void startDrawerSearch(String str, int result) {
        SearchProviderController controller = SearchProviderController.Companion
                .getInstance(mContext);
        SearchProvider provider = controller.getSearchProvider();
        if (shouldUseFallbackSearch(provider)) {
            searchFallback(str);
        } else if (controller.isGoogle()) {
            ConfigBuilder config = new ConfigBuilder(this, true);
            if (!getLauncher().getGoogleNow().startSearch(config.build(), config.getExtras())) {
                searchFallback(str);
                if (mFallback != null) {
                    mFallback.setHint("Search Apps");
                }
            }
        } else {
            provider.startSearch(intent -> {
                getLauncher().startActivity(intent);
                return null;
            });
        }
        mResult = result;
    }

    private boolean shouldUseFallbackSearch() {
        SearchProviderController controller = SearchProviderController.Companion
                .getInstance(getContext());
        SearchProvider provider = controller.getSearchProvider();
        return shouldUseFallbackSearch(provider);
    }

    private boolean shouldUseFallbackSearch(SearchProvider provider) {
        return !Utilities
                .getOmegaPrefs(getContext()).getAllAppsGlobalSearch()
                || provider instanceof AppsSearchProvider
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
            mFallback.setTextColor(mTextColor);
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

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        View view = (View) getParent();

        setTranslationX((float) ((view.getPaddingLeft() + (
                (((view.getWidth() - view.getPaddingLeft()) - view.getPaddingRight())
                        - (right - left))
                        / 2)) - left));

        int containerTopMargin = 0;
        if (!prefs.getAllAppsSearch()) {
            MarginLayoutParams mlp = (MarginLayoutParams) getLayoutParams();
            containerTopMargin = -(mlp.topMargin + mlp.height);
        }

        offsetTopAndBottom(mVerticalOffset - containerTopMargin);
    }

    @Override
    protected void drawQsb(@NonNull Canvas canvas) {
        if (mShadowAlpha > 0) {
            if (shadowBitmap == null) {
                shadowBitmap = createShadowBitmap(
                        getResources().getDimension(R.dimen.hotseat_qsb_scroll_shadow_blur_radius),
                        getResources().getDimension(R.dimen.hotseat_qsb_scroll_key_shadow_offset),
                        0, true);
            }
            mShadowHelper.paint.setAlpha(mShadowAlpha);
            drawShadow(shadowBitmap, canvas);
            mShadowHelper.paint.setAlpha(255);
        }
        super.drawQsb(canvas);
    }

    final void setShadowAlpha(int alpha) {
        alpha = Utilities.boundToRange(alpha, 0, 255);
        if (mShadowAlpha != alpha) {
            mShadowAlpha = alpha;
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

    @Override
    public void preDispatchKeyEvent(KeyEvent keyEvent) {
    }

    @Nullable
    @Override
    protected String getClipboardText() {
        return shouldUseFallbackSearch() ? super.getClipboardText() : null;
    }

    @Override
    protected void clearMainPillBg(Canvas canvas) {
    }

    @Override
    protected void clearPillBg(Canvas canvas, int left, int top, int right) {
        if (!mLowPerformanceMode && mClearBitmap != null) {
            mClearShadowHelper.draw(mClearBitmap, canvas, left, top, right);
        }
    }

    @Override
    public void setContentVisibility(int visibleElements, PropertySetter setter, Interpolator interpolator) {
        boolean drawerQsbEnabled = prefs.getAllAppsSearch();
        boolean drawerQsbVisible = (visibleElements & ALL_APPS_HEADER) != 0;
        boolean qsbVisible = drawerQsbEnabled && drawerQsbVisible;

        setter.setViewAlpha(this, qsbVisible ? 1 : 0, interpolator);
    }

    @Override
    public boolean isQsbVisible(int visibleElements) {
        // TODO: Implement
        return false;
    }

    @Nullable
    @Override
    public EditText setTextSearchEnabled(boolean isEnabled) {
        // TODO: Implement
        return null;
    }
}
