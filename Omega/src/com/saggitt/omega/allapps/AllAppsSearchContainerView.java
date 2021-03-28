/*
 *     Copyright (C) 2019 Lawnchair Team.
 *
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

package com.saggitt.omega.allapps;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import com.android.launcher3.Utilities;
import com.android.launcher3.allapps.LauncherAllAppsContainerView;
import com.google.android.apps.nexuslauncher.qsb.AllAppsQsbLayout;

public class AllAppsSearchContainerView extends LauncherAllAppsContainerView {
    private boolean mClearQsb;

    public AllAppsSearchContainerView(Context context) {
        this(context, null);
    }

    public AllAppsSearchContainerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AllAppsSearchContainerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressLint("WrongConstant")
    @Override
    protected void dispatchDraw(Canvas canvas) {
        View searchView = getSearchView();
        if (mClearQsb && searchView instanceof AllAppsQsbLayout) {
            AllAppsQsbLayout qsb = (AllAppsQsbLayout) searchView;
            int left = (int) (qsb.getLeft() + qsb.getTranslationX());
            int top = (int) (qsb.getTop() + qsb.getTranslationY());
            int right = left + qsb.getWidth() + 1;
            int bottom = top + qsb.getHeight() + 1;
            if (Utilities.ATLEAST_P && Utilities.HIDDEN_APIS_ALLOWED) {
                canvas.saveUnclippedLayer(left, 0, right, bottom);
            } else {
                int flags = Utilities.ATLEAST_P ? Canvas.ALL_SAVE_FLAG : 0x04 /* HAS_ALPHA_LAYER_SAVE_FLAG */;
                canvas.saveLayer(left, 0, right, bottom, null, flags);
            }
        }

        super.dispatchDraw(canvas);
    }

    @Override
    public void setTranslationY(float translationY) {
        super.setTranslationY(translationY);
        ((BlurQsbLayout) getSearchView()).invalidateBlur();
    }
}
