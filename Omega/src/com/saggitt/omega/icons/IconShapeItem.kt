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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.android.launcher3.R

@Composable
fun IconShapeItem(
    modifier: Modifier = Modifier,
    item: ShapeModel,
    checked: Boolean,
    onClick: (ShapeModel) -> Unit = {}
) {
    Column(
        modifier = modifier
            .padding(4.dp)
            .requiredWidth(72.dp)
            .clickable {
                onClick(item)
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box {
            Surface(
                modifier = Modifier
                    .fillMaxHeight(0.7f)
                    .fillMaxWidth(0.8f)
                    .aspectRatio(1f),
                shape = item.getIcon(),
                color = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
            ) {}
            if (checked) {
                Icon(
                    modifier = Modifier
                        .fillMaxHeight(0.4f)
                        .fillMaxWidth(0.5f)
                        .aspectRatio(1f)
                        .align(Alignment.Center),
                    painter = painterResource(
                        id = if (item.shapeName == "system") R.drawable.ic_style
                        else R.drawable.ic_check),
                    tint = MaterialTheme.colorScheme.onBackground,
                    contentDescription = item.shapeName
                )
            }
        }

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