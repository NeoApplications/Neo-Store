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
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import com.android.launcher3.LauncherState
import com.android.launcher3.R
import com.android.quickstep.SysUINavigationMode
import com.saggitt.omega.gestures.GestureController
import com.saggitt.omega.gestures.handlers.StateChangeGestureHandler
import com.saggitt.omega.gestures.handlers.VerticalSwipeGestureHandler
import org.json.JSONObject

open class OpenDrawerAction(context: Context, config: JSONObject?) : GestureAction(context, config),
        VerticalSwipeGestureHandler, StateChangeGestureHandler {

    override val displayName: String = context.getString(getNameRes())
    override val icon = AppCompatResources.getDrawable(context, R.drawable.ic_apps)
    override val iconResource: Intent.ShortcutIconResource by lazy {
        Intent.ShortcutIconResource.fromContext(
                context,
                R.drawable.ic_apps
        )
    }
    override val requiresForeground = false

    private fun getNameRes(): Int {
        return if (SysUINavigationMode.INSTANCE.get(context).mode == SysUINavigationMode.Mode.NO_BUTTON) {
            R.string.action_open_drawer_or_recents
        } else {
            R.string.action_open_drawer
        }
    }

    override fun onGestureTrigger(controller: GestureController, view: View?) {
        controller.launcher.stateManager.goToState(LauncherState.ALL_APPS)
    }

    override fun getTargetState(): LauncherState {
        return LauncherState.ALL_APPS
    }
}