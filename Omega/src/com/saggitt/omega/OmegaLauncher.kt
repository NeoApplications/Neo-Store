/*
 *  This file is part of Omega Launcher
 *  Copyright (c) 2021   Omega Launcher Team
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
package com.saggitt.omega

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.compose.compiler.plugins.kotlin.EmptyFunctionMetrics.packageName
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.android.launcher3.LauncherAppState
import com.android.launcher3.LauncherRootView
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.model.data.AppInfo
import com.android.launcher3.model.data.FolderInfo
import com.android.launcher3.model.data.ItemInfo
import com.android.launcher3.model.data.WorkspaceItemInfo
import com.android.launcher3.pm.UserCache
import com.android.launcher3.popup.SystemShortcut
import com.android.launcher3.uioverrides.QuickstepLauncher
import com.android.launcher3.util.ComponentKey
import com.android.launcher3.util.Executors.MODEL_EXECUTOR
import com.android.launcher3.views.OptionsPopupView
import com.android.launcher3.widget.RoundedCornerEnforcement
import com.android.systemui.plugins.shared.LauncherOverlayManager
import com.android.systemui.shared.system.QuickStepContract
import com.farmerbb.taskbar.lib.Taskbar
import com.google.android.libraries.gsa.launcherclient.LauncherClient
import com.google.systemui.smartspace.SmartSpaceView
import com.saggitt.omega.gestures.GestureController
import com.saggitt.omega.iconpack.CustomIconEntry
import com.saggitt.omega.iconpack.EditIconActivity
import com.saggitt.omega.override.CustomInfoProvider
import com.saggitt.omega.popup.OmegaShortcuts
import com.saggitt.omega.preferences.OmegaPreferences
import com.saggitt.omega.preferences.OmegaPreferencesChangeCallback
import com.saggitt.omega.theme.ThemeManager
import com.saggitt.omega.theme.ThemeOverride
import com.saggitt.omega.util.Config
import com.saggitt.omega.util.Config.Companion.CODE_EDIT_ICON
import com.saggitt.omega.util.DbHelper
import java.util.stream.Stream

class OmegaLauncher : QuickstepLauncher(), LifecycleOwner, SavedStateRegistryOwner,
    ThemeManager.ThemeableActivity, OmegaPreferences.OnPreferenceChangeListener {
    val gestureController by lazy { GestureController(this) }
    val dummyView by lazy { findViewById<View>(R.id.dummy_view)!! }
    val optionsView by lazy { findViewById<OptionsPopupView>(R.id.options_view)!! }
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override var currentTheme = 0
    override var currentAccent = 0
    private lateinit var themeOverride: ThemeOverride
    private val themeSet: ThemeOverride.ThemeSet get() = ThemeOverride.Settings()

    private var paused = false
    private var sRestart = false
    private val prefCallback = OmegaPreferencesChangeCallback(this)
    val prefs: OmegaPreferences by lazy { Utilities.getOmegaPrefs(this) }

    val hiddenApps = ArrayList<AppInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1
            && !Utilities.hasStoragePermission(this)
        ) Utilities.requestStoragePermission(this)

        themeOverride = ThemeOverride(themeSet, this)
        themeOverride.applyTheme(this)
        currentAccent = prefs.accentColor
        currentTheme = themeOverride.getTheme(this)
        val config = Config(this)
        config.setAppLanguage(prefs.language)

        theme.applyStyle(
            resources.getIdentifier(
                Integer.toHexString(currentAccent),
                "style",
                packageName
            ), true
        )

        savedStateRegistryController.performRestore(savedInstanceState)
        super.onCreate(savedInstanceState)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        prefs.registerCallback(prefCallback)
        prefs.addOnPreferenceChangeListener("pref_hideStatusBar", this)

        //Load hidden apps to use with hidden apps preference
        MODEL_EXECUTOR.handler.postAtFrontOfQueue { loadHiddenApps(prefs.hiddenAppSet) }

        mOverlayManager = defaultOverlay
        showFolderNotificationCount = prefs.folderBadgeCount
        if (prefs.customWindowCorner) {
            RoundedCornerEnforcement.sRoundedCornerEnabled = true
            QuickStepContract.sCustomCornerRadius = prefs.windowCornerRadius
        }

        /*CREATE DB TO HANDLE APPS COUNT*/
        val db = DbHelper(this)
        db.close()

    }

    private fun loadHiddenApps(hiddenAppsSet: Set<String>) {

        val apps = ArrayList<LauncherActivityInfo>()
        val iconCache = LauncherAppState.getInstance(this).iconCache
        val profiles = UserCache.INSTANCE.get(this).userProfiles
        val launcherApps: LauncherApps = applicationContext.getSystemService(
            LauncherApps::class.java
        )
        profiles.forEach { apps += launcherApps.getActivityList(null, it) }
        for (info in apps) {
            val key = ComponentKey(info.componentName, info.user);
            if (hiddenAppsSet.contains(key.toString())) {
                val appInfo = AppInfo(info, info.user, false)
                iconCache.getTitleAndIcon(appInfo, false)
                hiddenApps.add(appInfo)
            }
        }
    }

    override fun getSupportedShortcuts(): Stream<SystemShortcut.Factory<*>> {
        return Stream.concat(
            super.getSupportedShortcuts(),
            Stream.of(
                OmegaShortcuts.CUSTOMIZE,
                OmegaShortcuts.APP_REMOVE,
                OmegaShortcuts.APP_UNINSTALL
            )
        )
    }

    override fun onStart() {
        super.onStart()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
    }

    override fun onResume() {
        super.onResume()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        if (currentAccent != prefs.accentColor || currentTheme != themeOverride.getTheme(this)) onThemeChanged()
        restartIfPending()
        paused = false
    }

    override fun onPause() {
        super.onPause()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        paused = true
    }

    override fun onStop() {
        super.onStop()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)

        prefs.removeOnPreferenceChangeListener("pref_hideStatusBar", this)
        prefs.unregisterCallback()
        if (sRestart) {
            sRestart = false
        }
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        savedStateRegistryController.performSave(outState)
    }

    override fun setupViews() {
        super.setupViews()
        findViewById<LauncherRootView>(R.id.launcher).let {
            ViewTreeLifecycleOwner.set(it, this)
            it.setViewTreeSavedStateRegistryOwner(this)
        }
    }

    private fun restartIfPending() {
        if (sRestart) {
            omegaApp.restart(false)
        }
    }

    fun scheduleRestart() {
        if (paused) {
            sRestart = true
        } else {
            Utilities.restartLauncher(this)
        }
    }

    override fun onThemeChanged() = recreate()

    fun shouldRecreate() = !sRestart

    override fun getDefaultOverlay(): LauncherOverlayManager {
        if (mOverlayManager == null) {
            mOverlayManager = OverlayCallbackImpl(this)
        }
        return mOverlayManager
    }

    fun startEditIcon(itemInfo: ItemInfo, infoProvider: CustomInfoProvider<ItemInfo>) {

        val component: ComponentKey? = when (itemInfo) {
            is AppInfo -> itemInfo.toComponentKey()
            is WorkspaceItemInfo -> itemInfo.targetComponent?.let {
                ComponentKey(it, itemInfo.user)
            }
            is FolderInfo -> itemInfo.toComponentKey()
            else -> null
        }

        currentEditIcon = when (itemInfo) {
            is AppInfo -> BitmapDrawable(this.resources, itemInfo.bitmap.icon)
            is WorkspaceItemInfo -> BitmapDrawable(this.resources, itemInfo.bitmap.icon)
            is FolderInfo -> itemInfo.getDefaultIcon(this)
            else -> null
        }
        currentEditInfo = itemInfo
        val intent = EditIconActivity.newIntent(
            this,
            infoProvider.getTitle(itemInfo),
            itemInfo is FolderInfo,
            component
        )
        val flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or Intent.FLAG_ACTIVITY_CLEAR_TASK
        BlankActivity.startActivityForResult(
            this,
            intent,
            CODE_EDIT_ICON,
            flags
        ) { resultCode, data ->
            handleEditIconResult(resultCode, data)
        }
    }

    private fun handleEditIconResult(resultCode: Int, data: Bundle?) {
        if (resultCode == Activity.RESULT_OK) {
            val itemInfo = currentEditInfo ?: return
            val entryString = data?.getString(EditIconActivity.EXTRA_ENTRY)
            val customIconEntry =
                entryString?.let { CustomIconEntry.fromString(it) }
            CustomInfoProvider.forItem<ItemInfo>(this, itemInfo)?.setIcon(itemInfo, customIconEntry)
        }
    }

    inline fun prepareDummyView(view: View, crossinline callback: (View) -> Unit) {
        val rect = Rect()
        dragLayer.getViewRectRelativeToSelf(view, rect)
        prepareDummyView(rect.left, rect.top, rect.right, rect.bottom, callback)
    }

    inline fun prepareDummyView(left: Int, top: Int, crossinline callback: (View) -> Unit) {
        val size = resources.getDimensionPixelSize(R.dimen.options_menu_thumb_size)
        val halfSize = size / 2
        prepareDummyView(left - halfSize, top - halfSize, left + halfSize, top + halfSize, callback)
    }

    inline fun prepareDummyView(
        left: Int, top: Int, right: Int, bottom: Int,
        crossinline callback: (View) -> Unit
    ) {
        (dummyView.layoutParams as ViewGroup.MarginLayoutParams).let {
            it.width = right - left
            it.height = bottom - top
            it.leftMargin = left
            it.topMargin = top
        }
        dummyView.requestLayout()
        dummyView.post { callback(dummyView) }
    }

    fun registerSmartspaceView(smartspace: SmartSpaceView) {
        defaultOverlay.registerSmartSpaceView(smartspace)
    }

    fun getGoogleNow(): LauncherClient? {
        return defaultOverlay.client
    }

    override fun onValueChanged(key: String, prefs: OmegaPreferences, force: Boolean) {
        if (key == "pref_hideStatusBar") {
            if (prefs.hideStatusBar) {
                window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            } else if (!force) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            }
        }
        Taskbar.setEnabled(this, prefs.desktopModeEnabled)
    }

    companion object {
        var showFolderNotificationCount = false
        var currentEditInfo: ItemInfo? = null
        var currentEditIcon: Drawable? = null

        @JvmStatic
        fun getLauncher(context: Context): OmegaLauncher {
            return context as? OmegaLauncher
                ?: (context as ContextWrapper).baseContext as? OmegaLauncher
                ?: LauncherAppState.getInstance(context).launcher as OmegaLauncher
        }
    }

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }
}