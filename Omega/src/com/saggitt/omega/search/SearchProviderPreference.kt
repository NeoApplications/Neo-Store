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