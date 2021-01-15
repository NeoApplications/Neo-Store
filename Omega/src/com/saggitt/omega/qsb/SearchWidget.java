/*
 *  This file is part of Omega Launcher.
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

package com.saggitt.omega.qsb;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.launcher3.R;
import com.saggitt.omega.allapps.BlurQsbLayout;
import com.saggitt.omega.util.OmegaUtilsKt;

public class SearchWidget extends FrameLayout {
    public SearchWidget(@NonNull Context context) {
        this(context, null, 0);
    }

    public SearchWidget(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SearchWidget(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void addView(View child) {
        super.addView(child);
        if (child instanceof BlurQsbLayout) {
            ((BlurQsbLayout) child).setWidgetMode(true);

            ((BlurQsbLayout) child).setScrimView(OmegaUtilsKt.getLauncherOrNull(getContext()).findViewById(R.id.scrim_view));
        }
    }
}
