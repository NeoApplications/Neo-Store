package com.machiav3lli.fdroid.ui.navigation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.machiav3lli.fdroid.ui.pages.MainPage
import com.machiav3lli.fdroid.ui.pages.PermissionsPage
import com.machiav3lli.fdroid.ui.pages.PrefsPage

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
) = NavHost(
    modifier = modifier,
    navController = navController,
    startDestination = NavRoute.Permissions,
) {
    fadeComposable<NavRoute.Permissions> {
        PermissionsPage(navController)
    }
    slideInComposable<NavRoute.Main> {
        val args = it.toRoute<NavRoute.Main>()

        MainPage(
            pageIndex = args.page,
            navController = navController,
        )
    }
    slideInComposable<NavRoute.Prefs> {
        val args = it.toRoute<NavRoute.Prefs>()

        PrefsPage(
            pageIndex = args.page,
            navController = navController,
        )
    }
}


inline fun <reified T : NavRoute> NavGraphBuilder.slideInComposable(
    crossinline content: @Composable (AnimatedVisibilityScope.(NavBackStackEntry) -> Unit),
) {
    composable<T>(
        enterTransition = { slideInHorizontally { width -> width } },
        exitTransition = { slideOutHorizontally { width -> -width } },
        popEnterTransition = { slideInHorizontally { width -> -width } },
        popExitTransition = { slideOutHorizontally { width -> width } },
    ) {
        content(it)
    }
}


inline fun <reified T : NavRoute> NavGraphBuilder.fadeComposable(
    crossinline content: @Composable (AnimatedVisibilityScope.(NavBackStackEntry) -> Unit),
) {
    composable<T>(
        enterTransition = { fadeIn(initialAlpha = 0.3f) },
        exitTransition = { fadeOut(targetAlpha = 0.3f) }
    ) {
        content(it)
    }
}
