package com.machiav3lli.fdroid.ui.navigation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
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
import com.machiav3lli.fdroid.pages.MainPage
import com.machiav3lli.fdroid.pages.PermissionsPage
import com.machiav3lli.fdroid.pages.PrefsPage

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
) =
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = NavItem.Permissions.destination,
    ) {
        fadeComposable(NavItem.Permissions.destination) {
            PermissionsPage(navController)
        }
        slideInComposable(
            "${NavItem.Main.destination}?page={page}",
            args = listOf(
                navArgument("page") {
                    type = NavType.IntType
                    defaultValue = Preferences[Preferences.Key.DefaultTab].valueString.toInt()
                }
            )
        ) {
            val args = it.arguments!!
            val pi = args.getInt("page")
            MainPage(
                pageIndex = pi,
                navController = navController,
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
            val args = it.arguments!!
            val pi = args.getInt("page")
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
