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

import android.content.Intent
import android.content.pm.LauncherApps
import android.graphics.drawable.Drawable
import android.os.Process
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.ContentAlpha
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.android.launcher3.R
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.google.accompanist.insets.ui.LocalScaffoldPadding
import com.google.accompanist.navigation.animation.composable
import com.saggitt.omega.compose.components.*
import com.saggitt.omega.compose.navigation.resultSender
import com.saggitt.omega.compose.preferences.preferenceGraph
import com.saggitt.omega.data.IconPickerItem
import com.saggitt.omega.iconpack.CustomIconPack
import com.saggitt.omega.iconpack.IconPack
import com.saggitt.omega.iconpack.IconPackProvider
import com.saggitt.omega.iconpack.filter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.iconPickerGraph(route: String) {
    preferenceGraph(route, {
        IconListScreen(iconPackName = "")
    }) { subRoute ->
        composable(
            route = subRoute("{iconPackName}"),
            arguments = listOf(
                navArgument("iconPackName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val args = backStackEntry.arguments!!
            val packageName = args.getString("iconPackName")!!
            IconListScreen(packageName)
        }
    }
}

/*
* List Icons from a given IconPack
* */
@Composable
fun IconListScreen(
    iconPackName: String
) {
    val context = LocalContext.current
    var iconPackage = ""
    if (iconPackName != "system_icons") {
        iconPackage = iconPackName
    }

    val iconPack = remember {
        IconPackProvider.INSTANCE.get(context).getIconPackOrSystem(iconPackage)
    }
    if (iconPack == null) {
        val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
        SideEffect {
            backDispatcher?.onBackPressed()
        }
        return
    }
    var searchQuery by remember { mutableStateOf("") }
    val onClickItem = resultSender<IconPickerItem>()
    val pickerComponent = remember {
        val launcherApps = context.getSystemService<LauncherApps>()!!
        launcherApps
            .getActivityList(iconPack.packPackageName, Process.myUserHandle())
            .firstOrNull()?.componentName
    }
    val pickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val icon = it.data?.getParcelableExtra<Intent.ShortcutIconResource>(
                Intent.EXTRA_SHORTCUT_ICON_RESOURCE
            ) ?: return@rememberLauncherForActivityResult
            val entry = (iconPack as CustomIconPack).createFromExternalPicker(icon)
                ?: return@rememberLauncherForActivityResult
            onClickItem(entry)
        }
    Column() {
        SearchBarUI(
            searchInput = {
                SearchTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxSize(),
                    placeholder = {
                        Text(
                            text = iconPack.label,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = ContentAlpha.medium),
                        )
                    },
                    singleLine = true
                )
            },
            actions = {
                if (pickerComponent != null) {
                    OverflowMenu {
                        DropdownMenuItem(
                            onClick = {
                                val intent = Intent("com.novalauncher.THEME")
                                    .addCategory("com.novalauncher.category.CUSTOM_ICON_PICKER")
                                    .setComponent(pickerComponent)
                                pickerLauncher.launch(intent)
                                hideMenu()
                            },
                            text = { Text(text = stringResource(id = R.string.icon_pack_external_picker)) }
                        )
                    }
                }
            }
        ) {
            val scaffoldPadding = LocalScaffoldPadding.current
            val innerPadding = remember { MutablePaddingValues() }
            val layoutDirection = LocalLayoutDirection.current
            innerPadding.left = scaffoldPadding.calculateLeftPadding(layoutDirection)
            innerPadding.right = scaffoldPadding.calculateRightPadding(layoutDirection)
            innerPadding.bottom = scaffoldPadding.calculateBottomPadding()

            val topPadding = scaffoldPadding.calculateTopPadding()

            CompositionLocalProvider(LocalScaffoldPadding provides innerPadding) {
                IconPickerGrid(
                    iconPack = iconPack,
                    searchQuery = searchQuery,
                    onClickItem = onClickItem,
                    modifier = Modifier
                        .padding(top = topPadding)
                )
            }
            Spacer(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .fillMaxWidth()
                    .height(topPadding)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IconPickerGrid(
    iconPack: IconPack,
    searchQuery: String,
    onClickItem: (item: IconPickerItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var loadFailed by remember { mutableStateOf(false) }
    val categoriesFlow = remember {
        iconPack.getAllIcons()
            .catch { loadFailed = true }
    }
    val categories by categoriesFlow.collectAsState(emptyList())
    val filteredCategories by derivedStateOf {
        categories
            .map { it.filter(searchQuery) }
            .filter { it.items.isNotEmpty() }
    }

    val density = LocalDensity.current
    val gridLayout = remember {
        LazyGridLayout(
            minWidth = 56.dp,
            gapWidth = 16.dp,
            density = density
        )
    }
    val numColumns by gridLayout.numColumns
    PreferenceLazyColumn(modifier = modifier.then(gridLayout.onSizeChanged())) {
        if (numColumns != 0) {
            filteredCategories.forEach { category ->
                stickyHeader {
                    Text(
                        text = category.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(16.dp),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                verticalGridItems(
                    modifier = Modifier
                        .padding(horizontal = 8.dp),
                    items = category.items,
                    numColumns = numColumns,
                ) { _, item ->
                    IconPreview(
                        iconPack = iconPack,
                        iconItem = item,
                        onClick = {
                            onClickItem(item)
                        }
                    )
                }
            }
        }
        if (loadFailed) {
            item {
                PreferenceGroupDescription(
                    description = stringResource(id = R.string.icon_picker_load_failed)
                )
            }
        }
    }
}

@Composable
fun IconPreview(
    iconPack: IconPack,
    iconItem: IconPickerItem,
    onClick: () -> Unit
) {
    val drawable by produceState<Drawable?>(initialValue = null, iconPack, iconItem) {
        launch(Dispatchers.IO) {
            value = iconPack.getIcon(iconItem.toIconEntry(), 0)
        }
    }
    Box(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .padding(8.dp),
    ) {
        Image(
            painter = rememberDrawablePainter(drawable),
            contentDescription = iconItem.drawableName,
            modifier = Modifier.aspectRatio(1f),
        )
    }
}