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

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import com.android.launcher3.R
import com.saggitt.omega.dash.DashControlProvider

class Bluetooth(context: Context) : DashControlProvider(context) {
    override val name = context.getString(R.string.dash_bluetooth)
    override val description = context.getString(R.string.dash_bluetooth_summary)

    override val icon: Drawable?
        get() = AppCompatResources.getDrawable(context, R.drawable.ic_bluetooth).apply {
            this?.setTint(darkenColor(accentColor))
        }

    override var state: Boolean
        get() =
            BluetoothAdapter.getDefaultAdapter()?.isEnabled == true
        set(value) {
            if (value) {
                BluetoothAdapter.getDefaultAdapter()?.enable()
            } else {
                BluetoothAdapter.getDefaultAdapter()?.disable()
            }
        }
}