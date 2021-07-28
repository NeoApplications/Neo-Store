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

package com.saggitt.omega.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun LoadTranslators(translators: List<String>) {
    val translatorsSize = translators.size - 2
    val languages: ArrayList<String> = arrayListOf()
    Column {
        for (i in 0..translatorsSize step 4) {
            if (languages.size > 0 && !languages.contains(translators[i + 1])) {
                Spacer(modifier = Modifier.height(16.dp))
            }
            if (!languages.contains(translators[i + 1])) {
                languages.add(translators[i + 1])
                Column(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp)
                        .clip(shape = MaterialTheme.shapes.large)
                ) {
                    Text(
                        text = translators[i + 1],
                        style = MaterialTheme.typography.subtitle1,
                        color = MaterialTheme.colors.onBackground
                    )

                    CompositionLocalProvider(
                        LocalContentAlpha provides ContentAlpha.medium,
                        LocalContentColor provides MaterialTheme.colors.onBackground
                    ) {
                        Text(
                            text = translators[i] + " <" + translators[i + 2] + ">",
                            style = MaterialTheme.typography.body2
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp)
                        .clip(shape = MaterialTheme.shapes.large)
                ) {
                    CompositionLocalProvider(
                        LocalContentAlpha provides ContentAlpha.medium,
                        LocalContentColor provides MaterialTheme.colors.onBackground
                    ) {
                        Text(
                            text = translators[i] + " <" + translators[i + 2] + ">",
                            style = MaterialTheme.typography.body2
                        )
                    }
                }
            }
        }
    }
}