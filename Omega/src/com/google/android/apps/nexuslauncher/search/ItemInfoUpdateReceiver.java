package com.google.android.apps.nexuslauncher.search;

import android.content.SharedPreferences;

import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherCallbacks;
import com.android.launcher3.icons.IconCache;
import com.android.launcher3.model.data.ItemInfoWithIcon;

public class ItemInfoUpdateReceiver implements IconCache.ItemInfoUpdateReceiver, SharedPreferences.OnSharedPreferenceChangeListener {
    private final Launcher mLauncher;

    public ItemInfoUpdateReceiver(final Launcher launcher, final LauncherCallbacks callbacks) {
        this.mLauncher = launcher;
        int eD = launcher.getDeviceProfile().inv.numColumns;
    }

    public void onCreate() {
        mLauncher.getSharedPrefs().registerOnSharedPreferenceChangeListener(this);
    }

    public void onDestroy() {
        mLauncher.getSharedPrefs().unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String s) {
        if ("reflection_last_predictions".equals(s) || "pref_show_predictions".equals(s)) {
            //this.di();
        }
    }

    public void reapplyItemInfo(final ItemInfoWithIcon itemInfoWithIcon) {
    }
}
