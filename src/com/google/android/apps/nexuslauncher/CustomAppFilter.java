package com.google.android.apps.nexuslauncher;

import android.content.ComponentName;
import android.content.Context;
import android.os.UserHandle;

import com.android.launcher3.Utilities;
import com.android.launcher3.util.ComponentKey;
import com.saggitt.omega.allapps.OmegaAppFilter;

import java.util.HashSet;
import java.util.Set;

public class CustomAppFilter extends OmegaAppFilter {
    private final Context mContext;

    public CustomAppFilter(Context context) {
        super(context);
        mContext = context;
    }

    static void setComponentNameState(Context context, ComponentKey key, boolean hidden) {
        String comp = key.toString();
        Set<String> hiddenApps = new HashSet<>(getHiddenApps(context));
        while (hiddenApps.contains(comp)) {
            hiddenApps.remove(comp);
        }
        if (hidden) {
            hiddenApps.add(comp);
        }
        setHiddenApps(context, hiddenApps);
    }

    static boolean isHiddenApp(Context context, ComponentKey key) {
        return getHiddenApps(context).contains(key.toString());
    }

    @SuppressWarnings("ConstantConditions") // This can't be null anyway
    private static Set<String> getHiddenApps(Context context) {
        return Utilities.getOmegaPrefs(context).getHiddenAppSet();
    }

    public static void setHiddenApps(Context context, Set<String> hiddenApps) {
        Utilities.getOmegaPrefs(context).setHiddenAppSet(hiddenApps);
    }

    @Override
    public boolean shouldShowApp(ComponentName componentName, UserHandle user) {
        return super.shouldShowApp(componentName, user)
                && (user == null || !isHiddenApp(mContext, new ComponentKey(componentName, user)));
    }
}
