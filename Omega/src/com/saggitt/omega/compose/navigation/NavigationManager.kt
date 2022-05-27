package com.saggitt.omega.compose.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.saggitt.omega.compose.preferences.preferenceGraph
import com.saggitt.omega.compose.screens.aboutGraph
import com.saggitt.omega.compose.screens.editIconGraph
import com.saggitt.omega.compose.screens.iconPickerGraph
import soup.compose.material.motion.materialSharedAxisX

/*
sealed class Routes(val route: String) {
    object BlankScreen : Routes("android-app://androidx.navigation/")
    object AboutScreen : Routes("android-app://androidx.navigation/about")
    object Translators : Routes("android-app://androidx.navigation/about/translators")
    object Changelog : Routes("android-app://androidx.navigation/about/changelog")
    object License : Routes("android-app://androidx.navigation/about/license")

    object EditIconMainScreen : Routes("edit_icon")
    object IconListScreen :
        Routes("icon_picker?iconPackName={iconPackName}")
}

val LocalNavController = staticCompositionLocalOf<NavController> {
    error("CompositionLocal LocalNavController not present")
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalCoilApi::class)
@Composable
fun NavigationManager(mActivity: AppCompatActivity, navController: NavHostController, theme: Int = 0){
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val motionSpec = materialSharedAxisX()
    val density = LocalDensity.current
    CompositionLocalProvider(
        LocalNavController provides navController
    ) {
        AnimatedNavHost(
            navController = navController,
            startDestination = Routes.AboutScreen.route,
            enterTransition = { motionSpec.enter.transition(!isRtl, density) },
            exitTransition = { motionSpec.exit.transition(!isRtl, density) },
            popEnterTransition = { motionSpec.enter.transition(isRtl, density) },
            popExitTransition = { motionSpec.exit.transition(isRtl, density) },
        ){
            composable(route = Routes.AboutScreen.route) {
                mActivity.title = mActivity.getString(R.string.title__general_about)
                AboutScreen(navController)
            }
            composable(route = Routes.Translators.route) {
                mActivity.title = mActivity.getString(R.string.about_translators)
                TranslatorsScreen()
            }
            composable(route = Routes.License.route) {
                mActivity.title = mActivity.getString(R.string.title__about_changelog)
                LicenseScreen(theme.isDark)
            }
            composable(route = Routes.Changelog.route) {
                mActivity.title = mActivity.getString(R.string.category__about_licenses)
                ChangelogScreen(theme.isDark)
            }

            composable(route = Routes.BlankScreen.route) {
                BlankScreen()
            }
        }
    }
}

@Composable
fun BlankScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .fillMaxHeight()
            .padding(top = 26.dp, bottom = 45.dp)

    ) {
    }
}
*/

object Routes {
    const val ABOUT = "about"
    const val EDIT_ICON = "edit_icon"
    const val ICON_PICKER = "icon_picker"
}

val LocalNavController = staticCompositionLocalOf<NavController> {
    error("CompositionLocal LocalNavController not present")
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DefaultComposeView(navController: NavHostController) {
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val motionSpec = materialSharedAxisX()
    val density = LocalDensity.current
    CompositionLocalProvider(
        LocalNavController provides navController
    ) {
        AnimatedNavHost(
            navController = navController,
            startDestination = "/",
            enterTransition = { motionSpec.enter.transition(!isRtl, density) },
            exitTransition = { motionSpec.exit.transition(!isRtl, density) },
            popEnterTransition = { motionSpec.enter.transition(isRtl, density) },
            popExitTransition = { motionSpec.exit.transition(isRtl, density) },
        ) {
            preferenceGraph(route = "/", { BlankScreen() }) { subRoute ->
                aboutGraph(route = subRoute(Routes.ABOUT))
                editIconGraph(route = subRoute(Routes.EDIT_ICON))
                iconPickerGraph(route = subRoute(Routes.ICON_PICKER))
            }
        }
    }
}


@Composable
fun BlankScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .fillMaxHeight()
            .padding(top = 26.dp, bottom = 45.dp)

    ) {
    }
}