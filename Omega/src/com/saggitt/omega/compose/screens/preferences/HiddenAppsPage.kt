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

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.saggitt.omega.compose.components.ListItemWithIcon
import com.saggitt.omega.compose.components.OverflowMenu
import com.saggitt.omega.compose.components.ViewWithActionBar
import com.saggitt.omega.util.App
import com.saggitt.omega.util.appComparator
import com.saggitt.omega.util.appsList
import com.saggitt.omega.util.comparing

@Composable
fun HiddenAppsPage() {
    val context = LocalContext.current
    val hiddenApps = Utilities.getOmegaPrefs(context).drawerHiddenApps
    val allApps by appsList(comparator = hiddenAppsComparator(hiddenApps))
    HiddenAppsView(
        allApps = allApps,
        hiddenApps = hiddenApps
    )
}

@Composable
fun HiddenAppsView(
    allApps: List<App>,
    hiddenApps: Set<String>
) {
    val context = LocalContext.current
    val colors = CheckboxDefaults.colors(
        checkedColor = MaterialTheme.colorScheme.primary,
        uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
    var selected by remember { mutableStateOf(hiddenApps) }
    val title = if (selected.isEmpty()) stringResource(id = R.string.title__drawer_hide_apps)
    else stringResource(id = R.string.hide_app_selected, selected.size)

    ViewWithActionBar(
        title = title,
        actions = {
            OverflowMenu {
                DropdownMenuItem(
                    onClick = {
                        selected = emptySet()
                        hideMenu()
                    },
                    text = { Text(text = stringResource(id = R.string.hide_app_reset)) }
                )
            }
        },
        onBackAction = {
            Utilities.getOmegaPrefs(context).drawerHiddenApps = selected
        }
    ) {
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
            items(allApps) { app ->
                val isSelected = rememberSaveable(selected) {
                    mutableStateOf(selected.contains(app.key.toString()))
                }
                ListItemWithIcon(
                    title = app.label,
                    modifier = Modifier.clickable {
                        selected = if (isSelected.value) selected.minus(app.key.toString())
                        else selected.plus(app.key.toString())
                    },
                    summary = "",
                    startIcon = {
                        Image(
                            painter = BitmapPainter(app.icon.asImageBitmap()),
                            contentDescription = null,
                            modifier = Modifier.size(36.dp)
                        )
                    },
                    endCheckbox = {
                        Checkbox(
                            checked = isSelected.value,
                            onCheckedChange = {
                                selected = if (it) selected.plus(app.key.toString())
                                else selected.minus(app.key.toString())
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

@Composable
fun hiddenAppsComparator(hiddenApps: Set<String>): Comparator<App> = remember {
    comparing<App, Int> {
        if (hiddenApps.contains(it.key.toString())) 0 else 1
    }.then(appComparator)
}