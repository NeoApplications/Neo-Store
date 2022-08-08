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

package com.saggitt.omega.gestures.actions

import android.content.Context
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import com.android.launcher3.R
import com.saggitt.omega.gestures.GestureController
import com.saggitt.omega.gestures.handlers.SleepGestureHandler
import com.saggitt.omega.gestures.handlers.SleepMethodDeviceAdmin
import com.saggitt.omega.gestures.handlers.SleepMethodPieAccessibility
import org.json.JSONObject

class SleepAction(context: Context, config: JSONObject?) : GestureAction(context, config) {
    override val displayName: String = context.getString(R.string.action_sleep)
    override val icon = AppCompatResources.getDrawable(context, R.drawable.ic_sleep)

    override fun onGestureTrigger(controller: GestureController, view: View?) {
        method!!.sleep(controller)
    }

    // Preferred methods should appear earlier in the list
    private val method: SleepGestureHandler.SleepMethod? by lazy {
        listOf(
                SleepMethodPieAccessibility(context),
                SleepMethodDeviceAdmin(context)
        ).firstOrNull { it.supported }
    }

    override val isAvailable = true // At least the device admin method is always going to work
}
