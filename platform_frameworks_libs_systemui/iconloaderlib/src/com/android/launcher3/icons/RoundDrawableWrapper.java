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

import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableWrapper;

/**
 * A drawable which clips rounded corner around a child drawable
 */
public class RoundDrawableWrapper extends DrawableWrapper {

    private final RectF mTempRect = new RectF();
    private final Path mClipPath = new Path();
    private final float mRoundedCornersRadius;

    public RoundDrawableWrapper(Drawable dr, float radius) {
        super(dr);
        mRoundedCornersRadius = radius;
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        mTempRect.set(getBounds());
        mClipPath.reset();
        mClipPath.addRoundRect(mTempRect, mRoundedCornersRadius,
                mRoundedCornersRadius, Path.Direction.CCW);
        super.onBoundsChange(bounds);
    }

    @Override
    public final void draw(Canvas canvas) {
        int saveCount = canvas.save();
        canvas.clipPath(mClipPath);
        super.draw(canvas);
        canvas.restoreToCount(saveCount);
    }
}
