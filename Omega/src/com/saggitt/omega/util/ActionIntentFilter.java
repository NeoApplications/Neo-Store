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

