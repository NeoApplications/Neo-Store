/*
 *     This file is part of Lawnchair Launcher.
 *
 *     Lawnchair Launcher is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Lawnchair Launcher is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Lawnchair Launcher.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saggitt.omega.views;

import android.graphics.Canvas;
import android.graphics.Color;
import android.view.View;
import android.view.animation.Interpolator;

import androidx.core.graphics.ColorUtils;

import com.android.launcher3.R;
import com.android.launcher3.anim.Interpolators;
import com.android.launcher3.uioverrides.WallpaperColorInfo;

public class ColorScrim extends ViewScrim<View> {

    private final int mColor;
    private final Interpolator mInterpolator;
    private int mCurrentColor;

    public ColorScrim(View view, int color, Interpolator interpolator) {
        super(view);
        mColor = color;
        mInterpolator = interpolator;
    }

    public static ColorScrim createExtractedColorScrim(View view) {
        WallpaperColorInfo colors = WallpaperColorInfo.getInstance(view.getContext());
        int alpha = view.getResources().getInteger(R.integer.extracted_color_gradient_alpha);
        ColorScrim scrim = new ColorScrim(view, ColorUtils.setAlphaComponent(
                colors.getSecondaryColor(), alpha), Interpolators.LINEAR);
        scrim.attach();
        return scrim;
    }

    @Override
    protected void onProgressChanged() {
        mCurrentColor = ColorUtils.setAlphaComponent(mColor,
                Math.round(mInterpolator.getInterpolation(mProgress) * Color.alpha(mColor)));
    }

    @Override
    public void draw(Canvas canvas, int width, int height) {
        if (mProgress > 0) {
            canvas.drawColor(mCurrentColor);
        }
    }
}
