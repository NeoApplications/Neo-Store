package com.saggitt.omega.preferences.custom

import android.content.Context
import android.util.AttributeSet
import androidx.preference.ListPreference
import com.android.launcher3.R

class ThemePreference(context: Context, attrs: AttributeSet?) :
    ListPreference(context, attrs) {
    init {
        // @machiav3lli: For now I'll include all the different variations in the list preference.
        // If needed we can later add CheckBoxPreference(s) to manage the dimensions Dark Text and/or AMOLED
        entries = context.resources.getStringArray(R.array.themes)
        entryValues = context.resources.getStringArray(R.array.themeValues)
    }
}