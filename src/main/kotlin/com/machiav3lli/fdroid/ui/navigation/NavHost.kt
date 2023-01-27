package com.machiav3lli.fdroid.ui.navigation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.activity
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import com.machiav3lli.fdroid.ui.activities.PrefsActivityX

@OptIn(ExperimentalAnimationApi::class, ExperimentalPagerApi::class)
@Composable
fun MainNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    pagerState: PagerState,
    pages: List<NavItem>,
) =
    AnimatedNavHost(
        modifier = modifier,
        navController = navController,
        startDestination = NavItem.Main.destination,
    ) {
        slideDownComposable(NavItem.Main.destination) {
            SlidePager(
                pageItems = pages,
                pagerState = pagerState
            )
        }
        activity(NavItem.Prefs.destination) {
            this.activityClass = PrefsActivityX::class
        }
    }

@OptIn(ExperimentalAnimationApi::class, ExperimentalPagerApi::class)
@Composable
fun PrefsNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    pagerState: PagerState,
    pages: List<NavItem>,
) =
    AnimatedNavHost(
        modifier = modifier,
        navController = navController,
        startDestination = NavItem.Prefs.destination,
    ) {
        slideDownComposable(NavItem.Prefs.destination) {
            SlidePager(
                pageItems = pages,
                pagerState = pagerState
            )
        }
    }

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.slideDownComposable(
    route: String,
    args: List<NamedNavArgument> = emptyList(),
    composable: @Composable (AnimatedVisibilityScope.(NavBackStackEntry) -> Unit),
) {
    composable(
        route,
        args,
        enterTransition = { slideInVertically { height -> -height } + fadeIn() },
        exitTransition = { slideOutVertically { height -> height } + fadeOut() }
    ) {
        composable(it)
    }
}