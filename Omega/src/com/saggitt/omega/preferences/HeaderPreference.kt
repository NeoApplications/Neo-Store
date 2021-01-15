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

package com.saggitt.omega.preferences

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.android.launcher3.R
import com.android.launcher3.R.id.app_build
import com.saggitt.omega.util.AboutUtils

class HeaderPreference(context: Context, attrs: AttributeSet? = null) : Preference(context, attrs) {
    val aboutUtils by lazy { AboutUtils(context) }

    init {
        layoutResource = R.layout.activity_about
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder?) {
        super.onBindViewHolder(holder)
        val version: TextView = holder?.findViewById(R.id.app_version) as TextView
        version.text = context.resources.getString(R.string.app_version) + ": " + aboutUtils.appVersionName

        val build: TextView = holder.findViewById(app_build) as TextView
        build.text = context.resources.getString(R.string.app_build) + ": " + aboutUtils.appVersionCode
    }
}
