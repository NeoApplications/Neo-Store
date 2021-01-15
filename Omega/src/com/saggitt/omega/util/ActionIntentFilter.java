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

package com.saggitt.omega.util;

import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;

public class ActionIntentFilter {
    public static IntentFilter googleInstance(String... array) {
        return newInstance(Config.GOOGLE_QSB, array);
    }

    public static IntentFilter newInstance(String s, String... array) {
        IntentFilter intentFilter = new IntentFilter();
        for (int length = array.length, i = 0; i < length; ++i) {
            intentFilter.addAction(array[i]);
        }
        intentFilter.addDataScheme("package");
        intentFilter.addDataSchemeSpecificPart(s, 0);
        return intentFilter;
    }

    public static boolean googleEnabled(final Context context) {
        try {
            return context.getPackageManager().getApplicationInfo(Config.GOOGLE_QSB, 0).enabled;
        } catch (PackageManager.NameNotFoundException ex) {
            return false;
        }
    }
}

