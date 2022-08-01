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

package com.saggitt.omega.groups.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.launcher3.Utilities
import com.saggitt.omega.compose.components.CategorizationOption
import com.saggitt.omega.groups.AppGroupsManager

// TODO convert to single items
@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun AppCategorizationOptions() {
    val context = LocalContext.current
    val prefs = Utilities.getOmegaPrefs(context)

    val (selectedOption, onOptionSelected) = remember {
        mutableStateOf(prefs.drawerAppGroupsManager.categorizationType)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf(
            AppGroupsManager.CategorizationType.Folders,
            AppGroupsManager.CategorizationType.Tabs
        ).forEach {
            CategorizationOption(
                type = it,
                selected = selectedOption == it
            ) {
                prefs.drawerAppGroupsManager.categorizationType = it
                onOptionSelected(it)
            }
        }
    }
}
