/*
 *  Copyright (c) 2020 Omega Launcher
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.saggitt.omega;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.core.graphics.ColorUtils;

import com.android.launcher3.LauncherCallbacks;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.appprediction.PredictionUiStateManager;
import com.android.launcher3.appprediction.PredictionUiStateManager.Client;
import com.android.launcher3.config.FeatureFlags;
import com.android.launcher3.uioverrides.WallpaperColorInfo;
import com.android.launcher3.uioverrides.WallpaperColorInfo.OnChangeListener;
import com.android.launcher3.util.Themes;
import com.google.android.libraries.gsa.launcherclient.ClientOptions;
import com.google.android.libraries.gsa.launcherclient.ClientService;
import com.saggitt.omega.qsb.QsbAnimationController;
import com.saggitt.omega.settings.SettingsActivity;
import com.saggitt.omega.smartspace.SmartspaceView;
import com.saggitt.omega.util.Config;
import com.saggitt.omega.util.CustomLauncherClient;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public class OmegaLauncherCallbacks implements LauncherCallbacks,
        SharedPreferences.OnSharedPreferenceChangeListener, OnChangeListener {

    private Set<SmartspaceView> mSmartspaceViews = Collections.newSetFromMap(new WeakHashMap<>());

    private final OmegaLauncher mLauncher;
    private final Bundle mUiInformation = new Bundle();
    private OverlayCallbackImpl mOverlayCallbacks;
    private CustomLauncherClient mLauncherClient;
    private QsbAnimationController mQsbController;
    private SharedPreferences mPrefs;
    private boolean mStarted;
    private boolean mResumed;
    private boolean mAlreadyOnHome;
    private final Runnable mUpdatePredictionsIfResumed = this::updatePredictionsIfResumed;
    private PredictionUiStateManager predictionUiStateManager;

    public OmegaLauncherCallbacks(OmegaLauncher launcher) {
        mLauncher = launcher;
    }

    public static int primaryColor(WallpaperColorInfo wallpaperColorInfo, Context context, int alpha) {
        return compositeAllApps(ColorUtils.setAlphaComponent(wallpaperColorInfo.getMainColor(), alpha), context);
    }

    public static int secondaryColor(WallpaperColorInfo wallpaperColorInfo, Context context, int alpha) {
        return compositeAllApps(ColorUtils.setAlphaComponent(wallpaperColorInfo.getSecondaryColor(), alpha), context);
    }

    public static int compositeAllApps(int color, Context context) {
        return ColorUtils.compositeColors(Themes.getAttrColor(context, R.attr.allAppsScrimColor), color);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mPrefs = Utilities.getPrefs(mLauncher);
        mOverlayCallbacks = new OverlayCallbackImpl(mLauncher);

        mLauncherClient = new CustomLauncherClient(mLauncher, mOverlayCallbacks, getClientOptions(mPrefs));
        mQsbController = new QsbAnimationController(mLauncher);
        mOverlayCallbacks.setClient(mLauncherClient);
        mUiInformation.putInt("system_ui_visibility", mLauncher.getWindow().getDecorView().getSystemUiVisibility());
        applyFeedTheme(false);
        WallpaperColorInfo instance = WallpaperColorInfo.getInstance(mLauncher);
        instance.addOnChangeListener(this);
        onExtractedColorsChanged(instance);
        mPrefs.registerOnSharedPreferenceChangeListener(this);
        predictionUiStateManager = PredictionUiStateManager.INSTANCE.get(mLauncher);
        predictionUiStateManager.setTargetAppsView(mLauncher.getAppsView());
        if (FeatureFlags.REFLECTION_FORCE_OVERVIEW_MODE) {
            predictionUiStateManager.switchClient(Client.OVERVIEW);
        }
    }

    @Override
    public void onResume() {
        mResumed = true;
        if (mStarted) {
            mAlreadyOnHome = true;
        }

        mLauncherClient.onResume();

        Handler handler = mLauncher.getDragLayer().getHandler();
        if (handler != null) {
            handler.removeCallbacks(mUpdatePredictionsIfResumed);
            Utilities.postAsyncCallback(handler, mUpdatePredictionsIfResumed);
        }
    }

    @Override
    public void onStart() {
        mStarted = true;
        mLauncherClient.onStart();
    }

    @Override
    public void onStop() {
        mStarted = false;
        if (!mResumed) {
            mAlreadyOnHome = false;
        }

        mLauncherClient.onStop();
    }

    @Override
    public void onPause() {
        mResumed = false;
        mLauncherClient.onPause();
        for (SmartspaceView smartspace : mSmartspaceViews) {
            smartspace.onPause();
        }
    }

    @Override
    public void onDestroy() {
        if (!mLauncherClient.isDestroyed()) {
            mLauncherClient.getActivity().unregisterReceiver(mLauncherClient.mInstallListener);
        }

        mLauncherClient.setDestroyed(true);
        mLauncherClient.getBaseService().disconnect();
        if (mLauncherClient.getOverlayCallback() != null) {
            mLauncherClient.getOverlayCallback().mClient = null;
            mLauncherClient.getOverlayCallback().mWindowManager = null;
            mLauncherClient.getOverlayCallback().mWindow = null;
            mLauncherClient.setOverlayCallback(null);
        }

        ClientService service = mLauncherClient.getClientService();
        CustomLauncherClient client = (CustomLauncherClient) service.getClient();
        if (client != null && client.equals(mLauncherClient)) {
            service.mWeakReference = null;
            if (!mLauncherClient.getActivity().isChangingConfigurations()) {
                service.disconnect();
                if (ClientService.sInstance == service) {
                    ClientService.sInstance = null;
                }
            }
        }

        Utilities.getPrefs(mLauncher).unregisterOnSharedPreferenceChangeListener(this);
        WallpaperColorInfo.getInstance(mLauncher).removeOnChangeListener(this);
        PredictionUiStateManager.INSTANCE.get(mLauncher).setTargetAppsView(null);
    }

    private void updatePredictionsIfResumed() {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    }

    @Override
    public void onAttachedToWindow() {
        mLauncherClient.onAttachedToWindow();
    }

    @Override
    public void onDetachedFromWindow() {
        if (!mLauncherClient.isDestroyed()) {
            mLauncherClient.getEventInfo().parse(0, "detachedFromWindow", 0.0f);
            mLauncherClient.setParams(null);
        }
    }

    void registerSmartspaceView(SmartspaceView smartspace) {
        mSmartspaceViews.add(smartspace);
    }

    @Override
    public void dump(String prefix, FileDescriptor fd, PrintWriter w, String[] args) {
    }

    @Override
    public void onHomeIntent(boolean internalStateHandled) {
        mLauncherClient.hideOverlay(mAlreadyOnHome);
    }

    @Override
    public boolean handleBackPressed() {
        return false;
    }

    @Override
    public void onTrimMemory(int level) {
    }

    @Override
    public void onLauncherProviderChange() {
    }

    @Override
    public boolean startSearch(String initialQuery, boolean selectInitialQuery, Bundle appSearchData) {
        return false;
    }

    @Override
    public QsbAnimationController getQsbController() {
        return mQsbController;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (SettingsActivity.ENABLE_MINUS_ONE_PREF.equals(key)) {
            ClientOptions clientOptions = getClientOptions(prefs);
            if (clientOptions.options != mLauncherClient.mFlags) {
                mLauncherClient.mFlags = clientOptions.options;
                if (mLauncherClient.getParams() != null) {
                    mLauncherClient.updateConfiguration();
                }
                mLauncherClient.getEventInfo().parse("setClientOptions ", mLauncherClient.mFlags);
            }
        }
        if (SettingsActivity.FEED_THEME_PREF.equals(key))
            applyFeedTheme(true);
    }

    @Override
    public void onExtractedColorsChanged(WallpaperColorInfo wallpaperColorInfo) {
        int alpha = mLauncher.getResources().getInteger(R.integer.extracted_color_gradient_alpha);
        mUiInformation.putInt("background_color_hint", primaryColor(wallpaperColorInfo, mLauncher.getApplicationContext(), alpha));
        mUiInformation.putInt("background_secondary_color_hint", secondaryColor(wallpaperColorInfo, mLauncher, alpha));
        mUiInformation.putBoolean("is_background_dark", Themes.getAttrBoolean(mLauncher, R.attr.isMainColorDark));

        applyFeedTheme(true);
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
            mLauncherClient.redraw(mUiInformation);
        }
    }

    public CustomLauncherClient getClient() {
        return mLauncherClient;
    }

    private ClientOptions getClientOptions(SharedPreferences prefs) {
        boolean hasPackage = Config.hasPackageInstalled(mLauncher, Config.GOOGLE_QSB);
        boolean isEnabled = prefs.getBoolean(SettingsActivity.ENABLE_MINUS_ONE_PREF, true);
        int canUse = hasPackage && isEnabled ? 1 : 0;
        return new ClientOptions(canUse | 2 | 4 | 8);
    }
}