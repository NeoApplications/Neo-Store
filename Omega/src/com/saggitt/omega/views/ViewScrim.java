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
import android.util.Property;
import android.view.View;
import android.view.ViewParent;

import com.android.launcher3.R;

/**
 * A utility class that can be used to draw a scrim behind a view
 */
public abstract class ViewScrim<T extends View> {

    public static Property<ViewScrim, Float> PROGRESS =
            new Property<ViewScrim, Float>(Float.TYPE, "progress") {
                @Override
                public Float get(ViewScrim viewScrim) {
                    return viewScrim.mProgress;
                }

                @Override
                public void set(ViewScrim object, Float value) {
                    object.setProgress(value);
                }
            };

    protected final T mView;
    protected float mProgress = 0;

    public ViewScrim(T view) {
        mView = view;
    }

    public static ViewScrim get(View view) {
        return (ViewScrim) view.getTag(R.id.view_scrim);
    }

    public void attach() {
        mView.setTag(R.id.view_scrim, this);
    }

    public void setProgress(float progress) {
        if (mProgress != progress) {
            mProgress = progress;
            onProgressChanged();
            invalidate();
        }
    }

    public abstract void draw(Canvas canvas, int width, int height);

    protected void onProgressChanged() {
    }

    public void invalidate() {
        ViewParent parent = mView.getParent();
        if (parent != null) {
            ((View) parent).invalidate();
        }
    }
}
