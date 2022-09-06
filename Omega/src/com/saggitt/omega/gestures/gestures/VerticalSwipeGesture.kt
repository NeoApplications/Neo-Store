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

import com.android.launcher3.LauncherState
import com.android.launcher3.LauncherState.ALL_APPS
import com.saggitt.omega.gestures.Gesture
import com.saggitt.omega.gestures.GestureController
import com.saggitt.omega.gestures.handlers.NotificationsOpenGestureHandler
import com.saggitt.omega.gestures.handlers.StartGlobalSearchGestureHandler
import com.saggitt.omega.gestures.handlers.StateChangeGestureHandler
import com.saggitt.omega.gestures.handlers.VerticalSwipeGestureHandler

class VerticalSwipeGesture(controller: GestureController) : Gesture(controller) {

    override val isEnabled = true

    private val swipeUpHandler by controller.launcher.prefs.gestureSwipeUp
    private val dockSwipeUpHandler by controller.launcher.prefs.gestureDockSwipeUp
    private val swipeDownHandler by controller.launcher.prefs.gestureSwipeDown

    val customSwipeUp get() = controller.createGestureHandler(swipeUpHandler) !is VerticalSwipeGestureHandler
    val customDockSwipeUp get() = controller.createGestureHandler(dockSwipeUpHandler) !is VerticalSwipeGestureHandler
    val customSwipeDown get() = controller.createGestureHandler(swipeDownHandler) !is NotificationsOpenGestureHandler

    val swipeUpAppsSearch get() = controller.createGestureHandler(swipeUpHandler) is StartGlobalSearchGestureHandler
    val dockSwipeUpAppsSearch get() = controller.createGestureHandler(dockSwipeUpHandler) is StartGlobalSearchGestureHandler

    fun onSwipeUp() {
        controller.createGestureHandler(swipeUpHandler).onGestureTrigger(controller)
    }

    fun onDockSwipeUp() {
        controller.createGestureHandler(dockSwipeUpHandler).onGestureTrigger(controller)
    }

    fun onSwipeDown() {
        controller.createGestureHandler(swipeDownHandler).onGestureTrigger(controller)
    }

    fun onSwipeUpAllAppsComplete(fromDock: Boolean) {
        if (if (fromDock) dockSwipeUpAppsSearch else swipeUpAppsSearch) {
            controller.launcher.appsView.searchUiManager.startSearch()
        }
    }

    fun getTargetState(fromDock: Boolean): LauncherState {
        return if (fromDock) {
            (dockSwipeUpHandler as? StateChangeGestureHandler)?.getTargetState() ?: ALL_APPS
        } else {
            (swipeUpHandler as? StateChangeGestureHandler)?.getTargetState() ?: ALL_APPS
        }
    }
}
