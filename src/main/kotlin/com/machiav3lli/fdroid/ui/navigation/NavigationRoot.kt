package com.machiav3lli.fdroid.ui.navigation

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import com.machiav3lli.fdroid.ui.pages.MainPage
import com.machiav3lli.fdroid.ui.pages.PermissionsPage
import com.machiav3lli.fdroid.ui.pages.PrefsPage

@Composable
fun NavigationRoot(
    modifier: Modifier = Modifier,
    backStack: NavBackStack
) {
    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        entryDecorators = listOf(
            rememberSavedStateNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
            rememberSceneSetupNavEntryDecorator(),
        ),
        entryProvider = { key ->
            when (key) {
                is NavRoute.Permissions
                     -> {
                    NavEntry(key = key) {
                        PermissionsPage { backStack.add(it) }
                    }
                }

                is NavRoute.Main
                     -> {
                    NavEntry(key = key) {
                        MainPage(
                            navigator = { backStack.add(it) },
                            pageIndex = key.page,
                        )
                    }
                }

                is NavRoute.Prefs
                     -> {
                    NavEntry(key = key) {
                        PrefsPage(
                            pageIndex = key.page,
                            navigateUp = { backStack.removeLastOrNull() },
                        )
                    }
                }

                else -> throw RuntimeException("Invalid NavKey.")
            }
        },
        transitionSpec = {
            // Slide in from right when navigating forward
            slideInHorizontally(initialOffsetX = { it }) togetherWith
                    slideOutHorizontally(targetOffsetX = { -it })
        },
        popTransitionSpec = {
            // Slide in from left when navigating back
            slideInHorizontally(initialOffsetX = { -it }) togetherWith
                    slideOutHorizontally(targetOffsetX = { it })
        },
        predictivePopTransitionSpec = {
            // Slide in from left when navigating back
            slideInHorizontally(initialOffsetX = { -it }) togetherWith
                    slideOutHorizontally(targetOffsetX = { it })
        },
    )
}