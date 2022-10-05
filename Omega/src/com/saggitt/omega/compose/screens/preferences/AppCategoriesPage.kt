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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetState
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.saggitt.omega.compose.components.CategorizationOption
import com.saggitt.omega.compose.components.ComposeSwitchView
import com.saggitt.omega.compose.components.GroupItem
import com.saggitt.omega.compose.components.ViewWithActionBar
import com.saggitt.omega.groups.AppGroupsManager
import com.saggitt.omega.groups.DrawerFolders
import com.saggitt.omega.groups.DrawerTabs
import com.saggitt.omega.groups.FlowerpotTabs
import com.saggitt.omega.groups.ui.CategoryBottomSheet
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AppCategoriesPage() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val prefs = Utilities.getOmegaPrefs(context)
    val manager by lazy { prefs.drawerAppGroupsManager }

    val enableCategories by remember { mutableStateOf(manager.categorizationEnabled) }
    var categoryTitle by remember { mutableStateOf("") }

    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = BottomSheetState(BottomSheetValue.Collapsed)
    )

    var radius = 16.dp
    if (prefs.themeCornerRadius.onGetValue() > -1) {
        radius = prefs.themeCornerRadius.onGetValue().dp
    }

    val (selectedOption, onOptionSelected) = remember {
        mutableStateOf(manager.categorizationType)
    }

    val groups = remember(manager.categorizationType) {
        mutableStateListOf(
            *when (manager.categorizationType) {
                AppGroupsManager.CategorizationType.Tabs -> {
                    manager.drawerTabs.getGroups()
                }

                AppGroupsManager.CategorizationType.Folders -> {
                    manager.drawerFolders.getGroups()
                }

                AppGroupsManager.CategorizationType.Flowerpot -> {
                    manager.flowerpotTabs.getGroups()
                }

                else -> {
                    emptyList()
                }
            }.toTypedArray()
        )
    }

    when (manager.categorizationType) {
        AppGroupsManager.CategorizationType.Tabs, AppGroupsManager.CategorizationType.Flowerpot -> {
            categoryTitle = stringResource(id = R.string.app_categorization_tabs)
        }

        AppGroupsManager.CategorizationType.Folders -> {
            categoryTitle = stringResource(id = R.string.app_categorization_folders)
        }

        else -> {}
    }

    BottomSheetScaffold(
        scaffoldState = bottomSheetScaffoldState,
        sheetPeekHeight = 0.dp,
        sheetShape = RoundedCornerShape(topStart = radius, topEnd = radius),
        sheetElevation = 8.dp,
        sheetBackgroundColor = MaterialTheme.colorScheme.background,
        sheetContent = {
            CategoryBottomSheet(category = selectedOption)
        }
    )
    {
        ViewWithActionBar(
            title = stringResource(id = R.string.title_app_categorize),
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            if (bottomSheetScaffoldState.bottomSheetState.isExpanded) {
                                bottomSheetScaffoldState.bottomSheetState.collapse()
                            } else {
                                bottomSheetScaffoldState.bottomSheetState.expand()
                            }
                        }
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
                    },
                    modifier = Modifier.padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary.copy(0.65f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(id = R.string.title_create),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
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
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
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
            }
        }
    }
}

@Composable
@Preview
fun AppCategoriesPreview() {
    AppCategoriesPage()
}
