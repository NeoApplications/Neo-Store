/*
 *  This file is part of Omega Launcher
 *  Copyright (c) 2021   Omega Launcher Team
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

package com.saggitt.omega.widget

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.provider.AlarmClock
import android.view.View
import android.widget.RemoteViews
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.saggitt.omega.smartspace.SmartSpaceDataWidget
import java.util.*

class ClockWidgetCreator(val context: Context, val widgetId: Int) {
    private var dateFormat = "EEE, MMM d"
    private val timeFormat = "k:mm"
    private val prefs by lazy { Utilities.getOmegaPrefs(context) }

    fun createWidgetRemoteView(appWidgetId: Int): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.clock_widget_double_line)

        // Clock
        if (prefs.smartspaceTime) {
            views.setViewVisibility(R.id.appwidget_clock, View.VISIBLE)
            views.setCharSequence(R.id.appwidget_clock, getTimeFormat(), timeFormat)

            val clockIntent = PendingIntent.getActivity(
                context,
                widgetId,
                Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                },
                0
            )
            views.setOnClickPendingIntent(R.id.appwidget_clock, clockIntent)


        } else {
            views.setViewVisibility(R.id.appwidget_clock, View.GONE)
            views.setViewVisibility(R.id.timezones_container, View.GONE)
        }

        // Calendar
        val calendarIntent = PendingIntent.getActivity(
            context,
            widgetId,
            WidgetIntents.getCalendarIntent(),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        views.setOnClickPendingIntent(R.id.appwidget_date, calendarIntent)
        views.setTextViewText(R.id.appwidget_date, getFormattedDate())

        // Weather
        val intent = Intent(context, WeatherClickListenerReceiver::class.java)
        intent.action = "com.saggitt.omega.ACTION_OPEN_WEATHER_INTENT"
        val weatherIntent = PendingIntent.getBroadcast(context, widgetId, intent, 0)

        if (prefs.weatherProvider == SmartSpaceDataWidget::class.java.name) {
            //TODO: Change to visible when there is a weather provider
            views.setViewVisibility(R.id.weather_container, View.GONE)
            views.setOnClickPendingIntent(R.id.title_weather_icon, weatherIntent)
            views.setOnClickPendingIntent(R.id.title_weather_text, weatherIntent)
        } else {
            views.setViewVisibility(R.id.weather_container, View.GONE)
        }

        val refreshIntent = PendingIntent.getActivity(
            context,
            appWidgetId,
            WidgetIntents.getWidgetUpdateIntent(context),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        views.setOnClickPendingIntent(R.id.main_layout, refreshIntent)

        return views
    }

    private fun getTimeFormat(): String {
        return if (prefs.smartspaceTime24H)
            "setFormat24Hour"
        else
            "setFormat12Hour"
    }

    private fun getFormattedDate(): String {
        val now = Calendar.getInstance().apply {
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return SimpleDateFormat(dateFormat, Locale.getDefault()).format(now.time)
    }
}