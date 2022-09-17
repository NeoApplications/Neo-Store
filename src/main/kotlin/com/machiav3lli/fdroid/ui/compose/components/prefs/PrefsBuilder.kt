package com.machiav3lli.fdroid.ui.compose.components.prefs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.content.PrefsDependencies

@Composable
fun PrefsBuilder(
    prefKey: Preferences.Key<*>,
    onDialogPref: (Preferences.Key<*>) -> Unit,
    enabledSetState: SnapshotStateList<Preferences.Key<*>>,
    index: Int,
    size: Int
) {
    when {
        prefKey.default is Preferences.Value.BooleanValue -> SwitchPreference(
            prefKey = prefKey as Preferences.Key<Boolean>,
            index = index,
            groupSize = size,
            enabled = enabledSetState.contains(prefKey)
        ) {
            val dependents =
                PrefsDependencies.entries.filter { it.value == prefKey }.map { it.key }.toSet()
            if (it) enabledSetState.addAll(dependents)
            else enabledSetState.removeAll(dependents)
        }
        prefKey.default is Preferences.Value.StringValue -> StringPreference(
            prefKey = prefKey as Preferences.Key<String>,
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