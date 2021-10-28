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
import android.widget.RemoteViews
import com.android.launcher3.R

class ClockWidgetCreator(context: Context) {
    private val mContext: Context = context
    private val timeFormat = "k:mm"
    private val dateFormat = "EEE, MMM d"

    fun createWidgetRemoteView(): RemoteViews {
        val views = RemoteViews(mContext.packageName, R.layout.clock_widget)

        //set clock and date format
        views.setCharSequence(R.id.appwidget_clock, "setFormat24Hour", timeFormat);
        views.setCharSequence(R.id.appwidget_date, "setFormat24Hour", dateFormat);

        return views
    }
}