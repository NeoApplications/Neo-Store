/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.launcher3.icons;

import static com.android.launcher3.util.MainThreadInitializedObject.forOverride;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.UserHandle;

import androidx.annotation.RequiresApi;

import com.android.launcher3.AdaptiveIconCompat;
import com.android.launcher3.R;
import com.android.launcher3.icons.cache.IconPack;
import com.android.launcher3.icons.cache.IconPackProvider;
import com.android.launcher3.util.MainThreadInitializedObject;
import com.android.launcher3.util.ResourceBasedOverride;

import java.util.function.BiFunction;

public class IconProvider implements ResourceBasedOverride {
    private Context mContext;
    public static MainThreadInitializedObject<IconProvider> INSTANCE =
            forOverride(IconProvider.class, R.string.icon_provider_class);

    private static final BiFunction<LauncherActivityInfo, Integer, Drawable> LAI_LOADER =
            LauncherActivityInfo::getIcon;

    private static final BiFunction<ActivityInfo, PackageManager, Drawable> AI_LOADER =
            ActivityInfo::loadUnbadgedIcon;

    public static IconProvider newInstance(Context context) {
        return Overrides.getObject(IconProvider.class, context, R.string.icon_provider_class);
    }

    public IconProvider(Context context) {
        mContext = context;
    }

    public String getSystemStateForPackage(String systemState, String packageName) {
        return systemState;
    }

    /**
     * @param flattenDrawable true if the caller does not care about the specification of the
     *                        original icon as long as the flattened version looks the same.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public Drawable getIcon(LauncherActivityInfo info, int iconDpi, boolean flattenDrawable) {
        return AdaptiveIconCompat.wrap(info.getIcon(iconDpi));
    }

    /**
     * Loads the icon for the provided LauncherActivityInfo
     */
    public Drawable getIcon(LauncherActivityInfo info, int iconDpi) {
        return getIcon(info.getApplicationInfo().packageName, info.getUser(),
                info, iconDpi, LAI_LOADER);
    }

    private <T, P> Drawable getIcon(String packageName, UserHandle user, T obj, P param,
                                    BiFunction<T, P, Drawable> loader) {
        Drawable icon = null;
        /*if (mCalendar != null && mCalendar.getPackageName().equals(packageName)) {
            icon = loadCalendarDrawable(0);
        } else if (mClock != null
                && mClock.getPackageName().equals(packageName)
                && Process.myUserHandle().equals(user)) {
            icon = loadClockDrawable(0);
        }*/

        Drawable ret = icon == null ? loader.apply(obj, param) : icon;
        IconPack iconPack = IconPackProvider.loadAndGetIconPack(mContext);
        try {
            if (iconPack != null) {
                ret = iconPack.getIcon(packageName, ret, mContext.getPackageManager().getApplicationInfo(packageName, 0).name);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            ret = iconPack.getIcon(packageName, ret, "");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !(ret instanceof AdaptiveIconCompat)) {
            ret = IconPack.wrapAdaptiveIcon(ret, mContext);
        }
        return ret;
    }
}