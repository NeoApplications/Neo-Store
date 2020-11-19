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

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.RippleDrawable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.FloatProperty;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import com.android.launcher3.DeviceProfile;
import com.android.launcher3.Insettable;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.graphics.IconShape;
import com.android.launcher3.graphics.NinePatchDrawHelper;
import com.android.launcher3.icons.ShadowGenerator;
import com.android.launcher3.uioverrides.WallpaperColorInfo;
import com.android.launcher3.util.Themes;
import com.android.launcher3.util.TransformingTouchDelegate;
import com.android.launcher3.views.ActivityContext;
import com.saggitt.omega.OmegaLauncher;
import com.saggitt.omega.search.SearchProvider;
import com.saggitt.omega.search.SearchProviderController;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.saggitt.omega.util.Config.GOOGLE_QSB;
import static com.saggitt.omega.util.OmegaUtilsKt.round;

public abstract class AbstractQsbLayout extends FrameLayout implements OnSharedPreferenceChangeListener,
        OnClickListener, OnLongClickListener, Insettable, SearchProviderController.OnProviderChangeListener,
        WallpaperColorInfo.OnChangeListener {
    protected final static String TAG = "AbstractQsbLayout";
    public FloatProperty HOTSEAT_PROGRESS = new FloatProperty<AbstractQsbLayout>("hotseatProgress") {
        @Override
        public void setValue(AbstractQsbLayout qsb, float v) {
            if (qsb.mHotseatProgress != v) {
                qsb.mHotseatProgress = v;
                qsb.invalidate();
            }
        }

        @Override
        public Float get(AbstractQsbLayout qsb) {
            return qsb.mHotseatProgress;
        }
    };

    private static final Rect mSrcRect = new Rect();
    protected final TextPaint qsbTextHintSize;
    protected final Paint mMicStrokePaint;
    protected final Paint CV;
    protected final NinePatchDrawHelper mShadowHelper;
    protected final NinePatchDrawHelper mClearShadowHelper;
    protected final ActivityContext mActivity;
    protected Context mContext;
    protected final int qsbTextSpacing;
    protected final int twoBubbleGap;
    protected final int mSearchIconWidth;
    private final int qsbDoodle;
    private final int mShadowMargin;
    private final int qsbHintLenght;

    protected int mResult;
    private final TransformingTouchDelegate mTouchDelegate;
    protected int mColor;
    protected boolean mIsRtl;
    protected boolean mUseTwoBubbles;
    protected boolean mShowAssistant;
    public float micStrokeWidth;
    protected ImageView mMicIconView;
    protected ImageView mLogoIconView;
    protected boolean Dh;
    protected ImageView mHotseatLogoIconView;
    protected String Dg;
    protected FrameLayout mMicFrame;
    protected float mHotseatProgress = 1f;
    protected Bitmap mAllAppsShadowBitmap;
    protected Bitmap mHotseatShadowBitmap;
    protected Bitmap mBubbleShadowBitmap;
    protected Bitmap mClearBitmap;

    protected int mAllAppsBgColor;
    protected int mHotseatBgColor;
    protected int mBubbleBgColor;
    private float mRadius;

    public AbstractQsbLayout(Context context) {
        this(context, null);
    }

    public AbstractQsbLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public AbstractQsbLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        qsbTextHintSize = new TextPaint();
        mMicStrokePaint = new Paint(1);
        CV = new Paint(1);
        mShadowHelper = new NinePatchDrawHelper();
        mClearShadowHelper = new NinePatchDrawHelper();
        mClearShadowHelper.paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        mResult = 0;
        setOnLongClickListener(this);
        qsbDoodle = getResources().getDimensionPixelSize(R.dimen.qsb_doodle_tap_target_logo_width);
        mSearchIconWidth = getResources().getDimensionPixelSize(R.dimen.qsb_mic_width);
        qsbTextSpacing = getResources().getDimensionPixelSize(R.dimen.qsb_text_spacing);
        twoBubbleGap = getResources().getDimensionPixelSize(R.dimen.qsb_two_bubble_gap);
        qsbTextHintSize.setTextSize((float) getResources().getDimensionPixelSize(R.dimen.qsb_hint_text_size));
        mShadowMargin = getResources().getDimensionPixelSize(R.dimen.qsb_shadow_margin);
        qsbHintLenght = getResources().getDimensionPixelSize(R.dimen.qsb_max_hint_length);
        mIsRtl = Utilities.isRtl(getResources());
        mTouchDelegate = new TransformingTouchDelegate(this);
        setTouchDelegate(mTouchDelegate);
        CV.setColor(Color.WHITE);
        mRadius = round(Utilities.getOmegaPrefs(getContext()).getSearchBarRadius());
        mContext = context;
        mActivity = ActivityContext.lookupContext(context);
    }

    public static float getCornerRadius(Context context, float defaultRadius) {
        float radius = round(Utilities.getOmegaPrefs(context).getSearchBarRadius());
        if (radius >= 0f) {
            return radius;
        }
        TypedValue edgeRadius = IconShape.getShape().getAttrValue(R.attr.qsbEdgeRadius);
        if (edgeRadius != null) {
            return edgeRadius.getDimension(context.getResources().getDisplayMetrics());
        } else {
            return defaultRadius;
        }
    }

    public abstract void startSearch(String str, int i);

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getDevicePreferences().registerOnSharedPreferenceChangeListener(this);
        mTouchDelegate.setDelegateView(mMicFrame);
        SearchProviderController.Companion.getInstance(getContext()).addOnProviderChangeListener(this);
        WallpaperColorInfo instance = WallpaperColorInfo.getInstance(getContext());
        instance.addOnChangeListener(this);
        onExtractedColorsChanged(instance);

        updateConfiguration();
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getActionMasked() == 0) {
            View findViewById = findViewById(R.id.g_icon);
            int i = 0;
            int i2 = 1;
            if (mIsRtl) {
                if (Float.compare(motionEvent.getX(), (float) (dI() ? getWidth() - qsbDoodle : findViewById.getLeft())) >= 0) {
                    i = 1;
                }
            } else {
                if (Float.compare(motionEvent.getX(), (float) (dI() ? qsbDoodle : findViewById.getRight())) <= 0) {
                    i = 1;
                }
            }
            if (i == 0) {
                i2 = 2;
            }
            mResult = i2;
        }
        return super.onTouchEvent(motionEvent);
    }

    protected final SharedPreferences getDevicePreferences() {
        loadIcons();
        SharedPreferences devicePrefs = Utilities.getPrefs(getContext());
        loadPreferences(devicePrefs);
        return devicePrefs;
    }

    protected void loadIcons() {
        mLogoIconView = findViewById(R.id.g_icon);
        mMicIconView = findViewById(R.id.mic_icon);
        mHotseatLogoIconView = findViewById(R.id.g_icon_hotseat);
        mMicFrame = findViewById(R.id.mic_frame);
        mMicIconView.setOnClickListener(this);
        mLogoIconView.setOnClickListener(this);
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mIsRtl) {
            mSrcRect.left -= mShadowMargin;
        } else {
            mSrcRect.right += mShadowMargin;
        }
        mTouchDelegate.setBounds(mSrcRect.left, mSrcRect.top, mSrcRect.right, mSrcRect.bottom);
    }

    protected void onDetachedFromWindow() {
        Utilities.getPrefs(getContext()).unregisterOnSharedPreferenceChangeListener(this);
        SearchProviderController.Companion.getInstance(getContext()).removeOnProviderChangeListener(this);
        WallpaperColorInfo.getInstance(getContext()).removeOnChangeListener(this);
        super.onDetachedFromWindow();
    }

    @Override
    public void onExtractedColorsChanged(WallpaperColorInfo wallpaperColorInfo) {
        setColor(ColorUtils.compositeColors(ColorUtils.compositeColors(
                Themes.getAttrBoolean(mContext, R.attr.isMainColorDark)
                        ? -650362813 : -855638017, Themes.getAttrColor(mContext, R.attr.allAppsScrimColor)), wallpaperColorInfo.getMainColor()));
    }

    private void setColor(int color) {
        if (mColor != color) {
            mColor = color;
            mAllAppsShadowBitmap = null;
            setHotseatBgColor(color);
            setAllAppsBgColor(color);
            invalidate();
        }
    }

    public final void setAllAppsBgColor(int color) {
        if (mAllAppsBgColor != color) {
            mAllAppsBgColor = color;
            mAllAppsShadowBitmap = null;
            invalidate();
        }
    }

    public final void setHotseatBgColor(int color) {
        if (mHotseatBgColor != color) {
            mHotseatBgColor = color;
            mHotseatShadowBitmap = null;
            invalidate();
        }
    }

    public final void az(int i) {
        mBubbleBgColor = i;
        if (mBubbleBgColor != mAllAppsBgColor || mBubbleShadowBitmap != mAllAppsShadowBitmap) {
            mBubbleShadowBitmap = null;
            invalidate();
        }
    }

    public final void addOrUpdateSearchPaint(float value) {
        micStrokeWidth = TypedValue.applyDimension(1, value, getResources().getDisplayMetrics());
        mMicStrokePaint.setStrokeWidth(micStrokeWidth);
        mMicStrokePaint.setStyle(Style.STROKE);
        mMicStrokePaint.setColor(0xFFBDC1C6);
    }

    private void updateConfiguration() {
        addOrUpdateSearchPaint(0.0f);
        addOrUpdateSearchRipple();
    }

    @Override
    public void setInsets(Rect rect) {
        requestLayout();
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        DeviceProfile dp = mActivity.getDeviceProfile();
        int round = Math.round(((float) dp.iconSizePx) * 0.92f);
        setMeasuredDimension(calculateMeasuredDimension(dp, round, widthMeasureSpec), View.MeasureSpec.getSize(heightMeasureSpec));
        for (int childCount = getChildCount() - 1; childCount >= 0; childCount--) {
            View childAt = getChildAt(childCount);
            measureChildWithMargins(childAt, widthMeasureSpec, 0, heightMeasureSpec, 0);
            if (childAt.getMeasuredWidth() <= round) {
                LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
                int measuredWidth = (round - childAt.getMeasuredWidth()) / 2;
                layoutParams.rightMargin = measuredWidth;
                layoutParams.leftMargin = measuredWidth;
            }
        }
    }

    //SEARCH BAR WIDTH
    public int getMeasuredWidth(int width, @NotNull DeviceProfile dp) {
        int leftRightPadding = dp.desiredWorkspaceLeftRightMarginPx
                + dp.cellLayoutPaddingLeftRightPx;
        return width - leftRightPadding * 2;
    }

    public int calculateMeasuredDimension(DeviceProfile dp, int round, int widthMeasureSpec) {
        int width = getMeasuredWidth(MeasureSpec.getSize(widthMeasureSpec), dp);
        int calculateCellWidth = width - ((width / dp.inv.numHotseatIcons) - round);
        return getPaddingRight() + getPaddingLeft() + calculateCellWidth;
    }

    final void drawMainPill(Canvas canvas) {
        if (mAllAppsBgColor == mHotseatBgColor || mHotseatProgress == 0f) {
            drawShadow(mAllAppsShadowBitmap, canvas);
        } else if (mHotseatProgress == 1f) {
            drawShadow(mHotseatShadowBitmap, canvas);
        } /*else {
            mShadowHelper.paint.setAlpha(Math.round(255 * (1 - mHotseatProgress)));
            drawShadow(mAllAppsShadowBitmap, canvas);
            mShadowHelper.paint.setAlpha(Math.round(255 * mHotseatProgress));
            drawShadow(mHotseatShadowBitmap, canvas);
            mShadowHelper.paint.setAlpha(255);
        }*/
    }

    protected void clearMainPillBg(Canvas canvas) {
    }

    protected void clearPillBg(Canvas canvas, int left, int top, int right) {
    }

    public void draw(Canvas canvas) {
        ensureHotseatShadowBitmap();
        clearMainPillBg(canvas);
        drawQsb(canvas);
        super.draw(canvas);
    }

    public void drawQsb(Canvas canvas) {
        int i;
        drawMainPill(canvas);
        if (mUseTwoBubbles) {
            int paddingLeft;
            int paddingLeft2;
            if (mBubbleShadowBitmap == null) {
                if (mAllAppsBgColor == mBubbleBgColor) {
                    mBubbleShadowBitmap = mAllAppsShadowBitmap;
                } else {
                    mBubbleShadowBitmap = createShadowBitmap(mBubbleBgColor, true);
                }
            }
            Bitmap bitmap2 = mBubbleShadowBitmap;
            i = getShadowDimens(bitmap2);
            int paddingTop = getPaddingTop() - ((bitmap2.getHeight() - getHeightWithoutPadding()) / 2);
            if (mIsRtl) {
                paddingLeft = getPaddingLeft() - i;
                paddingLeft2 = getPaddingLeft() + i;
                i = getMicWidth();
            } else {
                paddingLeft = ((getWidth() - getPaddingRight()) - getMicWidth()) - i;
                paddingLeft2 = getWidth() - getPaddingRight();
            }
            clearPillBg(canvas, paddingLeft, paddingTop, paddingLeft2 + i);
            mShadowHelper.draw(bitmap2, canvas, (float) paddingLeft, (float) paddingTop, (float) (paddingLeft2 + i));
        }
        if (micStrokeWidth > 0.0f && mMicFrame.getVisibility() == View.VISIBLE) {
            float i2;
            i = mIsRtl ? getPaddingLeft() : (getWidth() - getPaddingRight()) - getMicWidth();
            int paddingTop = getPaddingTop();
            int paddingLeft = mIsRtl ? getPaddingLeft() + getMicWidth() : getWidth() - getPaddingRight();
            int paddingBottom = LauncherAppState.getInstance(getContext()).getInvariantDeviceProfile().iconBitmapSize - getPaddingBottom();
            float f = ((float) (paddingBottom - paddingTop)) * 0.5f;
            float i3 = micStrokeWidth / 2.0f;
            if (mUseTwoBubbles) {
                i2 = i3;
            } else {
                i2 = i3;
                canvas.drawRoundRect(i + i3, paddingTop + i3, paddingLeft - i3, (paddingBottom - i3) + 1, f, f, CV);
            }
            canvas.drawRoundRect(i + i2, paddingTop + i2, paddingLeft - i2, (paddingBottom - i2) + 1, f, f, mMicStrokePaint);
        }
    }

    final void ensureAllAppsShadowBitmap() {
        if (mAllAppsShadowBitmap == null) {
            mAllAppsShadowBitmap = createShadowBitmap(mAllAppsBgColor, true);
            mClearBitmap = null;
            if (Color.alpha(mAllAppsBgColor) != 255) {
                mClearBitmap = createShadowBitmap(0xFF000000, false);
            }
        }
    }

    final void ensureHotseatShadowBitmap() {
        ensureAllAppsShadowBitmap();
        if (mHotseatShadowBitmap == null) {
            if (mHotseatBgColor == mAllAppsBgColor) {
                mHotseatShadowBitmap = mAllAppsShadowBitmap;
            } else {
                mHotseatShadowBitmap = createShadowBitmap(mHotseatBgColor, true);
            }
        }
    }

    private Bitmap createShadowBitmap(int bgColor, boolean withShadow) {
        float iconSize = (float) LauncherAppState.getInstance(getContext()).getInvariantDeviceProfile().iconBitmapSize;
        return createShadowBitmap(0.010416667f * iconSize, iconSize * 0.020833334f, bgColor, withShadow);
    }

    protected final Bitmap createShadowBitmap(float f, float f2, int i, boolean withShadow) {
        int height = getHeightWithoutPadding();
        int i2 = height + 20;
        ShadowGenerator.Builder builder = new ShadowGenerator.Builder(i);
        builder.shadowBlur = f;
        builder.keyShadowDistance = f2;
        if (!withShadow) {
            builder.ambientShadowAlpha = 0;
        }
        builder.keyShadowAlpha = builder.ambientShadowAlpha;
        Bitmap pill;
        if (mRadius < 0) {
            TypedValue edgeRadius = IconShape.getShape().getAttrValue(R.attr.qsbEdgeRadius);
            if (edgeRadius != null) {
                pill = builder.createPill(i2, height,
                        edgeRadius.getDimension(getResources().getDisplayMetrics()));
            } else {
                pill = builder.createPill(i2, height);
            }
        } else {
            pill = builder.createPill(i2, height, mRadius);
        }

        return pill;
    }

    protected final void drawShadow(Bitmap bitmap, Canvas canvas) {
        drawPill(mShadowHelper, bitmap, canvas);
    }

    protected final void drawPill(NinePatchDrawHelper helper, Bitmap bitmap, Canvas canvas) {
        int shadowDimens = getShadowDimens(bitmap);
        int paddingLeft = getPaddingLeft() - shadowDimens;
        int paddingTop = getPaddingTop() - ((bitmap.getHeight() - getHeightWithoutPadding()) / 2);
        int width = (getWidth() - getPaddingRight()) + shadowDimens;
        if (mIsRtl) {
            paddingLeft += getRtlDimens();
        } else {
            width -= getRtlDimens();
        }
        helper.draw(bitmap, canvas, (float) paddingLeft, (float) paddingTop, (float) width);
    }

    protected final int getShadowDimens(@NotNull Bitmap bitmap) {
        return (bitmap.getWidth() - (getHeightWithoutPadding() + 20)) / 2;
    }

    protected final int getHeightWithoutPadding() {
        return (getHeight() - getPaddingTop()) - getPaddingBottom();
    }

    protected final int getIconWidth() {
        return mUseTwoBubbles ? mSearchIconWidth : mSearchIconWidth + qsbTextSpacing;
    }

    protected final void setHintText(String str, TextView textView) {
        String str2;
        if (TextUtils.isEmpty(str) || !dE()) {
            str2 = str;
        } else {
            str2 = TextUtils.ellipsize(str, qsbTextHintSize, (float) qsbHintLenght, TruncateAt.END).toString();
        }
        Dg = str2;
        textView.setText(this.Dg);
        int i = 17;
        if (dE()) {
            i = 8388629;
            if (mIsRtl) {
                textView.setPadding(getIconWidth(), 0, 0, 0);
            } else {
                textView.setPadding(0, 0, getIconWidth() + 75, 0);
            }
        }
        textView.setGravity(i);
        ((LayoutParams) textView.getLayoutParams()).gravity = i;
        textView.setContentDescription(str);
    }

    protected final boolean dE() {
        if (!Dh && !mUseTwoBubbles) {
            return mUseTwoBubbles;
        }
        return true;
    }

    protected final int getRtlDimens() {
        return mUseTwoBubbles ? getMicWidth() + twoBubbleGap : 0;
    }

    protected int getMicWidth() {
        if (!mUseTwoBubbles || TextUtils.isEmpty(this.Dg)) {
            return mSearchIconWidth;
        }
        return (Math.round(qsbTextHintSize.measureText(this.Dg)) + qsbTextSpacing) + mSearchIconWidth;
    }

    protected final void addOrUpdateSearchRipple() {
        int width;
        int height;
        int micWidth;
        int micHeight;
        InsetDrawable insetDrawable = (InsetDrawable) createRipple().mutate();
        RippleDrawable oldRipple = (RippleDrawable) insetDrawable.getDrawable();
        if (mIsRtl) {
            width = getRtlDimens();
            height = 0;
        } else {
            width = 0;
            height = getRtlDimens();
        }
        Objects.requireNonNull(oldRipple).setLayerInset(0, width, 0, height, 0);
        setBackground(insetDrawable);
        RippleDrawable newRipple = (RippleDrawable) Objects.requireNonNull(oldRipple.getConstantState()).newDrawable().mutate();
        newRipple.setLayerInset(0, 0, mShadowMargin, 0, mShadowMargin);
        mMicIconView.setBackground(newRipple);
        mMicFrame.getLayoutParams().width = getMicWidth();
        if (mIsRtl) {
            micWidth = 0;
        } else {
            micWidth = getMicWidth() - mSearchIconWidth;
        }
        if (mIsRtl) {
            micHeight = getMicWidth() - mSearchIconWidth;
        } else {
            micHeight = 0;
        }
        mMicIconView.setPadding(micWidth, 0, micHeight, 0);
        mMicIconView.requestLayout();
    }

    private InsetDrawable createRipple() {
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(getCornerRadius());
        shape.setColor(ContextCompat.getColor(getContext(), android.R.color.white));

        ColorStateList rippleColor = ContextCompat.getColorStateList(getContext(), R.color.focused_background);
        RippleDrawable ripple = new RippleDrawable(rippleColor, null, shape);
        return new InsetDrawable(ripple, mShadowMargin);
    }

    protected float getCornerRadius() {
        mRadius = getCornerRadius(getContext(),
                Utilities.pxFromDp(100, getResources().getDisplayMetrics()));
        return mRadius;
    }

    public boolean dI() {
        return false;
    }

    public void onClick(View view) {
        SearchProviderController controller = SearchProviderController.Companion.getInstance(mContext);
        SearchProvider provider = controller.getSearchProvider();
        if (view == mMicIconView) {
            if (controller.isGoogle()) {
                fallbackSearch(mShowAssistant ? Intent.ACTION_VOICE_COMMAND : "android.intent.action.VOICE_ASSIST");
            } else if (mShowAssistant && provider.getSupportsAssistant()) {
                provider.startAssistant(intent -> {
                    getContext().startActivity(intent);
                    return null;
                });
            } else if (provider.getSupportsVoiceSearch()) {
                provider.startVoiceSearch(intent -> {
                    getContext().startActivity(intent);
                    return null;
                });
            }
        } else if (view == mLogoIconView) {
            if (provider.getSupportsFeed() && logoCanOpenFeed()) {
                provider.startFeed(intent -> {
                    getContext().startActivity(intent);
                    return null;
                });
            } else {
                startSearch("", mResult);
            }
        }
    }

    protected boolean logoCanOpenFeed() {
        return true;
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case "opa_enabled":
            case "opa_assistant":
            case "pref_searchbar_radius":
            case "pref_bubbleSearchStyle":
                loadPreferences(sharedPreferences);
        }
    }

    @Override
    public void onSearchProviderChanged() {
        loadPreferences(Utilities.getPrefs(getContext()));
    }

    protected void loadPreferences(SharedPreferences sharedPreferences) {
        post(() -> {
            SearchProvider provider = SearchProviderController.Companion.getInstance(getContext()).getSearchProvider();
            boolean providerSupported = provider.getSupportsAssistant() || provider.getSupportsVoiceSearch();
            boolean showMic = sharedPreferences.getBoolean("opa_enabled", true) && providerSupported;
            mShowAssistant = sharedPreferences.getBoolean("opa_assistant", true);
            mLogoIconView.setImageDrawable(getIcon());
            mHotseatLogoIconView.setImageDrawable(getHotseatIcon(true));
            mMicFrame.setVisibility(showMic ? View.VISIBLE : View.GONE);
            mMicIconView.setVisibility(View.VISIBLE);
            mMicIconView.setImageDrawable(getMicIcon());
            mUseTwoBubbles = useTwoBubbles();
            mRadius = Utilities.getOmegaPrefs(getContext()).getSearchBarRadius();
            clearBitmaps();
            addOrUpdateSearchRipple();
            invalidate();
        });
    }

    private void clearBitmaps() {
        mAllAppsShadowBitmap = null;
        mHotseatShadowBitmap = null;
        mClearBitmap = null;
    }

    protected Drawable getHotseatIcon(boolean colored) {
        return getIcon(colored);
    }

    protected Drawable getIcon() {
        return getIcon(true);
    }

    protected Drawable getIcon(boolean colored) {
        SearchProvider provider = SearchProviderController.Companion.getInstance(getContext()).getSearchProvider();
        return provider.getIcon(colored);
    }

    protected Drawable getMicIcon() {
        return getMicIcon(true);
    }

    protected Drawable getMicIcon(boolean colored) {
        SearchProvider provider = SearchProviderController.Companion.getInstance(getContext()).getSearchProvider();
        if (mShowAssistant && provider.getSupportsAssistant()) {
            return provider.getAssistantIcon(colored);
        } else if (provider.getSupportsVoiceSearch()) {
            return provider.getVoiceIcon(colored);
        } else {
            mUseTwoBubbles = false;
            mMicIconView.setVisibility(View.GONE);
            return new ColorDrawable(Color.TRANSPARENT);
        }
    }

    public boolean onLongClick(View view) {
        if (view != this) {
            return false;
        }
        return dK();
    }

    protected boolean dK() {
        String clipboardText = getClipboardText();
        Intent settingsBroadcast = createSettingsBroadcast();
        Intent settingsIntent = createSettingsIntent();
        if (settingsIntent == null && settingsBroadcast == null && clipboardText == null) {
            return false;
        }

        startActionMode(new QsbActionMode(this, clipboardText, settingsBroadcast, settingsIntent), 1);

        return true;
    }

    @Nullable
    protected String getClipboardText() {
        ClipboardManager clipboardManager = ContextCompat
                .getSystemService(getContext(), ClipboardManager.class);
        ClipData primaryClip = Objects.requireNonNull(clipboardManager).getPrimaryClip();
        if (primaryClip != null) {
            for (int i = 0; i < primaryClip.getItemCount(); i++) {
                CharSequence text = primaryClip.getItemAt(i).coerceToText(getContext());
                if (!TextUtils.isEmpty(text)) {
                    return text.toString();
                }
            }
        }
        return null;
    }

    protected Intent createSettingsBroadcast() {
        return null;
    }

    protected Intent createSettingsIntent() {
        return null;
    }

    protected void fallbackSearch(String action) {
        try {
            getContext().startActivity(new Intent(action)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    .setPackage(GOOGLE_QSB));
        } catch (ActivityNotFoundException e) {
            noGoogleAppSearch();
        }
    }

    protected void noGoogleAppSearch() {
    }

    public boolean useTwoBubbles() {
        return mMicIconView.getVisibility() == View.VISIBLE && Utilities.getOmegaPrefs(mContext).getDualBubbleSearch();
    }

    protected OmegaLauncher getLauncher() {
        return (OmegaLauncher) mActivity;
    }
}

