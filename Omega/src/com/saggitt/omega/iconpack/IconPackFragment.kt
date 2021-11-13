/*
 *  This file is part of Omega Launcher
 *  Copyright (c) 2021   Omega Launcher Team
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

package com.saggitt.omega.iconpack

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.saggitt.omega.preferences.OmegaPreferences
import com.saggitt.omega.ui.component.ListItemWithIcon

class IconPackFragment : Fragment() {
    lateinit var iconPack: String
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return inflater.inflate(R.layout.base_compose_fragment, container, false).apply {
            findViewById<ComposeView>(R.id.base_compose_view).setContent {
                iconPack = iconPackList()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        OmegaPreferences.getInstance(requireContext()).iconPackPackage.onSetValue(iconPack)
    }
}

@Composable
fun iconPackList(): String {
    val context = LocalContext.current
    val ipm = IconPackProvider(context)
    val packList = ipm.getIconPackList()

    val prefs = Utilities.getOmegaPrefs(context)
    val (selectedOption, onOptionSelected) = remember {
        mutableStateOf(prefs.iconPackPackage.onGetValue())
    }

    LazyColumn {
        itemsIndexed(packList) { _, item ->
            ListItemWithIcon(
                title = { Text(text = item.name) },
                modifier = Modifier.clickable {
                    onOptionSelected(item.packageName)
                },

                description = {
                    if (prefs.showDebugInfo)
                        Text(text = item.packageName)
                },
                startIcon = {
                    Image(
                        painter = rememberDrawablePainter(drawable = item.icon),
                        contentDescription = null,
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(36.dp)
                            .background(
                                MaterialTheme.colors.onBackground.copy(alpha = 0.12F)
                            )
                    )
                },
                endCheckbox = {
                    RadioButton(
                        selected = (item.packageName == selectedOption),
                        onClick = { onOptionSelected(item.packageName) }
                    )
                }
            )
        }
    }
    return selectedOption
}