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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.saggitt.omega.compose.components.ViewWithActionBar
import com.saggitt.omega.compose.components.preferences.PreferenceGroup
import com.saggitt.omega.icons.IconShape
import com.saggitt.omega.icons.IconShapeItem
import com.saggitt.omega.icons.IconShapeManager
import com.saggitt.omega.icons.ShapeModel

@Composable
fun IconShapePage() {
    val context = LocalContext.current
    val prefs = Utilities.getOmegaPrefs(context)
    val currentShape = remember { mutableStateOf(prefs.themeIconShape.onGetValue()) }
    ViewWithActionBar(title = stringResource(id = R.string.title_theme_customize_icons)) { paddingValues ->
        val systemShape = IconShapeManager.getSystemIconShape(context)
        val iconShapes = arrayListOf(
            systemShape,
            IconShape.Circle,
            IconShape.Square,
            IconShape.RoundedSquare,
            IconShape.Squircle,
            IconShape.Sammy,
            IconShape.Teardrop,
            IconShape.Cylinder,
            IconShape.Cupertino
        )
        val listItems = iconShapes.map { ShapeModel(it.toString(), false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(
                    start = 8.dp,
                    end = 8.dp,
                    bottom = paddingValues.calculateBottomPadding() + 8.dp
                ),
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                contentPadding = PaddingValues(8.dp)
            ) {
                itemsIndexed(
                    items = listItems,
                    span = { _, _ -> GridItemSpan(1) },
                    key = { _: Int, item: ShapeModel -> item.shapeName }) { _, item ->
                    IconShapeItem(
                        item = item,
                        checked = (currentShape.value.toString() == item.shapeName),
                        onClick = {
                            currentShape.value =
                                IconShape.fromString(item.shapeName) ?: IconShape.Circle
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Divider(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp)
                    .height(1.dp)
            )

            val openDialog = remember { mutableStateOf(false) }
            var dialogPref by remember { mutableStateOf<Any?>(null) }
            val onPrefDialog = { pref: Any ->
                dialogPref = pref
                openDialog.value = true
            }

            val iconPrefs = listOf(
                prefs.themeIconAdaptify,
                prefs.themeIconColoredBackground
            )

            LazyColumn(
                modifier = Modifier
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    PreferenceGroup(
                        heading = null,
                        prefs = iconPrefs,
                        onPrefDialog = onPrefDialog
                    )
                }
            }
        }
    }

    DisposableEffect(key1 = null) {
        onDispose {
            prefs.themeIconShape.onSetValue(currentShape.value)
        }
    }
}