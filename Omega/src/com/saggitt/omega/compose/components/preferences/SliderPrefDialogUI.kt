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
package com.saggitt.omega.compose.components.preferences

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.saggitt.omega.compose.components.DialogNegativeButton
import com.saggitt.omega.compose.components.DialogPositiveButton
import com.saggitt.omega.preferences.custom.GridSize
import com.saggitt.omega.preferences.custom.GridSize2D

@Composable
fun GridSizePrefDialogUI(
    pref: GridSize,
    openDialogCustom: MutableState<Boolean>
) {
    val context = LocalContext.current
    val prefs = Utilities.getOmegaPrefs(context)
    var numColumns by remember { mutableStateOf(pref.numColumnsPref.onGetValue()) }
    var numRows by remember { mutableStateOf(if (pref is GridSize2D) pref.numRowsPref.onGetValue() else 0) }

    var radius = 16.dp
    if (prefs.themeCornerRadius.onGetValue() > -1f) {
        radius = prefs.themeCornerRadius.onGetValue().dp
    }
    val cornerRadius by remember { mutableStateOf(radius) }

    Card(
        shape = RoundedCornerShape(cornerRadius),
        modifier = Modifier.padding(8.dp),
        elevation = CardDefaults.elevatedCardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = stringResource(pref.titleId), style = MaterialTheme.typography.titleLarge)
            LazyColumn(
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 8.dp)
                    .weight(1f, false)
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            modifier = Modifier.size(28.dp),
                            painter = painterResource(id = R.drawable.ic_columns),
                            contentDescription = stringResource(id = R.string.title__drawer_columns)
                        )
                        Text(
                            text = numColumns.toString(),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.widthIn(min = 32.dp)
                        )
                        Spacer(modifier = Modifier.requiredWidth(8.dp))
                        Slider(
                            modifier = Modifier
                                .requiredHeight(24.dp)
                                .weight(1f),
                            value = numColumns.toFloat(),
                            valueRange = pref.numColumnsPref.minValue..pref.numColumnsPref.maxValue,
                            onValueChange = { numColumns = it.toInt() },
                            steps = pref.numColumnsPref.steps
                        )
                    }
                }
                if (pref is GridSize2D) item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            modifier = Modifier.size(28.dp),
                            painter = painterResource(id = R.drawable.ic_rows),
                            contentDescription = stringResource(id = R.string.title__drawer_rows)
                        )
                        Text(
                            text = numRows.toString(),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.widthIn(min = 32.dp),
                        )
                        Spacer(modifier = Modifier.requiredWidth(8.dp))
                        Slider(
                            modifier = Modifier
                                .requiredHeight(24.dp)
                                .weight(1f),
                            value = numRows.toFloat(),
                            valueRange = pref.numRowsPref.minValue..pref.numRowsPref.maxValue,
                            onValueChange = { numRows = it.toInt() },
                            steps = pref.numRowsPref.steps,
                        )
                    }
                }
            }

            Row(
                Modifier.fillMaxWidth()
            ) {
                DialogNegativeButton(
                    cornerRadius = cornerRadius,
                    onClick = { openDialogCustom.value = false }
                )
                Spacer(Modifier.weight(1f))
                DialogPositiveButton(
                    modifier = Modifier.padding(start = 16.dp),
                    cornerRadius = cornerRadius,
                    onClick = {
                        pref.numColumnsPref.onSetValue(numColumns)
                        if (pref is GridSize2D) pref.numRowsPref.onSetValue(numRows)
                        openDialogCustom.value = false
                    }
                )
            }
        }
    }
}
