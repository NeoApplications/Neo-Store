package com.saggitt.omega.compose.screens.preferences

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.saggitt.omega.compose.components.PreferenceGroup
import com.saggitt.omega.preferences.BasePreferences
import com.saggitt.omega.preferences.SeekBarPreference
import com.saggitt.omega.preferences.SwitchPreference
import com.saggitt.omega.theme.OmegaAppTheme

@Composable
fun WidgetsPrefsPage() {
    val context = LocalContext.current
    val prefs = Utilities.getOmegaPrefs(context)
    val smartspacePrefs = listOf(
        prefs.smartspaceEnable,
        prefs.smartspaceDate,
        prefs.smartspaceTime,
        prefs.smartspaceTimeAbove,
        prefs.smartspaceTime24H,
        prefs.smartspaceUsePillQsb,
        prefs.smartspaceWeatherProvider, // TODO
        prefs.smartspaceWeatherUnit, // TODO
        prefs.smartspaceEventProviders // TODO
    )
    val notificationsPrefs = listOf(
        // TODO Missing enable notification badge
        prefs.notificationCount,
        prefs.notificationCustomColor,
        prefs.notificationBackground
    )

    val composer = @Composable { pref: Any ->
        when (pref) {
            is BasePreferences.BooleanPref -> SwitchPreference(pref = pref)
            is BasePreferences.FloatPref -> SeekBarPreference(pref = pref)
        }
    }

    OmegaAppTheme {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // TODO Add Smartspace preview
            item {
                PreferenceGroup(stringResource(id = R.string.title__general_smartspace)) {
                    smartspacePrefs.forEach { composer(it) }
                }
            }
            item {
                PreferenceGroup(stringResource(id = R.string.pref_category__notifications)) {
                    notificationsPrefs.forEach { composer(it) }
                }
            }
        }
    }
}