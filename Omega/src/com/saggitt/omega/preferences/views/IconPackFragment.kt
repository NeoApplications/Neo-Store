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

package com.saggitt.omega.preferences.views

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.databinding.FragmentIconPackBinding
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.saggitt.omega.compose.components.ListItemWithIcon
import com.saggitt.omega.compose.components.PreferenceItem
import com.saggitt.omega.iconpack.IconPackProvider
import com.saggitt.omega.theme.OmegaAppTheme
import com.saggitt.omega.theme.OmegaTheme
import com.saggitt.omega.util.Config
import com.saggitt.omega.util.recreate

class IconPackFragment : Fragment(), SharedPreferences.OnSharedPreferenceChangeListener {
    lateinit var binding: FragmentIconPackBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentIconPackBinding.inflate(inflater, container, false)
        binding.installedPacks.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            val themeColor = Config.getCurrentTheme(requireContext())
            setContent {
                OmegaAppTheme(themeColor) {
                    IconPackList()
                }
            }

        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Utilities.getPrefs(requireContext()).registerOnSharedPreferenceChangeListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()

        Utilities.getPrefs(requireContext()).unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onResume() {
        super.onResume()
        requireActivity().title = requireActivity().getString(R.string.icon_pack)
    }

    override fun onSharedPreferenceChanged(sharedPrefs: SharedPreferences?, key: String) {
        if (key == "pref_icon_pack_package") {
            this@IconPackFragment.recreate()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun IconPackList() {
    val context = LocalContext.current
    val prefs = Utilities.getOmegaPrefs(context)
    val packs = IconPackProvider.INSTANCE.get(context).getIconPackList()

    val colors = RadioButtonDefaults.colors(
        selectedColor = Color(prefs.accentColor),
        unselectedColor = Color.Gray
    )

    val (selectedOption, onOptionSelected) = remember {
        mutableStateOf(prefs.iconPackPackage)
    }
    Column {

        LazyColumn {
            itemsIndexed(packs) { _, item ->
                ListItemWithIcon(
                    title = { Text(text = item.name, color = OmegaTheme.colors.textPrimary) },
                    modifier = Modifier.clickable {
                        prefs.iconPackPackage = item.packageName
                        onOptionSelected(item.packageName)
                    },
                    description = {
                        if (prefs.showDebugInfo)
                            Text(text = item.packageName, color = OmegaTheme.colors.textSecondary)
                    },
                    startIcon = {
                        Image(
                            painter = rememberDrawablePainter(drawable = item.icon),
                            contentDescription = null,
                            modifier = Modifier
                                .clip(CircleShape)
                                .size(36.dp)
                                .background(
                                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12F)
                                )
                        )
                    },
                    endCheckbox = {
                        RadioButton(
                            selected = (item.packageName == selectedOption),
                            onClick = {
                                prefs.iconPackPackage = item.packageName
                                onOptionSelected(item.packageName)
                            },
                            colors = colors
                        )
                    },
                    verticalPadding = 8.dp
                )
            }
        }

        PreferenceItem(
            title = {
                Text(
                    text = stringResource(id = R.string.get_more_icon_packs),
                    color = OmegaTheme.colors.textPrimary
                )
            },
            modifier = Modifier
                .clickable {
                    val intent =
                        Intent.parseUri(context.getString(R.string.market_search_intent), 0)
                    intent.data = intent.data!!.buildUpon()
                        .appendQueryParameter("q", context.getString(R.string.icon_pack)).build()
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                    if (intent.resolveActivity(context.packageManager) != null)
                        context.startActivity(intent)
                    else Toast.makeText(context, R.string.no_store_found, Toast.LENGTH_LONG)
                        .show()
                },
            startWidget = {
                Image(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = "",
                    modifier = Modifier
                        .size(44.dp)
                )
            },
            showDivider = true
        )
    }
}