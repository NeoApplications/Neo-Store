/*
 * Copyright (c) 2020 Omega Launcher
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
 */

package com.saggitt.omega.smartspace.eventprovider

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import android.text.TextUtils
import android.text.format.DateFormat
import androidx.core.graphics.drawable.toBitmap
import com.android.launcher3.R
import com.saggitt.omega.smartspace.OmegaSmartspaceController
import com.saggitt.omega.smartspace.OmegaSmartspaceController.*
import com.saggitt.omega.util.runOnMainThread
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.ceil

@SuppressLint("MissingPermission")
class CalendarEventProvider(controller: OmegaSmartspaceController)
    : PeriodicDataProvider(controller) {

    override val requiredPermissions = listOf(android.Manifest.permission.READ_CALENDAR)
    private val calendarProjection = arrayOf(
            CalendarContract.Instances._ID,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.DTSTART,
            CalendarContract.Instances.DTEND,
            CalendarContract.Instances.EVENT_LOCATION,
            CalendarContract.Instances.CUSTOM_APP_PACKAGE)

    private val oneMinute = TimeUnit.MINUTES.toMillis(1)

    private val includeBehind = oneMinute * 5
    private val includeAhead = oneMinute * 30

    override val timeout = oneMinute

    override fun updateData() {
        val card = createEventCard(getNextEvent())
        runOnMainThread {
            updateData(null, card)
        }
    }

    private fun createEventCard(event: CalendarEvent?): CardData? {
        if (event == null) return null
        val icon = context.getDrawable(R.drawable.ic_calendar)!!.toBitmap()
        val lines = mutableListOf<Line>()
        lines.add(Line("${event.title} ${formatTimeRelative(event.start)}", TextUtils.TruncateAt.MIDDLE))
        val timeText = "${formatTime(event.start)} – ${formatTime(event.end)}"
        if (event.location != null) {
            lines.add(Line("${event.location} $timeText"))
        } else {
            lines.add(Line(timeText))
        }
        return CardData(icon, lines, getPendingIntent(event))
    }

    private fun getNextEvent(): CalendarEvent? {
        val currentTime = System.currentTimeMillis()
        context.contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                calendarProjection,
                "${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTSTART} <= ?",
                arrayOf("${currentTime - includeBehind}", "${currentTime + includeAhead}"),
                "${CalendarContract.Events.DTSTART} ASC LIMIT 1")
                ?.use { c ->
                    while (c.moveToNext()) {
                        return CalendarEvent(
                                c.getLong(c.getColumnIndex(CalendarContract.Events._ID)),
                                c.getString(c.getColumnIndex(CalendarContract.Events.TITLE)),
                                c.getLong(c.getColumnIndex(CalendarContract.Events.DTSTART)),
                                c.getLong(c.getColumnIndex(CalendarContract.Events.DTEND)),
                                c.getString(c.getColumnIndex(CalendarContract.Events.EVENT_LOCATION)),
                                c.getString(c.getColumnIndex(CalendarContract.Events.CUSTOM_APP_PACKAGE)))
                    }
                }
        return null
    }

    private fun formatTime(time: Long) = DateFormat.getTimeFormat(context).format(Date(time))

    private fun formatTimeRelative(time: Long): String {
        val res = context.resources
        val currentTime = System.currentTimeMillis()
        if (time <= currentTime) {
            return res.getString(R.string.smartspace_now)
        }
        val minutesToEvent = ceil((time - currentTime).toDouble() / oneMinute).toInt()
        val timeString = if (minutesToEvent >= 60) {
            val hours = minutesToEvent / 60
            val minutes = minutesToEvent % 60
            val hoursString = res.getQuantityString(R.plurals.smartspace_hours, hours, hours)
            if (minutes <= 0) {
                hoursString
            } else {
                val minutesString =
                        res.getQuantityString(R.plurals.smartspace_minutes, minutes, minutes)
                res.getString(R.string.smartspace_hours_mins, hoursString, minutesString)
            }
        } else {
            res.getQuantityString(R.plurals.smartspace_minutes, minutesToEvent, minutesToEvent)
        }
        return res.getString(R.string.smartspace_in_time, timeString)
    }

    private fun getPendingIntent(event: CalendarEvent): PendingIntent? {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("content://com.android.calendar/events/${event.id}")
            `package` = event.appPackage
        }
        return PendingIntent.getActivity(context, 0, intent, 0)
    }

    data class CalendarEvent(
            val id: Long,
            val title: String,
            val start: Long,
            val end: Long,
            val location: String?,
            val appPackage: String?)
}
