/*
 *  This file is part of Omega Launcher
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

package com.android.launcher3;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.graphics.Shader;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import org.xmlpull.v1.XmlPullParser;

import java.lang.reflect.Method;

@SuppressLint({"SoonBlockedPrivateApi", "DiscouragedPrivateApi"})
@RequiresApi(api = Build.VERSION_CODES.O)
public class AdaptiveIconDrawableExt extends AdaptiveIconDrawable {

    public static final String TAG = "AdaptiveIconDrawableExt";
    private static Path sMask;
    /**
     * Scaled mask based on the view bounds.
     */
    private final Path mMask;
    private final Matrix mMaskMatrix;
    private final Region mTransparentRegion;
    private final int mMaskId;

    private final Canvas mCanvas;

    private static Method methodGetAdaptiveIconMaskPath;
    private static Method methodExtractThemeAttrs;
    private static Method methodCreateFromXmlInnerForDensity;

    private Bitmap mLayersBitmap;
    private Bitmap mMaskBitmap;
    AdaptiveIconDrawable mLayerState;
    private Shader mLayersShader;

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG |
            Paint.FILTER_BITMAP_FLAG);

    static {
        try {
            Class<?> classIconShapeManager = Class.forName("com.saggitt.omega.adaptive.IconShapeManager");
            methodGetAdaptiveIconMaskPath = classIconShapeManager.getMethod("getAdaptiveIconMaskPath");
            methodExtractThemeAttrs = TypedArray.class.getDeclaredMethod("extractThemeAttrs");
            methodCreateFromXmlInnerForDensity = Drawable.class.getDeclaredMethod(
                    "createFromXmlInnerForDensity", Resources.class,
                    XmlPullParser.class, AttributeSet.class, int.class, Resources.Theme.class);
            methodCreateFromXmlInnerForDensity.setAccessible(true);
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Constructor used to dynamically create this drawable.
     *
     * @param backgroundDrawable drawable that should be rendered in the background
     * @param foregroundDrawable drawable that should be rendered in the foreground
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public AdaptiveIconDrawableExt(Drawable backgroundDrawable, Drawable foregroundDrawable) {
        super(backgroundDrawable, foregroundDrawable);
        if (sMask == null) {
            sMask = createMaskPath();
        }
        mMask = createMaskPath();
        mMaskMatrix = new Matrix();
        mCanvas = new Canvas();
        mTransparentRegion = new Region();
        mMaskId = sMask.hashCode();
    }

    /**
     * When called before the bound is set, the returned path is identical to
     * R.string.config_icon_mask. After the bound is set, the
     * returned path's computed bound is same as the #getBounds().
     *
     * @return the mask path object used to clip the drawable
     */
    public Path getIconMask() {
        return mMask;
    }

    public boolean isMaskValid() {
        return sMask != null && mMaskId == sMask.hashCode();
    }

    public static void onShapeChanged() {
        sMask = null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("RestrictedApi")
    private Path createMaskPath() {
        try {
            return (Path) methodGetAdaptiveIconMaskPath.invoke(null);
        } catch (Exception e) {
            Log.d(TAG, "Can't load icon mask", e);
        }
        return new AdaptiveIconDrawable(null, null).getIconMask();
    }

    @NonNull
    public static Drawable wrap(@NonNull Drawable icon) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && icon instanceof AdaptiveIconDrawable) {
            AdaptiveIconDrawable adaptive = (AdaptiveIconDrawable) icon;
            return new AdaptiveIconDrawableExt(adaptive.getBackground(), adaptive.getForeground());
        } else {
            return icon;
        }
    }

    public static Drawable wrapNullable(@Nullable Drawable icon) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && icon instanceof AdaptiveIconDrawable) {
            AdaptiveIconDrawable adaptive = (AdaptiveIconDrawable) icon;
            return new AdaptiveIconDrawableExt(adaptive.getBackground(), adaptive.getForeground());
        } else {
            return icon;
        }
    }
}
