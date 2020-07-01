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
import androidx.preference.Preference

open class PreferenceController(val context: Context) {

    open val title: String? = null
    open val summary: String? = null
    open val onClick: Preference.OnPreferenceClickListener? = null
    open val onChange: Preference.OnPreferenceChangeListener? = null
    open val isVisible = true

    open fun onPreferenceAdded(preference: Preference): Boolean {
        if (!isVisible) {
            preference.parent?.removePreference(preference)
            return false
        }
        title?.let { preference.title = it }
        summary?.let { preference.summary = it }
        onClick?.let { preference.onPreferenceClickListener = it }
        onChange?.let { preference.onPreferenceChangeListener = it }
        return true
    }

    companion object {

        fun create(context: Context, controllerClass: String?): PreferenceController? {
            if (controllerClass?.startsWith(".") == true) {
                return create(context, "com.saggitt.omega.settings.controllers$controllerClass")
            }
            return try {
                Class.forName(controllerClass!!).getConstructor(Context::class.java)
                        .newInstance(context) as PreferenceController
            } catch (t: Throwable) {
                null
            }
        }
    }
}
