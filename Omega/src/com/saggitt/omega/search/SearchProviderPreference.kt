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

package com.saggitt.omega.search

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.preference.DialogPreference
import com.android.launcher3.R
import com.saggitt.omega.OmegaPreferences
import com.saggitt.omega.util.omegaPrefs

class SearchProviderPreference(context: Context, attrs: AttributeSet?) : DialogPreference(context, attrs),
        OmegaPreferences.OnPreferenceChangeListener {

    var value = ""
    var defaultValue = ""

    init {
        layoutResource = R.layout.preference_preview_icon
        updateIcon()
    }

    override fun onAttached() {
        super.onAttached()
        context.omegaPrefs.addOnPreferenceChangeListener("pref_globalSearchProvider", this)
    }

    override fun onDetached() {
        super.onDetached()
        context.omegaPrefs.removeOnPreferenceChangeListener("pref_globalSearchProvider", this)
    }

    override fun onValueChanged(key: String, prefs: OmegaPreferences, force: Boolean) {
        if (key == this.key) {
            value = getPersistedString(defaultValue)
            notifyChanged()
        }
    }

    override fun getSummary(): CharSequence {
        updateIcon()
        return SearchProviderController.getInstance(context).searchProvider.name
    }

    private fun updateIcon() {
        try {
            icon = SearchProviderController.getInstance(context).searchProvider.getIcon()
        } catch (ex: Exception) {
        }
    }

    override fun onSetInitialValue(restorePersistedValue: Boolean, defaultValue: Any?) {
        value = if (restorePersistedValue) {
            getPersistedString(defaultValue as String?) ?: ""
        } else {
            defaultValue as String? ?: ""
        }
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): String? {
        defaultValue = a.getString(index)!!
        return defaultValue
    }

    override fun getDialogLayoutResource() = R.layout.dialog_preference_recyclerview

}