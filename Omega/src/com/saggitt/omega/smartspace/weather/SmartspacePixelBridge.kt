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

package com.saggitt.omega.smartspace.weather

import android.os.Handler
import android.util.Log
import com.google.android.apps.nexuslauncher.smartspace.ISmartspace
import com.google.android.apps.nexuslauncher.smartspace.SmartspaceController
import com.google.android.apps.nexuslauncher.smartspace.SmartspaceDataContainer
import com.saggitt.omega.smartspace.OmegaSmartspaceController
import com.saggitt.omega.smartspace.SmartspaceDataWidget
import com.saggitt.omega.util.makeBasicHandler


class SmartspacePixelBridge(controller: OmegaSmartspaceController) :
        OmegaSmartspaceController.DataProvider(controller), ISmartspace, Runnable {

    private val smartspaceController = SmartspaceController.get(controller.context)
    private val handler = makeBasicHandler(true)
    private var data: SmartspaceDataContainer? = null
    private var ds = false

    override fun startListening() {
        super.startListening()

        updateData(null, null)
        smartspaceController.da(this)
    }

    override fun stopListening() {
        super.stopListening()
        smartspaceController.da(null)
    }

    override fun onGsaChanged() {
        ds = smartspaceController.cY()
        if (data != null) {
            cr(data)
        } else {
            Log.d("SmartspacePixelBridge", "onGsaChanged but no data present")
        }
    }

    override fun cr(data: SmartspaceDataContainer?) {
        this.data = data?.also { initListeners(it) }
    }

    private fun initListeners(e: SmartspaceDataContainer) {
        val weatherData: OmegaSmartspaceController.WeatherData? = if (e.isWeatherAvailable) {
            SmartspaceDataWidget.parseWeatherData(e.dO.icon, e.dO.title)
        } else {
            null
        }
        val cardData: OmegaSmartspaceController.CardData? = if (e.cS()) {
            val dp = e.dP
            OmegaSmartspaceController.CardData(dp.icon, dp.title, dp.cx(true), dp.cy(), dp.cx(false))
        } else {
            null
        }

        handler.removeCallbacks(this)
        if (e.cS() && e.dP.cv()) {
            val cw = e.dP.cw()
            var min = 61000L - System.currentTimeMillis() % 60000L
            if (cw > 0L) {
                min = Math.min(min, cw)
            }
            handler.postDelayed(this, min)
        }

        updateData(weatherData, cardData)
    }

    override fun run() {
        data?.let { initListeners(it) }
    }
}

