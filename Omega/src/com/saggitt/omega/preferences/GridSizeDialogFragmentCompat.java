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

import android.os.Bundle;
import android.view.View;
import android.widget.NumberPicker;

import androidx.appcompat.app.AlertDialog;
import androidx.core.util.Pair;
import androidx.preference.PreferenceDialogFragmentCompat;

import com.android.launcher3.R;
import com.saggitt.omega.util.OmegaUtilsKt;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class GridSizeDialogFragmentCompat extends PreferenceDialogFragmentCompat {

    private final String SAVE_STATE_ROWS = "rows";
    private final String SAVE_STATE_COLUMNS = "columns";

    private GridSizePreference gridSizePreference;

    private int numRows = 0;
    private int numColumns = 0;

    private NumberPicker numRowsPicker;
    private NumberPicker numColumnsPicker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gridSizePreference = (GridSizePreference) getPreference();
        Pair<Integer, Integer> size = gridSizePreference.getSize();

        if (savedInstanceState != null) {
            numRows = savedInstanceState.getInt(SAVE_STATE_ROWS);
            numColumns = savedInstanceState.getInt(SAVE_STATE_COLUMNS);
        } else {
            numRows = size.first;
            numColumns = size.second;
        }
    }

    public void onBindDialogView(View view) {
        super.onBindDialogView(view);

        numRowsPicker = view.findViewById(R.id.rowsPicker);
        numColumnsPicker = view.findViewById(R.id.columnsPicker);

        numRowsPicker.setMinValue(3);
        numRowsPicker.setMaxValue(9);
        numColumnsPicker.setMinValue(3);
        numColumnsPicker.setMaxValue(9);

        numRowsPicker.setValue(numRows);
        numColumnsPicker.setValue(numColumns);

    }

    public void onStart() {
        super.onStart();
        OmegaUtilsKt.applyAccent(((AlertDialog) Objects.requireNonNull(getDialog())));
    }

    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            gridSizePreference.setSize(numRowsPicker.getValue(), numColumnsPicker.getValue());
        }
    }

    public void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
        builder.setNeutralButton(R.string.theme_default, (dialogInterface, i) -> gridSizePreference.setSize(0, 0));
        builder.setNegativeButton(R.string.dialog_cancel, ((dialogInterface, i) -> {
            dismiss();
        }));
        builder.setPositiveButton(R.string.dialog_ok, ((dialogInterface, i) -> {
            saveGridSize();
        }));
    }

    public void saveGridSize() {
        gridSizePreference.setSize(numRowsPicker.getValue(), numColumnsPicker.getValue());
    }

    public void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(SAVE_STATE_ROWS, numRowsPicker.getValue());
        outState.putInt(SAVE_STATE_COLUMNS, numColumnsPicker.getValue());
    }

    public static GridSizeDialogFragmentCompat newInstance(String key) {
        GridSizeDialogFragmentCompat fragment = new GridSizeDialogFragmentCompat();
        Bundle bundle = new Bundle(1);
        bundle.putString(ARG_KEY, key);

        fragment.setArguments(bundle);
        return fragment;
    }
}
