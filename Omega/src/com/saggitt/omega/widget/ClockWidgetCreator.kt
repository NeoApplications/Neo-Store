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

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.view.View
import android.widget.RemoteViews
import com.android.launcher3.R
import com.android.launcher3.Utilities
import java.util.*

class ClockWidgetCreator(val context: Context, val widgetId: Int) {
    private var dateFormat = "EEE, MMM d"
    private val timeFormat = "k:mm"
    private val prefs by lazy { Utilities.getOmegaPrefs(context) }

    fun createWidgetRemoteView(): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.clock_widget_double_line)

        //Configure the clock
        if (prefs.smartspaceTime) {
            views.setViewVisibility(R.id.appwidget_clock, View.VISIBLE)
            views.setCharSequence(R.id.appwidget_clock, getTimeFormat(), timeFormat)
        } else {
            views.setViewVisibility(R.id.appwidget_clock, View.GONE)
            views.setViewVisibility(R.id.timezones_container, View.GONE)
        }

        views.setTextViewText(R.id.appwidget_date, getFormattedDate())

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