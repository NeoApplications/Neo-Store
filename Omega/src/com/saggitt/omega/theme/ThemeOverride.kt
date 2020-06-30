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
import com.android.launcher3.R
import com.android.launcher3.Utilities

class ThemeOverride (private val themeSet: ThemeSet, val listener: ThemeOverrideListener?) {

    fun getTheme(context: Context): Int {
        return themeSet.getTheme(context)
    }

    fun getTheme(themeFlags: Int) = themeSet.getTheme(themeFlags)

    interface ThemeOverrideListener {

        val isAlive: Boolean

        fun applyTheme(themeRes: Int)
        fun reloadTheme()
    }

    interface ThemeSet {

        val lightTheme: Int
        val darkTextTheme: Int
        val darkTheme: Int
        val darkDarkTextTheme: Int
        val blackTheme: Int
        val blackDarkTextTheme: Int

        fun getTheme(context: Context): Int {
            return getTheme(ThemeManager.getInstance(context).getCurrentFlags())
        }

        fun getTheme(themeFlags: Int): Int {
            val isDark = ThemeManager.isDark(themeFlags)
            val isDarkText = ThemeManager.isDarkText(themeFlags)
            val isBlack = isDark && ThemeManager.isBlack(themeFlags)
            return when {
                isBlack && isDarkText -> blackDarkTextTheme
                isBlack -> blackTheme
                isDark && isDarkText -> darkDarkTextTheme
                isDark -> darkTheme
                isDarkText -> darkTextTheme
                else -> lightTheme
            }
        }
    }

    class Settings : ThemeSet {

        override val lightTheme = R.style.SettingsTheme_Light
        override val darkTextTheme = R.style.SettingsTheme_Light
        override val darkTheme = R.style.SettingsTheme_Dark
        override val darkDarkTextTheme = R.style.SettingsTheme_Dark
        override val blackTheme = R.style.SettingsTheme_Black
        override val blackDarkTextTheme = R.style.SettingsTheme_Black
    }

}