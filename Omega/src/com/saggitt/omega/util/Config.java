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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.TypedValue;

import com.android.launcher3.BuildConfig;
import com.android.launcher3.LauncherModel;
import com.android.launcher3.R;
import com.android.launcher3.shortcuts.DeepShortcutManager;

import java.util.List;
import java.util.Locale;

public class Config {
    //APP DRAWER SORT MODE
    public static final int SORT_AZ = 0;
    public static final int SORT_ZA = 1;
    public static final int SORT_LAST_INSTALLED = 2;
    public static final int SORT_MOST_USED = 3;
    public static final int SORT_BY_COLOR = 4;

    //PERMISION FLAGS
    public static final int REQUEST_PERMISSION_STORAGE_ACCESS = 666;
    public static final int REQUEST_PERMISSION_LOCATION_ACCESS = 667;
    public static final int CODE_EDIT_ICON = 100;

    public final static String GOOGLE_QSB = "com.google.android.googlequicksearchbox";

    public Context mContext;

    public Config(Context context) {
        mContext = context;
    }

    public boolean defaultEnableBlur() {
        return mContext.getResources().getBoolean(R.bool.config_default_enable_blur);
    }

    public String getDefaultSearchProvider() {
        return mContext.getResources().getString(R.string.config_default_search_provider);
    }

    public String[] getDefaultIconPacks() {
        return mContext.getResources().getStringArray(R.array.config_default_icon_packs);
    }

    public float getDefaultBlurStrength() {
        TypedValue typedValue = new TypedValue();
        mContext.getResources().getValue(R.dimen.config_default_blur_strength, typedValue, true);
        return typedValue.getFloat();
    }

    public void setAppLanguage(String androidLC) {
        Locale locale = getLocaleByAndroidCode(androidLC);
        Configuration config = mContext.getResources().getConfiguration();

        Locale mLocale = (locale != null && !androidLC.isEmpty())
                ? locale : Resources.getSystem().getConfiguration().getLocales().get(0);
        config.setLocale(mLocale);

        mContext.createConfigurationContext(config);
    }

    public Locale getLocaleByAndroidCode(String androidLC) {
        if (!TextUtils.isEmpty(androidLC)) {
            return androidLC.contains("-r")
                    ? new Locale(androidLC.substring(0, 2), androidLC.substring(4, 6)) // de-rAt
                    : new Locale(androidLC); // de
        }
        return Resources.getSystem().getConfiguration().getLocales().get(0);
    }

    public final static String[] ICON_INTENTS = new String[]{
            "com.novalauncher.THEME",
            "org.adw.launcher.THEMES",
            "org.adw.launcher.icons.ACTION_PICK_ICON",
            "com.anddoes.launcher.THEME",
            "com.teslacoilsw.launcher.THEME",
            "com.fede.launcher.THEME_ICONPACK",
            "com.gau.go.launcherex.theme",
            "com.dlto.atom.launcher.THEME",
            "net.oneplus.launcher.icons.ACTION_PICK_ICON"
    };

    public static void reloadIcon(DeepShortcutManager shortcutManager, LauncherModel model, UserHandle user, String pkg) {
        model.onAppIconChanged(pkg, user);
        if (shortcutManager.wasLastCallSuccess()) {
            List<ShortcutInfo> shortcuts = shortcutManager.queryForPinnedShortcuts(pkg, user);
            if (!shortcuts.isEmpty()) {
                model.updatePinnedShortcuts(pkg, shortcuts, user);
            }
        }
    }

    public String getAppVersionName() {
        try {
            PackageManager manager = mContext.getPackageManager();
            PackageInfo info = manager.getPackageInfo(BuildConfig.APPLICATION_ID, 0);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "?";
        }
    }

    public int getAppVersionCode() {
        try {
            PackageManager manager = mContext.getPackageManager();
            PackageInfo info = manager.getPackageInfo(BuildConfig.APPLICATION_ID, 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public String getBuildConfigValue(String fieldName, String defaultValue) {
        Object field = getBuildConfigValue(fieldName);
        if (field instanceof String) {
            return (String) field;
        }
        return defaultValue;
    }

    public Object getBuildConfigValue(String fieldName) {
        String pkg = "com.android.launcher3.BuildConfig";
        try {
            Class<?> c = Class.forName(pkg);
            return c.getField(fieldName).get(null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getInstallSource() {
        String src = null;
        try {
            src = mContext.getPackageManager().getInstallerPackageName(mContext.getPackageName());
        } catch (Exception ignored) {
        }
        if (TextUtils.isEmpty(src)) {
            src = "Sideloaded";
        }
        switch (src) {
            case "com.android.vending":
            case "com.google.android.feedback": {
                return "Google Play Store";
            }
            case "org.fdroid.fdroid.privileged":
            case "org.fdroid.fdroid": {
                return "F-Droid";
            }
            case "com.github.yeriomin.yalpstore": {
                return "Yalp Store";
            }
            case "cm.aptoide.pt": {
                return "Aptoide";
            }
        }
        if (src.toLowerCase().contains(".amazon.")) {
            return "Amazon Appstore";
        }
        return src;
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