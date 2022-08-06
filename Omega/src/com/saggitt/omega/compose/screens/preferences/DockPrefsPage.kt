package com.saggitt.omega.compose.screens.preferences

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.android.launcher3.Utilities
import com.saggitt.omega.compose.components.PreferenceBuilder
import com.saggitt.omega.theme.OmegaAppTheme

@Composable
fun DockPrefsPage() {
    val context = LocalContext.current
    val prefs = Utilities.getOmegaPrefs(context)
    val dockPrefs = listOf(
        prefs.dockHide,
        prefs.dockBackground,
        prefs.dockBackgroundColor,
        prefs.dockOpacity,
        prefs.dockScale,
        prefs.dockNumIcons,
        prefs.dockSearchBar,
    )

    OmegaAppTheme {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items = dockPrefs) {
                PreferenceBuilder(it)
            }
        }
    }
}