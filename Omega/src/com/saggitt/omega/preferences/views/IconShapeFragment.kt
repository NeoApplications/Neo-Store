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

package com.saggitt.omega.preferences.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.databinding.FragmentIconCustomizationBinding
import com.saggitt.omega.icons.IconShape
import com.saggitt.omega.icons.IconShapeItem
import com.saggitt.omega.icons.IconShapeManager
import com.saggitt.omega.icons.ShapeModel
import com.saggitt.omega.preferences.OmegaPreferences
import com.saggitt.omega.theme.OmegaAppTheme
import com.saggitt.omega.util.recreate

class IconShapeFragment : Fragment() {
    private lateinit var prefs: OmegaPreferences
    private lateinit var binding: FragmentIconCustomizationBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentIconCustomizationBinding.inflate(inflater, container, false)
        binding.shapeView.setContent {
            IconShapesPage()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = Utilities.getOmegaPrefs(context)
    }

    override fun onResume() {
        super.onResume()
        requireActivity().title = requireActivity().getString(R.string.title_theme_customize_icons)
    }

    @Composable
    fun IconShapesPage() {
        val systemShape = IconShapeManager.getSystemIconShape(context!!)
        val iconShapes = arrayOf(
            systemShape,
            IconShape.Circle,
            IconShape.Square,
            IconShape.RoundedSquare,
            IconShape.Squircle,
            IconShape.Sammy,
            IconShape.Teardrop,
            IconShape.Cylinder,
            IconShape.Cupertino
        )

        OmegaAppTheme {
            LazyVerticalGrid(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                columns = GridCells.Fixed(4),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                contentPadding = PaddingValues(8.dp)
            ) {
                itemsIndexed(
                    items = iconShapes.map {
                        ShapeModel(
                            it.toString(),
                            Utilities.getOmegaPrefs(context).iconShape == it
                        )
                    },
                    span = { _, _ -> GridItemSpan(1) },
                    key = { _: Int, item: ShapeModel -> item.shapeName }) { _, item ->
                    IconShapeItem(
                        item = item,
                        checked = item.isSelected,
                        onClick = {
                            prefs.iconShape = IconShape.fromString(item.shapeName)!!
                            this@IconShapeFragment.recreate()
                        }
                    )
                }
            }
        }
    }
}