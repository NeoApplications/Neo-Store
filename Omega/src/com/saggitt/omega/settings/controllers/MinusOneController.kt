/*
 *     This file is part of Lawnchair Launcher.
 *
 *     Lawnchair Launcher is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Lawnchair Launcher is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Lawnchair Launcher.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saggitt.omega.settings.controllers

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.text.TextUtils
import androidx.annotation.Keep
import androidx.preference.Preference
import com.android.launcher3.R
import com.saggitt.omega.preferences.PreferenceController
import com.saggitt.omega.smartspace.FeedBridge

@Keep
class MinusOneController(context: Context) : PreferenceController(context) {

    override val title get() = getDisplayGoogleTitle()

    override val onChange = Preference.OnPreferenceChangeListener { pref, newValue ->
        if (newValue == true && !FeedBridge.getInstance(context).isInstalled()) {
            pref.preferenceManager.showDialog(pref)
            false
        } else {
            true
        }
    }

    @SuppressLint("StringFormatInvalid")
    private fun getDisplayGoogleTitle(): String {
        var charSequence: CharSequence? = null
        try {
            val resourcesForApplication = context.packageManager.getResourcesForApplication("com.google.android.googlequicksearchbox")
            val identifier = resourcesForApplication.getIdentifier("title_google_home_screen", "string", "com.google.android.googlequicksearchbox")
            if (identifier != 0) {
                charSequence = resourcesForApplication.getString(identifier)
            }
        } catch (ex: PackageManager.NameNotFoundException) {
        }

        if (TextUtils.isEmpty(charSequence)) {
            charSequence = context.getString(R.string.google_app)
        }
        return context.getString(R.string.title_show_google_app, charSequence)
    }
}