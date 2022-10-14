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

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
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
import com.saggitt.omega.compose.components.move
import com.saggitt.omega.compose.components.rememberDragDropListState
import com.saggitt.omega.groups.AppGroupsManager
import com.saggitt.omega.groups.DrawerFolders
import com.saggitt.omega.groups.DrawerTabs
import com.saggitt.omega.groups.FlowerpotTabs
import com.saggitt.omega.groups.ui.CreateGroupBottomSheet
import com.saggitt.omega.groups.ui.EditGroupBottomSheet
import com.saggitt.omega.groups.ui.SelectTabBottomSheet
import com.saggitt.omega.util.Config
import kotlinx.coroutines.Job
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

    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        confirmStateChange = { it != ModalBottomSheetValue.HalfExpanded }
    )

    var radius = 16.dp
    if (prefs.themeCornerRadius.onGetValue() > -1) {
        radius = prefs.themeCornerRadius.onGetValue().dp
    }

    var (selectedOption, onOptionSelected) = remember {
        mutableStateOf(manager.categorizationType)
    }
    val hasWorkApps = Config.hasWorkApps(LocalContext.current)

    val groups = remember(manager.categorizationType) {
        mutableStateListOf(
            *when (manager.categorizationType) {
                AppGroupsManager.CategorizationType.Tabs,
                AppGroupsManager.CategorizationType.Flowerpot -> {
                    if (hasWorkApps) {
                        manager.drawerTabs.getGroups()
                            .filter { it !is DrawerTabs.ProfileTab || !it.profile.matchesAll }
                    } else {
                        manager.drawerTabs.getGroups()
                            .filter { it !is DrawerTabs.ProfileTab || it.profile.matchesAll }
                    }
                }
                AppGroupsManager.CategorizationType.Folders -> {
                    manager.drawerFolders.getGroups()
                }
                else -> {
                    emptyList()
                }
            }.toTypedArray()
        )
    }
    val editGroup = remember {
        mutableStateOf(groups.firstOrNull())
    }

    when (manager.categorizationType) {
        AppGroupsManager.CategorizationType.Tabs,
        AppGroupsManager.CategorizationType.Flowerpot -> {
            categoryTitle = stringResource(id = R.string.app_categorization_tabs)
        }

        AppGroupsManager.CategorizationType.Folders -> {
            categoryTitle = stringResource(id = R.string.app_categorization_folders)
        }

        else -> {}
    }
    var sheetChanger by remember {
        mutableStateOf(Config.BS_SELECT_TAB_TYPE)
    }

    BackHandler(sheetState.isVisible) {
        coroutineScope.launch {
            sheetState.hide()
            sheetChanger = Config.BS_SELECT_TAB_TYPE
        }
    }
    if (sheetState.currentValue != ModalBottomSheetValue.Hidden) {
        DisposableEffect(Unit) {
            onDispose {
                sheetChanger = Config.BS_SELECT_TAB_TYPE
            }
        }
    }

    var overscrollJob by remember { mutableStateOf<Job?>(null) }
    val onMove: (Int, Int) -> Unit = { from, to ->
        groups.move(from, to)
    }
    val dragDropListState = rememberDragDropListState(onMove = onMove)

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetShape = RoundedCornerShape(topStart = radius, topEnd = radius),
        sheetElevation = 8.dp,
        sheetBackgroundColor = MaterialTheme.colorScheme.surfaceVariant,
        sheetContent = {
            when (sheetChanger) {
                Config.BS_SELECT_TAB_TYPE -> {
                    if (selectedOption == AppGroupsManager.CategorizationType.Tabs || selectedOption == AppGroupsManager.CategorizationType.Flowerpot) {
                        SelectTabBottomSheet { changer, categorizationType ->
                            selectedOption = categorizationType
                            sheetChanger = changer
                        }
                    } else {
                        CreateGroupBottomSheet(selectedOption) {
                            sheetChanger = it
                            coroutineScope.launch {
                                sheetState.hide()
                            }
                        }
                    }
                }

                Config.BS_CREATE_GROUP -> {
                    CreateGroupBottomSheet(selectedOption) {
                        sheetChanger = it
                    }
                }

                Config.BS_EDIT_GROUP -> {
                    editGroup.value?.let { editGroup ->
                        EditGroupBottomSheet(selectedOption, editGroup) {
                            sheetChanger = it
                            coroutineScope.launch {
                                sheetState.hide()
                            }
                        }
                    }
                }
            }
        }
    )
    {
        ViewWithActionBar(
            title = stringResource(id = R.string.title_app_categorize),
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            if (sheetState.isVisible) {
                                sheetState.hide()
                            } else {
                                sheetState.show()
                            }
                        }
                    },
                    modifier = Modifier.padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary.copy(0.65f),
                    contentColor = Color.White
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(id = R.string.title_create),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            },
            onBackAction = {
                //todo save sorted group
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
                            selectedOption = it
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

                LazyColumn(
                    modifier = Modifier
                        .pointerInput(Unit) {
                            detectDragGesturesAfterLongPress(
                                onDrag = { change, offset ->
                                    change.consumeAllChanges()
                                    dragDropListState.onDrag(offset)

                                    if (overscrollJob?.isActive == true)
                                        return@detectDragGesturesAfterLongPress

                                    dragDropListState.checkForOverScroll()
                                        .takeIf { it != 0f }
                                        ?.let {
                                            overscrollJob = coroutineScope.launch {
                                                dragDropListState.lazyListState.scrollBy(it)
                                            }
                                        }
                                        ?: run { overscrollJob?.cancel() }
                                },
                                onDragStart = { offset -> dragDropListState.onDragStart(offset) },
                                onDragEnd = {
                                    dragDropListState.onDragInterrupted()
                                    when (manager.categorizationType) {
                                        AppGroupsManager.CategorizationType.Tabs,
                                        AppGroupsManager.CategorizationType.Flowerpot -> {
                                            manager.drawerTabs.setGroups(groups as List<DrawerTabs.Tab>)
                                            manager.drawerTabs.saveToJson()
                                        }
                                        AppGroupsManager.CategorizationType.Folders -> {
                                            manager.drawerFolders.setGroups(groups as List<DrawerFolders.Folder>)
                                            manager.drawerFolders.saveToJson()
                                        }
                                        else -> {}
                                    }
                                },
                                onDragCancel = { dragDropListState.onDragInterrupted() }
                            )
                        },
                    state = dragDropListState.lazyListState
                ) {
                    itemsIndexed(groups) { index, item ->
                        GroupItem(
                            title = item.title,
                            summary = item.summary,
                            modifier = Modifier.composed {
                                val offsetOrNull =
                                    dragDropListState.elementDisplacement.takeIf {
                                        index == dragDropListState.currentIndexOfDraggedItem
                                    }

                                Modifier
                                    .graphicsLayer {
                                        translationY = offsetOrNull ?: 0f
                                    }
                            },
                            removable = item.type in arrayOf(
                                DrawerTabs.TYPE_CUSTOM,
                                FlowerpotTabs.TYPE_FLOWERPOT
                            ),
                            onClick = {
                                coroutineScope.launch {
                                    sheetChanger = Config.BS_EDIT_GROUP
                                    editGroup.value = item
                                    sheetState.show()
                                }
                            },
                            onRemoveClick = {
                                groups.remove(item)
                                when (manager.categorizationType) {
                                    AppGroupsManager.CategorizationType.Tabs,
                                    AppGroupsManager.CategorizationType.Flowerpot -> {
                                        manager.drawerTabs.removeGroup(item as DrawerTabs.Tab)
                                        manager.drawerTabs.saveToJson()
                                    }
                                    AppGroupsManager.CategorizationType.Folders -> {
                                        manager.drawerFolders.removeGroup(item as DrawerFolders.Folder)
                                        manager.drawerFolders.saveToJson()
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
}