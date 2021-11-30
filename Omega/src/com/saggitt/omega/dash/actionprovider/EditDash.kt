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
import com.android.launcher3.R
import com.saggitt.omega.dash.DashActionProvider
import com.saggitt.omega.settings.SettingsActivity

class EditDash(context: Context) : DashActionProvider(context) {
    override val itemId = 5
    override val name = context.getString(R.string.edit_dash)
    override val description = context.getString(R.string.edit_dash_summary)

    override val icon: Drawable?
        get() = AppCompatResources.getDrawable(context, R.drawable.ic_edit_dash).apply {
            this?.setTint(darkenColor(accentColor))
        }

    override fun runAction(context: Context) {
        val fragment = "com.saggitt.omega.dash.DashFragment"
        SettingsActivity.startFragment(context, fragment, R.string.edit_dash)
    }
}