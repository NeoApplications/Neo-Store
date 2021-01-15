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

package com.saggitt.omega.icons;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;

import com.android.launcher3.R;

public class ShapeModel {

    private final String shapeName;
    private boolean isSelected;

    public ShapeModel(String shape, boolean selected) {
        this.shapeName = shape;
        this.isSelected = selected;
    }

    public String getShapeName() {
        return shapeName;
    }

    public boolean getIsSelected() {
        return isSelected;
    }

    public void setIsSelected(boolean selected) {
        this.isSelected = selected;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public Drawable getIcon(Context context, String shapeName) {
        Drawable drawable = context.getResources().getDrawable(R.drawable.shape_circle, null);
        switch (shapeName) {
            case "circle":
                drawable = context.getDrawable(R.drawable.shape_circle);
                break;
            case "square":
                drawable = context.getDrawable(R.drawable.shape_square);
                break;
            case "roundedSquare":
                drawable = context.getDrawable(R.drawable.shape_rounded);
                break;
            case "squircle":
                drawable = context.getDrawable(R.drawable.shape_squircle);
                break;
            case "teardrop":
                drawable = context.getDrawable(R.drawable.shape_teardrop);
                break;
            case "cylinder":
                drawable = context.getDrawable(R.drawable.shape_cylinder);
                break;
        }
        return drawable;
    }
}
