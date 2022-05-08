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

package com.saggitt.omega.groups.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.saggitt.omega.groups.AppGroupsManager

// TODO convert to single items
@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun AppCategorizationOptions() {
    val context = LocalContext.current
    val prefs = Utilities.getOmegaPrefs(context)

    val (selectedOption, onOptionSelected) = remember {
        mutableStateOf(prefs.appGroupsManager.categorizationType)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp)
    ) {
        /* Crear opcion de Folders */
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            tonalElevation = 2.dp,
            color = MaterialTheme.colorScheme.background,
            onClick = {
                prefs.appGroupsManager.categorizationType =
                    AppGroupsManager.CategorizationType.Folders
                onOptionSelected(AppGroupsManager.CategorizationType.Folders)
            }
        ) {
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.padding(start = 18.dp, top = 8.dp, bottom = 8.dp, end = 8.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(R.string.app_categorization_folders),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.pref_appcategorization_folders_summary),
                        style = MaterialTheme.typography.headlineLarge,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (prefs.appGroupsManager.categorizationType != AppGroupsManager.CategorizationType.Tabs) {
                    Column(
                        modifier = Modifier
                            .width(48.dp)
                            .padding(top = 6.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_check),
                            contentDescription = "",
                            modifier = Modifier
                                .size(30.dp)
                                .clickable {
                                    prefs.appGroupsManager.categorizationType =
                                        AppGroupsManager.CategorizationType.Folders
                                    onOptionSelected(AppGroupsManager.CategorizationType.Folders)
                                }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.requiredHeight(8.dp))

        /* Crear opcion de Tabs */
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            tonalElevation = 2.dp,
            color = MaterialTheme.colorScheme.background,
            onClick = {
                prefs.appGroupsManager.categorizationType =
                    AppGroupsManager.CategorizationType.Tabs
                onOptionSelected(AppGroupsManager.CategorizationType.Tabs)
            }
        ) {
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.padding(start = 18.dp, top = 8.dp, bottom = 8.dp, end = 8.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(R.string.app_categorization_tabs),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = stringResource(R.string.pref_appcategorization_tabs_summary),
                        style = MaterialTheme.typography.headlineLarge,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (prefs.appGroupsManager.categorizationType == AppGroupsManager.CategorizationType.Tabs) {
                    Column(
                        modifier = Modifier
                            .width(48.dp)
                            .padding(top = 6.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_check),
                            contentDescription = "",
                            modifier = Modifier
                                .size(30.dp)
                                .clickable {
                                    prefs.appGroupsManager.categorizationType =
                                        AppGroupsManager.CategorizationType.Tabs
                                    onOptionSelected(AppGroupsManager.CategorizationType.Tabs)
                                }
                        )
                    }
                }
            }
        }
    }
}
