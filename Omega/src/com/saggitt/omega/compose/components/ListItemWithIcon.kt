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

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.launcher3.R
import com.saggitt.omega.util.addIf

@Composable
fun ListItemWithIcon(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    description: @Composable () -> Unit = {},
    startIcon: (@Composable () -> Unit)? = null,
    endCheckbox: (@Composable () -> Unit)? = null,
    isEnabled: Boolean = true,
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
            startIcon?.let {
                startIcon()
                if (applyPaddings) {
                    Spacer(modifier = Modifier.requiredWidth(16.dp))
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .addIf(!isEnabled) {
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

            endCheckbox?.let {
                if (applyPaddings) {
                    Spacer(modifier = Modifier.requiredWidth(16.dp))
                }
                endCheckbox()
            }
        }
    }
}

@Preview
@Composable
fun PreviewListItemWithIcon() {
    ListItemWithIcon(
        title = { Text(text = "System Iconpack") },
        modifier = Modifier.clickable { },
        description = { Text(text = "com.saggitt.iconpack") },
        startIcon = {
            Image(
                painterResource(id = R.drawable.ic_github),
                contentDescription = null,
                modifier = Modifier
                    .clip(CircleShape)
                    .size(40.dp)
                    .background(
                        MaterialTheme.colors.onBackground.copy(alpha = 0.12F)
                    )
            )

        },
        endCheckbox = {
            RadioButton(
                selected = false,
                onClick = null
            )
        }
    )
}