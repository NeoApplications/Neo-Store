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

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.android.launcher3.Launcher
import com.android.launcher3.LauncherAppState
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.popup.SystemShortcut
import com.android.launcher3.uioverrides.QuickstepLauncher
import com.android.launcher3.views.OptionsPopupView
import com.android.systemui.plugins.shared.LauncherOverlayManager
import com.farmerbb.taskbar.lib.Taskbar
import com.google.android.apps.nexuslauncher.OverlayCallbackImpl
import com.saggitt.omega.gestures.GestureController
import com.saggitt.omega.popup.OmegaShortcuts
import com.saggitt.omega.preferences.OmegaPreferences
import com.saggitt.omega.preferences.OmegaPreferencesChangeCallback
import com.saggitt.omega.theme.ThemeOverride
import com.saggitt.omega.util.Config
import com.saggitt.omega.util.DbHelper
import com.saggitt.omega.util.omegaPrefs
import java.util.stream.Stream

class OmegaLauncher : QuickstepLauncher(), OmegaPreferences.OnPreferenceChangeListener {
    val gestureController by lazy { GestureController(this) }
    val dummyView by lazy { findViewById<View>(R.id.dummy_view)!! }
    val optionsView by lazy { findViewById<OptionsPopupView>(R.id.options_view)!! }

    private var currentTheme = 0
    private var currentAccent = 0
    private lateinit var themeOverride: ThemeOverride
    private val themeSet: ThemeOverride.ThemeSet get() = ThemeOverride.Settings()

    private val hideStatusBarKey = "pref_hideStatusBar"
    private var paused = false
    private var sRestart = false
    private val prefCallback = OmegaPreferencesChangeCallback(this)
    val prefs: OmegaPreferences by lazy { Utilities.getOmegaPrefs(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        themeOverride = ThemeOverride(themeSet, this)
        themeOverride.applyTheme(this)
        currentAccent = omegaPrefs.accentColor
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 && !Utilities.hasStoragePermission(
                this
            )
        ) {
            Utilities.requestStoragePermission(this)
        }
        prefs.registerCallback(prefCallback)

        super.onCreate(savedInstanceState)

        prefs.addOnPreferenceChangeListener(hideStatusBarKey, this)

        /*CREATE DB TO HANDLE APPS COUNT*/
        val db = DbHelper(this)
        db.close()
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

    override fun onResume() {
        super.onResume()
        if (currentAccent != omegaPrefs.accentColor) recreate()
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

    override fun onDestroy() {
        super.onDestroy()

        prefs.unregisterCallback()
        prefs.removeOnPreferenceChangeListener(hideStatusBarKey, this)
        if (sRestart) {
            sRestart = false
            OmegaPreferences.destroyInstance()
        }
    }

    fun shouldRecreate() = !sRestart

    override fun getDefaultOverlay(): LauncherOverlayManager {
        return OverlayCallbackImpl(this)
    }

    private val customLayoutInflater by lazy {
        OmegaLayoutInflater(
            super.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater,
            this
        )
    }

    override fun getSystemService(name: String): Any? {
        if (name == Context.LAYOUT_INFLATER_SERVICE) {
            return customLayoutInflater
        }
        return super.getSystemService(name)
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

    override fun onValueChanged(key: String, prefs: OmegaPreferences, force: Boolean) {
        if (key == hideStatusBarKey) {
            if (prefs.hideStatusBar) {
                window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            } else if (!force) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            }
        }
        Taskbar.setEnabled(this, prefs.desktopModeEnabled)
    }
}

fun Launcher.getOmegaLauncher(): OmegaLauncher = this as? OmegaLauncher
    ?: (this as ContextWrapper).baseContext as? OmegaLauncher
    ?: LauncherAppState.getInstance(this).launcher as OmegaLauncher
