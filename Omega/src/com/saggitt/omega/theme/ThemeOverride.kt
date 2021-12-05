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
import androidx.appcompat.app.AppCompatActivity
import com.android.launcher3.R
import java.lang.ref.WeakReference

class ThemeOverride(private val themeSet: ThemeSet, val listener: ThemeOverrideListener?) {
    constructor(themeSet: ThemeSet, activity: AppCompatActivity) : this(
        themeSet,
        ActivityListener(activity)
    )

    constructor(themeSet: ThemeSet, context: Context) : this(themeSet, ContextListener(context))

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
        override val lightTheme = R.style.AppTheme
        override val darkTheme = R.style.AppTheme_Dark
        override val blackTheme = R.style.AppTheme_Black
    }

    class Settings : ThemeSet {
        override val lightTheme = R.style.SettingsTheme_Light
        override val darkTheme = R.style.SettingsTheme_Dark
        override val blackTheme = R.style.SettingsTheme_Black
    }

    interface ThemeSet {
        val lightTheme: Int
        val darkTheme: Int
        val blackTheme: Int

        fun getTheme(context: Context): Int {
            return getTheme(ThemeManager.getInstance(context).getCurrentFlags())
        }

        fun getTheme(themeFlags: Int): Int {
            val isBlack = ThemeManager.isBlack(themeFlags)
            val isDark = ThemeManager.isDark(themeFlags)
            return when {
                isBlack -> blackTheme
                isDark -> darkTheme
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