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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.util.ComponentKey
import com.saggitt.omega.groups.DrawerTabs
import com.saggitt.omega.preferences.views.AppCategorizationFragment
import com.saggitt.omega.preferences.views.PreferencesActivity
import com.saggitt.omega.util.addOrRemove

@Composable
fun AppTabDialog(
        componentKey: ComponentKey,
        openDialogCustom: MutableState<Boolean>
) {
    Dialog(onDismissRequest = { openDialogCustom.value = false }) {
        AppTabDialogUI(
                componentKey = componentKey,
                openDialogCustom = openDialogCustom
        )
    }
}

@Composable
fun AppTabDialogUI(
        componentKey: ComponentKey,
        openDialogCustom: MutableState<Boolean>
) {
    val context = LocalContext.current
    val prefs = Utilities.getOmegaPrefs(context)

    var radius = 16.dp
    if (prefs.customWindowCorner) {
        radius = prefs.windowCornerRadius.dp
    }
    val cornerRadius by remember { mutableStateOf(radius) }

    Card(
            shape = RoundedCornerShape(cornerRadius),
            modifier = Modifier.padding(8.dp),
            elevation = 8.dp,
            backgroundColor = MaterialTheme.colorScheme.background
    ) {
        Column {
            val tabs: List<DrawerTabs.CustomTab> =
                    prefs.drawerTabs.getGroups().mapNotNull { it as? DrawerTabs.CustomTab }
            val entries = tabs.map { it.title }.toList()
            val checkedEntries = tabs.map {
                it.contents.value().contains(componentKey)
            }.toBooleanArray()

            val selectedItems = checkedEntries.toMutableList()
            LazyColumn(
                    modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
            ) {
                itemsIndexed(entries) { index, tabName ->
                    var isSelected by rememberSaveable { mutableStateOf(selectedItems[index]) }
                    Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    isSelected = !isSelected
                                    selectedItems[index] = isSelected
                                },
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                                checked = isSelected,
                                onCheckedChange = {
                                    isSelected = !isSelected
                                    selectedItems[index] = isSelected
                                },
                                modifier = Modifier.padding(start = 8.dp, end = 8.dp),
                                colors = CheckboxDefaults.colors(
                                        checkedColor = MaterialTheme.colorScheme.primary,
                                )
                        )
                        Text(text = tabName, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            //Button Rows
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                OutlinedButton(
                        shape = RoundedCornerShape(cornerRadius),
                        onClick = {
                            openDialogCustom.value = false
                            PreferencesActivity.startFragment(
                                    context, AppCategorizationFragment::class.java.name,
                                    context.resources.getString(R.string.title__drawer_categorization)
                            )
                        },
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(0.95f)),
                        colors = ButtonDefaults.buttonColors(
                                backgroundColor = MaterialTheme.colorScheme.primary.copy(0.65f),
                                contentColor = MaterialTheme.colorScheme.surface
                        )
                ) {
                    Text(
                            text = stringResource(id = R.string.tabs_manage),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)
                    )
                }

                Spacer(Modifier.weight(1f))

                OutlinedButton(
                        shape = RoundedCornerShape(cornerRadius),
                        onClick = {
                            openDialogCustom.value = false
                        },
                        colors = ButtonDefaults.buttonColors(
                                backgroundColor = MaterialTheme.colorScheme.surface.copy(0.15f),
                                contentColor = MaterialTheme.colorScheme.onSurface
                        )
                ) {
                    Text(
                            text = stringResource(id = android.R.string.cancel),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)
                    )
                }

                OutlinedButton(
                        shape = RoundedCornerShape(cornerRadius),
                        onClick = {
                            tabs.forEachIndexed { index, tab ->
                                tab.contents.value().addOrRemove(componentKey, selectedItems[index])
                            }
                            tabs.hashCode()
                            prefs.appGroupsManager.drawerTabs.saveToJson()
                            openDialogCustom.value = false
                        },
                        modifier = Modifier.padding(start = 16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(0.95f)),
                        colors = ButtonDefaults.buttonColors(
                                backgroundColor = MaterialTheme.colorScheme.primary.copy(0.65f),
                                contentColor = MaterialTheme.colorScheme.surface
                        )
                ) {
                    Text(
                            text = stringResource(id = android.R.string.ok),
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)
                    )
                }
            }
        }
    }
}