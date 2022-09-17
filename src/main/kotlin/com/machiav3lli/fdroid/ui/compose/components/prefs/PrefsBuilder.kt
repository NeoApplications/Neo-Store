package com.machiav3lli.fdroid.ui.compose.components.prefs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.content.PrefsDependencies

@Composable
fun PrefsBuilder(
    prefKey: Preferences.Key<*>,
    onDialogPref: (Any) -> Unit,
    enabledSetState: SnapshotStateList<Preferences.Key<*>>,
    index: Int,
    size: Int
) {
    when (prefKey.default.value) {
        is Boolean -> SwitchPreference(
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
    }
}