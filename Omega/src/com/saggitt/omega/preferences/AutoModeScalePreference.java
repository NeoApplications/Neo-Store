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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AutoModeScalePreference extends AutoModeSeekbarPreference {

    public AutoModeScalePreference(@NotNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoModeScalePreference(@NotNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void updateDisplayedValue() {
        super.updateDisplayedValue();
        if (current < low && current != -1f) {
            persistFloat(current);
        }
    }

    public boolean persistFloat(Float value) {
        return super.persistFloat((value < low) ? -1f : value);
    }
}
