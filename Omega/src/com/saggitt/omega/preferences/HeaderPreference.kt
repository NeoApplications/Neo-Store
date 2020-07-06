/*
 *  Copyright (c) 2020 Omega Launcher
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
 *
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
