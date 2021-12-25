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

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.saggitt.omega.icons.IconShape
import com.saggitt.omega.icons.IconShapeManager
import com.saggitt.omega.icons.ItemIconShape
import com.saggitt.omega.icons.ShapeModel
import com.saggitt.omega.preferences.OmegaPreferences

class IconShapeFragment : Fragment(){

    private lateinit var prefs: OmegaPreferences

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_icon_customization, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context: Context? = activity
        val systemShape = IconShapeManager.getSystemIconShape(context!!)
        val iconShapes = arrayOf(systemShape, IconShape.Circle, IconShape.Square, IconShape.RoundedSquare,
            IconShape.Squircle, IconShape.Sammy, IconShape.Teardrop, IconShape.Cylinder)
        val iconShapeItems = iconShapes.map {
            ItemIconShape(context, ShapeModel(
                    it.toString(),
                    Utilities.getOmegaPrefs(context).iconShape == it))
        }

        prefs = Utilities.getOmegaPrefs(context)

        val fastItemAdapter = ItemAdapter<ItemIconShape>()

        val fastAdapter = FastAdapter.with(fastItemAdapter)
        fastAdapter.setHasStableIds(true)

        fastItemAdapter.set(iconShapeItems)

        //Load Shapes
        val shapeView: RecyclerView = view.findViewById(R.id.shape_view)
        shapeView.layoutManager = GridLayoutManager(context, 4)
        shapeView.adapter = fastAdapter
    }

    override fun onResume() {
        super.onResume()
        requireActivity().title = requireActivity().getString(R.string.title_theme_customize_icons)
    }
}