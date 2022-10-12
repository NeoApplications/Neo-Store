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

package com.saggitt.omega.groups.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.saggitt.omega.compose.screens.preferences.AppSelectionPage
import com.saggitt.omega.util.Config

@Composable
fun SelectTabBottomSheet(
    onClose: (Int) -> Unit,
) {
    val context = LocalContext.current
    val prefs = Utilities.getOmegaPrefs(context)
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Divider(
            modifier = Modifier
                .width(48.dp)
                .height(2.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(id = R.string.default_tab_name),
            modifier = Modifier.fillMaxWidth(),
            color = Color(prefs.themeAccentColor.onGetValue()),
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        CategoryTabItem(
            titleId = R.string.tab_type_smart,
            summaryId = R.string.pref_appcategorization_flowerpot_summary,
            modifier = Modifier.height(72.dp),
            iconId = R.drawable.ic_category,
            onClick = {
                onClose(Config.BS_CREATE_GROUP)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        CategoryTabItem(
            titleId = R.string.custom,
            summaryId = R.string.tab_type_custom_desc,
            modifier = Modifier.height(72.dp),
            iconId = R.drawable.ic_squares_four,
            onClick = {
                onClose(Config.BS_CREATE_GROUP)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun GroupAppSelection(
    selectedApps: Set<String>,
    onSave: (Set<String>) -> Unit
) {
    var selected: Set<String> by remember {
        mutableStateOf(selectedApps)
    }
    val pageTitle = stringResource(id = R.string.selected_apps, selected.size)
    AppSelectionPage(
        pageTitle = pageTitle,
        selectedApps = selected,
        pluralTitleId = R.string.selected_apps,
        onSave = {
            selected = it
            onSave(it)
        }
    )
}

