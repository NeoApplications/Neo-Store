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

package com.saggitt.omega.gestures

class NavSwipeUpGesture(controller: GestureController) : Gesture(controller) {

    override val isEnabled = true

    private val blankHandler = controller.blankGestureHandler
    private val leftHandlerPref by controller.createHandlerPref("pref_gesture_nav_swipe_up_left", blankHandler)
    private val rightHandlerPref by controller.createHandlerPref("pref_gesture_nav_swipe_up_right", blankHandler)

    val leftHandler get() = leftHandlerPref.takeUnless { it is BlankGestureHandler }
    val rightHandler get() = rightHandlerPref.takeUnless { it is BlankGestureHandler }
}