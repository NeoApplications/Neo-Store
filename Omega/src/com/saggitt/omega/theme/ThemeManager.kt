/*
 *
 *  *
 *  *  * Copyright (c) 2020 Omega Launcher
 *  *  *
 *  *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  *  * you may not use this file except in compliance with the License.
 *  *  * You may obtain a copy of the License at
 *  *  *
 *  *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *  *
 *  *  * Unless required by applicable law or agreed to in writing, software
 *  *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  * See the License for the specific language governing permissions and
 *  *  * limitations under the License.
 *  *
 *
 */

package com.saggitt.omega.theme

import android.content.Context
import com.android.launcher3.uioverrides.WallpaperColorInfo
import com.saggitt.omega.util.SingletonHolder
import com.saggitt.omega.util.ensureOnMainThread
import com.saggitt.omega.util.useApplicationContext

class ThemeManager(val context: Context): WallpaperColorInfo.OnChangeListener{
    private val wallpaperColorInfo = WallpaperColorInfo.getInstance(context)!!
    private var themeFlags = 0
    val isDark get() = themeFlags and THEME_DARK != 0
    val supportsDarkText get() = themeFlags and THEME_DARK_TEXT != 0

    init {
        onExtractedColorsChanged(null)
        wallpaperColorInfo.addOnChangeListener(this)
    }

    fun getCurrentFlags() = themeFlags
    override fun onExtractedColorsChanged(ignore: WallpaperColorInfo?) {
        updateTheme()
    }

    fun updateTheme() {

    }

    interface ThemeableActivity {
        fun onThemeChanged()
    }

    companion object : SingletonHolder<ThemeManager, Context>(ensureOnMainThread(useApplicationContext(::ThemeManager))) {

        const val THEME_FOLLOW_WALLPAPER = 1         // 000001 = 1
        const val THEME_DARK_TEXT = 1 shl 1          // 000010 = 2
        const val THEME_DARK = 1 shl 2               // 000100 = 4
        const val THEME_USE_BLACK = 1 shl 3          // 001000 = 8
        const val THEME_FOLLOW_NIGHT_MODE = 1 shl 4  // 010000 = 16
        const val THEME_FOLLOW_DAYLIGHT = 1 shl 5    // 100000 = 32

        const val THEME_AUTO_MASK = THEME_FOLLOW_WALLPAPER or THEME_FOLLOW_NIGHT_MODE or THEME_FOLLOW_DAYLIGHT
        const val THEME_DARK_MASK = THEME_DARK or THEME_AUTO_MASK

        fun isDarkText(flags: Int) = (flags and THEME_DARK_TEXT) != 0
        fun isDark(flags: Int) = (flags and THEME_DARK) != 0
        fun isBlack(flags: Int) = (flags and THEME_USE_BLACK) != 0
    }
}