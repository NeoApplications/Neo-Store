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

package com.saggitt.omega.util

import android.content.ContentResolver
import android.content.Context
import android.content.res.Resources
import android.database.ContentObserver
import android.os.Handler
import android.provider.Settings
import com.android.quickstep.SysUINavigationMode

class NavigationModeCompat(context: Context) {

    private var swipeUpEnabled = false
        set(value) {
            if (field != value) {
                field = value
                notifyChange()
            }
        }
    private var fullGestureMode by context.omegaPrefs.BooleanPref("pref_fullGestureMode", false, ::notifyChange)

    var listener: Listener? = null
    val currentMode
        get() = when {
            !swipeUpEnabled -> SysUINavigationMode.Mode.THREE_BUTTONS
            fullGestureMode -> SysUINavigationMode.Mode.NO_BUTTON
            else -> SysUINavigationMode.Mode.TWO_BUTTONS
        }

    init {
        SwipeUpGestureEnabledSettingObserver(context.contentResolver).register()
    }

    private fun notifyChange() {
        listener?.onNavigationModeChange()
    }

    private inner class SwipeUpGestureEnabledSettingObserver(
            private val resolver: ContentResolver) :
            ContentObserver(Handler()) {

        private val defaultValue: Int = if (getSystemBooleanRes(SWIPE_UP_ENABLED_DEFAULT_RES_NAME)) 1 else 0

        private val value: Boolean
            get() = Settings.Secure.getInt(resolver, SWIPE_UP_SETTING_NAME, defaultValue) == 1

        fun register() {
            resolver.registerContentObserver(Settings.Secure.getUriFor(SWIPE_UP_SETTING_NAME),
                    false, this)
            swipeUpEnabled = value
        }

        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            swipeUpEnabled = value
        }

        override fun deliverSelfNotifications(): Boolean {
            return true
        }
    }

    interface Listener {

        fun onNavigationModeChange()
    }

    companion object : OmegaSingletonHolder<NavigationModeCompat>(::NavigationModeCompat) {

        private const val SWIPE_UP_ENABLED_DEFAULT_RES_NAME = "config_swipe_up_gesture_default"

        private const val SWIPE_UP_SETTING_NAME = "swipe_up_to_switch_apps_enabled"

        private fun getSystemBooleanRes(resName: String): Boolean {
            val res = Resources.getSystem()
            val resId = res.getIdentifier(resName, "bool", "android")

            return if (resId != 0) {
                res.getBoolean(resId)
            } else {
                false
            }
        }
    }
}
