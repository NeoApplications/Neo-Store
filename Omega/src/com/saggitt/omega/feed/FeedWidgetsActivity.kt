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
package com.saggitt.omega.feed

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import com.android.launcher3.LauncherAppState
import com.saggitt.omega.OmegaLauncher
import com.saggitt.omega.theme.OmegaAppTheme
import com.saggitt.omega.theme.ThemeManager
import com.saggitt.omega.theme.ThemeOverride
import com.saggitt.omega.theme.ThemedContextProvider

class FeedWidgetsActivity : ComponentActivity() {
    val themedContext = ThemedContextProvider(this, null, ThemeOverride.Settings()).get()
    val isDark = ThemeManager.getInstance(themedContext).isDark
    private var mAppWidgetManager: AppWidgetManager? = null
    private var mAppWidgetHost: AppWidgetHost? = null
    private val mLauncher
        get() = (LauncherAppState.getInstance(this).launcher as? OmegaLauncher)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mLauncher?.let {
            mAppWidgetManager = AppWidgetManager.getInstance(it.applicationContext)
            mAppWidgetHost = it.appWidgetHost
        }

        mLauncher?.let {
            mAppWidgetManager = AppWidgetManager.getInstance(it.applicationContext)
            mAppWidgetHost = it.appWidgetHost
        }
        setContent {
            OmegaAppTheme(isDark) {
                WidgetFeed(this)
            }
        }
    }
}

@Composable
fun WidgetFeed(context: Context) {
    Scaffold(
        content = {
            WidgetHomeContent(context)
        }
    )
}