package com.android.launcher3.icons;

import android.content.Context;
import android.content.SharedPreferences;

public class Utilities {
    public static final String SHARED_PREFERENCES_KEY = "com.android.launcher3.prefs";

    public static SharedPreferences getPrefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
    }
}