package com.saggitt.omega.compose.components

import androidx.compose.runtime.Composable
import com.saggitt.omega.preferences.BasePreferences
import com.saggitt.omega.preferences.SeekBarPreference
import com.saggitt.omega.preferences.SwitchPreference

val PreferenceBuilder = @Composable { pref: Any ->
    when (pref) {
        is BasePreferences.BooleanPref -> SwitchPreference(pref = pref)
        is BasePreferences.FloatPref -> SeekBarPreference(pref = pref)
        is BasePreferences.ColorIntPref -> ColorIntPreference(pref = pref)
    }
}