/*
 * This file is part of Omega Launcher
 * Copyright (c) 2022   Omega Launcher Team
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

package com.saggitt.omega.compose.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.annotation.ExperimentalCoilApi
import com.android.launcher3.R
import com.android.launcher3.util.Themes
import com.saggitt.omega.compose.navigation.Routes
import com.saggitt.omega.theme.OmegaAppTheme

@ExperimentalCoilApi
class AboutFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val isDark = Themes.getAttrBoolean(requireActivity(), R.attr.isMainColorDark)
        return inflater.inflate(R.layout.base_compose_fragment, container, false).apply {
            findViewById<ComposeView>(R.id.base_compose_view).setContent {
                OmegaAppTheme(isDark) {
                    AboutNavController(requireActivity())
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().title = requireActivity().getString(R.string.title__general_about)
    }
}

@OptIn(ExperimentalCoilApi::class)
@Composable
fun AboutNavController(mActivity: FragmentActivity) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.MainScreen.route) {
        composable(route = Routes.MainScreen.route) {
            mActivity.title = mActivity.getString(R.string.title__general_about)
            AboutScreen(navController = navController)
        }
        composable(route = Routes.Translators.route) {
            mActivity.title = mActivity.getString(R.string.about_translators)
            TranslatorsScreen()
        }
    }
}