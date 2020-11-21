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
