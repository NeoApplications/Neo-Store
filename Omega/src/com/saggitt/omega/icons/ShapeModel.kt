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

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import com.android.launcher3.R

class ShapeModel(val shapeName: String, var isSelected: Boolean) {

    fun getIcon(context: Context, shapeName: String?): Drawable? {
        return when (shapeName) {
            "circle" -> AppCompatResources.getDrawable(context, R.drawable.shape_circle)
            "square" -> AppCompatResources.getDrawable(context, R.drawable.shape_square)
            "rounded" -> AppCompatResources.getDrawable(context, R.drawable.shape_rounded)
            "squircle" -> AppCompatResources.getDrawable(context, R.drawable.shape_squircle)
            "sammy" -> AppCompatResources.getDrawable(context, R.drawable.shape_squircle)
            "teardrop" -> AppCompatResources.getDrawable(context, R.drawable.shape_teardrop)
            "cylinder" -> AppCompatResources.getDrawable(context, R.drawable.shape_cylinder)
            "cupertino" -> AppCompatResources.getDrawable(context, R.drawable.shape_cupertino)
            "octagon" -> AppCompatResources.getDrawable(context, R.drawable.shape_octagon)
            else -> AppCompatResources.getDrawable(context, R.drawable.ic_style)
        }
    }
}