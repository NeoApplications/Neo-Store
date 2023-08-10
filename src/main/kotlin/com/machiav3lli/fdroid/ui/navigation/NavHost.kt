package com.machiav3lli.fdroid.ui.navigation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.pages.PermissionsPage
import com.machiav3lli.fdroid.pages.PrefsPage
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    pagerState: PagerState,
    pages: List<NavItem>,
) =
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = NavItem.Permissions.destination,
    ) {
        fadeComposable(NavItem.Permissions.destination) {
            PermissionsPage {
                navController.navigate(NavItem.Main.destination)
            }
        }
        fadeComposable(
            "${NavItem.Main.destination}?page={page}",
            args = listOf(
                navArgument("page") {
                    type = NavType.IntType
                    defaultValue = Preferences[Preferences.Key.DefaultTab].valueString.toInt()
                }
            )
        ) {
            val scope = rememberCoroutineScope()
            val args = it.arguments!!
            val pi = args.getInt("page")
            if (pi != Preferences[Preferences.Key.DefaultTab].valueString.toInt()) pagerState.apply {
                scope.launch { scrollToPage(pi) }
            }
            SlidePager(
                pagerState = pagerState,
                pageItems = pages,
                navController = navController
            )
        }
        slideInComposable(
            "${NavItem.Prefs.destination}?page={page}",
            args = listOf(
                navArgument("page") {
                    type = NavType.IntType
                    defaultValue = Preferences[Preferences.Key.DefaultTab].valueString.toInt()
                }
            )
        ) {
            val scope = rememberCoroutineScope()
            val args = it.arguments!!
            val pi = args.getInt("page")
            if (pi != Preferences[Preferences.Key.DefaultTab].valueString.toInt()) pagerState.apply {
                scope.launch { scrollToPage(pi) }
            }
            PrefsPage(
                pageIndex = pi,
                navController = navController,
            )
        }
    }

fun NavGraphBuilder.slideInComposable(
    route: String,
    args: List<NamedNavArgument> = emptyList(),
    content: @Composable (AnimatedVisibilityScope.(NavBackStackEntry) -> Unit),
) {
    composable(
        route,
        args,
        enterTransition = { slideInHorizontally { width -> width } },
        exitTransition = { slideOutHorizontally { width -> -width } },
        popEnterTransition = { slideInHorizontally { width -> -width } },
        popExitTransition = { slideOutHorizontally { width -> width } },
    ) {
        content(it)
    }
}

fun NavGraphBuilder.fadeComposable(
    route: String,
    args: List<NamedNavArgument> = emptyList(),
    content: @Composable (AnimatedVisibilityScope.(NavBackStackEntry) -> Unit),
) {
    composable(
        route,
        args,
        enterTransition = { fadeIn(initialAlpha = 0.3f) },
        exitTransition = { fadeOut(targetAlpha = 0.3f) }
    ) {
        content(it)
    }
}
