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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.saggitt.omega.compose.components.CategorizationOption
import com.saggitt.omega.compose.components.ComposeSwitchView
import com.saggitt.omega.compose.components.GroupItem
import com.saggitt.omega.compose.components.ViewWithActionBar
import com.saggitt.omega.groups.AppGroupsManager

@Composable
fun AppCategoriesPage() {
    val context = LocalContext.current
    val prefs = Utilities.getOmegaPrefs(context)
    val manager by lazy { prefs.drawerAppGroupsManager }

    val enableCategories by remember { mutableStateOf(manager.categorizationEnabled) }
    var categoryTitle by remember { mutableStateOf("") }

    val (selectedOption, onOptionSelected) = remember {
        mutableStateOf(manager.categorizationType)
    }

    ViewWithActionBar(
        title = stringResource(id = R.string.title_app_categorize)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding(), start = 8.dp, end = 8.dp
                )
        ) {
            ComposeSwitchView(
                title = stringResource(id = R.string.title_app_categorization_enable),
                summary = stringResource(id = R.string.summary_app_categorization_enable),
                verticalPadding = 8.dp,
                horizontalPadding = 16.dp,
                isChecked = enableCategories,
                onCheckedChange = {
                    manager.categorizationEnabled = !manager.categorizationEnabled
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Divider(
                modifier = Modifier
                    .height(1.dp)
                    .padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
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
                        manager.categorizationType = it
                        onOptionSelected(it)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = categoryTitle,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Divider(
                modifier = Modifier
                    .height(1.dp)
                    .padding(horizontal = 16.dp)
            )
            Text(
                text = stringResource(id = R.string.pref_app_groups_edit_tip),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            val groups = when (manager.categorizationType) {
                AppGroupsManager.CategorizationType.Tabs -> {
                    categoryTitle = stringResource(id = R.string.app_categorization_tabs)
                    manager.drawerTabs.getGroups()
                }
                AppGroupsManager.CategorizationType.Folders -> {
                    categoryTitle = stringResource(id = R.string.app_categorization_folders)
                    manager.drawerFolders.getGroups()
                }
                AppGroupsManager.CategorizationType.Flowerpot -> {
                    categoryTitle = stringResource(id = R.string.app_categorization_tabs)
                    manager.flowerpotTabs.getGroups()
                }
                else -> {
                    emptyList()
                }
            }

            groups.forEach {
                GroupItem(title = it.title, summary = it.summary)
            }
        }
    }
}