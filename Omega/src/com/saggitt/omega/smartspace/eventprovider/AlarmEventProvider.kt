/*
 *     This file is part of Lawnchair Launcher.
 *
 *     Lawnchair Launcher is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Lawnchair Launcher is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Lawnchair Launcher.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saggitt.omega.smartspace.eventprovider

import android.app.AlarmManager
import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.os.SystemClock
import android.util.Log
import androidx.annotation.Keep
import com.android.launcher3.R
import com.android.launcher3.Utilities.drawableToBitmap
import com.saggitt.omega.smartspace.OmegaSmartspaceController
import com.saggitt.omega.util.formatTime
import com.saggitt.omega.util.runOnMainThread
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

@Keep
class AlarmEventProvider(controller: OmegaSmartspaceController) :
        OmegaSmartspaceController.DataProvider(controller) {

    private val handlerThread by lazy { HandlerThread("") }
    private val handler by lazy { Handler(handlerThread.looper) }

    init {
        Log.d(javaClass.name, "class initializer: init")
        handlerThread.start();
        forceUpdate()
    }

    private fun updateInformation() = runOnMainThread {
        val alarmManager =
                controller.context.getSystemService(Context.ALARM_SERVICE) as AlarmManager;
        if (alarmManager.nextAlarmClock != null &&
                alarmManager.nextAlarmClock!!.triggerTime - System.currentTimeMillis() <= TimeUnit.MINUTES.toMillis(
                        30)) {
            val alarmClock = alarmManager.nextAlarmClock!!
            val string: MutableList<OmegaSmartspaceController.Line> = ArrayList();
            string.add(OmegaSmartspaceController.Line(
                    controller.context.getString(R.string.resuable_text_alarm)));
            string.add(OmegaSmartspaceController.Line(
                    formatTime(Date(alarmClock.triggerTime), controller.context)))
            updateData(null, OmegaSmartspaceController.CardData(
                    drawableToBitmap(controller.context.getDrawable(R.drawable.ic_alarm_on_black_24dp)),
                    string, true))
        } else {
            updateData(null, null)
        }
    }

    override fun forceUpdate() {
        updateInformation()
        handler.postAtTime(this::forceUpdate, SystemClock.uptimeMillis() + TimeUnit.SECONDS.toMillis(5))
    }
}
