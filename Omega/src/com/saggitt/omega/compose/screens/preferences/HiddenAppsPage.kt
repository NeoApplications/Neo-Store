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
package com.saggitt.omega.compose.screens.preferences

import android.content.pm.LauncherActivityInfo
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.saggitt.omega.compose.components.ListItemWithIcon
import com.saggitt.omega.compose.components.OverflowMenu
import com.saggitt.omega.compose.components.ViewWithActionBar
import com.saggitt.omega.compose.components.preferences.PreferenceGroup
import com.saggitt.omega.util.Config

@Composable
fun HiddenAppsPage() {

    val context = LocalContext.current
    val hiddenApps = Utilities.getOmegaPrefs(context).drawerHiddenApps
    val allApps = Config(context).getAppsList(filter = null).sortedBy { it.label.toString() }
    val title = if (hiddenApps.isEmpty()) stringResource(id = R.string.title__drawer_hide_apps)
    else stringResource(id = R.string.hide_app_selected, hiddenApps.size)

    HiddenAppsView(
        title = title,
        allApps = allApps,
        hiddenApps = hiddenApps
    )
}

@Composable
fun HiddenAppsView(
    title: String,
    allApps: List<LauncherActivityInfo>,
    hiddenApps: Set<String>
) {
    ViewWithActionBar(
        title = title,
        actions = {
            OverflowMenu {
                DropdownMenuItem(
                    onClick = {
                        hideMenu()
                    },
                    text = { Text(text = stringResource(id = R.string.hide_app_reset)) }
                )
            }
        }
    ) {
        val colors = CheckboxDefaults.colors(
            checkedColor = MaterialTheme.colorScheme.primary,
            uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 8.dp,
                end = 8.dp,
                top = 48.dp,
                bottom = 8.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
                items(allApps) {
                    ListItemWithIcon(
                        title = it.label.toString(),
                        modifier = Modifier.clickable { },
                        summary = "",
                        startIcon = {
                            Image(
                                painter = rememberDrawablePainter(
                                    drawable = it.getIcon(LocalContext.current.resources.displayMetrics.densityDpi)
                                ),
                                contentDescription = null,
                                modifier = Modifier.size(36.dp)
                            )
                        },
                        endCheckbox = {
                            Checkbox(
                                checked = false,
                                onCheckedChange = {

                                },
                                colors = colors
                            )
                        },
                        verticalPadding = 2.dp
                    )
                }
            }
        }
    }