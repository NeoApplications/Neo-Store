package com.saggitt.omega.icons.calendar;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import java.util.Calendar;

public class DynamicCalendar {
    public static final String CALENDAR = "com.google.android.calendar";

    public static Drawable load(Context context, ComponentName component, int iconDpi) {
        try {
            PackageManager pm = context.getPackageManager();
            Bundle metaData = pm.getActivityInfo(component,
                    PackageManager.GET_META_DATA | PackageManager.GET_UNINSTALLED_PACKAGES).metaData;

            Resources resourcesForApplication = pm.getResourcesForApplication(DynamicCalendar.CALENDAR);
            int dayResId = DynamicCalendar.getDayResId(metaData, resourcesForApplication);
            if (dayResId != 0) {
                return resourcesForApplication.getDrawableForDensity(dayResId, iconDpi);
            }
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        return null;
    }

    public static int getDayResId(Bundle bundle, Resources resources) {
        if (bundle != null) {
            int dateArrayId = bundle.getInt(CALENDAR + ".dynamic_icons_nexus_round", 0);
            if (dateArrayId != 0) {
                try {
                    TypedArray dateIds = resources.obtainTypedArray(dateArrayId);
                    int dateId = dateIds.getResourceId(getDayOfMonth(), 0);
                    dateIds.recycle();
                    return dateId;
                } catch (Resources.NotFoundException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return 0;
    }

    public static int getDayOfMonth() {
        return Calendar.getInstance().get(Calendar.DAY_OF_MONTH) - 1;
    }
}
