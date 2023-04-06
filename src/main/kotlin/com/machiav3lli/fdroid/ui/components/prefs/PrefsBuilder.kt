package com.machiav3lli.fdroid.ui.components.prefs

import androidx.compose.runtime.Composable
import com.machiav3lli.fdroid.content.Preferences

@Composable
fun PrefsBuilder(
    prefKey: Preferences.Key<*>,
    onDialogPref: (Preferences.Key<*>) -> Unit,
    index: Int,
    size: Int
) {
    when {
        prefKey.default is Preferences.Value.BooleanValue -> SwitchPreference(
            prefKey = prefKey as Preferences.Key<Boolean>,
            index = index,
            groupSize = size,
        )
        prefKey is Preferences.Key.Language -> LanguagePreference(
            prefKey = prefKey as Preferences.Key<String>,
            index = index,
            groupSize = size,
        ) { onDialogPref(prefKey) }
        prefKey is Preferences.Key.DownloadDirectory -> LaunchPreference(
            prefKey = prefKey as Preferences.Key<String>,
            index = index,
            groupSize = size,
        ) { onDialogPref(prefKey) }
        prefKey.default is Preferences.Value.StringValue -> StringPreference(
            prefKey = prefKey as Preferences.Key<String>,
            index = index,
            groupSize = size,
        ) { onDialogPref(prefKey) }
        prefKey.default is Preferences.Value.IntValue -> IntPreference(
            prefKey = prefKey as Preferences.Key<Int>,
            index = index,
            groupSize = size,
        ) { onDialogPref(prefKey) }
        prefKey.default.value is Preferences.Enumeration<*> -> EnumPreference(
            prefKey = prefKey as Preferences.Key<Preferences.Enumeration<*>>,
            index = index,
            groupSize = size,
        ) { onDialogPref(prefKey) }
        else -> {}
    }
}