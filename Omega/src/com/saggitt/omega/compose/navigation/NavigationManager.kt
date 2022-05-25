package com.saggitt.omega.compose.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.saggitt.omega.compose.preferences.preferenceGraph
import com.saggitt.omega.compose.screens.aboutGraph
import com.saggitt.omega.compose.screens.editIconGraph
import com.saggitt.omega.compose.screens.iconPickerGraph

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
fun DefaultComposeView() {
    val navController = rememberAnimatedNavController()
    CompositionLocalProvider(
        LocalNavController provides navController
    ) {
        AnimatedNavHost(
            navController = navController,
            startDestination = "/"
        ) {
            preferenceGraph(route = "/", { }) { subRoute ->
                aboutGraph(route = subRoute(Routes.ABOUT))
                editIconGraph(route = subRoute(Routes.EDIT_ICON))
                iconPickerGraph(route = subRoute(Routes.ICON_PICKER))
            }
        }
    }
}