/*
 *  Copyright (c) 2020 Omega Launcher
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
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
