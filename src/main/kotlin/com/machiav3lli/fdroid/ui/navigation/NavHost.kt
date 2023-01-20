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
import com.machiav3lli.fdroid.ui.activities.PrefsActivityX
import com.machiav3lli.fdroid.ui.pages.ExplorePage
import com.machiav3lli.fdroid.ui.pages.InstalledPage
import com.machiav3lli.fdroid.ui.pages.LatestPage
import com.machiav3lli.fdroid.ui.pages.PrefsOtherPage
import com.machiav3lli.fdroid.ui.pages.PrefsPersonalPage
import com.machiav3lli.fdroid.ui.pages.PrefsReposPage
import com.machiav3lli.fdroid.ui.pages.PrefsUpdatesPage

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
) =
    AnimatedNavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Preferences[Preferences.Key.DefaultTab].valueString
    ) {
        slideDownComposable(NavItem.Explore.destination) {
            val viewModel = MainApplication.mainActivity?.exploreViewModel!!
            ExplorePage(viewModel)
        }
        slideDownComposable(route = NavItem.Latest.destination) {
            val viewModel = MainApplication.mainActivity?.latestViewModel!!
            LatestPage(viewModel)
        }
        slideDownComposable(NavItem.Installed.destination) {
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
        slideDownComposable(NavItem.PersonalPrefs.destination) {
            PrefsPersonalPage()
        }
        slideDownComposable(NavItem.UpdatesPrefs.destination) {
            PrefsUpdatesPage()
        }
        slideDownComposable(
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
            val fingerprint = args.getString("fingerprint") ?: ""
            PrefsReposPage(viewModel, address, fingerprint)
        }
        slideDownComposable(NavItem.OtherPrefs.destination) {
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