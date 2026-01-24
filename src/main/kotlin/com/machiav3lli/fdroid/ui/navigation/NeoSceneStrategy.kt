package com.machiav3lli.fdroid.ui.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.ui.navigation.BottomSheetScene.Companion.BOTTOM_SHEET_KEY
import com.machiav3lli.fdroid.ui.navigation.ListDetailScene.Companion.DETAIL_KEY
import com.machiav3lli.fdroid.ui.navigation.ListDetailScene.Companion.LIST_KEY
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalMaterial3Api::class)
class NeoSceneStrategy<T : Any>(val windowSizeClass: WindowSizeClass) : SceneStrategy<T> {
    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        val lastEntry = entries.lastOrNull() ?: return null

        // Default
        if (lastEntry.metadata.keys.intersect(navRouteKeys).isEmpty())
            return null

        // BottomSheet
        val bottomSheetProperties =
            lastEntry.metadata.takeIf { it.containsKey(BOTTOM_SHEET_KEY) }
                ?.get(BOTTOM_SHEET_KEY) as? ModalBottomSheetProperties

        bottomSheetProperties?.let { properties ->
            return BottomSheetScene(
                key = lastEntry.contentKey,
                previousEntries = entries.dropLast(1),
                overlaidEntries = entries.dropLast(1),
                entry = lastEntry,
                modalBottomSheetProperties = properties,
                onBack = onBack
            )
        }

        // Detail-List
        if (!windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)
            || Preferences[Preferences.Key.DisableListDetail]
        ) return null
        val detailEntry = lastEntry
            .takeIf { it.metadata.containsKey(DETAIL_KEY) }

        val listEntry = entries
            .findLast { it.metadata.containsKey(LIST_KEY) }
            ?: return null

        return ListDetailScene(
            key = listEntry.contentKey,
            previousEntries = entries.dropLast(1),
            listEntry = listEntry,
            detailEntry = detailEntry
        )
    }

    companion object {
        val navRouteKeys = persistentListOf(
            LIST_KEY, DETAIL_KEY, BOTTOM_SHEET_KEY
        )
    }
}

@Composable
fun <T : Any> rememberNeoSceneStrategy(): NeoSceneStrategy<T> {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    return remember(windowSizeClass) {
        NeoSceneStrategy(windowSizeClass)
    }
}