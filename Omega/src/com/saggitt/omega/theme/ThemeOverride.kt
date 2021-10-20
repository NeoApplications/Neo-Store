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

package com.saggitt.omega.theme

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.android.launcher3.R
import java.lang.ref.WeakReference

class ThemeOverride(private val themeSet: ThemeSet, val listener: ThemeOverrideListener?) {
    constructor(themeSet: ThemeSet, activity: AppCompatActivity) : this(themeSet, ActivityListener(activity))

    val isAlive get() = listener?.isAlive == true

    fun applyTheme(context: Context) {
        listener?.applyTheme(getTheme(context))
    }

    fun applyTheme(themeFlags: Int) {
        listener?.applyTheme(getTheme(themeFlags))
    }

    fun getTheme(context: Context): Int {
        return themeSet.getTheme(context)
    }

    fun getTheme(themeFlags: Int) = themeSet.getTheme(themeFlags)

    fun onThemeChanged(themeFlags: Int) {
        listener?.reloadTheme()
    }

    class Launcher : ThemeSet {

        /*Light Theme*/
        override val lightTheme = R.style.AppTheme
        override val darkTextTheme = R.style.AppTheme_DarkText
        override val darkMainColorTheme = R.style.AppTheme_DarkMainColor

        /*Dark Theme*/
        override val darkTheme = R.style.AppTheme_Dark
        override val darkDarkTextTheme = R.style.AppTheme_Dark_DarkText
        override val darkDarkMainColorTheme = R.style.AppTheme_Dark_DarkMainColor
    }

    class Settings : ThemeSet {
        override val lightTheme = R.style.SettingsTheme_Light
        override val darkTextTheme = R.style.SettingsTheme_Light
        override val darkTheme = R.style.SettingsTheme_Dark
        override val darkDarkTextTheme = R.style.SettingsTheme_Dark
    }
    interface ThemeSet {

        val lightTheme: Int
        val darkTextTheme: Int
        val darkMainColorTheme get() = lightTheme
        val darkTheme: Int
        val darkDarkTextTheme: Int
        val darkDarkMainColorTheme get() = darkTheme

        fun getTheme(context: Context): Int {
            return getTheme(ThemeManager.getInstance(context).getCurrentFlags())
        }

        fun getTheme(themeFlags: Int): Int {
            val isDark = ThemeManager.isDark(themeFlags)
            val isDarkText = ThemeManager.isDarkText(themeFlags)
            val isDarkMainColor = ThemeManager.isDarkMainColor(themeFlags)
            return when {
                isDark && isDarkMainColor -> darkDarkMainColorTheme
                isDark && isDarkText -> darkDarkTextTheme
                isDark -> darkTheme
                isDarkMainColor -> darkMainColorTheme
                isDarkText -> darkTextTheme
                else -> lightTheme
            }
        }
    }

    interface ThemeOverrideListener {
        val isAlive: Boolean
        fun applyTheme(themeRes: Int)
        fun reloadTheme()
    }

    class ActivityListener(activity: AppCompatActivity) : ThemeOverrideListener {
        private val activityRef = WeakReference(activity)
        override val isAlive = activityRef.get() != null

        override fun applyTheme(themeRes: Int) {
            activityRef.get()?.setTheme(themeRes)
        }

        override fun reloadTheme() {
            activityRef.get()?.recreate()
        }
    }

    class ContextListener(context: Context) : ThemeOverrideListener {
        private val contextRef = WeakReference(context)
        override val isAlive = contextRef.get() != null

        override fun applyTheme(themeRes: Int) {
            contextRef.get()?.setTheme(themeRes)
        }
        override fun reloadTheme() {
            // Unsupported
        }
    }

}