package com.saggitt.omega.preferences.custom

import android.content.Context
import android.util.AttributeSet
import androidx.preference.ListPreference
import com.android.launcher3.R

class SortAppsPreference(context: Context, attrs: AttributeSet?) :
    ListPreference(context, attrs) {
    init {
        // Is there any special cases to handle?
        entries = context.resources.getStringArray(R.array.sortMode)
        entryValues = context.resources.getStringArray(R.array.sortModeValues)
    }
}