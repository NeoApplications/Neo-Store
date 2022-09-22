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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
                GroupItem(
                    title = it.title,
                    summary = it.summary,
                    removable = it.type in arrayOf(
                        DrawerTabs.TYPE_CUSTOM,
                        FlowerpotTabs.TYPE_FLOWERPOT
                    ),
                    onRemoveClick = {
                        groups.remove(it)
                        when (manager.categorizationType) {
                            AppGroupsManager.CategorizationType.Tabs -> {
                                manager.drawerTabs.removeGroup(it as DrawerTabs.Tab)
                                manager.drawerTabs.saveToJson()
                            }
                            AppGroupsManager.CategorizationType.Folders -> {
                                manager.drawerFolders.removeGroup(it as DrawerFolders.Folder)
                                manager.drawerFolders.saveToJson()
                            }
                            AppGroupsManager.CategorizationType.Flowerpot -> {
                                manager.flowerpotTabs.removeGroup(it as DrawerTabs.Tab)
                                manager.flowerpotTabs.saveToJson()
                            }
                            else -> {}
                        }
                    }
                )
            }
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxSize()
            ) {
                ElevatedButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        /* TODO create Compose Dialog/BottomSheet as replacement to this code
                        when (manager.categorizationType) {
                            AppGroupsManager.CategorizationType.Tabs, AppGroupsManager.CategorizationType.Flowerpot -> {
                                DrawerTabTypeSelectionBottomSheet.show(
                                    context, mapOf(
                                        FlowerpotTabs.TYPE_FLOWERPOT to arrayOf(
                                            R.string.tab_type_smart,
                                            R.string.pref_appcategorization_flowerpot_summary,
                                            R.drawable.ic_category
                                        ),
                                        DrawerTabs.TYPE_CUSTOM to arrayOf(
                                            R.string.custom,
                                            R.string.tab_type_custom_desc,
                                            R.drawable.ic_squares_four
                                        )
                                    )
                                ) {
                                    when (it) {
                                        DrawerTabs.TYPE_CUSTOM -> {
                                            val newGroup = DrawerTabs.CustomTab(context)
                                            DrawerGroupBottomSheet.newGroup(
                                                context,
                                                newGroup,
                                                false
                                            ) {
                                                newGroup.customizations.applyFrom(it)
                                                manager.drawerTabs.setGroups(groups.plus(newGroup) as List<DrawerTabs.Tab>)
                                                manager.drawerTabs.saveToJson()
                                            }
                                        }
                                        FlowerpotTabs.TYPE_FLOWERPOT -> {
                                            val newGroup = FlowerpotTabs.FlowerpotTab(context)
                                            DrawerGroupBottomSheet.newGroup(
                                                context,
                                                newGroup,
                                                false
                                            ) {
                                                newGroup.customizations.applyFrom(it)
                                                manager.flowerpotTabs.setGroups(groups.plus(newGroup) as List<DrawerTabs.Tab>)
                                                manager.flowerpotTabs.saveToJson()
                                            }
                                        }
                                    }
                                }
                            }
                            AppGroupsManager.CategorizationType.Folders -> {
                                val newGroup = DrawerFolders.CustomFolder(context)
                                DrawerGroupBottomSheet.newGroup(context, newGroup, false) {
                                    newGroup.customizations.applyFrom(it)
                                    manager.drawerFolders.setGroups(groups.plus(newGroup) as List<DrawerFolders.Folder>)
                                    manager.drawerFolders.saveToJson()
                                }
                            }
                            else -> {}
                        }*/
                        //groups.add(newGroup)
                    }) {
                    Icon(
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .size(24.dp),
                        painter = painterResource(id = R.drawable.ic_add),
                        contentDescription = stringResource(id = R.string.title_create)
                    )
                    Text(
                        modifier = Modifier.weight(1f),
                        text = stringResource(id = R.string.title_create),
                        textAlign = TextAlign.Center
                    )

                }
            }
        }
    }
}