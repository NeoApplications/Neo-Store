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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import androidx.annotation.Keep
import com.android.launcher3.R
import com.saggitt.omega.smartspace.OmegaSmartspaceController
import com.saggitt.omega.util.dayOfYear
import com.saggitt.omega.util.hourOfDay
import java.util.*
import kotlin.math.abs
import kotlin.random.Random

@Keep
class PersonalityProvider(controller: OmegaSmartspaceController) :
        OmegaSmartspaceController.DataProvider(controller) {
    private val updateInterval = 60 * 1000
    private val timeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            onUpdate()
        }
    }

    var time = currentTime()
    var randomIndex = 0
    val isMorning get() = time.hourOfDay in 5 until 9
    val isEvening get() = time.hourOfDay in 19 until 21
    val isNight get() = time.hourOfDay in 22 until 24 || time.hourOfDay in 0 until 4
    val morningGreeting get() = morningStrings[randomIndex % morningStrings.size]
    val eveningGreeting get() = eveningStrings[randomIndex % eveningStrings.size]
    val nightGreeting get() = nightStrings[randomIndex % nightStrings.size]

    private val morningStrings = controller.context.resources.getStringArray(R.array.greetings_morning)
    private val eveningStrings = controller.context.resources.getStringArray(R.array.greetings_evening)
    private val nightStrings = controller.context.resources.getStringArray(R.array.greetings_night)

    private val handler = Handler(Looper.getMainLooper())
    private val onUpdateRunnable = ::onUpdate

    private fun currentTime() = Calendar.getInstance()

    override fun startListening() {
        super.startListening()
        context.registerReceiver(
                timeReceiver,
                IntentFilter().apply {
                    addAction(Intent.ACTION_DATE_CHANGED)
                    addAction(Intent.ACTION_TIME_CHANGED)
                    addAction(Intent.ACTION_TIMEZONE_CHANGED)
                })
        onUpdate()
    }

    private fun onUpdate() {
        time = currentTime()
        randomIndex = abs(Random(time.dayOfYear).nextInt())
        updateData(null, getEventCard())

        val now = System.currentTimeMillis()
        handler.removeCallbacks(onUpdateRunnable)
        handler.postDelayed(onUpdateRunnable, updateInterval - now % updateInterval)
    }

    override fun stopListening() {
        super.stopListening()
        handler.removeCallbacks(onUpdateRunnable)
    }

    private fun getEventCard(): OmegaSmartspaceController.CardData? {
        val lines = mutableListOf<OmegaSmartspaceController.Line>()
        when {
            isMorning -> lines.add(OmegaSmartspaceController.Line(morningGreeting))
            isEvening -> lines.add(OmegaSmartspaceController.Line(eveningGreeting))
            isNight -> lines.add(OmegaSmartspaceController.Line(nightGreeting))
            else -> return null
        }
        return OmegaSmartspaceController.CardData(
                lines = lines,
                forceSingleLine = true)
    }
}