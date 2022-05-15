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

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.android.launcher3.R
import com.android.launcher3.util.ComponentKey
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.saggitt.omega.OmegaLauncher
import com.saggitt.omega.compose.components.ListItemWithIcon
import com.saggitt.omega.compose.navigation.Routes
import com.saggitt.omega.compose.screens.*
import com.saggitt.omega.data.IconOverrideRepository
import com.saggitt.omega.data.IconPickerItem
import com.saggitt.omega.iconpack.EditIconActivity.Companion.EXTRA_ENTRY
import com.saggitt.omega.theme.OmegaAppTheme
import com.saggitt.omega.util.Config
import com.saggitt.omega.util.isDark
import kotlinx.coroutines.launch

class EditIconActivity : AppCompatActivity() {

    val packs = IconPackProvider.INSTANCE.get(this).getIconPackList()
    private val isFolder by lazy { intent.getBooleanExtra(EXTRA_FOLDER, false) }
    private val component by lazy {
        if (intent.hasExtra(EXTRA_COMPONENT)) {
            ComponentKey(
                intent.getParcelableExtra(EXTRA_COMPONENT),
                intent.getParcelableExtra(EXTRA_USER)
            )
        } else null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = intent.getStringExtra(EXTRA_TITLE)
        setContent {
            OmegaAppTheme {
                IconEditNavController(this, app, component, packs, isFolder)
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

@Composable
fun IconEditNavController(
    mActivity: AppCompatActivity,
    appInfo:String?,
    componentKey:ComponentKey?,
    iconPacks:List<IconPackInfo>,
    isFolder: Boolean=false){
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.EditIconMainScreen.route) {
        composable(route = Routes.EditIconMainScreen.route) {
            mActivity.title = appInfo
            EditIconScreen(mActivity, appInfo, componentKey, iconPacks, isFolder, navController = navController)
        }

        composable(route = Routes.IconListScreen.route) {
            IconListScreen()
        }
    }

}