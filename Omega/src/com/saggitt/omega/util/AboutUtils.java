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

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.StringRes;

import java.util.Locale;

public class AboutUtils {
    public Context mContext;

    public AboutUtils(Context context) {
        mContext = context;
    }

    public void openWebBrowser(final String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        try {
            mContext.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public boolean setClipboard(CharSequence text) {
        android.content.ClipboardManager cm = ((android.content.ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE));
        if (cm != null) {
            ClipData clip = ClipData.newPlainText(mContext.getPackageName(), text);
            cm.setPrimaryClip(clip);
            return true;
        }
        return false;
    }

    public String bcstr(String fieldName, String defaultValue) {
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

    public String getAppVersionName() {
        try {
            PackageManager manager = mContext.getPackageManager();
            PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "?";
        }
    }

    public int getAppVersionCode() {
        try {
            PackageManager manager = mContext.getPackageManager();
            PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public String getPackageName() {
        String pkg = rstr("manifest_package_id");
        return pkg != null ? pkg : mContext.getPackageName();
    }

    public String getAppInstallationSource() {
        String src = null;
        try {
            src = mContext.getPackageManager().getInstallerPackageName(getPackageName());
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

    /**
     * Get String by given string ressource id (nuermic)
     */
    public String rstr(@StringRes int strResId) {
        return mContext.getString(strResId);
    }

    /**
     * Get String by given string ressource identifier (textual)
     */
    public String rstr(String strResKey) {
        try {
            return rstr(getResId("String", strResKey));
        } catch (Resources.NotFoundException e) {
            return null;
        }
    }

    public int getResId(String resType, final String name) {
        return mContext.getResources().getIdentifier(name, resType, mContext.getPackageName());
    }

    /**
     * Get an {@link Locale} out of a android language code
     * The {@code androidLC} may be in any of the forms: de, en, de-rAt
     */
    public Locale getLocaleByAndroidCode(String androidLC) {
        if (!TextUtils.isEmpty(androidLC)) {
            return androidLC.contains("-r")
                    ? new Locale(androidLC.substring(0, 2), androidLC.substring(4, 6)) // de-rAt
                    : new Locale(androidLC); // de
        }
        return Resources.getSystem().getConfiguration().locale;
    }

    /**
     * Set the apps language
     * {@code androidLC} may be in any of the forms: en, de, de-rAt
     * If given an empty string, the default (system) locale gets loaded
     */
    public void setAppLanguage(String androidLC) {
        Locale locale = getLocaleByAndroidCode(androidLC);
        Configuration config = mContext.getResources().getConfiguration();
        config.locale = (locale != null && !androidLC.isEmpty())
                ? locale : Resources.getSystem().getConfiguration().locale;
        mContext.getResources().updateConfiguration(config, null);
    }
}
