/*
 *  This file is part of Omega Launcher
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

package com.google.android.apps.nexuslauncher;

import static com.saggitt.omega.settings.SettingsActivity.ENABLE_MINUS_ONE_PREF;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.core.graphics.ColorUtils;

import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.uioverrides.WallpaperColorInfo;
import com.android.launcher3.util.Themes;
import com.android.systemui.plugins.shared.LauncherOverlayManager;
import com.android.systemui.plugins.shared.LauncherOverlayManager.LauncherOverlay;
import com.google.android.apps.nexuslauncher.qsb.QsbAnimationController;
import com.google.android.apps.nexuslauncher.smartspace.SmartspaceController;
import com.google.android.apps.nexuslauncher.smartspace.SmartspaceView;
import com.google.android.libraries.gsa.launcherclient.ISerializableScrollCallback;
import com.google.android.libraries.gsa.launcherclient.LauncherClient;
import com.google.android.libraries.gsa.launcherclient.LauncherClientCallbacks;
import com.google.android.libraries.gsa.launcherclient.LauncherClientService;
import com.google.android.libraries.gsa.launcherclient.StaticInteger;
import com.saggitt.omega.settings.SettingsActivity;
import com.saggitt.omega.smartspace.FeedBridge;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Implements {@link LauncherOverlay} and passes all the corresponding events to {@link
 * LauncherClient}. {@see setClient}
 *
 * <p>Implements {@link LauncherClientCallbacks} and sends all the corresponding callbacks to {@link
 * Launcher}.
 */
public class OverlayCallbackImpl
        implements LauncherOverlay, LauncherClientCallbacks, LauncherOverlayManager,
        SharedPreferences.OnSharedPreferenceChangeListener, ISerializableScrollCallback
        , WallpaperColorInfo.OnChangeListener {

    final static String PREF_PERSIST_FLAGS = "pref_persistent_flags";

    private final Launcher mLauncher;
    public final LauncherClient mClient;

    private LauncherOverlayCallbacks mLauncherOverlayCallbacks;
    private boolean mWasOverlayAttached = false;
    boolean mFlagsChanged = false;
    private int mFlags;
    public QsbAnimationController mQsbAnimationController;
    private final Bundle mUiInformation = new Bundle();
    private final Set<SmartspaceView> mSmartspaceViews = Collections.newSetFromMap(new WeakHashMap<>());

    public OverlayCallbackImpl(Launcher launcher) {
        SharedPreferences prefs = Utilities.getPrefs(launcher);

        mLauncher = launcher;
        mClient = new LauncherClient(launcher, this, new StaticInteger(
                (prefs.getBoolean(ENABLE_MINUS_ONE_PREF,
                        FeedBridge.useBridge(launcher)) ? 1 : 0) | 2 | 4 | 8));
        prefs.registerOnSharedPreferenceChangeListener(this);
        SmartspaceController.get(mLauncher).cW();

        mQsbAnimationController = new QsbAnimationController(launcher);
        mUiInformation.putInt("system_ui_visibility", mLauncher.getWindow().getDecorView().getSystemUiVisibility());
        applyFeedTheme(false);
        WallpaperColorInfo instance = WallpaperColorInfo.getInstance(mLauncher);
        instance.addOnChangeListener(this);
        onExtractedColorsChanged(instance);
    }

    @Override
    public void onExtractedColorsChanged(WallpaperColorInfo wallpaperColorInfo) {
        int alpha = mLauncher.getResources().getInteger(R.integer.extracted_color_gradient_alpha);

        mUiInformation.putInt("background_color_hint", primaryColor(wallpaperColorInfo, mLauncher, alpha));
        mUiInformation.putInt("background_secondary_color_hint", secondaryColor(wallpaperColorInfo, mLauncher, alpha));

        applyFeedTheme(true);
    }

    public static int primaryColor(WallpaperColorInfo wallpaperColorInfo, Context context, int alpha) {
        return compositeAllApps(ColorUtils.setAlphaComponent(wallpaperColorInfo.getMainColor(), alpha), context);
    }

    public static int secondaryColor(WallpaperColorInfo wallpaperColorInfo, Context context, int alpha) {
        return compositeAllApps(ColorUtils.setAlphaComponent(wallpaperColorInfo.getSecondaryColor(), alpha), context);
    }

    private static int compositeAllApps(int color, Context context) {
        return ColorUtils.compositeColors(Themes.getAttrColor(context, R.attr.allAppsScrimColor), color);
    }

    private void applyFeedTheme(boolean redraw) {
        String prefValue = Utilities.getPrefs(mLauncher).getString(SettingsActivity.FEED_THEME_PREF, null);
        int feedTheme;
        try {
            feedTheme = Integer.parseInt(prefValue == null ? "1" : prefValue);
        } catch (Exception e) {
            feedTheme = 1;
        }
        boolean auto = (feedTheme & 1) != 0;
        boolean preferDark = (feedTheme & 2) != 0;
        boolean isDark = auto ? Themes.getAttrBoolean(mLauncher, R.attr.isMainColorDark) : preferDark;
        mUiInformation.putBoolean("is_background_dark", isDark);

        if (redraw) {
            mClient.redraw(mUiInformation);
        }
    }

    @Override
    public void onDeviceProvideChanged() {
        mClient.redraw();
    }

    @Override
    public void onAttachedToWindow() {
        mClient.onAttachedToWindow();
    }

    @Override
    public void onDetachedFromWindow() {
        mClient.onDetachedFromWindow();
    }

    @Override
    public void openOverlay() {
        mClient.showOverlay(true);
    }

    @Override
    public void hideOverlay(boolean animate) {
        mClient.hideOverlay(animate);
    }

    @Override
    public void hideOverlay(int duration) {
        mClient.hideOverlay(duration);
    }

    @Override
    public boolean startSearch(byte[] config, Bundle extras) {
        View gIcon = mLauncher.findViewById(R.id.g_icon);
        while (gIcon != null && !gIcon.isClickable()) {
            if (gIcon.getParent() instanceof View) {
                gIcon = (View) gIcon.getParent();
            } else {
                gIcon = null;
            }
        }
        return false;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
        // Not called
    }

    @Override
    public void onActivityStarted(Activity activity) {
        mClient.onStart();
    }

    @Override
    public void onActivityResumed(Activity activity) {
        mClient.onResume();
    }

    @Override
    public void onActivityPaused(Activity activity) {
        mClient.onPause();
        for (SmartspaceView smartspace : mSmartspaceViews) {
            smartspace.onPause();
        }
    }

    public void registerSmartspaceView(SmartspaceView smartspace) {
        mSmartspaceViews.add(smartspace);
    }

    @Override
    public void onActivityStopped(Activity activity) {
        mClient.onStop();
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        LauncherClient launcherClient = mClient;
        if (!launcherClient.mDestroyed) {
            launcherClient.mActivity.unregisterReceiver(launcherClient.googleInstallListener);
        }
        launcherClient.mDestroyed = true;
        launcherClient.mBaseService.disconnect();
        if (launcherClient.mOverlayCallback != null) {
            launcherClient.mOverlayCallback.mClient = null;
            launcherClient.mOverlayCallback.mWindowManager = null;
            launcherClient.mOverlayCallback.mWindow = null;
            launcherClient.mOverlayCallback = null;
        }

        LauncherClientService service = launcherClient.mLauncherService;
        LauncherClient client = service.getClient();
        if (client != null && client.equals(launcherClient)) {
            service.mClient = null;
            if (!launcherClient.mActivity.isChangingConfigurations()) {
                service.disconnect();
                if (LauncherClientService.sInstance == service) {
                    LauncherClientService.sInstance = null;
                }
            }
        }

        Utilities.getPrefs(mLauncher).unregisterOnSharedPreferenceChangeListener(this);
        WallpaperColorInfo.getInstance(mLauncher).removeOnChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        switch (key) {
            case SettingsActivity.ENABLE_MINUS_ONE_PREF:
                mClient.showOverlay(prefs.getBoolean(ENABLE_MINUS_ONE_PREF, FeedBridge.useBridge(mLauncher)));
                break;
            case SettingsActivity.FEED_THEME_PREF:
                applyFeedTheme(true);
                break;
        }
    }

    @Override
    public void onOverlayScrollChanged(float progress) {
        if (mLauncherOverlayCallbacks != null) {
            mLauncherOverlayCallbacks.onScrollChanged(progress);
        }
    }

    @Override
    public void onServiceStateChanged(boolean overlayAttached, boolean hotwordActive) {
        this.onServiceStateChanged(overlayAttached);
    }

    @Override
    public void onServiceStateChanged(boolean overlayAttached) {
        if (overlayAttached != mWasOverlayAttached) {
            mWasOverlayAttached = overlayAttached;
            mLauncher.setLauncherOverlay(overlayAttached ? this : null);
        }
    }

    @Override
    public void onScrollInteractionBegin() {
        mClient.startScroll();
    }

    @Override
    public void onScrollInteractionEnd() {
        mClient.endScroll();
    }

    @Override
    public void onScrollChange(float progress, boolean rtl) {
        mClient.setScroll(progress);
    }

    @Override
    public void setOverlayCallbacks(LauncherOverlayCallbacks callbacks) {
        mLauncherOverlayCallbacks = callbacks;
    }

    @Override
    public void setPersistentFlags(int flags) {
        flags &= (8 | 16);
        if (flags != mFlags) {
            mFlagsChanged = true;
            mFlags = flags;
            Utilities.getDevicePrefs(mLauncher).edit().putInt(PREF_PERSIST_FLAGS, flags).apply();
        }
    }
}