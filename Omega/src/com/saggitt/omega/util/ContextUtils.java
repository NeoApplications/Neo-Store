/*
 * Copyright (c) 2020 Omega Launcher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package com.saggitt.omega.util;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;

import androidx.annotation.RawRes;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.preference.Preference;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class ContextUtils {
    public Context mContext;

    public ContextUtils(Context context) {
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

    public void showDialogWithHtmlTextView(@StringRes int resTitleId, String html) {
        showDialogWithHtmlTextView(resTitleId, html, true, null);
    }

    public void showDialogWithHtmlTextView(@StringRes int resTitleId, String text, boolean isHtml, DialogInterface.OnDismissListener dismissedListener) {
        AppCompatTextView textView = new AppCompatTextView(mContext);
        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16,
                mContext.getResources().getDisplayMetrics());
        textView.setMovementMethod(new LinkMovementMethod());
        textView.setPadding(padding, 0, padding, 0);

        textView.setText(isHtml ? new SpannableString(Html.fromHtml(text)) : text);

        AlertDialog.Builder dialog = new AlertDialog.Builder(mContext)
                .setPositiveButton(android.R.string.ok, null)
                .setOnDismissListener(dismissedListener)
                .setTitle(resTitleId)
                .setView(textView);
        dialog.show();
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

    public String readTextfileFromRawRes(@RawRes int rawResId, String linePrefix, String linePostfix) {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        String line;

        linePrefix = linePrefix == null ? "" : linePrefix;
        linePostfix = linePostfix == null ? "" : linePostfix;

        try {
            br = new BufferedReader(new InputStreamReader(mContext.getResources().openRawResource(rawResId)));
            while ((line = br.readLine()) != null) {
                sb.append(linePrefix);
                sb.append(line);
                sb.append(linePostfix);
                sb.append("\n");
            }
        } catch (Exception ignored) {
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ignored) {
                }
            }
        }
        return sb.toString();
    }

    public void getAppInfoSummary(Preference prefx) {
        Locale locale = Locale.getDefault();
        Preference pref;
        String tmp;
        if ((pref = prefx) != null && pref.getSummary() == null) {
            String summary = String.format(locale, "\n<b>Package:</b> %s\n<b>Version:</b> v%s (build %s)", getPackageName(), getAppVersionName(), getAppVersionCode());
            summary += (tmp = bcstr("FLAVOR", "")).isEmpty() ? "" : ("\n<b>Flavor:</b> " + tmp.replace("flavor", ""));
            summary += (tmp = bcstr("BUILD_TYPE", "")).isEmpty() ? "" : (" (" + tmp + ")");
            summary += (tmp = bcstr("BUILD_DATE", "")).isEmpty() ? "" : ("\n<b>Build date:</b> " + tmp);
            summary += (tmp = getAppInstallationSource()).isEmpty() ? "" : ("\n<b>ISource:</b> " + tmp);
            summary += (tmp = bcstr("GITHASH", "")).isEmpty() ? "" : ("\n<b>VCS Hash:</b> " + tmp);
            pref.setSummary(htmlToSpanned(summary.trim().replace("\n", "<br/>")));

        }
    }

    public Spanned htmlToSpanned(String html) {
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(html);
        }
        return result;
    }

    public String bcstr(String fieldName, String defaultValue) {
        Object field = getBuildConfigValue(fieldName);
        if (field != null && field instanceof String) {
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
