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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
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
import com.saggitt.omega.theme.OmegaTheme

@Preview
@Composable
fun AppCategorizationItem() {
    val context = LocalContext.current
    val prefs = Utilities.getOmegaPrefs(context)
    val manager = AppGroupsManager(prefs)

    val (selectedOption, onOptionSelected) = remember {
        mutableStateOf(manager.categorizationType)
    }

    Column(
            modifier = Modifier
                    .fillMaxWidth()
                    .padding(2.dp)
    ) {
        Surface(
                modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            manager.categorizationType = AppGroupsManager.CategorizationType.Folders
                            onOptionSelected(AppGroupsManager.CategorizationType.Folders)
                        },
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, OmegaTheme.colors.border),
                elevation = 2.dp,
                color = OmegaTheme.colors.surface
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
                            style = MaterialTheme.typography.body1,
                            fontWeight = FontWeight.Bold,
                            color = OmegaTheme.colors.textPrimary
                    )
                    Text(
                            text = stringResource(R.string.pref_appcategorization_folders_summary),
                            style = MaterialTheme.typography.subtitle1,
                            fontSize = 14.sp,
                            color = OmegaTheme.colors.textSecondary
                    )
                }
                Log.d("AppCatItem", "Categorization: " + manager.categorizationType)
                if (manager.categorizationType != AppGroupsManager.CategorizationType.Tabs) {
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
                                            manager.categorizationType = AppGroupsManager.CategorizationType.Folders
                                            onOptionSelected(AppGroupsManager.CategorizationType.Folders)
                                        }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.requiredHeight(16.dp))

        Surface(
                modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            manager.categorizationType = AppGroupsManager.CategorizationType.Tabs
                            onOptionSelected(AppGroupsManager.CategorizationType.Tabs)
                        },
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, OmegaTheme.colors.border),
                elevation = 2.dp,
                color = OmegaTheme.colors.surface
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
                            style = MaterialTheme.typography.body1,
                            fontWeight = FontWeight.Bold,
                            color = OmegaTheme.colors.textPrimary
                    )
                    Text(
                            text = stringResource(R.string.pref_appcategorization_tabs_summary),
                            style = MaterialTheme.typography.subtitle1,
                            fontSize = 14.sp,
                            color = OmegaTheme.colors.textSecondary
                    )
                }

                if (manager.categorizationType == AppGroupsManager.CategorizationType.Tabs) {
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
                                            manager.categorizationType = AppGroupsManager.CategorizationType.Tabs
                                            onOptionSelected(AppGroupsManager.CategorizationType.Tabs)
                                        }
                        )
                    }
                }
            }
        }
    }
}
