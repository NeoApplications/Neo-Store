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

package com.saggitt.omega.compose.screens

import android.app.Activity
import android.content.Intent
import android.content.pm.LauncherApps
import android.graphics.drawable.Drawable
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TabRowDefaults.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.getSystemService
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.android.launcher3.util.ComponentKey
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.google.accompanist.navigation.animation.composable
import com.saggitt.omega.compose.components.ListItemWithIcon
import com.saggitt.omega.compose.navigation.LocalNavController
import com.saggitt.omega.compose.navigation.OnResult
import com.saggitt.omega.compose.navigation.Routes
import com.saggitt.omega.compose.preferences.preferenceGraph
import com.saggitt.omega.data.IconOverrideRepository
import com.saggitt.omega.data.IconPickerItem
import com.saggitt.omega.iconpack.IconPack
import com.saggitt.omega.iconpack.IconPackProvider
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.editIconGraph(route: String) {
    preferenceGraph(route, { }) { subRoute ->
        composable(
            route = subRoute("{packageName}/{nameAndUser}"),
            arguments = listOf(
                navArgument("packageName") { type = NavType.StringType },
                navArgument("nameAndUser") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val args = backStackEntry.arguments!!
            val packageName = args.getString("packageName")
            val nameAndUser = args.getString("nameAndUser")
            val key = ComponentKey.fromString("$packageName/$nameAndUser")!!
            EditIconScreen(key)
        }
    }
}

@Composable
fun EditIconScreen(
    componentKey: ComponentKey
) {
    val context = LocalContext.current
    val iconPacks = IconPackProvider.INSTANCE.get(context).getIconPackList()
    val isFolder = componentKey.componentName.packageName.contains("com.saggitt.omega.folder")
    val navController = LocalNavController.current
    val launcherApps = context.getSystemService<LauncherApps>()!!
    val intent = Intent().setComponent(componentKey.componentName)
    val activity = launcherApps.resolveActivity(intent, componentKey.user)
    val originalIcon: Drawable = activity.getIcon(context.resources.displayMetrics.densityDpi)

    val title = remember(componentKey) {
        activity.label.toString()
    }

    val scope = rememberCoroutineScope()
    val repo = IconOverrideRepository.INSTANCE.get(context)
    OnResult<IconPickerItem> { item ->
        scope.launch {
            repo.setOverride(componentKey, item)
            (context as Activity).let {
                it.setResult(Activity.RESULT_OK)
                it.finish()
            }
        }
    }

    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxHeight()
            .fillMaxWidth()

    ) {
        Text(
            text = title,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 24.sp,
            textAlign = TextAlign.Center
        )

        val scrollState = rememberScrollState()

        Row(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(8f))
                .horizontalScroll(scrollState)

        ) {
            //Original Icon
            Image(
                painter = rememberDrawablePainter(drawable = originalIcon),
                contentDescription = null,
                modifier = Modifier.requiredSize(60.dp)
            )

            Divider(
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(start = 16.dp, end = 16.dp)
                    .width(1.dp)
            )

            //Package Icons
            val iconDpi = LocalContext.current.resources.configuration.densityDpi
            val ip = IconPackProvider.INSTANCE.get(LocalContext.current)

            if (isFolder) {
                //TODO: Add support for custom folder icons
            } else {
                iconPacks.forEach {
                    val pack: IconPack? = ip.getIconPackOrSystem(it.packageName)
                    if (pack != null) {
                        pack.loadBlocking()
                        val iconEntry = pack.getIcon(componentKey.componentName)
                        if (iconEntry != null) {
                            val mIcon: Drawable? = ip.getDrawable(
                                iconEntry,
                                iconDpi,
                                componentKey.user
                            )
                            if (mIcon != null) {
                                Image(
                                    painter = rememberDrawablePainter(drawable = mIcon),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .requiredSize(64.dp)
                                        .padding(start = 8.dp, end = 8.dp)
                                        .clickable {
                                            val iconPickerItem = IconPickerItem(
                                                pack.packPackageName,
                                                iconEntry.name,
                                                iconEntry.name,
                                                iconEntry.type
                                            )
                                            scope.launch {
                                                repo.setOverride(componentKey, iconPickerItem)
                                                (context as Activity).finish()
                                            }
                                        }
                                )
                            }
                        }
                    }
                }
            }
        }

        Divider(
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier
                .height(1.dp)
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp)
        )

        //Icon Packs
        iconPacks.forEach {
            ListItemWithIcon(
                title = it.name,
                modifier = Modifier
                    .clickable {
                        if (it.packageName == "") {
                            navController.navigate("/${Routes.ICON_PICKER}/")
                        } else {
                            navController.navigate("/${Routes.ICON_PICKER}/${it.packageName}/")
                        }
                    }
                    .padding(start = 16.dp),
                startIcon = {
                    Image(
                        painter = rememberDrawablePainter(drawable = it.icon),
                        contentDescription = null,
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(44.dp)
                            .background(
                                MaterialTheme.colorScheme.background.copy(alpha = 0.12F)
                            )
                    )
                },
                horizontalPadding = 8.dp,
                verticalPadding = 8.dp
            )
        }
    }
}