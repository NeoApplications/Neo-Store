package com.machiav3lli.fdroid.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.machiav3lli.fdroid.ui.pages.AppPage
import com.machiav3lli.fdroid.ui.pages.MainPage
import com.machiav3lli.fdroid.ui.pages.PermissionsPage
import com.machiav3lli.fdroid.ui.pages.PrefsPage
import com.machiav3lli.fdroid.ui.pages.SearchPage
import com.machiav3lli.fdroid.ui.pages.SortFilterSheet

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AppNavDisplay(
    backStack: NavBackStack<NavRoute>,
    modifier: Modifier = Modifier,
) {
    val listDetailStrategy = rememberNeoSceneStrategy<NavRoute>()

    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        sceneStrategy = listDetailStrategy,
        transitionSpec = {
            slideInHorizontally(tween(600)) { it } togetherWith
                    slideOutHorizontally(tween(600)) { -it }
        },
        popTransitionSpec = {
            slideInHorizontally(tween(600)) { -it } togetherWith
                    slideOutHorizontally(tween(600)) { it }
        },
        predictivePopTransitionSpec = {
            slideInHorizontally(tween(600)) { -it } togetherWith
                    slideOutHorizontally(tween(600)) { it }
        },
        entryProvider = entryProvider {
            // TODO add conditional to avoid PermissionsPage when not needed
            fadeInEntry<NavRoute.Permissions> {
                PermissionsPage {
                    backStack.navigateUnique(it)
                    backStack.remove(NavRoute.Permissions)
                }
            }
            slideInEntry<NavRoute.Main>(
                metadata = ListDetailScene.listPane()
            ) { key ->
                MainPage(
                    pageIndex = key.page,
                    navigator = { backStack.navigateUnique(it) },
                )
            }
            slideInEntry<NavRoute.Prefs> { key ->
                PrefsPage(
                    pageIndex = key.page,
                    navigateUp = { backStack.removeLastOrNull() },
                )
            }
            slideInEntry<NavRoute.App>(
                metadata = ListDetailScene.detailPane()
            ) { key ->
                AppPage(key.packageName) {
                    backStack.removeLastOrNull()
                }
            }
            slideInEntry<NavRoute.SortFilter>(
                metadata = BottomSheetScene.bottomSheet()
            ) { key ->
                SortFilterSheet(key.page) {
                    backStack.removeLastOrNull()
                }
            }
            slideInEntry<NavRoute.Search>(
                metadata = ListDetailScene.listPane()
            ) {
                SearchPage {
                    backStack.removeLastOrNull()
                }
            }
        }
    )
}

inline fun <reified K : NavRoute> EntryProviderScope<NavRoute>.slideInEntry(
    metadata: Map<String, Any> = emptyMap(),
    noinline content: @Composable (K) -> Unit,
) {
    entry<K>(
        metadata = metadata + NavDisplay.transitionSpec {
            slideInHorizontally(tween(600)) { it } togetherWith
                    slideOutHorizontally(tween(600)) { -it }
        } + NavDisplay.popTransitionSpec {
            slideInHorizontally(tween(600)) { -it } togetherWith
                    slideOutHorizontally(tween(600)) { it }
        } + NavDisplay.predictivePopTransitionSpec {
            slideInHorizontally(tween(600)) { -it } togetherWith
                    slideOutHorizontally(tween(600)) { it }
        }
    ) {
        content(it)
    }
}

inline fun <reified K : NavRoute> EntryProviderScope<NavRoute>.fadeInEntry(
    metadata: Map<String, Any> = emptyMap(),
    noinline content: @Composable (K) -> Unit,
) {
    entry<K>(
        metadata = metadata + NavDisplay.transitionSpec {
            fadeIn(tween(400), 0.3f) togetherWith
                    fadeOut(tween(400), 0.3f)
        } + NavDisplay.popTransitionSpec {
            fadeIn(tween(400), 0.3f) togetherWith
                    fadeOut(tween(400), 0.3f)
        } + NavDisplay.predictivePopTransitionSpec {
            fadeIn(tween(400), 0.3f) togetherWith
                    fadeOut(tween(400), 0.3f)
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

fun MutableList<NavRoute>.navigate(key: NavRoute) {
    val lastKey = lastOrNull()
    if (lastKey != null && lastKey == key) return
    remove(key)
    add(key)
}