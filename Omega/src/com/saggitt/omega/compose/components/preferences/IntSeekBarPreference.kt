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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.launcher3.R
import com.saggitt.omega.preferences.BasePreferences
import com.saggitt.omega.util.Config

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IntSeekBarPreference(
    modifier: Modifier = Modifier,
    pref: BasePreferences.IdpIntPref,
    index: Int = 1,
    groupSize: Int = 1,
    isEnabled: Boolean = true,
    onValueChange: ((Float) -> Unit) = {}
) {
    var currentValue by remember(pref) { mutableStateOf(pref.onGetValue()) }
    val defaultValue = Config.getIdpDefaultValue(LocalContext.current, pref.key)
    BasePreference(
        modifier = modifier,
        titleId = pref.titleId,
        summaryId = pref.summaryId,
        index = index,
        groupSize = groupSize,
        isEnabled = isEnabled,
        bottomWidget = {
            Row {
                var menuExpanded by remember { mutableStateOf(false) }
                Text(
                    text = pref.specialOutputs(currentValue),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .widthIn(min = 52.dp)
                        .combinedClickable(
                            onClick = {},
                            onLongClick = {
                                menuExpanded = !menuExpanded
                            }
                        )
                )

                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(
                        onClick = {
                            pref.onSetValue(defaultValue)
                            onValueChange(defaultValue.toFloat())
                            currentValue = defaultValue
                            menuExpanded = false
                        },
                        text = { Text(text = stringResource(id = R.string.reset_to_default)) }
                    )
                }

                Spacer(modifier = Modifier.requiredWidth(8.dp))
                Slider(
                    modifier = Modifier
                        .requiredHeight(24.dp)
                        .weight(1f),
                    value = currentValue.toFloat(),
                    valueRange = pref.minValue..pref.maxValue,
                    onValueChange = { currentValue = it.toInt() },
                    steps = pref.steps,
                    onValueChangeFinished = {
                        pref.onSetValue(currentValue)
                        onValueChange(currentValue.toFloat())
                    },
                    enabled = isEnabled
                )
            }
        }
    )
}