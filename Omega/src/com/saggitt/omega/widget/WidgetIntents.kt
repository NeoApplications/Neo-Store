/*
 * This file is part of Neo Launcher
 * Copyright (c) 2022   Neo Launcher Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saggitt.omega.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import com.android.launcher3.BuildConfig
import com.saggitt.omega.OmegaLauncher
import com.saggitt.omega.util.Config
import java.util.*

class WidgetIntents {
    companion object {

        fun getWeatherIntent(): Intent {
            return Intent(Intent.ACTION_VIEW).apply {
                addCategory(Intent.CATEGORY_DEFAULT)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                data = Uri.parse("dynact://velour/weather/ProxyActivity")
                component = ComponentName(
                    Config.GOOGLE_QSB,
                    "com.google.android.apps.gsa.velour.DynamicActivityTrampoline"
                )
                setClassName(
                    Config.GOOGLE_QSB,
                    "com.google.android.apps.gsa.velour.DynamicActivityTrampoline"
                )
            }
        }

        fun getCalendarIntent(): Intent {
            val calendarUri = CalendarContract.CONTENT_URI
                .buildUpon()
                .appendPath("time")
                .appendPath(Calendar.getInstance().timeInMillis.toString())
                .build()

            return Intent(Intent.ACTION_VIEW).apply {
                data = calendarUri
            }
        }

        fun getWidgetUpdateIntent(context: Context): Intent {
            val widgetManager = AppWidgetManager.getInstance(context)
            val widgetComponent = ComponentName(context, BuildConfig.APPLICATION_ID)
            val widgetIds = widgetManager.getAppWidgetIds(widgetComponent)
            return Intent(context, OmegaLauncher::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
        }
    }
}