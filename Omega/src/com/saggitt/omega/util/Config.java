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
import android.content.pm.ShortcutInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.TypedValue;

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
        config.locale = (locale != null && !androidLC.isEmpty())
                ? locale : Resources.getSystem().getConfiguration().locale;
        mContext.getResources().updateConfiguration(config, null);
    }

    public Locale getLocaleByAndroidCode(String androidLC) {
        if (!TextUtils.isEmpty(androidLC)) {
            return androidLC.contains("-r")
                    ? new Locale(androidLC.substring(0, 2), androidLC.substring(4, 6)) // de-rAt
                    : new Locale(androidLC); // de
        }
        return Resources.getSystem().getConfiguration().locale;
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
}