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

package com.saggitt.omega.compose

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavHostController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.launcher3.R
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.android.material.snackbar.Snackbar
import com.saggitt.omega.backup.BackupFile
import com.saggitt.omega.backup.BackupListAdapter
import com.saggitt.omega.backup.RestoreBackupFragment
import com.saggitt.omega.compose.navigation.DefaultComposeView
import com.saggitt.omega.compose.navigation.Routes
import com.saggitt.omega.icons.prefs
import com.saggitt.omega.preferences.OmegaPreferences
import com.saggitt.omega.theme.OmegaAppTheme
import com.saggitt.omega.theme.ThemeManager
import com.saggitt.omega.theme.ThemeOverride
import com.saggitt.omega.util.omegaPrefs

/*
    Blank activity to handle Compose calls
 */
class ComposeActivity : AppCompatActivity(), ThemeManager.ThemeableActivity {

    override var currentTheme = 0
    override var currentAccent = 0
    private lateinit var themeOverride: ThemeOverride
    private val themeSet: ThemeOverride.ThemeSet get() = ThemeOverride.Settings()
    private var paused = false
    lateinit var navController: NavHostController

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themeOverride = ThemeOverride(themeSet, this)
        themeOverride.applyTheme(this)
        currentAccent = omegaPrefs.themeAccentColor.onGetValue()
        currentTheme = themeOverride.getTheme(this)
        theme.applyStyle(
            resources.getIdentifier(
                Integer.toHexString(currentAccent),
                "style",
                packageName
            ), true
        )
        if (supportActionBar != null) {
            supportActionBar?.hide()
        }
        setContent {
            OmegaAppTheme {
                navController = rememberAnimatedNavController()
                DefaultComposeView(navController)
            }
        }
    }

    override fun onThemeChanged(forceUpdate: Boolean) {
        if (currentTheme == themeOverride.getTheme(this) && !forceUpdate) return
        if (paused) {
            recreate()
        } else {
            val currentRoute = navController.currentDestination?.route ?: "${Routes.PREFS_MAIN}/"
            navController.popBackStack()
            navController.navigate(currentRoute)
        }
    }

    companion object {

        fun createIntent(context: Context, destination: String): Intent {
            val uri = "android-app://androidx.navigation//$destination".toUri()
            Log.d("ComposeActivity", "Creating intent for $uri")
            return Intent(Intent.ACTION_VIEW, uri, context, ComposeActivity::class.java)
        }

        fun getFragmentManager(context: Context): FragmentManager {
            return (context as ComposeActivity).supportFragmentManager
        }
    }
}