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
import android.content.Intent
import android.graphics.drawable.Drawable
import android.view.View
import com.android.launcher3.R
import com.saggitt.omega.gestures.GestureController
import org.json.JSONObject

abstract class GestureAction(val context: Context, val config: JSONObject?) {
    abstract val displayName: String
    open val requiresForeground: Boolean = false
    open val hasConfig = false
    open val configIntent: Intent? = null
    open val isAvailable: Boolean = true
    open val icon: Drawable? = null
    open val iconResource: Intent.ShortcutIconResource
            by lazy { Intent.ShortcutIconResource.fromContext(context, R.mipmap.ic_launcher) }

    abstract fun onGestureTrigger(controller: GestureController, view: View? = null)

    open fun getOnCompleteRunnable(controller: GestureController): Runnable? {
        return Runnable { }
    }

    protected open fun saveConfig(config: JSONObject) {}
    open fun onConfigResult(data: Intent?) {}
    open fun onDestroy() {}

    override fun toString(): String {
        return JSONObject().apply {
            put("class", this@GestureAction::class.java.name)
            if (hasConfig) {
                val config = JSONObject()
                saveConfig(config)
                put("config", config)
            }
        }.toString()
    }

    companion object {
        fun getLauncherActions(context: Context, hasBlank: Boolean) =
                mutableListOf(
                        LauncherSettingsAction(context, null),
                        DeviceSettingsAction(context, null),
                        OpenDashAction(context, null),
                        OpenDrawerAction(context, null),
                        NotificationsOpenAction(context, null),
                        OpenFeedAction(context, null),
                        OpenWidgetsAction(context, null),
                        StartGlobalSearchAction(context, null),
                        StartAppSearchAction(context, null),
                        OpenOverviewAction(context, null),
                        SleepAction(context, null),
                ).apply {
                    if (hasBlank) {
                        add(0, BlankGestureAction(context, null))
                    }
                }
    }
}