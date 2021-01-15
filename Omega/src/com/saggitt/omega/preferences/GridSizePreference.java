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

package com.saggitt.omega.preferences;

import android.content.Context;
import android.util.AttributeSet;

import androidx.core.util.Pair;
import androidx.preference.DialogPreference;

import com.android.launcher3.R;
import com.android.launcher3.Utilities;

public class GridSizePreference extends DialogPreference {
    private GridSize2D gridSize;
    private Pair defaultSize;

    public GridSizePreference(Context context) {
        this(context, null, 0);
    }

    public GridSizePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        updateSummary();
    }

    public GridSizePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        updateSummary();
    }

    public Pair getSize() {

        gridSize = Utilities.getOmegaPrefs(getContext()).getGridSize();
        defaultSize = new Pair(gridSize.getNumRowsOriginal(), gridSize.getNumColumnsOriginal());

        int rows = gridSize.fromPref(gridSize.getNumRows(), (int) defaultSize.first);
        int columns = gridSize.fromPref(gridSize.getNumColumns(), (int) defaultSize.second);
        return new Pair(rows, columns);
    }


    public void setSize(int rows, int columns) {
        gridSize.setNumRowsPref(gridSize.toPref(rows, (int) defaultSize.first));
        gridSize.setNumColumnsPref(gridSize.toPref(columns, (int) defaultSize.second));
        updateSummary();
    }


    private void updateSummary() {
        Pair value = getSize();
        setSummary(String.format("%d x %d", value.first, value.second));
    }

    @Override
    public int getDialogLayoutResource() {
        return R.layout.pref_dialog_grid_size;
    }
}
