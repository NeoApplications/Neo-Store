/*
 * This file is part of Neo Launcher
 * Copyright (c) 2022   Neo Launcher Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saggitt.omega.iconpack

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.android.launcher3.util.ComponentKey
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.saggitt.omega.compose.navigation.LocalNavController
import com.saggitt.omega.compose.navigation.Routes
import com.saggitt.omega.compose.screens.EditIconScreen
import com.saggitt.omega.compose.screens.IconListScreen
import com.saggitt.omega.theme.OmegaAppTheme

class EditIconActivity : AppCompatActivity() {

    private val isFolder by lazy { intent.getBooleanExtra(EXTRA_FOLDER, false) }
    private val component by lazy {
        if (intent.hasExtra(EXTRA_COMPONENT)) {
            ComponentKey(
                intent.getParcelableExtra(EXTRA_COMPONENT),
                intent.getParcelableExtra(EXTRA_USER)
            )
        } else null
    }

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = intent.getStringExtra(EXTRA_TITLE)
        setContent {
            OmegaAppTheme {
                val navController = rememberAnimatedNavController()
                IconEditNavController(this, app, component, isFolder, navController)
            }
        }
    }

    companion object {
        const val EXTRA_ENTRY = "entry"
        const val EXTRA_TITLE = "title"
        const val EXTRA_COMPONENT = "component"
        const val EXTRA_USER = "user"
        const val EXTRA_FOLDER = "is_folder"

        fun newIntent(
            context: Context,
            title: String,
            isFolder: Boolean,
            componentKey: ComponentKey? = null
        ): Intent {
            return Intent(context, EditIconActivity::class.java).apply {
                putExtra(EXTRA_TITLE, title)
                componentKey?.run {
                    putExtra(EXTRA_COMPONENT, componentName)
                    putExtra(EXTRA_USER, user)
                    putExtra(EXTRA_FOLDER, isFolder)
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun IconEditNavController(
    mActivity: AppCompatActivity,
    appInfo: String?,
    componentKey: ComponentKey?,
    isFolder: Boolean = false,
    navController: NavHostController
) {
    CompositionLocalProvider(
        LocalNavController provides navController
    ) {
        AnimatedNavHost(
            navController = navController,
            startDestination = Routes.EditIconMainScreen.route
        ) {
            composable(route = Routes.EditIconMainScreen.route) {
                mActivity.title = appInfo
                EditIconScreen(
                    mActivity,
                    appInfo,
                    componentKey,
                    isFolder,
                    navController = navController
                )
            }

            composable(
                route = Routes.IconListScreen.route,
                arguments = listOf(
                    navArgument("iconPackName") {
                        defaultValue = ""
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                backStackEntry.arguments?.getString("iconPackName")?.let {
                    IconListScreen(it)
                }
            }
        }
    }
}