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

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.saggitt.omega.util.addIf

@Composable
fun PreferenceItem(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    description: @Composable () -> Unit = {},
    startWidget: (@Composable () -> Unit)? = null,
    endWidget: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    showDivider: Boolean = false,
    dividerIndent: Dp = 0.dp,
    applyPaddings: Boolean = true,
    horizontalPadding: Dp = 16.dp,
    verticalPadding: Dp = 16.dp,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically
) {
    Column {
        if (showDivider) {
            Divider(
                modifier = Modifier.padding(horizontal = 16.dp),
                startIndent = dividerIndent,
            )
        }
        Row(
            verticalAlignment = verticalAlignment,
            modifier = modifier
                .fillMaxWidth()
                .addIf(applyPaddings) {
                    padding(horizontal = horizontalPadding, vertical = verticalPadding)
                }
        ) {
            startWidget?.let {
                startWidget()
                if (applyPaddings) {
                    Spacer(modifier = Modifier.requiredWidth(16.dp))
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .addIf(!enabled) {
                        alpha(ContentAlpha.disabled)
                    }
            ) {
                CompositionLocalProvider(
                    LocalContentColor provides MaterialTheme.colors.onBackground,
                    LocalTextStyle provides MaterialTheme.typography.subtitle1
                ) {
                    title()
                }
                CompositionLocalProvider(
                    LocalContentAlpha provides ContentAlpha.medium,
                    LocalContentColor provides MaterialTheme.colors.onBackground,
                    LocalTextStyle provides MaterialTheme.typography.body2
                ) {
                    description()
                }
            }
            endWidget?.let {
                if (applyPaddings) {
                    Spacer(modifier = Modifier.requiredWidth(16.dp))
                }
                endWidget()
            }
        }
    }
}
