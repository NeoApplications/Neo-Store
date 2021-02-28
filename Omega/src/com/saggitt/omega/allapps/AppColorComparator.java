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

package com.saggitt.omega.allapps;

import android.content.Context;

import androidx.core.graphics.ColorUtils;

import com.android.launcher3.allapps.AppInfoComparator;
import com.android.launcher3.icons.ColorExtractor;
import com.android.launcher3.model.data.AppInfo;

public class AppColorComparator extends AppInfoComparator {
    static int REPETITIONS = 6;

    public AppColorComparator(Context context) {
        super(context);
    }

    @Override
    public int compare(AppInfo a, AppInfo b) {
        float[] hslA = new float[3];
        float[] hslB = new float[3];

        ColorExtractor colorExtractor = new ColorExtractor();

        ColorUtils.colorToHSL(colorExtractor.findDominantColorByHue(a.bitmap.icon), hslA);
        ColorUtils.colorToHSL(colorExtractor.findDominantColorByHue(b.bitmap.icon), hslB);

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

    public static int remapHue(Float hue) {
        return (int) (hue / 360 * REPETITIONS);
    }

    public static int remap(Float value) {
        return (int) (value * REPETITIONS);
    }
}
