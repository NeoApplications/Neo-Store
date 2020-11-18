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

package com.saggitt.omega.theme.ui

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import androidx.annotation.Keep
import com.android.launcher3.R
import com.saggitt.omega.OmegaPreferences
import com.saggitt.omega.preferences.CustomDialogPreference
import com.saggitt.omega.theme.ThemeManager
import com.saggitt.omega.util.hasFlag
import com.saggitt.omega.util.hasFlags
import com.saggitt.omega.util.omegaPrefs

@Keep
class ThemePreference(context: Context, attrs: AttributeSet?) : CustomDialogPreference(context, attrs),
        OmegaPreferences.OnPreferenceChangeListener {

    private val prefs = context.omegaPrefs

    override fun onAttached() {
        super.onAttached()

        prefs.addOnPreferenceChangeListener("pref_launcherTheme", this)
    }

    override fun onDetached() {
        super.onDetached()

        prefs.removeOnPreferenceChangeListener("pref_launcherTheme", this)
    }

    override fun onValueChanged(key: String, prefs: OmegaPreferences, force: Boolean) {
        reloadSummary()
    }

    private fun reloadSummary() {
        val theme = prefs.launcherTheme

        val forceDark = theme.hasFlag(ThemeManager.THEME_DARK)
        val forceDarkText = theme.hasFlag(ThemeManager.THEME_DARK_TEXT)
        val followWallpaper = theme.hasFlag(ThemeManager.THEME_FOLLOW_WALLPAPER)
        val followNightMode = theme.hasFlag(ThemeManager.THEME_FOLLOW_NIGHT_MODE)
        val followDaylight = theme.hasFlag(ThemeManager.THEME_FOLLOW_DAYLIGHT)

        val light = !theme.hasFlag(ThemeManager.THEME_DARK_MASK)
        val useBlack = theme.hasFlags(ThemeManager.THEME_USE_BLACK, ThemeManager.THEME_DARK_MASK)

        val themeDesc = ArrayList<Int>()
        when {
            forceDark && useBlack -> themeDesc.add(R.string.theme_black)
            forceDark -> themeDesc.add(R.string.theme_dark)
            followNightMode -> themeDesc.add(R.string.theme_dark_theme_mode_follow_system)
            followDaylight -> themeDesc.add(R.string.theme_dark_theme_mode_follow_daylight)
            followWallpaper -> themeDesc.add(R.string.theme_dark_theme_mode_follow_wallpaper)
            light -> themeDesc.add(R.string.theme_light)
        }
        if (useBlack && !forceDark) {
            themeDesc.add(R.string.theme_black)
        }
        if (forceDarkText) {
            themeDesc.add(R.string.theme_with_dark_text)
        }

        val res = context.resources
        val strings = ArrayList<String>()
        themeDesc.mapTo(strings) { res.getString(it) }
        for (i in (1 until strings.size)) {
            strings[i] = strings[i].toLowerCase()
        }
        summary = TextUtils.join(", ", strings)
    }
}
