package com.machiav3lli.fdroid.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.machiav3lli.fdroid.ui.pages.MainPage
import com.machiav3lli.fdroid.ui.pages.PermissionsPage
import com.machiav3lli.fdroid.ui.pages.PrefsPage

@Composable
fun AppNavDisplay(
    backStack: NavBackStack<NavRoute>,
    modifier: Modifier = Modifier,
) = NavDisplay(
    modifier = modifier,
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryProvider = entryProvider {
        // TODO add conditional to avoid PermissionsPage when not needed
        fadeInEntry<NavRoute.Permissions> {
            PermissionsPage { backStack.add(it) }
        }
        slideInEntry<NavRoute.Main> { key ->
            MainPage(
                pageIndex = key.page,
                navigator = { backStack.add(it) },
            )
        }
        slideInEntry<NavRoute.Prefs> { key ->
            PrefsPage(
                pageIndex = key.page,
                navigateUp = { backStack.removeLastOrNull() },
            )
        }
    }
)

inline fun <reified K : NavRoute> EntryProviderScope<NavRoute>.slideInEntry(
    noinline content: @Composable (K) -> Unit,
) {
    entry<K>(
        metadata = NavDisplay.transitionSpec {
            slideInHorizontally(tween(1000)) { it } togetherWith
                    slideOutHorizontally(tween(1000)) { -it }
        } + NavDisplay.popTransitionSpec {
            slideInHorizontally(tween(1000)) { -it } togetherWith
                    slideOutHorizontally(tween(1000)) { it }
        } + NavDisplay.predictivePopTransitionSpec {
            slideInHorizontally(tween(1000)) { -it } togetherWith
                    slideOutHorizontally(tween(1000)) { it }
        }
    ) {
        content(it)
    }
}

inline fun <reified K : NavRoute> EntryProviderScope<NavRoute>.fadeInEntry(
    noinline content: @Composable (K) -> Unit,
) {
    entry<K>(
        metadata = NavDisplay.transitionSpec {
            fadeIn(tween(1000), 0.3f) togetherWith
                    fadeOut(tween(1000), 0.3f)
        } + NavDisplay.popTransitionSpec {
            fadeIn(tween(1000), 0.3f) togetherWith
                    fadeOut(tween(1000), 0.3f)
        } + NavDisplay.predictivePopTransitionSpec {
            fadeIn(tween(1000), 0.3f) togetherWith
                    fadeOut(tween(1000), 0.3f)
        }
    ) {
        content(it)
    }
}

fun MutableList<NavRoute>.navigateUnique(key: NavRoute) {
    val lastKey = lastOrNull()
    if (lastKey != null && lastKey == key) return
    removeAll { existing -> existing::class == key::class }
    add(key)
}