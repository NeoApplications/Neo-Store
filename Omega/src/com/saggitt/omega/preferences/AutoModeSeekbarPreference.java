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

package com.saggitt.omega.preferences;

import android.content.Context;
import android.util.AttributeSet;

import com.android.launcher3.R;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AutoModeSeekbarPreference extends SeekbarPreference {
    protected Float low = min;
    private Context mContext;

    public AutoModeSeekbarPreference(@NotNull Context context) {
        this(context, null, 0);
    }

    public AutoModeSeekbarPreference(@NotNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoModeSeekbarPreference(@NotNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        min -= (max - min) / steps;
        steps += 1;
        defaultValue = min;
    }

    public void updateSummary() {
        if (current < low) {
            mValueText.setText(mContext.getString(R.string.automatic_short));
        } else {
            super.updateSummary();
        }
    }
}
