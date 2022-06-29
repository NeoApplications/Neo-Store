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

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import com.android.launcher3.R
import com.saggitt.omega.gestures.GestureController
import org.json.JSONObject
import java.lang.reflect.InvocationTargetException

class NotificationsOpenAction(context: Context, config: JSONObject?) : GestureAction(context, config) {

    override val displayName = context.getString(R.string.action_open_notifications)
    override val icon = AppCompatResources.getDrawable(context, R.drawable.hotseat_edu_notification_icon)

    @SuppressLint("PrivateApi", "WrongConstant")
    override fun onGestureTrigger(controller: GestureController, view: View?) {
        try {
            Class.forName("android.app.StatusBarManager")
                    .getMethod("expandNotificationsPanel")
                    .invoke(controller.launcher.getSystemService("statusbar"))
        } catch (_: ClassNotFoundException) {
        } catch (_: NoSuchMethodException) {
        } catch (_: IllegalAccessException) {
        } catch (_: InvocationTargetException) {
        }
    }
}