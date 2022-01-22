/*
 * Copyright (C) 2021 The Android Open Source Project
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

import static android.content.res.Configuration.UI_MODE_NIGHT_MASK;
import static android.content.res.Configuration.UI_MODE_NIGHT_YES;
import static android.content.res.Resources.ID_NULL;
import static com.android.launcher3.icons.GraphicsUtils.getExpectedBitmapSize;
import static com.android.launcher3.icons.IconProvider.ICON_TYPE_CALENDAR;
import static com.android.launcher3.icons.IconProvider.ICON_TYPE_CLOCK;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Process;
import android.os.UserHandle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.arch.core.util.Function;

import com.android.launcher3.icons.BitmapInfo.Extender;
import com.android.launcher3.icons.cache.BaseIconCache;
import com.saggitt.omega.icons.CustomAdaptiveIconDrawable;
import com.saggitt.omega.icons.ExtendedBitmapDrawable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Class to handle monochrome themed app icons
 */
@SuppressWarnings("NewApi")
public class ThemedIconDrawable extends FastBitmapDrawable {

    public static final String TAG = "ThemedIconDrawable";

    final ThemedBitmapInfo bitmapInfo;
    final int colorFg, colorBg;

    // The foreground/monochrome icon for the app
    private final Drawable mMonochromeIcon;
    private final AdaptiveIconDrawable mBgWrapper;
    private final Rect mBadgeBounds;

    protected ThemedIconDrawable(ThemedConstantState constantState) {
        super(constantState.mBitmap, constantState.colorFg, constantState.mIsDisabled);
        bitmapInfo = constantState.bitmapInfo;
        colorBg = constantState.colorBg;
        colorFg = constantState.colorFg;

        mMonochromeIcon = bitmapInfo.mThemeData.loadMonochromeDrawable(colorFg);
        mBgWrapper = new CustomAdaptiveIconDrawable(new ColorDrawable(colorBg), null);
        mBadgeBounds = bitmapInfo.mUserBadge == null ? null :
                new Rect(0, 0, bitmapInfo.mUserBadge.getWidth(), bitmapInfo.mUserBadge.getHeight());

    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        mBgWrapper.setBounds(bounds);
        mMonochromeIcon.setBounds(bounds);
    }

    @Override
    protected void drawInternal(Canvas canvas, Rect bounds) {
        int count = canvas.save();
        canvas.scale(bitmapInfo.mNormalizationScale, bitmapInfo.mNormalizationScale,
                bounds.exactCenterX(), bounds.exactCenterY());
        mPaint.setColor(colorBg);
        canvas.drawPath(mBgWrapper.getIconMask(), mPaint);
        mMonochromeIcon.draw(canvas);
        canvas.restoreToCount(count);
        if (mBadgeBounds != null) {
            canvas.drawBitmap(bitmapInfo.mUserBadge, mBadgeBounds, getBounds(), mPaint);
        }
    }

    @Override
    public boolean isThemed() {
        return true;
    }

    @Override
    public ConstantState getConstantState() {
        return new ThemedConstantState(bitmapInfo, colorBg, colorFg, mIsDisabled);
    }

    static class ThemedConstantState extends FastBitmapConstantState {

        final ThemedBitmapInfo bitmapInfo;
        final int colorFg, colorBg;

        public ThemedConstantState(ThemedBitmapInfo bitmapInfo,
                                   int colorBg, int colorFg, boolean isDisabled) {
            super(bitmapInfo.icon, bitmapInfo.color, isDisabled);
            this.bitmapInfo = bitmapInfo;
            this.colorBg = colorBg;
            this.colorFg = colorFg;
        }

        @Override
        public FastBitmapDrawable newDrawable() {
            return new ThemedIconDrawable(this);
        }
    }

    public static class ThemedBitmapInfo extends BitmapInfo {

        final ThemeData mThemeData;
        final float mNormalizationScale;
        final Bitmap mUserBadge;

        public ThemedBitmapInfo(Bitmap icon, int color, ThemeData themeData,
                                float normalizationScale, Bitmap userBadge) {
            super(icon, color);
            mThemeData = themeData;
            mNormalizationScale = normalizationScale;
            mUserBadge = userBadge;
        }

        @Override
        public FastBitmapDrawable newThemedIcon(Context context) {
            int[] colors = getColors(context);
            FastBitmapDrawable drawable = new ThemedConstantState(this, colors[0], colors[1], false)
                    .newDrawable();
            drawable.mDisabledAlpha = GraphicsUtils.getFloat(context, R.attr.disabledIconAlpha, 1f);
            return drawable;
        }

        @Nullable
        public byte[] toByteArray() {
            if (isNullOrLowRes()) {
                return null;
            }
            String resName = mThemeData.mResources.getResourceName(mThemeData.mResID);
            ByteArrayOutputStream out = new ByteArrayOutputStream(
                    getExpectedBitmapSize(icon) + 3 + resName.length());
            try {
                DataOutputStream dos = new DataOutputStream(out);
                dos.writeByte(TYPE_THEMED);
                dos.writeFloat(mNormalizationScale);
                dos.writeUTF(resName);
                icon.compress(Bitmap.CompressFormat.PNG, 100, dos);

                dos.flush();
                dos.close();
                return out.toByteArray();
            } catch (IOException e) {
                Log.w(TAG, "Could not write bitmap");
                return null;
            }
        }

        static ThemedBitmapInfo decode(byte[] data, int color,
                                       BitmapFactory.Options decodeOptions, UserHandle user, BaseIconCache iconCache,
                                       Context context) {
            try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data))) {
                dis.readByte(); // type
                float normalizationScale = dis.readFloat();

                String resName = dis.readUTF();
                int resId = context.getResources()
                        .getIdentifier(resName, "drawable", context.getPackageName());
                if (resId == ID_NULL) {
                    return null;
                }

                Bitmap userBadgeBitmap = null;
                if (!Process.myUserHandle().equals(user)) {
                    try (BaseIconFactory iconFactory = iconCache.getIconFactory()) {
                        userBadgeBitmap = iconFactory.getUserBadgeBitmap(user);
                    }
                }

                ThemeData themeData = new ThemeData(context.getResources(), resId);
                Bitmap icon = BitmapFactory.decodeStream(dis, null, decodeOptions);
                return new ThemedBitmapInfo(icon, color, themeData, normalizationScale,
                        userBadgeBitmap);
            } catch (IOException e) {
                return null;
            }
        }
    }

    public static class ThemeData {

        final Resources mResources;
        final int mResID;

        public ThemeData(Resources resources, int resID) {
            mResources = resources;
            mResID = resID;
        }

        Drawable loadMonochromeDrawable(int accentColor) {
            Drawable d = mResources.getDrawable(mResID).mutate();
            d.setTint(accentColor);
            d = new InsetDrawable(d, .2f);
            return d;
        }

        public Drawable wrapDrawable(Drawable original, int iconType) {
            if (!(original instanceof AdaptiveIconDrawable) && !(original instanceof BitmapDrawable)) {
                return original;
            }
            String resourceType = mResources.getResourceTypeName(mResID);
            if (iconType == ICON_TYPE_CALENDAR && "array".equals(resourceType)) {
                TypedArray ta = mResources.obtainTypedArray(mResID);
                int id = ta.getResourceId(IconProvider.getDay(), ID_NULL);
                ta.recycle();
                return id == ID_NULL ? original
                        : wrapWithThemeData(original, new ThemeData(mResources, id));
            } else if (iconType == ICON_TYPE_CLOCK && "array".equals(resourceType)) {
                if (original instanceof ClockDrawableWrapper) {
                    ((ClockDrawableWrapper) original).mThemeData = this;
                }
                return original;
            } else if ("drawable".equals(resourceType)) {
                return wrapWithThemeData(original, this);
            } else {
                return original;
            }
        }

        private Drawable wrapWithThemeData(Drawable original, ThemeData themeData) {
            if (original instanceof AdaptiveIconDrawable) {
                return new ThemedAdaptiveIcon((AdaptiveIconDrawable) original, themeData);
            } else if (original instanceof BitmapDrawable) {
                return new ThemedBitmapIcon(mResources, (BitmapDrawable) original, themeData);
            }
            throw new IllegalArgumentException("original must be AdaptiveIconDrawable or BitmapDrawable");
        }
    }

    static class ThemedAdaptiveIcon extends CustomAdaptiveIconDrawable implements Extender {

        protected final ThemeData mThemeData;

        public ThemedAdaptiveIcon(AdaptiveIconDrawable parent, ThemeData themeData) {
            super(parent.getBackground(), parent.getForeground());
            mThemeData = themeData;
        }

        @Override
        public BitmapInfo getExtendedInfo(Bitmap bitmap, int color, BaseIconFactory iconFactory,
                                          float normalizationScale, UserHandle user) {
            Bitmap userBadge = Process.myUserHandle().equals(user)
                    ? null : iconFactory.getUserBadgeBitmap(user);
            return new ThemedBitmapInfo(bitmap, color, mThemeData, normalizationScale, userBadge);
        }

        @Override
        public void drawForPersistence(Canvas canvas) {
            draw(canvas);
        }

        @Override
        public Drawable getThemedDrawable(Context context) {
            int[] colors = getColors(context);
            Drawable bg = new ColorDrawable(colors[0]);
            float inset = getExtraInsetFraction() / (1 + 2 * getExtraInsetFraction());
            Drawable fg = new InsetDrawable(mThemeData.loadMonochromeDrawable(colors[1]), inset);
            return new CustomAdaptiveIconDrawable(bg, fg);
        }
    }

    static class ThemedBitmapIcon extends ExtendedBitmapDrawable implements Extender {

        protected final ThemeData mThemeData;

        public ThemedBitmapIcon(Resources res, BitmapDrawable parent, ThemeData themeData) {
            super(res, parent.getBitmap(), ExtendedBitmapDrawable.isFromIconPack(parent));
            mThemeData = themeData;
        }

        @Override
        public BitmapInfo getExtendedInfo(Bitmap bitmap, int color, BaseIconFactory iconFactory,
                                          float normalizationScale, UserHandle user) {
            Bitmap userBadge = Process.myUserHandle().equals(user)
                    ? null : iconFactory.getUserBadgeBitmap(user);
            return new ThemedBitmapInfo(bitmap, color, mThemeData, normalizationScale, userBadge);
        }

        @Override
        public void drawForPersistence(Canvas canvas) {
            draw(canvas);
        }

        @Override
        public Drawable getThemedDrawable(Context context) {
            int[] colors = getColors(context);
            Drawable bg = new ColorDrawable(colors[0]);
            float extraInsetFraction = CustomAdaptiveIconDrawable.getExtraInsetFraction();
            float inset = extraInsetFraction / (1 + 2 * extraInsetFraction);
            Drawable fg = new InsetDrawable(mThemeData.loadMonochromeDrawable(colors[1]), inset);
            return new CustomAdaptiveIconDrawable(bg, fg);
        }
    }

    /**
     * Get an int array representing background and foreground colors for themed icons
     */
    public static int[] getColors(Context context) {
        if (COLORS_LOADER != null) {
            return COLORS_LOADER.apply(context);
        }
        Resources res = context.getResources();
        int[] colors = new int[2];
        if ((res.getConfiguration().uiMode & UI_MODE_NIGHT_MASK) == UI_MODE_NIGHT_YES) {
            colors[0] = res.getColor(android.R.color.system_neutral1_800);
            colors[1] = res.getColor(android.R.color.system_accent1_100);
        } else {
            colors[0] = res.getColor(android.R.color.system_accent1_100);
            colors[1] = res.getColor(android.R.color.system_neutral2_700);
        }
        return colors;
    }

    public static Function<Context, int[]> COLORS_LOADER;
}
