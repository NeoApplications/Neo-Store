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
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TabRowDefaults.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.android.launcher3.util.ComponentKey
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.saggitt.omega.OmegaLauncher
import com.saggitt.omega.compose.components.ListItemWithIcon
import com.saggitt.omega.compose.navigation.Routes
import com.saggitt.omega.iconpack.EditIconActivity.Companion.EXTRA_ENTRY
import com.saggitt.omega.iconpack.IconPack
import com.saggitt.omega.iconpack.IconPackInfo
import com.saggitt.omega.iconpack.IconPackProvider

@Composable
fun EditIconScreen(
    activity: Activity,
    title: String?,
    component: ComponentKey?,
    iconPacks: List<IconPackInfo>,
    isFolder: Boolean,
    navController: NavController
) {
    Column {
        Text(
            text = title ?: "",
            modifier = Modifier
                .padding(start = 24.dp)
                .fillMaxWidth(),
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
                painter = rememberDrawablePainter(drawable = OmegaLauncher.currentEditIcon),
                contentDescription = null,
                modifier = Modifier.requiredSize(60.dp)
            )

            //Vertical Divider
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
            iconPacks.forEach {
                if (it.packageName == "") {
                    Image(
                        painter = rememberDrawablePainter(drawable = OmegaLauncher.currentEditIcon),
                        contentDescription = null,
                        modifier = Modifier
                            .requiredSize(64.dp)
                            .padding(start = 4.dp, end = 8.dp)
                    )
                    return@forEach
                }
                val pack: IconPack? = ip.getIconPack(it.packageName)
                if (pack != null) {
                    pack.loadBlocking()
                    val iconEntry = pack.getIcon(component!!.componentName)
                    if (iconEntry != null) {
                        val mIcon: Drawable? = ip.getDrawable(
                            iconEntry,
                            iconDpi,
                            component.user
                        )
                        if (mIcon != null) {
                            Image(
                                painter = rememberDrawablePainter(drawable = mIcon),
                                contentDescription = null,
                                modifier = Modifier
                                    .requiredSize(64.dp)
                                    .padding(start = 8.dp, end = 8.dp)
                                    .clickable {
                                        val customEntry = iconEntry.toCustomEntry()
                                        val entryString = customEntry.toString()
                                        activity.setResult(RESULT_OK, Intent().putExtra(EXTRA_ENTRY, entryString))
                                        activity.finish()
                                    }
                            )
                        }
                    }
                }
            }
        }

        //Divider
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
                title = {
                    Text(
                        text = it.name,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 16.sp
                    )
                },
                modifier = Modifier
                    .clickable {
                        navController.navigate(
                            Routes.IconListScreen.route
                        )
                    }
                    .padding(start = 16.dp),
                description = {},
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