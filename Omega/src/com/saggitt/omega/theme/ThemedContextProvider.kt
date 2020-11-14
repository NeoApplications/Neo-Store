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

package com.saggitt.omega.theme

import android.content.Context
import android.view.ContextThemeWrapper

class ThemedContextProvider(private val base: Context, var listener: Listener?, themeSet: ThemeOverride.ThemeSet)
    : ThemeOverride.ThemeOverrideListener {

    override val isAlive = true

    private val themeOverride = ThemeOverride(themeSet, this)

    private var currentTheme = themeOverride.getTheme(base)
        set(value) {
            if (field != value) {
                field = value
                themedContext = ContextThemeWrapper(base, value)
                listener?.onThemeChanged()
            }
        }
    private var themedContext = ContextThemeWrapper(base, currentTheme)

    fun startListening() {
        ThemeManager.getInstance(base).addOverride(themeOverride)
    }

    fun stopListening() {
        ThemeManager.getInstance(base).removeOverride(themeOverride)
    }

    override fun reloadTheme() {
        currentTheme = themeOverride.getTheme(base)
    }

    override fun applyTheme(themeRes: Int) {
        currentTheme = themeOverride.getTheme(base)
    }

    fun get() = themedContext

    interface Listener {

        fun onThemeChanged()
    }
}