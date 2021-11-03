/*
 *  This file is part of Omega Launcher
 *  Copyright (c) 2021   Omega Launcher Team
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saggitt.omega.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PreferenceGroup(
    heading: String? = null,
    content: @Composable () -> Unit
) {
    PreferenceGroupHeading(heading)
    val columnModifier = Modifier
    CompositionLocalProvider(
        LocalContentAlpha provides ContentAlpha.medium,
        LocalContentColor provides MaterialTheme.colorScheme.onBackground
    ) {
        Card(
            //border = BorderStroke(1.dp, Grey600),
            shape = RoundedCornerShape(8.dp)
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
                LocalContentColor provides MaterialTheme.colorScheme.onBackground
            ) {
                Text(
                    text = heading,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}