/*
 *  This file is part of Omega Launcher
 *  Copyright (c) 2021   Saul Henriquez
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
package com.saggitt.omega.dash.controlprovider

import android.content.Context
import android.content.Context.WIFI_SERVICE
import android.content.Intent
import android.net.wifi.WifiManager
import android.provider.Settings
import com.android.launcher3.R
import com.saggitt.omega.dash.DashControlProvider

class Wifi(context: Context) : DashControlProvider(context) {
    override val itemId = 17
    override val name = context.getString(R.string.dash_wifi)
    override val description = context.getString(R.string.dash_wifi_summary)
    override val extendable = true
    override val icon = R.drawable.ic_wifi

    private var wifiManager: WifiManager =
        context.getSystemService(WIFI_SERVICE) as WifiManager

    override var state: Boolean
        get() =
            wifiManager.isWifiEnabled
        set(value) {
            // wifiManager.setWifiEnabled(value) this can be used when targetSDK < Q
            context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
        }
}