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

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;

import com.android.launcher3.R;

public class PreviewIconModel {
    private int itemIcon;
    private int itemName;
    private int iconBackground;

    public PreviewIconModel(int icon, int name, int color) {
        itemIcon = icon;
        itemName = name;
        iconBackground = color;
    }

    public Drawable getItemIcon(Context context) {
        return context.getResources().getDrawable(itemIcon, null);
    }

    public int getItemName() {
        return itemName;
    }

    public int getItemColor() {
        return iconBackground;
    }

    public Drawable getShape(Context context, String shapeName, int tintColor) {
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
        drawable.setColorFilter(tintColor, PorterDuff.Mode.SRC_IN);
        return drawable;
    }
}
