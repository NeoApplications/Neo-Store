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

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saggitt.omega.compose.navigation.LocalNavController
import com.saggitt.omega.compose.navigation.subRoute
import com.saggitt.omega.preferences.BasePreferences
import com.saggitt.omega.preferences.custom.GridSize
import com.saggitt.omega.preferences.custom.GridSize2D
import com.saggitt.omega.util.addIf

@Composable
fun BasePreference(
    modifier: Modifier = Modifier,
    @StringRes titleId: Int,
    @StringRes summaryId: Int = -1,
    summary: String? = null,
    isEnabled: Boolean = true,
    index: Int = 1,
    groupSize: Int = 1,
    startWidget: (@Composable () -> Unit)? = null,
    endWidget: (@Composable () -> Unit)? = null,
    bottomWidget: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    val base = index.toFloat() / groupSize
    val rank = (index + 1f) / groupSize

    Column(
        modifier = Modifier
            .clip(
                RoundedCornerShape(
                    topStart = if (base == 0f) 16.dp else 6.dp,
                    topEnd = if (base == 0f) 16.dp else 6.dp,
                    bottomStart = if (rank == 1f) 16.dp else 6.dp,
                    bottomEnd = if (rank == 1f) 16.dp else 6.dp
                )
            )
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation((rank * 24).dp))
            .heightIn(min = 64.dp)
            .addIf(onClick != null) {
                clickable(enabled = isEnabled, onClick = onClick!!)
            }, verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            startWidget?.let {
                startWidget()
                Spacer(modifier = Modifier.requiredWidth(8.dp))
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .addIf(!isEnabled) {
                        alpha(0.3f)
                    }
            ) {
                Text(
                    text = stringResource(id = titleId),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 16.sp
                )
                if (summaryId != -1 || summary != null) {
                    Text(
                        text = summary ?: stringResource(id = summaryId),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                bottomWidget?.let {
                    Spacer(modifier = Modifier.requiredWidth(8.dp))
                    bottomWidget()
                }
            }
            endWidget?.let {
                Spacer(modifier = Modifier.requiredWidth(8.dp))
                endWidget()
            }
        }
    }
}

@Composable
fun StringPreference(
    modifier: Modifier = Modifier,
    pref: BasePreferences.StringPref,
    index: Int = 1,
    groupSize: Int = 1,
    isEnabled: Boolean = true,
) {
    val navController = LocalNavController.current
    val route = subRoute(pref.navRoute)
    BasePreference(
        modifier = modifier,
        titleId = pref.titleId,
        summaryId = pref.summaryId,
        index = index,
        groupSize = groupSize,
        isEnabled = isEnabled,
        onClick = {
            if (pref.navRoute != "") {
                navController.navigate(route)
            } else {
                pref.onClick?.invoke()
            }
        }
    )
}

@Composable
fun StringSetPreference(
    modifier: Modifier = Modifier,
    pref: BasePreferences.StringSetPref,
    index: Int = 1,
    groupSize: Int = 1,
    isEnabled: Boolean = true,
) {
    val navController = LocalNavController.current
    val route = subRoute(pref.navRoute)
    BasePreference(
        modifier = modifier,
        titleId = pref.titleId,
        summaryId = pref.summaryId,
        index = index,
        groupSize = groupSize,
        isEnabled = isEnabled,
        onClick = {
            if (pref.navRoute != "") {
                navController.navigate(route)
            }
        }
    )
}

@Composable
fun SwitchPreference(
    modifier: Modifier = Modifier,
    pref: BasePreferences.BooleanPref,
    index: Int = 1,
    groupSize: Int = 1,
    isEnabled: Boolean = true,
    onCheckedChange: ((Boolean) -> Unit) = {},
) {
    val context = LocalContext.current
    val (checked, check) = remember(pref) { mutableStateOf(pref.onGetValue()) }

    BasePreference(
        modifier = modifier,
        titleId = pref.titleId,
        summaryId = pref.summaryId,
        index = index,
        groupSize = groupSize,
        isEnabled = isEnabled,
        onClick = {
            onCheckedChange(!checked)
            pref.onSetValue(!checked)
            check(!checked)
        },
        endWidget = {
            Switch(
                modifier = Modifier
                    .height(24.dp),
                checked = checked,
                onCheckedChange = {
                    onCheckedChange(it)
                    pref.onSetValue(it)
                    check(it)
                },
                enabled = isEnabled,
            )
        }
    )
}

@Composable
fun SeekBarPreference(
    modifier: Modifier = Modifier,
    pref: BasePreferences.FloatPref,
    index: Int = 1,
    groupSize: Int = 1,
    isEnabled: Boolean = true,
    onValueChange: ((Float) -> Unit) = {},
) {
    var currentValue by remember(pref) { mutableStateOf(pref.onGetValue()) }

    BasePreference(
        modifier = modifier,
        titleId = pref.titleId,
        summaryId = pref.summaryId,
        index = index,
        groupSize = groupSize,
        isEnabled = isEnabled,
        bottomWidget = {
            Row {
                Text(
                    text = pref.specialOutputs(currentValue),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.widthIn(min = 52.dp)
                )
                Spacer(modifier = Modifier.requiredWidth(8.dp))
                Slider(
                    modifier = Modifier
                        .requiredHeight(24.dp)
                        .weight(1f),
                    value = currentValue,
                    valueRange = pref.minValue..pref.maxValue,
                    onValueChange = { currentValue = it },
                    steps = pref.steps,
                    onValueChangeFinished = {
                        pref.onSetValue(currentValue)
                        onValueChange(currentValue)
                    },
                    enabled = isEnabled
                )
            }
        }
    )
}

@Composable
fun PagePreference(
    modifier: Modifier = Modifier,
    @StringRes titleId: Int,
    @DrawableRes iconId: Int = -1,
    index: Int = 1,
    groupSize: Int = 1,
    isEnabled: Boolean = true,
    route: String
) {
    val navController = LocalNavController.current
    val destination = subRoute(route)
    BasePreference(
        modifier = modifier,
        titleId = titleId,
        startWidget =
        if (iconId != -1) {
            {
                Icon(
                    painter = painterResource(id = iconId),
                    contentDescription = stringResource(id = titleId),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        } else null,
        index = index,
        groupSize = groupSize,
        isEnabled = isEnabled,
        onClick = { navController.navigate(destination) }
    )
}

@Composable
fun IntSelectionPreference(
    modifier: Modifier = Modifier,
    pref: BasePreferences.IntSelectionPref,
    index: Int = 1,
    groupSize: Int = 1,
    isEnabled: Boolean = true,
    onClick: (() -> Unit) = {},
) {
    BasePreference(
        modifier = modifier,
        titleId = pref.titleId,
        summaryId = pref.summaryId,
        summary = pref.entries[pref.onGetValue()]?.let { stringResource(id = it) },
        index = index,
        groupSize = groupSize,
        isEnabled = isEnabled,
        onClick = onClick
    )
}

@Composable
fun StringSelectionPreference(
    modifier: Modifier = Modifier,
    pref: BasePreferences.StringSelectionPref,
    index: Int = 1,
    groupSize: Int = 1,
    isEnabled: Boolean = true,
    onClick: (() -> Unit) = {},
) {
    BasePreference(
        modifier = modifier,
        titleId = pref.titleId,
        summaryId = pref.summaryId,
        summary = pref.entries[pref.onGetValue()],
        index = index,
        groupSize = groupSize,
        isEnabled = isEnabled,
        onClick = onClick
    )
}

@Composable
fun GridSizePreference(
    modifier: Modifier = Modifier,
    pref: GridSize,
    index: Int = 1,
    groupSize: Int = 1,
    isEnabled: Boolean = true,
    onClick: (() -> Unit) = {},
) {
    BasePreference(
        modifier = modifier,
        titleId = pref.titleId,
        summary = pref.numColumnsPref.onGetValue().toString(),
        index = index,
        groupSize = groupSize,
        isEnabled = isEnabled,
        onClick = onClick
    )
}

@Composable
fun GridSize2DPreference(
    modifier: Modifier = Modifier,
    pref: GridSize2D,
    index: Int = 1,
    groupSize: Int = 1,
    isEnabled: Boolean = true,
    onClick: (() -> Unit) = {},
) {
    BasePreference(
        modifier = modifier,
        titleId = pref.titleId,
        summary = "${pref.numColumns}x${pref.numRows}",
        index = index,
        groupSize = groupSize,
        isEnabled = isEnabled,
        onClick = onClick
    )
}

@Composable
fun StringMultiSelectionPreference(
    modifier: Modifier = Modifier,
    pref: BasePreferences.StringMultiSelectionPref,
    index: Int = 1,
    groupSize: Int = 1,
    isEnabled: Boolean = true,
    onClick: (() -> Unit) = {},
) {
    BasePreference(
        modifier = modifier,
        titleId = pref.titleId,
        summaryId = pref.summaryId,
        summary = pref.entries
            .filter { pref.onGetValue().contains(it.key) }
            .values.let {
                it.map { stringResource(id = it) }.joinToString(separator = ", ")
            },
        index = index,
        groupSize = groupSize,
        isEnabled = isEnabled,
        onClick = onClick
    )
}
