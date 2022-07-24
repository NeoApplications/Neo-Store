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
package com.saggitt.omega.dash.actionprovider

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import com.android.launcher3.LauncherAppState
import com.android.launcher3.LauncherState
import com.android.launcher3.R
import com.saggitt.omega.dash.DashActionProvider

class AllAppsShortcut(context: Context) : DashActionProvider(context) {
    override val itemId = 1
    override val name = context.getString(R.string.dash_all_apps_title)
    override val description = context.getString(R.string.dash_all_apps_summary)

    override val icon: Drawable?
        get() = AppCompatResources.getDrawable(context, R.drawable.ic_apps).apply {
            this?.setTint(darkenColor(accentColor))
        }

    override fun runAction(context: Context) {
        if (LauncherAppState.getInstance(context).launcher.stateManager.state != LauncherState.ALL_APPS) {
            LauncherAppState.getInstance(context).launcher.stateManager.goToState(LauncherState.ALL_APPS)
        }
    }
}