/*
 *
 *  *
 *  *  * Copyright (c) 2020 Omega Launcher
 *  *  *
 *  *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  *  * you may not use this file except in compliance with the License.
 *  *  * You may obtain a copy of the License at
 *  *  *
 *  *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *  *
 *  *  * Unless required by applicable law or agreed to in writing, software
 *  *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  * See the License for the specific language governing permissions and
 *  *  * limitations under the License.
 *  *
 *
 */

package com.saggitt.omega.util;

import android.content.Context;
import android.util.TypedValue;

import com.android.launcher3.R;

import org.jetbrains.annotations.NotNull;

public class Config {
    private static final String TAG = "Config";

    //APP DRAWER SORT MODE
    public static final int SORT_AZ = 0;
    public static final int SORT_ZA = 1;
    public static final int SORT_LAST_INSTALLED = 2;
    public static final int SORT_MOST_USED = 3;
    public static final int SORT_BY_COLOR = 4;

    private static final Object sInstanceLock = new Object();
    private static Config sInstance;
    public Context mContext;

    public Config(Context context) {
        mContext = context;
    }

    @NotNull
    public static Config getInstance(@NotNull Context context) {
        synchronized (sInstanceLock) {
            if (sInstance == null) {
                sInstance = new Config(context.getApplicationContext());
            }
            return sInstance;
        }
    }

    public boolean defaultEnableBlur() {
        return mContext.getResources().getBoolean(R.bool.config_default_enable_blur);
    }

    public float getDefaultBlurStrength() {
        TypedValue typedValue = new TypedValue();
        mContext.getResources().getValue(R.dimen.config_default_blur_strength, typedValue, true);
        return typedValue.getFloat();
    }
}
