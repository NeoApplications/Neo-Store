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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.android.launcher3.R

class WeatherClickListenerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.saggitt.omega.ACTION_OPEN_WEATHER_INTENT") {
            try {
                context.startActivity(WidgetIntents.getWeatherIntent())
            } catch (e: Exception) {
                e.printStackTrace()
                val uri = Uri.parse("http://www.google.com/search?q=weather")
                val i = Intent(Intent.ACTION_VIEW, uri)
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                try {
                    context.startActivity(i)
                } catch (ignored: Exception) {
                    Toast.makeText(context, R.string.error_no_browser, Toast.LENGTH_SHORT).show()
                }
            }

        }
    }
}