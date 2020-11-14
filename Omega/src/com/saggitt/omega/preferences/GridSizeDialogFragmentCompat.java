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
