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
package com.saggitt.omega.gestures.gestures

import com.saggitt.omega.gestures.Gesture
import com.saggitt.omega.gestures.GestureController
import com.saggitt.omega.gestures.handlers.OpenDashGestureHandler

class LaunchAssistantGesture(controller: GestureController) : Gesture(controller) {

    private val handler by controller.createHandlerPref(
        "pref_gesture_launch_assistant",
        OpenDashGestureHandler(controller.launcher, null)
    )
    override val isEnabled = true

    /*get() {
        controller.launcher.baseContext.packageManager.resolveActivity(
            Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER),
            PackageManager.MATCH_DEFAULT_ONLY
        )?.let {
            return it.activityInfo.packageName == RunHandlerActivity::javaClass.name && it.isDefault
        }
        return false
    }*/

    override fun onEvent(): Boolean {
        handler.onGestureTrigger(controller)
        return true
    }
}
