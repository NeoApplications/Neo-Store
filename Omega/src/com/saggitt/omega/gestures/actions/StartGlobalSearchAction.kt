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
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.android.launcher3.R
import com.saggitt.omega.gestures.GestureController
import com.saggitt.omega.search.SearchProviderController
import org.json.JSONObject

class StartGlobalSearchAction(context: Context, config: JSONObject?) : GestureAction(context, config) {
    private val searchProvider get() = SearchProviderController.getInstance(context).searchProvider
    override val displayName: String = context.getString(R.string.action_global_search)
    override val icon: Drawable? by lazy { searchProvider.icon }
    override val requiresForeground = false

    override fun onGestureTrigger(controller: GestureController, view: View?) {
        searchProvider.startSearch {
            try {
                if (context !is AppCompatActivity) {
                    it.flags = it.flags or Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(it)
            } catch (e: Exception) {
                Log.e("LauncherGestureHandler", "Failed to start global search", e)
            }
        }
    }
}