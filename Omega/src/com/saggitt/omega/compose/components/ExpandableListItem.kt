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

import android.graphics.drawable.Drawable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.launcher3.R
import com.google.accompanist.drawablepainter.rememberDrawablePainter

@Composable
fun ExpandableListItem(
    title: String,
    icon: Drawable,
    content: @Composable ColumnScope.() -> Unit,
) {
    var isContentVisible by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                isContentVisible = !isContentVisible
            }
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberDrawablePainter(
                    drawable = icon
                ),
                contentDescription = null,
                modifier = Modifier
                    .clip(CircleShape)
                    .size(36.dp)
                    .background(
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05F)
                    )
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f)
            )

            val arrow =
                if (isContentVisible) R.drawable.ic_expand_less else R.drawable.ic_expand_more
            Image(
                painter = painterResource(id = arrow),
                contentDescription = null,
                modifier = Modifier
                    .clip(CircleShape)
                    .size(24.dp)
            )
            Spacer(modifier = Modifier.requiredWidth(12.dp))
        }
        AnimatedVisibility(visible = isContentVisible) {
            Column {
                content()
            }
        }
    }
}

@Preview
@Composable
fun ExpandableListItemPreview() {
    ExpandableListItem(
        title = "Title",
        icon = LocalContext.current.getDrawable(R.drawable.ic_launcher_foreground)!!,
        content = {
            Text(text = "Content")
        }
    )
}
