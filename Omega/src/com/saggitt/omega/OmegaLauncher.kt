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

package com.saggitt.omega

import android.animation.AnimatorSet
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.android.launcher3.LauncherAppState
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.model.data.AppInfo
import com.android.launcher3.model.data.FolderInfo
import com.android.launcher3.model.data.ItemInfo
import com.android.launcher3.model.data.WorkspaceItemInfo
import com.android.launcher3.uioverrides.QuickstepLauncher
import com.android.launcher3.util.ComponentKey
import com.android.systemui.plugins.shared.LauncherOverlayManager
import com.google.android.apps.nexuslauncher.OverlayCallbackImpl
import com.google.android.apps.nexuslauncher.smartspace.SmartspaceView
import com.google.android.libraries.gsa.launcherclient.LauncherClient
import com.saggitt.omega.gestures.GestureController
import com.saggitt.omega.iconpack.EditIconActivity
import com.saggitt.omega.iconpack.IconPackManager
import com.saggitt.omega.override.CustomInfoProvider
import com.saggitt.omega.util.Config
import com.saggitt.omega.util.Config.*
import com.saggitt.omega.util.Config.Companion.CODE_EDIT_ICON
import com.saggitt.omega.util.Config.Companion.REQUEST_PERMISSION_LOCATION_ACCESS
import com.saggitt.omega.util.Config.Companion.REQUEST_PERMISSION_STORAGE_ACCESS
import com.saggitt.omega.util.DbHelper
import com.saggitt.omega.util.applyAccent
import com.saggitt.omega.views.OmegaBackgroundView
import com.saggitt.omega.views.OptionsPanel

class OmegaLauncher : QuickstepLauncher(), OmegaPreferences.OnPreferenceChangeListener {
    private val hideStatusBarKey = "pref_hideStatusBar"
    val gestureController by lazy { GestureController(this) }
    val background by lazy { findViewById<OmegaBackgroundView>(R.id.omega_background)!! }
    val dummyView by lazy { findViewById<View>(R.id.dummy_view)!! }
    val optionsView by lazy { findViewById<OptionsPanel>(R.id.options_view)!! }
    private val prefCallback = OmegaPreferencesChangeCallback(this)
    private var paused = false
    private var sRestart = false
    private var defaultOverlay: OverlayCallbackImpl? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 && !Utilities.hasStoragePermission(
                this
            )
        ) {
            Utilities.requestStoragePermission(this)
        }
        IconPackManager.getInstance(this).defaultPack.dynamicClockDrawer
        super.onCreate(savedInstanceState)

        val mPrefs = Utilities.getOmegaPrefs(this)
        mPrefs.registerCallback(prefCallback)
        mPrefs.addOnPreferenceChangeListener(hideStatusBarKey, this)
        if (mPrefs.firstRun) {
            mPrefs.firstRun = false
            mPrefs.iconShape = "cylinder"
        }
        val config = Config(this)
        config.setAppLanguage(mPrefs.language)

        /*CREATE DB TO HANDLE APPS COUNT*/
        val db = DbHelper(this)
        db.close()
    }

    override fun onResume() {
        super.onResume()

        restartIfPending()
        paused = false
    }

    override fun onPause() {
        super.onPause()
        paused = true
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

    fun refreshGrid() {
        workspace.refreshChildren()
    }

    override fun onDestroy() {
        super.onDestroy()

        Utilities.getOmegaPrefs(this).unregisterCallback()
        Utilities.getOmegaPrefs(this).removeOnPreferenceChangeListener(hideStatusBarKey, this)

        if (sRestart) {
            sRestart = false
            OmegaPreferences.destroyInstance()
        }
    }

    fun startEditIcon(itemInfo: ItemInfo, infoProvider: CustomInfoProvider<ItemInfo>) {
        val component: ComponentKey? = when (itemInfo) {
            is AppInfo -> itemInfo.toComponentKey()
            is WorkspaceItemInfo -> itemInfo.targetComponent?.let {
                ComponentKey(
                    it,
                    itemInfo.user
                )
            }
            is FolderInfo -> itemInfo.toComponentKey()
            else -> null
        }
        currentEditIcon = when (itemInfo) {
            is AppInfo -> IconPackManager.getInstance(this)
                .getEntryForComponent(component!!)?.drawable
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
            this, intent, CODE_EDIT_ICON,
            flags
        ) { resultCode, data -> handleEditIconResult(resultCode, data) }
    }

    private fun handleEditIconResult(resultCode: Int, data: Bundle?) {
        if (resultCode == Activity.RESULT_OK) {
            val itemInfo = currentEditInfo ?: return
            val entryString = data?.getString(EditIconActivity.EXTRA_ENTRY)
            val customIconEntry =
                entryString?.let { IconPackManager.CustomIconEntry.fromString(it) }
            CustomInfoProvider.forItem<ItemInfo>(this, itemInfo)?.setIcon(itemInfo, customIconEntry)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_PERMISSION_STORAGE_ACCESS) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                AlertDialog.Builder(this)
                    .setTitle(R.string.title_storage_permission_required)
                    .setMessage(R.string.content_storage_permission_required)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        Utilities
                            .requestStoragePermission(this@OmegaLauncher)
                    }
                    .setCancelable(false)
                    .create().apply {
                        show()
                        applyAccent()
                    }
            }
        } else if (requestCode == REQUEST_PERMISSION_LOCATION_ACCESS) {
            omegaApp.smartspace.updateWeatherData()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun getSystemService(name: String): Any? {
        if (name == Context.LAYOUT_INFLATER_SERVICE) {
            return customLayoutInflater
        }
        return super.getSystemService(name)
    }

    fun shouldRecreate() = !sRestart

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

    override fun onValueChanged(key: String, prefs: OmegaPreferences, force: Boolean) {
        if (key == hideStatusBarKey) {
            if (prefs.hideStatusBar) {
                window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            } else if (!force) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            }
        }
    }

    override fun getDefaultOverlay(): LauncherOverlayManager? {
        if (defaultOverlay == null) {
            defaultOverlay = OverlayCallbackImpl(this)
        }
        return defaultOverlay
    }

    private val customLayoutInflater by lazy {
        OmegaLayoutInflater(
            super.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater,
            this
        )
    }

    fun playQsbAnimation() {
        defaultOverlay!!.mQsbAnimationController.dZ()
    }

    fun openQsb(): AnimatorSet? {
        return defaultOverlay!!.mQsbAnimationController.openQsb()
    }

    fun getGoogleNow(): LauncherClient? {
        return defaultOverlay?.mClient
    }

    fun registerSmartspaceView(smartspace: SmartspaceView) {
        defaultOverlay?.registerSmartspaceView(smartspace)
    }

    companion object {
        var sRestart = false
        var currentEditInfo: ItemInfo? = null
        var currentEditIcon: Drawable? = null

        @JvmStatic
        fun getLauncher(context: Context): OmegaLauncher {
            return context as? OmegaLauncher
                ?: (context as ContextWrapper).baseContext as? OmegaLauncher
                ?: LauncherAppState.getInstance(context).launcher as OmegaLauncher
        }
    }
}