package com.google.android.apps.nexuslauncher.qsb;

import static java.lang.Math.round;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.LauncherApps;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Process;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import com.android.launcher3.DeviceProfile;
import com.android.launcher3.Insettable;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.R;
import com.android.launcher3.ResourceUtils;
import com.android.launcher3.Utilities;
import com.android.launcher3.graphics.IconShape;
import com.android.launcher3.graphics.NinePatchDrawHelper;
import com.android.launcher3.icons.ShadowGenerator.Builder;
import com.android.launcher3.uioverrides.WallpaperColorInfo;
import com.android.launcher3.util.Themes;
import com.android.launcher3.util.TransformingTouchDelegate;
import com.android.launcher3.views.ActivityContext;
import com.saggitt.omega.OmegaLauncher;
import com.saggitt.omega.search.SearchProvider;
import com.saggitt.omega.search.SearchProviderController;

public abstract class AbstractQsbLayout extends FrameLayout implements OnSharedPreferenceChangeListener,
        OnClickListener, OnLongClickListener, Insettable, SearchProviderController.OnProviderChangeListener, WallpaperColorInfo.OnChangeListener {
    private static final Rect mSrcRect = new Rect();
    public static FloatProperty HOTSEAT_PROGRESS = new FloatProperty<AbstractQsbLayout>("hotseatProgress") {
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
    protected final TextPaint qsbTextHint;
    protected final Paint mMicStrokePaint;
    protected final NinePatchDrawHelper mShadowHelper;
    protected final NinePatchDrawHelper mClearShadowHelper;
    protected final ActivityContext mActivity;
    protected Context mContext;
    protected final int qsbTextSpacing;
    protected final int twoBubbleGap;
    protected final int mSearchIconWidth;
    protected final boolean mIsRtl;
    private final int qsbDoodle;
    private final int mShadowMargin;
    private final int qsbHintLenght;
    private final TransformingTouchDelegate mTouchDelegate;
    protected int mColor;
    private final boolean mIsWorkspaceDarkText;
    public float micStrokeWidth;
    protected Bitmap mBubbleShadowBitmap;
    protected int mAllAppsBgColor;
    protected int mHotseatBgColor;
    protected int mBubbleBgColor;
    protected ImageView mLogoIconView;
    protected ImageView mHotseatLogoIconView;
    protected FrameLayout mMicFrame;
    protected ImageView mMicIconView;
    protected String Dg;
    protected boolean showHintAssitant;
    protected int mResult;
    protected boolean mUseTwoBubbles;
    protected Bitmap mAllAppsShadowBitmap;
    protected Bitmap mHotseatShadowBitmap;
    protected Bitmap mClearBitmap;
    protected float mHotseatProgress = 1f;
    private boolean mShowAssistant;
    private float mRadius = -1.0f;
    SearchProvider searchProvider;

    public AbstractQsbLayout(Context context) {
        this(context, null);
    }

    public AbstractQsbLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public AbstractQsbLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        qsbTextHint = new TextPaint();
        mMicStrokePaint = new Paint(1);
        mShadowHelper = new NinePatchDrawHelper();
        mClearShadowHelper = new NinePatchDrawHelper();
        mClearShadowHelper.paint.setXfermode(new PorterDuffXfermode(Mode.DST_OUT));
        mResult = 0;
        mIsWorkspaceDarkText = Themes.getAttrBoolean(context, R.attr.isWorkspaceDarkText);
        setOnLongClickListener(this);
        qsbDoodle = getResources().getDimensionPixelSize(R.dimen.qsb_doodle_tap_target_logo_width);
        mSearchIconWidth = getResources().getDimensionPixelSize(R.dimen.qsb_mic_width);
        qsbTextSpacing = getResources().getDimensionPixelSize(R.dimen.qsb_text_spacing);
        twoBubbleGap = getResources().getDimensionPixelSize(R.dimen.qsb_two_bubble_gap);
        qsbTextHint.setTextSize((float) getResources().getDimensionPixelSize(R.dimen.qsb_hint_text_size));
        mShadowMargin = getResources().getDimensionPixelSize(R.dimen.qsb_shadow_margin);
        qsbHintLenght = getResources().getDimensionPixelSize(R.dimen.qsb_max_hint_length);
        mIsRtl = Utilities.isRtl(getResources());
        mTouchDelegate = new TransformingTouchDelegate(this);
        setTouchDelegate(mTouchDelegate);
        mContext = context;
        mActivity = ActivityContext.lookupContext(context);
    }

    public static float getCornerRadius(Context context, float defaultRadius) {
        float radius = round(Utilities.getOmegaPrefs(context).getSearchBarRadius());
        if (radius > 0f) {
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

    protected abstract int aA(int i);

    public abstract void l(String str);

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

    private void updateConfiguration() {
        addOrUpdateSearchPaint(0.0f);
        addOrUpdateSearchRipple();
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

    protected final void loadIcons() {
        mLogoIconView = findViewById(R.id.g_icon);
        mLogoIconView.setOnClickListener(this);
        mHotseatLogoIconView = findViewById(R.id.g_icon_hotseat);
        mMicFrame = findViewById(R.id.mic_frame);
        mMicIconView = findViewById(R.id.mic_icon);
        mMicIconView.setOnClickListener(this);
    }

    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        if (this.mIsRtl) {
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

    public final void addOrUpdateSearchPaint(float f) {
        this.micStrokeWidth = TypedValue.applyDimension(1, f, getResources().getDisplayMetrics());
        this.mMicStrokePaint.setStrokeWidth(this.micStrokeWidth);
        this.mMicStrokePaint.setStyle(Style.STROKE);
        this.mMicStrokePaint.setColor(0xFFBDC1C6);
    }

    public void setInsets(Rect rect) {
        requestLayout();
    }

    protected void onMeasure(int i, int i2) {
        DeviceProfile deviceProfile = mActivity.getDeviceProfile();
        int aA = aA(MeasureSpec.getSize(i));
        int i3 = aA / deviceProfile.inv.numHotseatIcons;
        int round = round(0.92f * ((float) deviceProfile.iconSizePx));
        setMeasuredDimension(((aA - (i3 - round)) + getPaddingLeft()) + getPaddingRight(), MeasureSpec.getSize(i2));
        for (aA = getChildCount() - 1; aA >= 0; aA--) {
            View childAt = getChildAt(aA);
            measureChildWithMargins(childAt, i, 0, i2, 0);
            if (childAt.getMeasuredWidth() <= round) {
                LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
                int measuredWidth = (round - childAt.getMeasuredWidth()) / 2;
                layoutParams.rightMargin = measuredWidth;
                layoutParams.leftMargin = measuredWidth;
            }
        }
    }

    protected final Bitmap dA() {
        ensureAllAppsShadowBitmap();
        return this.mAllAppsShadowBitmap;
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

    protected void drawQsb(@NonNull Canvas canvas) {
        int i;
        drawMainPill(canvas);
        if (this.mUseTwoBubbles) {
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
            i = a(bitmap2);
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
            int paddingTop2 = getPaddingTop();
            int paddingLeft3 = mIsRtl ? getPaddingLeft() + getMicWidth() : getWidth() - getPaddingRight();
            int paddingBottom = LauncherAppState.getInstance(getContext()).getInvariantDeviceProfile().iconBitmapSize - getPaddingBottom();
            float f = ((float) (paddingBottom - paddingTop2)) * 0.5f;
            float i3 = micStrokeWidth / 2.0f;
            if (mUseTwoBubbles) {
                i2 = i3;
            } else {
                i2 = i3;
                canvas.drawRoundRect(i + i3, paddingTop2 + i3, paddingLeft3 - i3, (paddingBottom - i3) + 1, f, f, mMicStrokePaint);
            }
            canvas.drawRoundRect(i + i2, paddingTop2 + i2, paddingLeft3 - i2, (paddingBottom - i2) + 1, f, f, mMicStrokePaint);
        }
    }

    private void drawMainPill(Canvas canvas) {
        if (mAllAppsBgColor == mHotseatBgColor || mHotseatProgress == 0f) {
            drawShadow(mAllAppsShadowBitmap, canvas);
        } else if (mHotseatProgress == 1f) {
            drawShadow(mHotseatShadowBitmap, canvas);
        } else {
            mShadowHelper.paint.setAlpha(Math.round(255 * (1 - mHotseatProgress)));
            drawShadow(mAllAppsShadowBitmap, canvas);
            mShadowHelper.paint.setAlpha(Math.round(255 * mHotseatProgress));
            drawShadow(mHotseatShadowBitmap, canvas);
            mShadowHelper.paint.setAlpha(255);
        }
    }

    protected final void drawShadow(Bitmap bitmap, Canvas canvas) {
        drawPill(mShadowHelper, bitmap, canvas);
    }

    protected final void drawPill(NinePatchDrawHelper helper, Bitmap bitmap, Canvas canvas) {
        int a = a(bitmap);
        int left = getPaddingLeft() - a;
        int top = getPaddingTop() - ((bitmap.getHeight() - getHeightWithoutPadding()) / 2);
        int right = (getWidth() - getPaddingRight()) + a;
        if (mIsRtl) {
            left += getRtlDimens();
        } else {
            right -= getRtlDimens();
        }
        helper.draw(bitmap, canvas, (float) left, (float) top, (float) right);
    }

    private Bitmap createShadowBitmap(int bgColor, boolean withShadow) {
        float f = (float) LauncherAppState.getInstance(getContext()).getInvariantDeviceProfile().iconBitmapSize;
        return createShadowBitmap(0.010416667f * f, f * 0.020833334f, bgColor, withShadow);
    }

    protected final Bitmap createShadowBitmap(float f, float f2, int i, boolean withShadow) {
        int dC = getHeightWithoutPadding();
        int i2 = dC + 20;
        Builder builder = new Builder(i);
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
                pill = builder.createPill(i2, dC,
                        edgeRadius.getDimension(getResources().getDisplayMetrics()));
            } else {
                pill = builder.createPill(i2, dC);
            }
        } else {
            pill = builder.createPill(i2, dC, mRadius);
        }
        if (Utilities.ATLEAST_P) {
            return pill.copy(Config.HARDWARE, false);
        }
        return pill;
    }

    protected final int a(Bitmap bitmap) {
        return (bitmap.getWidth() - (getHeightWithoutPadding() + 20)) / 2;
    }

    protected final int getHeightWithoutPadding() {
        return (getHeight() - getPaddingTop()) - getPaddingBottom();
    }

    protected final int dD() {
        return mUseTwoBubbles ? mSearchIconWidth : mSearchIconWidth + qsbTextSpacing;
    }

    protected final void setHintText(String str, TextView textView) {
        String str2;
        if (TextUtils.isEmpty(str) || !dE()) {
            str2 = str;
        } else {
            str2 = TextUtils.ellipsize(str, qsbTextHint, (float) qsbHintLenght, TruncateAt.END).toString();
        }
        Dg = str2;
        textView.setText(Dg);
        int i = 17;
        if (dE()) {
            i = 8388629;
            if (mIsRtl) {
                textView.setPadding(dD(), 0, 0, 0);
            } else {
                textView.setPadding(0, 0, dD(), 0);
            }
        }
        textView.setGravity(i);
        ((LayoutParams) textView.getLayoutParams()).gravity = i;
        textView.setContentDescription(str);
    }

    protected final boolean dE() {
        if (!showHintAssitant) {
            return mUseTwoBubbles;
        }
        return true;
    }

    protected final int getRtlDimens() {
        return mUseTwoBubbles ? getMicWidth() + twoBubbleGap : 0;
    }

    protected final int getMicWidth() {
        if (!mUseTwoBubbles || TextUtils.isEmpty(this.Dg)) {
            return mSearchIconWidth;
        }
        return (Math.round(qsbTextHint.measureText(this.Dg)) + qsbTextSpacing) + mSearchIconWidth;
    }

    protected final void addOrUpdateSearchRipple() {
        int width;
        int height;
        int micWidth;
        int micHeight;
        InsetDrawable insetDrawable = (InsetDrawable) createRipple().mutate();
        RippleDrawable rippleDrawable = (RippleDrawable) insetDrawable.getDrawable();
        if (mIsRtl) {
            width = getRtlDimens();
        } else {
            width = 0;
        }
        if (mIsRtl) {
            height = 0;
        } else {
            height = getRtlDimens();
        }
        rippleDrawable.setLayerInset(0, width, 0, height, 0);
        setBackground(insetDrawable);
        RippleDrawable newRipple = (RippleDrawable) rippleDrawable.getConstantState().newDrawable().mutate();
        newRipple.setLayerInset(0, 0, mShadowMargin, 0, mShadowMargin);
        mMicIconView.setBackground(newRipple);
        mMicFrame.getLayoutParams().width = getMicWidth();
        if (mIsRtl) {
            micWidth = 0;
            micHeight = getMicWidth() - mSearchIconWidth;
        } else {
            micWidth = getMicWidth() - mSearchIconWidth;
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
        return new InsetDrawable(ripple, getResources().getDimensionPixelSize(R.dimen.qsb_shadow_margin));
    }

    protected float getCornerRadius() {
        return getCornerRadius(getContext(),
                ResourceUtils.pxFromDp(100, getResources().getDisplayMetrics()));
    }

    public boolean dI() {
        return false;
    }

    public void onClick(View view) {
        SearchProviderController controller = SearchProviderController.Companion
                .getInstance(getContext());
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
                    mContext.startActivity(intent);
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

    protected final void k(String str) {
        try {
            getContext().startActivity(new Intent(str).addFlags(268468224).setPackage("com.google.android.googlequicksearchbox"));
        } catch (ActivityNotFoundException e) {
            getContext().getSystemService(LauncherApps.class).startAppDetailsActivity(new ComponentName("com.google.android.googlequicksearchbox", ".SearchActivity"), Process.myUserHandle(), null, null);
        }
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case "opa_enabled":
            case "opa_assistant":
            case "pref_bubbleSearchStyle":
                loadPreferences(sharedPreferences);
        }
        if (key.equals("pref_searchbarRadius")) {
            loadPreferences(sharedPreferences);
        }
    }

    private void clearBitmaps() {
        mAllAppsShadowBitmap = null;
        mHotseatShadowBitmap = null;
        mClearBitmap = null;
    }

    @Override
    public void onSearchProviderChanged() {
        loadPreferences(Utilities.getPrefs(getContext()));
    }

    protected void loadPreferences(SharedPreferences sharedPreferences) {
        post(() -> {
            searchProvider = SearchProviderController.Companion.getInstance(getContext()).getSearchProvider();
            boolean providerSupported = searchProvider.getSupportsAssistant() || searchProvider.getSupportsVoiceSearch();
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

    protected Drawable getIcon() {
        return getIcon(true);
    }

    protected Drawable getIcon(boolean colored) {
        return searchProvider.getIcon(colored);
    }

    protected Drawable getHotseatIcon(boolean colored) {
        return getIcon(colored);
    }

    protected Drawable getMicIcon() {
        return getMicIcon(true);
    }

    protected Drawable getMicIcon(boolean colored) {
        if (searchProvider.getSupportsAssistant()) {
            return searchProvider.getAssistantIcon(colored);
        } else if (searchProvider.getSupportsVoiceSearch()) {
            return searchProvider.getVoiceIcon(colored);
        } else {
            mMicIconView.setVisibility(GONE);
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
        ClipData primaryClip = clipboardManager.getPrimaryClip();
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
                    .setPackage(com.saggitt.omega.util.Config.GOOGLE_QSB));
        } catch (ActivityNotFoundException e) {
            noGoogleAppSearch();
        }
    }

    protected void noGoogleAppSearch() {
    }

    public boolean useTwoBubbles() {
        return mMicFrame != null && mMicFrame.getVisibility() == View.VISIBLE &&
                Utilities.getOmegaPrefs(getContext()).getDualBubbleSearch();
    }

    public OmegaLauncher getLauncher() {
        return (OmegaLauncher) mActivity;
    }
}
