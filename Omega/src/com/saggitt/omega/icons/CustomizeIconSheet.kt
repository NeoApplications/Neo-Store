/*
 *  This file is part of Neo Launcher
 *  Copyright (c) 2022   Neo Launcher Team
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

import android.app.Activity
import android.graphics.drawable.Drawable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.android.launcher3.LauncherAppState
import com.android.launcher3.R
import com.android.launcher3.util.ComponentKey
import com.android.launcher3.util.PackageManagerHelper
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.saggitt.omega.allapps.CustomAppFilter
import com.saggitt.omega.compose.ComposeActivity
import com.saggitt.omega.compose.components.ComposeSwitchView
import com.saggitt.omega.compose.components.PreferenceItem
import com.saggitt.omega.compose.components.preferences.PreferenceGroup
import com.saggitt.omega.groups.ui.AppTabDialog
import com.saggitt.omega.preferences.OmegaPreferences
import com.saggitt.omega.util.addIfNotNull

@Composable
fun CustomizeIconSheet(
    icon: Drawable,
    defaultTitle: String,
    componentKey: ComponentKey,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val prefs = OmegaPreferences.getInstance(context)
    var title by remember { mutableStateOf("") }
    val request =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult
            onClose()
        }

    val openEditIcon = {
        val destination = "edit_icon/$componentKey/"
        request.launch(ComposeActivity.createIntent(context, destination))
    }

    DisposableEffect(key1 = null) {
        title = prefs.customAppName[componentKey] ?: defaultTitle
        onDispose {
            val previousTitle = prefs.customAppName[componentKey]
            val newTitle = if (title != defaultTitle) title else null
            if (newTitle != previousTitle) {
                prefs.customAppName[componentKey] = newTitle
                val model = LauncherAppState.getInstance(context).model
                model.onPackageChanged(componentKey.componentName.packageName, componentKey.user)
            }
        }
    }

    CustomizeIconView(
        icon = icon,
        title = title,
        onTitleChange = { title = it },
        defaultTitle = defaultTitle,
        componentKey = componentKey,
        launchSelectIcon = openEditIcon,
    )
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CustomizeIconView(
    icon: Drawable,
    title: String,
    onTitleChange: (String) -> Unit,
    defaultTitle: String,
    componentKey: ComponentKey,
    launchSelectIcon: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val prefs = OmegaPreferences.getInstance(context)
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Divider(
            modifier = Modifier
                .width(48.dp)
                .height(2.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 16.dp)
                .addIfNotNull(launchSelectIcon) {
                    clickable(onClick = it)
                }
                .clip(MaterialTheme.shapes.small)
        ) {
            Image(
                painter = rememberDrawablePainter(icon),
                contentDescription = title,
                modifier = Modifier
                    .requiredSize(64.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            modifier = Modifier
                .fillMaxWidth(),
            trailingIcon = {
                if (title != defaultTitle) {
                    IconButton(
                        onClick = { onTitleChange(defaultTitle) }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.undo),
                            contentDescription = stringResource(id = R.string.accessibility_close)
                        )
                    }
                }
            },
            singleLine = true,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12F),
                textColor = MaterialTheme.colorScheme.onSurface
            ),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = {
                keyboardController?.hide()
            }),
            shape = MaterialTheme.shapes.large,
            label = { Text(text = stringResource(id = R.string.app_name)) },
            isError = title.isEmpty()
        )

        Spacer(modifier = Modifier.height(16.dp))

        PreferenceGroup {
            if (!componentKey.componentName.equals("com.saggitt.omega.folder")) {
                ComposeSwitchView(
                    title = stringResource(R.string.hide_app),
                    isChecked = CustomAppFilter.isHiddenApp(context, componentKey),
                    onCheckedChange = { newValue ->
                        CustomAppFilter.setComponentNameState(
                            context,
                            componentKey.toString(),
                            newValue
                        )
                    }
                )

                if (prefs.drawerTabs.isEnabled) {
                    val openDialogCustom = remember { mutableStateOf(false) }
                    PreferenceItem(
                        title = stringResource(R.string.app_categorization_tabs),
                        modifier = Modifier.clickable {
                            openDialogCustom.value = true
                        }
                    )
                    if (openDialogCustom.value) {
                        AppTabDialog(
                            componentKey = componentKey,
                            openDialogCustom = openDialogCustom
                        )
                    }
                }
            }
        }
        if (prefs.showDebugInfo.onGetValue()) {
            val appInfo =
                componentKey.componentName.packageName + "/" + componentKey.componentName.className
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 16.dp, start = 8.dp, end = 8.dp)
                    .height(1.dp),
                color = MaterialTheme.colorScheme.outline
            )

            Text(
                text = stringResource(id = R.string.debug_options_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))
            PreferenceItem(
                title = stringResource(id = R.string.debug_component_name),
                summary = appInfo
            )
            PreferenceItem(
                title = stringResource(id = R.string.app_version),
                summary = PackageManagerHelper(context).getPackageVersion(componentKey.componentName.packageName)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}