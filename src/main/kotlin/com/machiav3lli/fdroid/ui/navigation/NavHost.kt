package com.machiav3lli.fdroid.ui.navigation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.activity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.machiav3lli.fdroid.pages.PermissionsPage
import com.machiav3lli.fdroid.ui.activities.PrefsActivityX

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
        fadeComposable(NavItem.Main.destination) {
            SlidePager(
                pagerState = pagerState,
                pageItems = pages,
                navController = navController
            )
        }
        activity(NavItem.Prefs.destination) {
            this.activityClass = PrefsActivityX::class
        }
    }

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PrefsNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    pagerState: PagerState,
    pages: List<NavItem>,
) =
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = NavItem.Prefs.destination
    ) {
        fadeComposable(NavItem.Prefs.destination) {
            SlidePager(
                pagerState = pagerState,
                pageItems = pages,
                navController = navController
            )
        }
    }

fun NavGraphBuilder.slideDownComposable(
    route: String,
    args: List<NamedNavArgument> = emptyList(),
    content: @Composable (AnimatedVisibilityScope.(NavBackStackEntry) -> Unit),
) {
    composable(
        route,
        args,
        enterTransition = { slideInVertically { height -> -height } + fadeIn() },
        exitTransition = { slideOutVertically { height -> height } + fadeOut() }
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
