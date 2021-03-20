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

package com.saggitt.omega;

import android.animation.AnimatorSet;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.android.launcher3.LauncherAppState;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.model.data.AppInfo;
import com.android.launcher3.model.data.FolderInfo;
import com.android.launcher3.model.data.ItemInfo;
import com.android.launcher3.model.data.WorkspaceItemInfo;
import com.android.launcher3.uioverrides.QuickstepLauncher;
import com.android.launcher3.util.ComponentKey;
import com.google.android.apps.nexuslauncher.NexusLauncher;
import com.google.android.apps.nexuslauncher.smartspace.SmartspaceView;
import com.google.android.libraries.gsa.launcherclient.LauncherClient;
import com.saggitt.omega.gestures.GestureController;
import com.saggitt.omega.iconpack.EditIconActivity;
import com.saggitt.omega.iconpack.IconPackManager;
import com.saggitt.omega.override.CustomInfoProvider;
import com.saggitt.omega.settings.SettingsActivity;
import com.saggitt.omega.smartspace.FeedBridge;
import com.saggitt.omega.util.Config;
import com.saggitt.omega.util.ContextUtils;
import com.saggitt.omega.util.DbHelper;
import com.saggitt.omega.views.OmegaBackgroundView;
import com.saggitt.omega.views.OptionsPanel;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;

import static com.saggitt.omega.iconpack.IconPackManager.CustomIconEntry;
import static com.saggitt.omega.util.Config.REQUEST_PERMISSION_LOCATION_ACCESS;
import static com.saggitt.omega.util.Config.REQUEST_PERMISSION_STORAGE_ACCESS;

public class OmegaLauncher extends QuickstepLauncher implements OmegaPreferences.OnPreferenceChangeListener {
    public static boolean showFolderNotificationCount;
    public static Drawable currentEditIcon = null;
    public ItemInfo currentEditInfo = null;
    public Context mContext;
    private boolean paused = false;
    private boolean sRestart = false;
    private OmegaPreferencesChangeCallback prefCallback = new OmegaPreferencesChangeCallback(this);

    private GestureController mGestureController;
    public View dummyView;
    public OmegaBackgroundView background;
    private OptionsPanel optionsView;
    private String hideStatusBarKey = "pref_hideStatusBar";

    private final NexusLauncher launcherClient;

    public OmegaLauncher() {
        launcherClient = new NexusLauncher(this);
    }

    public static OmegaLauncher getLauncher(Context context) {
        if (context instanceof OmegaLauncher) {
            return (OmegaLauncher) context;
        } else {
            return (OmegaLauncher) LauncherAppState.getInstance(context).getLauncher();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 && !Utilities.hasStoragePermission(this)) {
            Utilities.requestStoragePermission(this);
        }
        IconPackManager.Companion.getInstance(this);
        super.onCreate(savedInstanceState);

        mContext = this;

        OmegaPreferences mPrefs = Utilities.getOmegaPrefs(mContext);
        mPrefs.registerCallback(prefCallback);
        mPrefs.addOnPreferenceChangeListener(hideStatusBarKey, this);
        if (!FeedBridge.Companion.getInstance(this).isInstalled()) {
            mPrefs.getEditor().putBoolean(SettingsActivity.ENABLE_MINUS_ONE_PREF, false).apply();
        }
        if (mPrefs.getFirstRun()) {
            mPrefs.setFirstRun(false);
            mPrefs.setIconShape("cylinder");
        }

        ContextUtils contextUtils = new ContextUtils(this);
        contextUtils.setAppLanguage(mPrefs.getLanguage());
        showFolderNotificationCount = mPrefs.getNotificationCount();
        dummyView = findViewById(R.id.dummy_view);

        /*CREATE DB TO HANDLE APPS COUNT*/
        DbHelper db = new DbHelper(this);
        db.close();

    }

    public GestureController getGestureController() {
        if (mGestureController == null)
            mGestureController = new GestureController(this);
        return mGestureController;
    }

    @Override
    public void onResume() {
        super.onResume();
        restartIfPending();
        paused = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        paused = true;
    }

    public void refreshGrid() {
        mWorkspace.refreshChildren();
    }

    public void onDestroy() {
        super.onDestroy();
        Utilities.getOmegaPrefs(this).unregisterCallback();
        Utilities.getOmegaPrefs(this).removeOnPreferenceChangeListener(hideStatusBarKey, this);

        if (sRestart) {
            sRestart = false;
            OmegaPreferences.Companion.destroyInstance();
        }
    }

    public void onValueChanged(String key, @NotNull OmegaPreferences prefs, boolean force) {
        if (key.equals(hideStatusBarKey)) {
            if (prefs.getHideStatusBar()) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            } else if (!force) {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
        }
    }

    public OmegaBackgroundView getBackground() {
        return background = findViewById(R.id.omega_background);
    }

    public OptionsPanel getOptionsView() {
        return optionsView = findViewById(R.id.options_view);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_STORAGE_ACCESS) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle(R.string.title_storage_permission_required)
                        .setMessage(R.string.content_storage_permission_required)
                        .setCancelable(false)
                        .setNegativeButton(android.R.string.no, null)
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> Utilities.requestStoragePermission(this))
                        .show();
            }
        }
        if (requestCode == REQUEST_PERMISSION_LOCATION_ACCESS) {
            OmegaAppKt.getOmegaApp(this).getSmartspace().updateWeatherData();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public boolean shouldRecreate() {
        return !sRestart;
    }

    public void scheduleRestart() {
        if (paused) {
            sRestart = true;
        } else {
            Utilities.restartLauncher(mContext);
        }
    }

    public void restartIfPending() {
        if (sRestart) {
            OmegaAppKt.getOmegaApp(this).restart(false);
        }
    }

    public void startEditIcon(ItemInfo itemInfo, CustomInfoProvider<ItemInfo> infoProvider) {
        ComponentKey component = null;
        currentEditInfo = itemInfo;

        if (itemInfo instanceof AppInfo) {
            component = ((AppInfo) itemInfo).toComponentKey();
            currentEditIcon = Objects.requireNonNull(IconPackManager.Companion.getInstance(this)
                    .getEntryForComponent(component)).getDrawable();
        } else if (itemInfo instanceof WorkspaceItemInfo) {
            component = new ComponentKey(itemInfo.getTargetComponent(), itemInfo.user);
            currentEditIcon = new BitmapDrawable(mContext.getResources(), ((WorkspaceItemInfo) itemInfo).bitmap.icon);
        } else if (itemInfo instanceof FolderInfo) {
            component = ((FolderInfo) itemInfo).toComponentKey();
            currentEditIcon = ((FolderInfo) itemInfo).getDefaultIcon(this);
        }

        boolean folderInfo = itemInfo instanceof FolderInfo;
        int flags = Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_CLEAR_TASK;
        Intent intent = EditIconActivity.Companion.newIntent(this, infoProvider.getTitle(itemInfo), folderInfo, component);

        BlankActivity.Companion.startActivityForResult(this, intent, Config.CODE_EDIT_ICON, flags, (resultCode, data) -> {
            handleEditIconResult(resultCode, data);
            return null;
        });

    }

    private void handleEditIconResult(int resultCode, Bundle data) {
        if (resultCode == Activity.RESULT_OK) {
            if (currentEditInfo == null) {
                return;
            }
            ItemInfo itemInfo = currentEditInfo;
            String entryString = data.getString(EditIconActivity.EXTRA_ENTRY);
            if (entryString != null) {
                CustomIconEntry customIconEntry = CustomIconEntry.Companion.fromString(entryString);
                (CustomInfoProvider.Companion.forItem(this, itemInfo)).setIcon(itemInfo, customIconEntry);
                Log.d("OmegaLauncher", "Provider " + CustomInfoProvider
                        .Companion.forItem(this, itemInfo).toString());
            } else {
                CustomInfoProvider.Companion.forItem(this, itemInfo).setIcon(itemInfo, null);
            }
        }
    }

    public void prepareDummyView(View view, @NotNull Function0<Unit> callback) {
        Rect rect = new Rect();
        getDragLayer().getViewRectRelativeToSelf(view, rect);
        prepareDummyView(rect.left, rect.top, rect.right, rect.bottom, callback);
    }

    public void prepareDummyView(int left, int top, @NotNull Function0<Unit> callback) {
        int size = getResources().getDimensionPixelSize(R.dimen.options_menu_thumb_size);
        int halfSize = size / 2;
        prepareDummyView(left - halfSize, top - halfSize, left + halfSize, top + halfSize, callback);
    }

    public void prepareDummyView(int left, int top, int right, int bottom, @NotNull Function0<Unit> callback) {
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) dummyView.getLayoutParams();
        lp.leftMargin = left;
        lp.topMargin = top;
        lp.height = bottom - top;
        lp.width = right - left;
        dummyView.setLayoutParams(lp);
        dummyView.requestLayout();
        dummyView.post(callback::invoke);
    }

    @Nullable
    public LauncherClient getGoogleNow() {
        return launcherClient.mClient;
    }

    public void playQsbAnimation() {
        launcherClient.mQsbAnimationController.dZ();
    }

    public AnimatorSet openQsb() {
        return launcherClient.mQsbAnimationController.openQsb();
    }

    public void registerSmartspaceView(SmartspaceView smartspace) {
        launcherClient.registerSmartspaceView(smartspace);
    }
}
