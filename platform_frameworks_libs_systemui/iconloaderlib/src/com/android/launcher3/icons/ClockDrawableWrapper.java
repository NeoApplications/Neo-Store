/*
 * Copyright (C) 2019 The Android Open Source Project
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
package com.android.launcher3.icons;

import static com.android.launcher3.icons.ThemedIconDrawable.getColors;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.Log;
import android.util.TypedValue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Supplier;

import com.android.launcher3.icons.ThemedIconDrawable.ThemeData;
import com.saggitt.omega.icons.ClockMetadata;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import java.util.function.IntFunction;

/**
 * Wrapper over {@link AdaptiveIconDrawable} to intercept icon flattening logic for dynamic
 * clock icons
 */
@TargetApi(Build.VERSION_CODES.O)
public class ClockDrawableWrapper extends AdaptiveIconDrawable implements BitmapInfo.Extender {

    private static final String TAG = "ClockDrawableWrapper";

    private static final boolean DISABLE_SECONDS = true;

    // Time after which the clock icon should check for an update. The actual invalidate
    // will only happen in case of any change.
    public static final long TICK_MS = DISABLE_SECONDS ? TimeUnit.MINUTES.toMillis(1) : 200L;

    private static final String LAUNCHER_PACKAGE = "com.android.launcher3";
    private static final String ROUND_ICON_METADATA_KEY = LAUNCHER_PACKAGE
            + ".LEVEL_PER_TICK_ICON_ROUND";
    private static final String HOUR_INDEX_METADATA_KEY = LAUNCHER_PACKAGE + ".HOUR_LAYER_INDEX";
    private static final String MINUTE_INDEX_METADATA_KEY = LAUNCHER_PACKAGE
            + ".MINUTE_LAYER_INDEX";
    private static final String SECOND_INDEX_METADATA_KEY = LAUNCHER_PACKAGE
            + ".SECOND_LAYER_INDEX";
    private static final String DEFAULT_HOUR_METADATA_KEY = LAUNCHER_PACKAGE
            + ".DEFAULT_HOUR";
    private static final String DEFAULT_MINUTE_METADATA_KEY = LAUNCHER_PACKAGE
            + ".DEFAULT_MINUTE";
    private static final String DEFAULT_SECOND_METADATA_KEY = LAUNCHER_PACKAGE
            + ".DEFAULT_SECOND";

    /* Number of levels to jump per second for the second hand */
    private static final int LEVELS_PER_SECOND = 10;

    public static final int INVALID_VALUE = -1;

    private final AnimationInfo mAnimationInfo = new AnimationInfo();
    private int mTargetSdkVersion;
    protected ThemeData mThemeData;

    public ClockDrawableWrapper(AdaptiveIconDrawable base) {
        super(base.getBackground(), base.getForeground());
    }

    /**
     * Loads and returns the wrapper from the provided package, or returns null
     * if it is unable to load.
     */
    public static ClockDrawableWrapper forPackage(Context context, String pkg, int iconDpi) {
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo appInfo = pm.getApplicationInfo(pkg,
                    PackageManager.MATCH_UNINSTALLED_PACKAGES | PackageManager.GET_META_DATA);
            Resources res = pm.getResourcesForApplication(appInfo);
            return forExtras(appInfo, appInfo.metaData,
                    resId -> res.getDrawableForDensity(resId, iconDpi));
        } catch (Exception e) {
            Log.d(TAG, "Unable to load clock drawable info", e);
        }
        return null;
    }

    private static ClockDrawableWrapper fromThemeData(Context context, ThemeData themeData) {
        try {
            TypedArray ta = themeData.mResources.obtainTypedArray(themeData.mResID);
            int count = ta.length();
            Bundle extras = new Bundle();
            for (int i = 0; i < count; i += 2) {
                TypedValue v = ta.peekValue(i + 1);
                extras.putInt(ta.getString(i), v.type >= TypedValue.TYPE_FIRST_INT
                        && v.type <= TypedValue.TYPE_LAST_INT
                        ? v.data : v.resourceId);
            }
            ta.recycle();
            ClockDrawableWrapper drawable = ClockDrawableWrapper.forExtras(
                    context.getApplicationInfo(), extras, resId -> {
                        int[] colors = getColors(context);
                        Drawable bg = new ColorDrawable(colors[0]);
                        Drawable fg = themeData.mResources.getDrawable(resId).mutate();
                        fg.setTint(colors[1]);
                        return new AdaptiveIconDrawable(bg, fg);
                    });
            if (drawable != null) {
                return drawable;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading themed clock", e);
        }
        return null;
    }

    private static ClockDrawableWrapper forExtras(ApplicationInfo appInfo, Bundle metadata,
                                                  IntFunction<Drawable> drawableProvider) {
        if (metadata == null) {
            return null;
        }

        int drawableId = metadata.getInt(ROUND_ICON_METADATA_KEY, 0);
        if (drawableId == 0) {
            return null;
        }

        int hourLayerIndex = metadata.getInt(HOUR_INDEX_METADATA_KEY, INVALID_VALUE);
        int minuteLayerIndex = metadata.getInt(MINUTE_INDEX_METADATA_KEY, INVALID_VALUE);
        int secondLayerIndex = metadata.getInt(SECOND_INDEX_METADATA_KEY, INVALID_VALUE);

        int defaultHour = metadata.getInt(DEFAULT_HOUR_METADATA_KEY, 0);
        int defaultMinute = metadata.getInt(DEFAULT_MINUTE_METADATA_KEY, 0);
        int defaultSecond = metadata.getInt(DEFAULT_SECOND_METADATA_KEY, 0);

        ClockMetadata clockMetadata = new ClockMetadata(
                hourLayerIndex,
                minuteLayerIndex,
                secondLayerIndex,
                defaultHour,
                defaultMinute,
                defaultSecond
        );

        return forMeta(appInfo.targetSdkVersion, clockMetadata, () -> drawableProvider.apply(drawableId));
    }

    public static ClockDrawableWrapper forMeta(int targetSdkVersion,
                                               @NonNull ClockMetadata metadata, Supplier<Drawable> drawableProvider) {
        Drawable drawable = drawableProvider.get().mutate();
        if (!(drawable instanceof AdaptiveIconDrawable)) {
            return null;
        }

        ClockDrawableWrapper wrapper =
                new ClockDrawableWrapper((AdaptiveIconDrawable) drawable);
        wrapper.mTargetSdkVersion = targetSdkVersion;
        AnimationInfo info = wrapper.mAnimationInfo;

        info.baseDrawableState = drawable.getConstantState();

        info.hourLayerIndex = metadata.getHourLayerIndex();
        info.minuteLayerIndex = metadata.getMinuteLayerIndex();
        info.secondLayerIndex = metadata.getSecondLayerIndex();

        info.defaultHour = metadata.getDefaultHour();
        info.defaultMinute = metadata.getDefaultMinute();
        info.defaultSecond = metadata.getDefaultSecond();

        LayerDrawable foreground = (LayerDrawable) wrapper.getForeground();
        int layerCount = foreground.getNumberOfLayers();
        if (info.hourLayerIndex < 0 || info.hourLayerIndex >= layerCount) {
            info.hourLayerIndex = INVALID_VALUE;
        }
        if (info.minuteLayerIndex < 0 || info.minuteLayerIndex >= layerCount) {
            info.minuteLayerIndex = INVALID_VALUE;
        }
        if (info.secondLayerIndex < 0 || info.secondLayerIndex >= layerCount) {
            info.secondLayerIndex = INVALID_VALUE;
        } else if (DISABLE_SECONDS) {
            foreground.setDrawable(info.secondLayerIndex, null);
            info.secondLayerIndex = INVALID_VALUE;
        }
        info.applyTime(Calendar.getInstance(), foreground);
        return wrapper;
    }

    @Override
    public ClockBitmapInfo getExtendedInfo(Bitmap bitmap, int color,
                                           BaseIconFactory iconFactory, float normalizationScale, UserHandle user) {
        iconFactory.disableColorExtraction();
        AdaptiveIconDrawable background = new AdaptiveIconDrawable(
                getBackground().getConstantState().newDrawable(), null);
        BitmapInfo bitmapInfo = iconFactory.createBadgedIconBitmap(background,
                Process.myUserHandle(), mTargetSdkVersion, false);

        return new ClockBitmapInfo(bitmap, color, normalizationScale,
                mAnimationInfo, bitmapInfo.icon, mThemeData);
    }

    @Override
    public void drawForPersistence(Canvas canvas) {
        LayerDrawable foreground = (LayerDrawable) getForeground();
        resetLevel(foreground, mAnimationInfo.hourLayerIndex);
        resetLevel(foreground, mAnimationInfo.minuteLayerIndex);
        resetLevel(foreground, mAnimationInfo.secondLayerIndex);
        draw(canvas);
        mAnimationInfo.applyTime(Calendar.getInstance(), (LayerDrawable) getForeground());
    }

    @Override
    public Drawable getThemedDrawable(Context context) {
        if (mThemeData != null) {
            ClockDrawableWrapper drawable = fromThemeData(context, mThemeData);
            return drawable == null ? this : drawable;
        }
        return this;
    }

    private void resetLevel(LayerDrawable drawable, int index) {
        if (index != INVALID_VALUE) {
            drawable.getDrawable(index).setLevel(0);
        }
    }

    private static class AnimationInfo {

        public ConstantState baseDrawableState;

        public int hourLayerIndex;
        public int minuteLayerIndex;
        public int secondLayerIndex;
        public int defaultHour;
        public int defaultMinute;
        public int defaultSecond;

        boolean applyTime(Calendar time, LayerDrawable foregroundDrawable) {
            time.setTimeInMillis(System.currentTimeMillis());

            // We need to rotate by the difference from the default time if one is specified.
            int convertedHour = (time.get(Calendar.HOUR) + (12 - defaultHour)) % 12;
            int convertedMinute = (time.get(Calendar.MINUTE) + (60 - defaultMinute)) % 60;
            int convertedSecond = (time.get(Calendar.SECOND) + (60 - defaultSecond)) % 60;

            boolean invalidate = false;
            if (hourLayerIndex != INVALID_VALUE) {
                final Drawable hour = foregroundDrawable.getDrawable(hourLayerIndex);
                if (hour.setLevel(convertedHour * 60 + time.get(Calendar.MINUTE))) {
                    invalidate = true;
                }
            }

            if (minuteLayerIndex != INVALID_VALUE) {
                final Drawable minute = foregroundDrawable.getDrawable(minuteLayerIndex);
                if (minute.setLevel(time.get(Calendar.HOUR) * 60 + convertedMinute)) {
                    invalidate = true;
                }
            }

            if (secondLayerIndex != INVALID_VALUE) {
                final Drawable second = foregroundDrawable.getDrawable(secondLayerIndex);
                if (second.setLevel(convertedSecond * LEVELS_PER_SECOND)) {
                    invalidate = true;
                }
            }

            return invalidate;
        }
    }

    static class ClockBitmapInfo extends BitmapInfo {

        public final float scale;
        public final int offset;
        public final AnimationInfo animInfo;
        public final Bitmap mFlattenedBackground;

        public final ThemeData themeData;
        public final ColorFilter bgFilter;

        ClockBitmapInfo(Bitmap icon, int color, float scale, AnimationInfo animInfo,
                        Bitmap background, ThemeData themeData) {
            this(icon, color, scale, animInfo, background, themeData, null);
        }

        ClockBitmapInfo(Bitmap icon, int color, float scale, AnimationInfo animInfo,
                        Bitmap background, ThemeData themeData, ColorFilter bgFilter) {
            super(icon, color);
            this.scale = scale;
            this.animInfo = animInfo;
            this.offset = (int) Math.ceil(ShadowGenerator.BLUR_FACTOR * icon.getWidth());
            this.mFlattenedBackground = background;
            this.themeData = themeData;
            this.bgFilter = bgFilter;
        }

        @Override
        public FastBitmapDrawable newThemedIcon(Context context) {
            if (themeData != null) {
                ClockDrawableWrapper wrapper = fromThemeData(context, themeData);
                if (wrapper != null) {
                    int[] colors = getColors(context);
                    ColorFilter bgFilter = new PorterDuffColorFilter(colors[0], Mode.SRC_ATOP);
                    return new ClockBitmapInfo(icon, colors[1], scale,
                            wrapper.mAnimationInfo, mFlattenedBackground, themeData, bgFilter)
                            .newIcon(context);
                }
            }
            return super.newThemedIcon(context);
        }

        @Override
        public FastBitmapDrawable newIcon(Context context) {
            ClockIconDrawable d = new ClockIconDrawable(this);
            d.mDisabledAlpha = GraphicsUtils.getFloat(context, R.attr.disabledIconAlpha, 1f);
            return d;
        }

        @Nullable
        @Override
        public byte[] toByteArray() {
            return null;
        }

        void drawBackground(Canvas canvas, Rect bounds, Paint paint) {
            // draw the background that is already flattened to a bitmap
            ColorFilter oldFilter = paint.getColorFilter();
            if (bgFilter != null) {
                paint.setColorFilter(bgFilter);
            }
            canvas.drawBitmap(mFlattenedBackground, null, bounds, paint);
            paint.setColorFilter(oldFilter);
        }
    }

    private static class ClockIconDrawable extends FastBitmapDrawable implements Runnable {

        private final Calendar mTime = Calendar.getInstance();

        private final ClockBitmapInfo mInfo;

        private final AdaptiveIconDrawable mFullDrawable;
        private final LayerDrawable mForeground;

        ClockIconDrawable(ClockBitmapInfo clockInfo) {
            super(clockInfo);

            mInfo = clockInfo;
            mFullDrawable = (AdaptiveIconDrawable) mInfo.animInfo.baseDrawableState
                    .newDrawable().mutate();
            mForeground = (LayerDrawable) mFullDrawable.getForeground();
        }

        @Override
        protected void onBoundsChange(Rect bounds) {
            super.onBoundsChange(bounds);
            mFullDrawable.setBounds(bounds);
        }

        @Override
        public void drawInternal(Canvas canvas, Rect bounds) {
            if (mInfo == null) {
                super.drawInternal(canvas, bounds);
                return;
            }
            mInfo.drawBackground(canvas, bounds, mPaint);

            // prepare and draw the foreground
            mInfo.animInfo.applyTime(mTime, mForeground);

            canvas.scale(mInfo.scale, mInfo.scale,
                    bounds.exactCenterX() + mInfo.offset, bounds.exactCenterY() + mInfo.offset);
            canvas.clipPath(mFullDrawable.getIconMask());
            mForeground.draw(canvas);

            reschedule();
        }

        @Override
        public boolean isThemed() {
            return mInfo.bgFilter != null;
        }

        @Override
        protected void updateFilter() {
            super.updateFilter();
            mFullDrawable.setColorFilter(mPaint.getColorFilter());
        }

        @Override
        public void run() {
            if (mInfo.animInfo.applyTime(mTime, mForeground)) {
                invalidateSelf();
            } else {
                reschedule();
            }
        }

        @Override
        public boolean setVisible(boolean visible, boolean restart) {
            boolean result = super.setVisible(visible, restart);
            if (visible) {
                reschedule();
            } else {
                unscheduleSelf(this);
            }
            return result;
        }

        private void reschedule() {
            if (!isVisible()) {
                return;
            }

            unscheduleSelf(this);
            final long upTime = SystemClock.uptimeMillis();
            final long step = TICK_MS; /* tick every 200 ms */
            scheduleSelf(this, upTime - ((upTime % step)) + step);
        }

        @Override
        public ConstantState getConstantState() {
            return new ClockConstantState(mInfo, isDisabled());
        }

        private static class ClockConstantState extends FastBitmapConstantState {

            private final ClockBitmapInfo mInfo;

            ClockConstantState(ClockBitmapInfo info, boolean isDisabled) {
                super(info.icon, info.color, isDisabled);
                mInfo = info;
            }

            @Override
            public FastBitmapDrawable newDrawable() {
                ClockIconDrawable drawable = new ClockIconDrawable(mInfo);
                drawable.setIsDisabled(mIsDisabled);
                return drawable;
            }
        }
    }
}
