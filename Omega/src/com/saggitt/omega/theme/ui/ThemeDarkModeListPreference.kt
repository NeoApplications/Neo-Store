/*
 *     This file is part of Lawnchair Launcher.
 *
 *     Lawnchair Launcher is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Lawnchair Launcher is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Lawnchair Launcher.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saggitt.omega.theme.ui

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.Keep
import androidx.preference.ListPreference
import com.android.launcher3.R
import com.saggitt.omega.OmegaPreferences
import com.saggitt.omega.theme.ThemeManager
import com.saggitt.omega.util.addFlag
import com.saggitt.omega.util.buildEntries
import com.saggitt.omega.util.omegaPrefs
import com.saggitt.omega.util.removeFlag

@Keep
class ThemeDarkModeListPreference(context: Context, attrs: AttributeSet?) : ListPreference(context, attrs),
        OmegaPreferences.OnPreferenceChangeListener {

    private val prefs = context.omegaPrefs

    init {
        buildEntries {
            addEntry(R.string.theme_dark_theme_mode_follow_wallpaper, ThemeManager.THEME_FOLLOW_WALLPAPER)
            addEntry(R.string.theme_dark_theme_mode_follow_system, ThemeManager.THEME_FOLLOW_NIGHT_MODE)
            addEntry(R.string.theme_dark_theme_mode_follow_daylight, ThemeManager.THEME_FOLLOW_DAYLIGHT)
            addEntry(R.string.theme_dark_theme_mode_on, ThemeManager.THEME_DARK)
            addEntry(R.string.theme_dark_theme_mode_off, 0)
        }
    }

    override fun onAttached() {
        super.onAttached()

        prefs.addOnPreferenceChangeListener("pref_launcherTheme", this)
    }

    override fun onDetached() {
        super.onDetached()

        prefs.removeOnPreferenceChangeListener("pref_launcherTheme", this)
    }

    override fun onValueChanged(key: String, prefs: OmegaPreferences, force: Boolean) {
        value = "${prefs.launcherTheme and ThemeManager.THEME_DARK_MASK}"
    }

    override fun persistString(value: String): Boolean {
        val newFlag = prefs.launcherTheme
                .removeFlag(ThemeManager.THEME_DARK_MASK)
                .addFlag(value.toInt())
        if (prefs.launcherTheme != newFlag) {
            prefs.launcherTheme = newFlag
        }
        return true
    }

}
