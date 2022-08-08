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
import com.android.launcher3.R
import com.saggitt.omega.gestures.GestureController
import org.json.JSONObject

class StartAppSearchAction(context: Context, config: JSONObject?) :
    OpenDrawerAction(context, config) {
    override val displayName: String = context.getString(R.string.action_app_search)
    override val requiresForeground = false
    override val icon = R.drawable.ic_search
    override val iconResource: Intent.ShortcutIconResource
            by lazy {
                Intent.ShortcutIconResource.fromContext(
                    context,
                    R.drawable.ic_search
                )
            }

    override fun getOnCompleteRunnable(controller: GestureController): Runnable {
        return Runnable { controller.launcher.appsView.searchUiManager.startSearch() }
    }
}