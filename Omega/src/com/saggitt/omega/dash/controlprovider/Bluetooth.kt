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

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import com.android.launcher3.R
import com.saggitt.omega.dash.DashControlProvider

class Bluetooth(context: Context) : DashControlProvider(context) {
    override val itemId = 13
    override val name = context.getString(R.string.dash_bluetooth)
    override val description = context.getString(R.string.dash_bluetooth_summary)

    override val icon: Drawable?
        get() = AppCompatResources.getDrawable(context, R.drawable.ic_bluetooth).apply {
            this?.setTint(darkenColor(accentColor))
        }

    override var state: Boolean
        get() =
            (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter?.isEnabled == true
        set(value) {
            if (value) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        ActivityCompat.requestPermissions(
                            context as Activity,
                            arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                            34
                        )
                    }
                    return
                }
                (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter?.enable()
            } else {
                (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter?.disable()
            }
        }
}