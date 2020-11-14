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
