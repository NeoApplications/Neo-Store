package com.google.android.apps.nexuslauncher.utils;

import android.content.IntentFilter;

public class ActionIntentFilter {
    public static IntentFilter googleInstance(String... array) {
        return newInstance("com.google.android.googlequicksearchbox", array);
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
}
