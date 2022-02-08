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

package com.saggitt.omega;

import android.app.Activity;
import android.os.Bundle;

import com.android.launcher3.Launcher;
import com.android.launcher3.Utilities;
import com.android.systemui.plugins.shared.LauncherOverlayManager;
import com.android.systemui.plugins.shared.LauncherOverlayManager.LauncherOverlay;
import com.google.android.libraries.gsa.launcherclient.ISerializableScrollCallback;
import com.google.android.libraries.gsa.launcherclient.LauncherClient;
import com.google.android.libraries.gsa.launcherclient.LauncherClientCallbacks;
import com.google.android.libraries.gsa.launcherclient.StaticInteger;
import com.google.systemui.smartspace.SmartSpaceView;
import com.saggitt.omega.preferences.OmegaPreferences;

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
        ISerializableScrollCallback {

    final static String PREF_PERSIST_FLAGS = "pref_persistent_flags";
    public final LauncherClient mClient;
    private final Launcher mLauncher;
    boolean mFlagsChanged = false;
    private LauncherOverlayCallbacks mLauncherOverlayCallbacks;
    private boolean mWasOverlayAttached = false;
    private int mFlags;
    private final Set<SmartSpaceView> mSmartSpaceViews = Collections.newSetFromMap(new WeakHashMap<>());

    public OverlayCallbackImpl(Launcher launcher) {
        OmegaPreferences prefs = Utilities.getOmegaPrefs(launcher);

        mLauncher = launcher;
        mClient = new LauncherClient(mLauncher, this, new StaticInteger(
                (prefs.getEnableMinus() ? 1 : 0) | 2 | 4 | 8));
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
        for (SmartSpaceView smartspace : mSmartSpaceViews) {
            smartspace.onPause();
        }
    }

    public void registerSmartspaceView(SmartSpaceView smartspace) {
        mSmartSpaceViews.add(smartspace);
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
        mClient.onDestroy();
        mClient.mDestroyed = true;
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