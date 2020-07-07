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

package com.saggitt.omega.allapps;

import android.content.Context;

import androidx.core.graphics.ColorUtils;

import com.android.launcher3.AppInfo;
import com.android.launcher3.allapps.AppInfoComparator;

public class AppColorComparator extends AppInfoComparator {
    int REPETITIONS = 6;

    public AppColorComparator(Context context) {
        super(context);
    }

    @Override
    public int compare(AppInfo a, AppInfo b) {
        float[] hslA = new float[3];
        float[] hslB = new float[3];

        ColorUtils.colorToHSL(a.iconColor, hslA);
        ColorUtils.colorToHSL(b.iconColor, hslB);

        Integer h2A = remapHue(hslA[0]);
        int h2B = remapHue(hslB[0]);
        Integer s2A = remap(hslA[1]);
        int s2B = remap(hslB[1]);
        Integer l2A = remap(hslA[2]);
        int l2B = remap(hslB[2]);

        if (h2A % 2 == 1) {
            s2A = REPETITIONS - s2A;
            l2A = REPETITIONS - l2A;
        }

        if (h2B % 2 == 1) {
            s2B = REPETITIONS - s2B;
            l2B = REPETITIONS - l2B;
        }

        int result = h2A.compareTo(h2B);
        if (result != 0) {
            return result;
        }

        result = l2A.compareTo(l2B);
        if (result != 0) {
            return result;
        }

        result = s2A.compareTo(s2B);
        if (result != 0) {
            return result;
        }

        return super.compare(a, b);
    }

    public int remapHue(Float hue){
        return (int) (hue / 360 * REPETITIONS);
    }
    public int remap(Float value){
        return (int) (value * REPETITIONS);
    }
}
