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
import com.saggitt.omega.compose.navigation.LocalNavController
import com.saggitt.omega.compose.objects.PageItem
import com.saggitt.omega.preferences.PagePreference
import com.saggitt.omega.theme.OmegaAppTheme

@Composable
fun MainPrefsPage() {
    val context = LocalContext.current
    val navController = LocalNavController.current
    val prefs = Utilities.getOmegaPrefs(context)
    val uiPrefs = listOf(
        PageItem.PrefsProfile,
        PageItem.PrefsDesktop,
        PageItem.PrefsDock,
        PageItem.PrefsDrawer
    )
    val featuresPrefs = listOf(
        PageItem.PrefsWidgetsNotifications,
        PageItem.PrefsSearchFeed,
        PageItem.PrefsGesturesDash
    )
    val otherPrefs = listOfNotNull(
        PageItem.PrefsBackup,
        PageItem.PrefsDesktopMode,
        if (prefs.developerOptionsEnabled) PageItem.PrefsDeveloper
        else null
    )

    val composer = @Composable { page: PageItem ->
        PagePreference(titleId = page.titleId, iconId = page.iconId) {
            navController.navigate(page.route)
        }
    }

    OmegaAppTheme {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                PreferenceGroup(stringResource(id = R.string.pref_category__interfaces)) {
                    uiPrefs.forEach { composer(it) }
                }
            }
            item {
                PreferenceGroup(stringResource(id = R.string.pref_category__features)) {
                    featuresPrefs.forEach { composer(it) }
                }
            }
            item {
                PreferenceGroup(stringResource(id = R.string.pref_category__others)) {
                    otherPrefs.forEach { composer(it) }
                }
            }
        }
    }
}