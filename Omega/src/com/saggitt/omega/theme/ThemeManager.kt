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

package com.saggitt.omega.theme

import android.content.Context
import android.content.res.Configuration
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.uioverrides.WallpaperColorInfo
import com.android.launcher3.util.Executors.MAIN_EXECUTOR
import com.saggitt.omega.BlankActivity
import com.saggitt.omega.omegaApp
import com.saggitt.omega.twilight.TwilightListener
import com.saggitt.omega.twilight.TwilightManager
import com.saggitt.omega.twilight.TwilightState
import com.saggitt.omega.util.*
import com.saggitt.omega.util.Config.Companion.REQUEST_PERMISSION_LOCATION_ACCESS

class ThemeManager(val context: Context) : WallpaperColorInfo.OnChangeListener, TwilightListener {

    private val app = context.omegaApp
    private val wallpaperColorInfo = WallpaperColorInfo.getInstance(context)!!
    private val listeners = HashSet<ThemeOverride>()
    private val prefs = context.omegaPrefs
    private var themeFlags = 0
    private var usingNightMode = context.resources.configuration.usingNightMode
        set(value) {
            if (field != value) {
                field = value
                updateTheme()
            }
        }

    val isDark get() = themeFlags and THEME_DARK != 0
    val isBlack get() = themeFlags and THEME_USE_BLACK != 0
    val supportsDarkText get() = false
    val displayName: String
        get() {
            val values = context.resources.getIntArray(R.array.themeValues)
            val strings = context.resources.getStringArray(R.array.themes)
            val index = values.indexOf(themeFlags)
            return strings.getOrNull(index) ?: context.resources.getString(R.string.theme_auto)
        }
    private val twilightManager by lazy { TwilightManager.getInstance(context) }
    private val handler = MAIN_EXECUTOR.handler
    private var listenToTwilight = false
        set(value) {
            if (field != value) {
                field = value
                if (value) {
                    twilightManager.registerListener(this, handler)
                    onTwilightStateChanged(twilightManager.lastTwilightState)
                } else {
                    twilightManager.unregisterListener(this)
                }
            }
        }
    private var isDuringNight = false
        set(value) {
            field = value
            if (!prefs.launcherTheme.hasFlag(THEME_FOLLOW_DAYLIGHT)) return
            if (themeFlags.hasFlag(THEME_DARK) != value) {
                updateTheme()
            }
        }

    init {
        updateTheme()
        wallpaperColorInfo.addOnChangeListener(this)
    }

    fun addOverride(themeOverride: ThemeOverride) {
        synchronized(listeners) {
            removeDeadListeners()
            listeners.add(themeOverride)
        }
        themeOverride.applyTheme(themeFlags)
    }

    fun removeOverride(themeOverride: ThemeOverride) {
        synchronized(listeners) {
            listeners.remove(themeOverride)
        }
    }

    fun getCurrentFlags() = themeFlags

    private fun removeDeadListeners() {
        val it = listeners.iterator()
        while (it.hasNext()) {
            if (!it.next().isAlive) {
                it.remove()
            }
        }
    }

    override fun onExtractedColorsChanged(ignore: WallpaperColorInfo?) {
        updateTheme()
    }

    fun updateTheme() {
        val theme = updateTwilightState(prefs.launcherTheme)
        val isBlack = isBlack(theme)

        val isDark = when {
            theme.hasFlag(THEME_FOLLOW_NIGHT_MODE) -> usingNightMode
            theme.hasFlag(THEME_FOLLOW_WALLPAPER) -> wallpaperColorInfo.isDark
            theme.hasFlag(THEME_FOLLOW_DAYLIGHT) -> isDuringNight
            else -> theme.hasFlag(THEME_DARK)
        }

        var newFlags = 0
        if (isDark) newFlags = newFlags or THEME_DARK
        if (isBlack) newFlags = newFlags or THEME_USE_BLACK
        if (newFlags == themeFlags) return
        themeFlags = newFlags
        // TODO no listeners are added for now, either we keep this logic and use it in all classes or just use reloadActivities
        reloadActivities()
        synchronized(listeners) {
            removeDeadListeners()
            listeners.forEach { it.onThemeChanged(themeFlags) }
        }
    }

    private fun updateTwilightState(theme: Int): Int {
        if (!theme.hasFlag(THEME_FOLLOW_DAYLIGHT)) {
            listenToTwilight = false
            return theme
        }
        if (twilightManager.isAvailable) {
            listenToTwilight = true
            return theme
        }

        BlankActivity.requestPermission(
            context, android.Manifest.permission.ACCESS_COARSE_LOCATION,
            REQUEST_PERMISSION_LOCATION_ACCESS
        ) { granted ->
            if (granted) {
                listenToTwilight = true
            } else {
                prefs.launcherTheme = theme.removeFlag(THEME_DARK_MASK)
            }
        }

        return theme.removeFlag(THEME_DARK_MASK)
    }

    override fun onTwilightStateChanged(state: TwilightState?) {
        isDuringNight = state?.isNight == true
    }

    private fun reloadActivities() {
        HashSet(app.activityHandler.activities).forEach {
            if (it is ThemeableActivity) {
                it.onThemeChanged()
            } else {
                it.recreate()
            }
        }
    }

    fun updateNightMode(newConfig: Configuration) {
        usingNightMode = newConfig.usingNightMode
    }

    // TODO make all activities (including the desktop one) apply the chosen theme
    interface ThemeableActivity {
        var currentTheme: Int
        var currentAccent: Int
        fun onThemeChanged()
    }

    companion object :
        SingletonHolder<ThemeManager, Context>(ensureOnMainThread(useApplicationContext(::ThemeManager))) {

        const val THEME_DARK = 0b00001                // 1
        const val THEME_USE_BLACK = 0b00010           // 2
        const val THEME_FOLLOW_WALLPAPER = 0b00100    // 4
        const val THEME_FOLLOW_NIGHT_MODE = 0b01000   // 8
        const val THEME_FOLLOW_DAYLIGHT = 0b10000     // 16

        const val THEME_AUTO_MASK =
            THEME_FOLLOW_WALLPAPER or THEME_FOLLOW_NIGHT_MODE or THEME_FOLLOW_DAYLIGHT
        const val THEME_DARK_MASK = THEME_DARK or THEME_AUTO_MASK

        fun isDark(flags: Int) = (flags and THEME_DARK) != 0
        fun isBlack(flags: Int) = (flags and THEME_USE_BLACK) != 0

        fun getDefaultTheme(): Int {
            return if (Utilities.ATLEAST_Q) {
                THEME_FOLLOW_NIGHT_MODE
            } else {
                THEME_FOLLOW_WALLPAPER
            }
        }
    }
}