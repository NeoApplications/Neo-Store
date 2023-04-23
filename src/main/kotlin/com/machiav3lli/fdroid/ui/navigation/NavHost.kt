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
import androidx.navigation.NavType
import androidx.navigation.activity
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.machiav3lli.fdroid.MainApplication
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.pages.ExplorePage
import com.machiav3lli.fdroid.pages.InstalledPage
import com.machiav3lli.fdroid.pages.LatestPage
import com.machiav3lli.fdroid.pages.PermissionsPage
import com.machiav3lli.fdroid.pages.PrefsOtherPage
import com.machiav3lli.fdroid.pages.PrefsPersonalPage
import com.machiav3lli.fdroid.pages.PrefsReposPage
import com.machiav3lli.fdroid.pages.PrefsUpdatesPage
import com.machiav3lli.fdroid.ui.activities.PrefsActivityX

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
) =
    AnimatedNavHost(
        modifier = modifier,
        navController = navController,
        startDestination = NavItem.Permissions.destination,
    ) {
        fadeComposable(NavItem.Permissions.destination) {
            PermissionsPage {
                navController.navigate(Preferences[Preferences.Key.DefaultTab].valueString)
            }
        }
        fadeComposable(NavItem.Explore.destination) {
            val viewModel = MainApplication.mainActivity?.exploreViewModel!!
            ExplorePage(viewModel)
        }
        fadeComposable(route = NavItem.Latest.destination) {
            val viewModel = MainApplication.mainActivity?.latestViewModel!!
            LatestPage(viewModel)
        }
        fadeComposable(NavItem.Installed.destination) {
            val viewModel = MainApplication.mainActivity?.installedViewModel!!
            InstalledPage(viewModel)
        }
        activity(NavItem.Prefs.destination) {
            this.activityClass = PrefsActivityX::class
        }
    }

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PrefsNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
) =
    AnimatedNavHost(
        modifier = modifier,
        navController = navController,
        startDestination = NavItem.PersonalPrefs.destination
    ) {
        fadeComposable(NavItem.PersonalPrefs.destination) {
            PrefsPersonalPage()
        }
        fadeComposable(NavItem.UpdatesPrefs.destination) {
            PrefsUpdatesPage()
        }
        fadeComposable(
            "${NavItem.ReposPrefs.destination}?address={address}?fingerprint={fingerprint}",
            args = listOf(
                navArgument("address") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("fingerprint") {
                    type = NavType.StringType
                    defaultValue = ""
                },
            )
        ) {
            val viewModel = MainApplication.prefsActivity?.prefsViewModel!!
            val args = it.arguments!!
            val address = args.getString("address") ?: ""
            val fingerprint = args.getString("fingerprint")?.uppercase() ?: ""
            PrefsReposPage(viewModel, address, fingerprint)
        }
        fadeComposable(NavItem.OtherPrefs.destination) {
            val viewModel = MainApplication.prefsActivity?.prefsViewModel!!
            PrefsOtherPage(viewModel)
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

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.fadeComposable(
    route: String,
    args: List<NamedNavArgument> = emptyList(),
    composable: @Composable (AnimatedVisibilityScope.(NavBackStackEntry) -> Unit),
) {
    composable(
        route,
        args,
        enterTransition = { fadeIn(initialAlpha = 0.3f) },
        exitTransition = { fadeOut(targetAlpha = 0.3f) }
    ) {
        composable(it)
    }
}
