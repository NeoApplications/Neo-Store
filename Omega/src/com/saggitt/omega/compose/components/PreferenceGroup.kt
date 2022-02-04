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

package com.saggitt.omega.compose.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.saggitt.omega.theme.OmegaTheme

@Composable
fun PreferenceGroup(
    heading: String? = null,
    content: @Composable () -> Unit
) {
    PreferenceGroupHeading(heading)
    val columnModifier = Modifier
    CompositionLocalProvider(
        LocalContentAlpha provides ContentAlpha.medium,
            LocalContentColor provides OmegaTheme.colors.primary
    ) {
        Card(
                shape = RoundedCornerShape(16.dp),
                backgroundColor = OmegaTheme.colors.surface,
                border = BorderStroke(1.dp, OmegaTheme.colors.border)
        ) {
            Column(modifier = columnModifier) {
                content()
            }
        }
    }
}

@Composable
fun PreferenceGroupHeading(
    heading: String? = null
) {
    var spacerHeight = 0
    if (heading == null) spacerHeight += 8
    Spacer(modifier = Modifier.requiredHeight(spacerHeight.dp))
    if (heading != null) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .height(48.dp)
                .padding(horizontal = 32.dp)
                .fillMaxWidth()
        ) {
            CompositionLocalProvider(
                LocalContentAlpha provides ContentAlpha.medium,
                LocalContentColor provides MaterialTheme.colors.onBackground
            ) {
                Text(
                    text = heading,
                    style = MaterialTheme.typography.body2,
                        color = OmegaTheme.colors.primary
                )
            }
        }
    }
}