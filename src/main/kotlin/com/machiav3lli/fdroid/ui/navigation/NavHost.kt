package com.machiav3lli.fdroid.ui.navigation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.activity
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.machiav3lli.fdroid.database.DatabaseX
import com.machiav3lli.fdroid.ui.activities.PrefsActivityX
import com.machiav3lli.fdroid.ui.fragments.Source
import com.machiav3lli.fdroid.ui.pages.ExplorePage
import com.machiav3lli.fdroid.ui.pages.InstalledPage
import com.machiav3lli.fdroid.ui.pages.LatestPage
import com.machiav3lli.fdroid.ui.pages.PrefsOtherPage
import com.machiav3lli.fdroid.ui.pages.PrefsPersonalPage
import com.machiav3lli.fdroid.ui.pages.PrefsReposPage
import com.machiav3lli.fdroid.ui.pages.PrefsUpdatesPage
import com.machiav3lli.fdroid.ui.viewmodels.MainNavFragmentViewModelX
import com.machiav3lli.fdroid.ui.viewmodels.RepositoriesViewModelX

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController
) =
    AnimatedNavHost(
        modifier = modifier,
        navController = navController,
        startDestination = NavItem.Explore.destination
    ) {
        slideDownComposable(NavItem.Explore.destination) {
            val viewModel = viewModel<MainNavFragmentViewModelX>(
                factory = MainNavFragmentViewModelX.Factory(
                    DatabaseX.getInstance(navController.context),
                    Source.AVAILABLE,
                    Source.AVAILABLE,
                )
            )
            ExplorePage(viewModel)
        }
        slideDownComposable(route = NavItem.Latest.destination) {
            val viewModel = viewModel<MainNavFragmentViewModelX>(
                factory = MainNavFragmentViewModelX.Factory(
                    DatabaseX.getInstance(navController.context),
                    Source.UPDATED,
                    Source.NEW,
                )
            )
            LatestPage(viewModel)
        }
        slideDownComposable(NavItem.Installed.destination) {
            val viewModel = viewModel<MainNavFragmentViewModelX>(
                factory = MainNavFragmentViewModelX.Factory(
                    DatabaseX.getInstance(navController.context),
                    Source.INSTALLED,
                    Source.UPDATES,
                )
            )
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
    navController: NavHostController
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
        slideDownComposable(NavItem.ReposPrefs.destination) {
            val viewModel = viewModel<RepositoriesViewModelX>(
                factory = RepositoriesViewModelX.Factory(
                    DatabaseX.getInstance(navController.context).repositoryDao
                )
            )
            PrefsReposPage(viewModel)
        }
        slideDownComposable(NavItem.OtherPrefs.destination) {
            PrefsOtherPage()
        }
    }

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.slideDownComposable(
    route: String,
    composable: @Composable (AnimatedVisibilityScope.(NavBackStackEntry) -> Unit)
) {
    composable(
        route,
        enterTransition = { slideInVertically { height -> -height } + fadeIn() },
        exitTransition = { slideOutVertically { height -> height } + fadeOut() }
    ) {
        composable(it)
    }
}