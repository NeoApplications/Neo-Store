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
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.graphics.drawable.Drawable
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import androidx.appcompat.content.res.AppCompatResources
import com.android.launcher3.R
import com.saggitt.omega.dash.DashControlProvider

class Location(context: Context) : DashControlProvider(context) {
    override val name = context.getString(R.string.dash_location)
    override val description = context.getString(R.string.dash_location_summary)
    var locationManager =
        context.getSystemService(LOCATION_SERVICE) as LocationManager

    override val icon: Drawable?
        get() = AppCompatResources.getDrawable(context, R.drawable.ic_location).apply {
            this?.setTint(darkenColor(accentColor))
        }

    override var state: Boolean
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            locationManager.isLocationEnabled
        } else {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
            )
        }
        set(value) {
            context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
}