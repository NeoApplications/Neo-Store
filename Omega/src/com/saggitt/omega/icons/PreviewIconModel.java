/*
 * Copyright (c) 2020 Omega Launcher
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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
