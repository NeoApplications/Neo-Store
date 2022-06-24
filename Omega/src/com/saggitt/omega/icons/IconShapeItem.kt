/*
 *  This file is part of Omega Launcher
 *  Copyright (c) 2021   Saul Henriquez
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
package com.saggitt.omega.icons

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.accompanist.drawablepainter.rememberDrawablePainter

// TODO add checkmark on selected shape?
@Composable
fun IconShapeItem(
    modifier: Modifier = Modifier,
    item: ShapeModel,
    checked: Boolean,
    onClick: (ShapeModel) -> Unit = {}
) {
    val (checked, check) = remember { mutableStateOf(checked) }

    Column(
        modifier = modifier
            .padding(4.dp)
            .requiredWidth(72.dp)
            .clickable {
                onClick(item)
                item.isSelected = !item.isSelected
                check(item.isSelected)
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            modifier = Modifier
                .fillMaxHeight(0.7f)
                .fillMaxWidth(0.8f)
                .aspectRatio(1f),
            painter = rememberDrawablePainter(
                drawable = item.getIcon(
                    LocalContext.current,
                    item.shapeName
                )
            ),
            tint = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
            contentDescription = item.shapeName
        )

        Text(
            text = item.shapeName,
            modifier = Modifier.fillMaxWidth(),
            softWrap = true,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleSmall
        )
    }
}