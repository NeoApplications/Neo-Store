package com.google.android.apps.nexuslauncher.qsb;

import static java.lang.Math.round;

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
import android.graphics.PorterDuff.Mode;
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
import android.util.TypedValue;
import android.view.Gravity;
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
import com.saggitt.omega.OmegaPreferences;
import com.saggitt.omega.search.SearchProvider;
import com.saggitt.omega.search.SearchProviderController;

import org.jetbrains.annotations.NotNull;

public abstract class AbstractQsbLayout extends FrameLayout implements OnSharedPreferenceChangeListener,
        OnClickListener, OnLongClickListener, Insettable, SearchProviderController.OnProviderChangeListener, WallpaperColorInfo.OnChangeListener {
    private static final Rect mSrcRect = new Rect();
    protected Paint mMicStrokePaint;
    protected NinePatchDrawHelper mShadowHelper;
    protected NinePatchDrawHelper mClearShadowHelper;
    protected ActivityContext mActivity;
    protected Context mContext;
    protected int qsbTextSpacing;
    protected int twoBubbleGap;
    protected int mSearchIconWidth;
    protected boolean mIsRtl;
    private int mShadowMargin;
    protected TextPaint qsbTextHint;
    private int qsbHintLenght;
    private TransformingTouchDelegate mTouchDelegate;
    protected int mColor;
    public float micStrokeWidth;
    protected Bitmap mBubbleShadowBitmap;
    protected int mAllAppsBgColor;
    protected int mBubbleBgColor;
    protected ImageView mLogoIconView;
    protected FrameLayout mMicFrame;
    protected ImageView mMicIconView;
    protected String hintTextValue;
    protected boolean showHintAssitant;
    protected int mResult;
    protected boolean mUseTwoBubbles;
    protected Bitmap mAllAppsShadowBitmap;
    protected Bitmap mClearBitmap;
    private boolean mShowAssistant;
    private float mRadius = -1.0f;
    protected SearchProvider searchProvider;
    protected OmegaPreferences prefs;

    public AbstractQsbLayout(Context context) {
        super(context);
        init(context);
    }

    public AbstractQsbLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context);
    }

    public AbstractQsbLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init(context);
    }

    private void init(Context context) {
        qsbTextHint = new TextPaint();
        mMicStrokePaint = new Paint(1);
        mMicStrokePaint.setColor(Color.GREEN);
        mShadowHelper = new NinePatchDrawHelper();
        mClearShadowHelper = new NinePatchDrawHelper();
        mClearShadowHelper.paint.setXfermode(new PorterDuffXfermode(Mode.DST_OUT));
        mResult = 0;
        setOnLongClickListener(this);
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
        prefs = Utilities.getOmegaPrefs(mContext);
    }

    /*
     * Obtener el radio redondeado desde las preferencias o desde el icono
     * */
    public float getCornerRadius() {
        float radius = round(prefs.getSearchBarRadius());
        if (radius > 0f) {
            return radius;
        }
        TypedValue edgeRadius = IconShape.getShape().getAttrValue(R.attr.qsbEdgeRadius);
        if (edgeRadius != null) {
            return edgeRadius.getDimension(mContext.getResources().getDisplayMetrics());
        } else {
            return ResourceUtils.pxFromDp(100, getResources().getDisplayMetrics());
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

    protected void onDetachedFromWindow() {
        getDevicePreferences().unregisterOnSharedPreferenceChangeListener(this);
        SearchProviderController.Companion.getInstance(getContext()).removeOnProviderChangeListener(this);
        WallpaperColorInfo.getInstance(getContext()).removeOnChangeListener(this);
        super.onDetachedFromWindow();
    }

    private void updateConfiguration() {
        addOrUpdateSearchPaint(0.0f);
        addOrUpdateSearchRipple();
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getActionMasked() == 0) {
            View gIcon = findViewById(R.id.g_icon);
            int result = 0;
            int newResult = 1;
            if (mIsRtl) {
                if (Float.compare(motionEvent.getX(), (float) gIcon.getLeft()) >= 0) {
                    result = 1;
                }
            } else {
                if (Float.compare(motionEvent.getX(), (float) gIcon.getRight()) <= 0) {
                    result = 1;
                }
            }
            if (result == 0) {
                newResult = 2;
            }
            mResult = newResult;
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
        mMicFrame = findViewById(R.id.mic_frame);
        mMicIconView = findViewById(R.id.mic_icon);
        mMicIconView.setOnClickListener(this);
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

    @Override
    public void onExtractedColorsChanged(WallpaperColorInfo wallpaperColorInfo) {
        setColor(ColorUtils.compositeColors(ColorUtils.compositeColors(
                Themes.getAttrBoolean(mContext, R.attr.isMainColorDark)
                        ? -650362813 : -855638017, Themes.getAttrColor(mContext, R.attr.allAppsScrimColor)),
                wallpaperColorInfo.getMainColor()));
    }

    private void setColor(int color) {
        if (mColor != color) {
            mColor = color;
            mAllAppsShadowBitmap = null;
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

    public final void setBubbleBgColor(int color) {
        mBubbleBgColor = color;
        if (mBubbleBgColor != mAllAppsBgColor || mBubbleShadowBitmap != mAllAppsShadowBitmap) {
            mBubbleShadowBitmap = null;
            invalidate();
        }
    }

    public final void addOrUpdateSearchPaint(float f) {
        micStrokeWidth = TypedValue.applyDimension(1, f, getResources().getDisplayMetrics());
        mMicStrokePaint.setStrokeWidth(micStrokeWidth);
        mMicStrokePaint.setStyle(Style.STROKE);
        mMicStrokePaint.setColor(0xFFBDC1C6);
    }

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

    /*
     * Obtener el ancho de la barra
     * */
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

    final void ensureAllAppsShadowBitmap() {
        if (mAllAppsShadowBitmap == null) {
            mAllAppsShadowBitmap = createShadowBitmap(mAllAppsBgColor, true);
            mClearBitmap = null;
            if (Color.alpha(mAllAppsBgColor) != 255) {
                mClearBitmap = createShadowBitmap(0xFF000000, false);
            }
        }
    }

    protected void clearMainPillBg(Canvas canvas) {
    }

    protected void clearPillBg(Canvas canvas, int left, int top, int right) {
    }

    public void draw(Canvas canvas) {
        ensureAllAppsShadowBitmap();
        clearMainPillBg(canvas);
        drawQsb(canvas);
        super.draw(canvas);
    }

    /*
     * Crear barra principal en color blanco o negro
     * */
    protected void drawQsb(@NonNull Canvas canvas) {
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
            int paddingTop2 = getPaddingTop();
            int paddingLeft3 = mIsRtl ? getPaddingLeft() + getMicWidth() : getWidth() - getPaddingRight();
            int paddingBottom = LauncherAppState.getInstance(getContext()).getInvariantDeviceProfile().iconBitmapSize - getPaddingBottom();
            float f = ((float) (paddingBottom - paddingTop2)) * 0.5f;
            float i3 = micStrokeWidth / 2.0f;
            if (mUseTwoBubbles) {
                i2 = i3;
                canvas.drawRoundRect(i + i2, paddingTop2 + i2, paddingLeft3 - i2, (paddingBottom - i2) + 1, f, f, mMicStrokePaint);

            } else {
                canvas.drawRoundRect(i + i3, paddingTop2 + i3, paddingLeft3 - i3, (paddingBottom - i3) + 1, f, f, mMicStrokePaint);
            }
        }
    }

    /*
     * Dibuja la barra transparente que aparece al hacer click
     * Incluye el la burbuja del micrófono (newRipple)
     * */
    protected final void addOrUpdateSearchRipple() {
        int width = mIsRtl ? getRtlDimens() : 0;
        int height = mIsRtl ? 0 : getRtlDimens();

        InsetDrawable insetDrawable = (InsetDrawable) createRipple().mutate();
        RippleDrawable oldRipple = (RippleDrawable) insetDrawable.getDrawable();
        oldRipple.setLayerInset(0, width, 0, height, 0);
        setBackground(insetDrawable);

        RippleDrawable newRipple = (RippleDrawable) oldRipple.getConstantState().newDrawable().mutate();
        newRipple.setLayerInset(0, 0, mShadowMargin, 0, mShadowMargin);

        mMicIconView.setBackground(newRipple);
        mMicFrame.getLayoutParams().width = getMicWidth();
        mMicFrame.setPadding(16, 0, 0, 0);

        int micWidth = mIsRtl ? 0 : getMicWidth() - mSearchIconWidth;
        int micHeight = mIsRtl ? getMicWidth() - mSearchIconWidth : 0;

        mMicIconView.setPadding(micWidth, 0, micHeight, 0);
        mMicIconView.requestLayout();
    }

    private void drawMainPill(Canvas canvas) {
        drawShadow(mAllAppsShadowBitmap, canvas);
        mShadowHelper.paint.setAlpha(255);
    }

    protected final void drawShadow(Bitmap bitmap, Canvas canvas) {
        drawPill(mShadowHelper, bitmap, canvas);
    }

    protected final void drawPill(NinePatchDrawHelper helper, Bitmap bitmap, Canvas canvas) {
        int shadowDimens = getShadowDimens(bitmap);
        int left = getPaddingLeft() - shadowDimens;
        int top = getPaddingTop() - ((bitmap.getHeight() - getHeightWithoutPadding()) / 2);
        int right = (getWidth() - getPaddingRight()) + shadowDimens;
        if (mIsRtl) {
            left += getRtlDimens();
        } else {
            right -= getRtlDimens();
        }
        helper.draw(bitmap, canvas, (float) left, (float) top, (float) right);
    }

    protected final int getShadowDimens(@NotNull Bitmap bitmap) {
        return (bitmap.getWidth() - (getHeightWithoutPadding() + 20)) / 2;
    }

    private Bitmap createShadowBitmap(int bgColor, boolean withShadow) {
        float f = (float) LauncherAppState.getInstance(getContext()).getInvariantDeviceProfile().iconBitmapSize;
        return createShadowBitmap(0.010416667f * f, f * 0.020833334f, bgColor, withShadow);
    }

    protected final Bitmap createShadowBitmap(float shadowBlur, float keyShadowDistance, int color, boolean withShadow) {
        int height = getHeightWithoutPadding();
        Builder builder = new Builder(color);
        builder.shadowBlur = shadowBlur;
        builder.keyShadowDistance = keyShadowDistance;
        if (!withShadow) {
            builder.ambientShadowAlpha = 0;
        }
        builder.keyShadowAlpha = builder.ambientShadowAlpha;
        Bitmap pill;
        if (mRadius < 0) {
            TypedValue edgeRadius = IconShape.getShape().getAttrValue(R.attr.qsbEdgeRadius);
            if (edgeRadius != null) {
                pill = builder.createPill(height, height,
                        edgeRadius.getDimension(getResources().getDisplayMetrics()));
            } else {
                pill = builder.createPill(height, height);
            }
        } else {
            pill = builder.createPill(height, height, mRadius);
        }
        if (Utilities.ATLEAST_P) {
            return pill.copy(Bitmap.Config.HARDWARE, false);
        }
        return pill;
    }

    protected int getHeightWithoutPadding() {
        return (getHeight() - getPaddingTop()) - getPaddingBottom();
    }

    protected final int getSearchIconWidth() {
        return mUseTwoBubbles ? mSearchIconWidth : mSearchIconWidth + qsbTextSpacing;
    }

    protected final void setHintText(String str, TextView textView) {
        if (TextUtils.isEmpty(str) || !dE()) {
            hintTextValue = str;
        } else {
            hintTextValue = TextUtils.ellipsize(str, qsbTextHint, (float) qsbHintLenght, TruncateAt.END).toString();
        }
        textView.setText(hintTextValue);
        int gravity = Gravity.CENTER;
        if (dE()) {
            gravity = Gravity.END;
            if (mIsRtl) {
                textView.setPadding(getSearchIconWidth(), 0, 0, 0);
            } else {
                textView.setPadding(0, 0, getSearchIconWidth(), 0);
            }
        }
        textView.setGravity(gravity);
        ((LayoutParams) textView.getLayoutParams()).gravity = gravity;
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
        if (!mUseTwoBubbles || TextUtils.isEmpty(hintTextValue)) {
            return mSearchIconWidth;
        }
        return (Math.round(qsbTextHint.measureText(hintTextValue)) + qsbTextSpacing) + mSearchIconWidth;
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

    public void onClick(View view) {
        SearchProviderController controller = SearchProviderController.Companion
                .getInstance(getContext());
        SearchProvider provider = controller.getSearchProvider();
        if (view == mMicIconView) {
            if (mShowAssistant && provider.getSupportsAssistant()) {
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
            if (provider.getSupportsFeed()) {
                provider.startFeed(intent -> {
                    mContext.startActivity(intent);
                    return null;
                });
            } else {
                startSearch("", mResult);
            }
        }
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case "opa_enabled":
            case "opa_assistant":
            case "pref_bubbleSearchStyle":
            case "pref_searchbarRadius":
                loadPreferences(sharedPreferences);
        }
    }

    private void clearBitmaps() {
        mAllAppsShadowBitmap = null;
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
            mLogoIconView.setImageDrawable(getIcon(true));
            mMicFrame.setVisibility(showMic ? View.VISIBLE : View.GONE);
            mMicIconView.setVisibility(View.VISIBLE);
            mMicIconView.setImageDrawable(getMicIcon(true));
            mUseTwoBubbles = useTwoBubbles();
            mRadius = getCornerRadius();
            clearBitmaps();
            addOrUpdateSearchRipple();
            invalidate();
        });
    }

    protected Drawable getIcon(boolean colored) {
        return searchProvider.getIcon(colored);
    }

    protected Drawable getMicIcon(boolean colored) {
        if (searchProvider.getSupportsAssistant() && mShowAssistant) {
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
        assert clipboardManager != null;
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

    public boolean useTwoBubbles() {
        return mMicFrame != null && mMicFrame.getVisibility() == View.VISIBLE &&
                prefs.getDualBubbleSearch();
    }

    public OmegaLauncher getLauncher() {
        return (OmegaLauncher) mActivity;
    }
}
