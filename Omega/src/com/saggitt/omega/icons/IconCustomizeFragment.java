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
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.saggitt.omega.OmegaPreferences;
import com.saggitt.omega.util.OmegaUtilsKt;

import java.util.Objects;

public class IconCustomizeFragment extends Fragment {

    private View coloredView;
    private View shapeLessView;
    private View legacyView;
    private View whiteView;
    private View adaptiveView;
    private OmegaPreferences prefs;
    private boolean coloredIcons;
    private boolean shapeLess;
    private boolean legacy;
    private boolean white;
    private boolean adaptive;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_icon_customization, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Context mContext = getActivity();

        //Load Preferences
        prefs = Utilities.getOmegaPrefs(mContext);
        coloredIcons = prefs.getColorizedLegacyTreatment();
        shapeLess = prefs.getForceShapeless();
        legacy = prefs.getEnableLegacyTreatment();
        white = prefs.getEnableWhiteOnlyTreatment();
        adaptive = prefs.getAdaptifyIconPacks();

        //Load Shapes
        RecyclerView shapeView = view.findViewById(R.id.shape_view);
        GridLayoutManager layoutManager = new GridLayoutManager(mContext, 4);
        shapeView.setLayoutManager(layoutManager);
        IconShapeAdapter adapter = new IconShapeAdapter(Objects.requireNonNull(mContext));
        shapeView.setAdapter(adapter);

        //Load switch preferences
        coloredView = view.findViewById(R.id.colored_icons);
        shapeLessView = view.findViewById(R.id.shapeless_icons);
        legacyView = view.findViewById(R.id.legacy_icons);
        whiteView = view.findViewById(R.id.white_icons);
        adaptiveView = view.findViewById(R.id.adaptive_icons);

        //setup switch preferences
        setupSwitchView(shapeLessView, shapeLess);
        setupSwitchView(legacyView, legacy);
        setupSwitchView(whiteView, white);
        setupSwitchView(adaptiveView, adaptive);
        setupSwitchView(coloredView, coloredIcons);
        hideViews();
    }

    /*
     * Hidde options when the android version is lower than oreo
     * */
    private void hideViews() {
        if (!Utilities.ATLEAST_OREO) {
            coloredView.setVisibility(View.GONE);
            shapeLessView.setVisibility(View.GONE);
            legacyView.setVisibility(View.GONE);
            whiteView.setVisibility(View.GONE);
            adaptiveView.setVisibility(View.GONE);
        }
    }

    /*
     * Sync switch view according to the preference state.
     * */
    private void setupSwitchView(View itemView, boolean isChecked) {
        Switch switchView = itemView.findViewById(R.id.switchWidget);
        OmegaUtilsKt.applyColor(switchView, prefs.getAccentColor());
        syncSwitch(switchView, isChecked);
        itemView.setOnClickListener(view -> performClick(view, switchView));
    }

    public void performClick(View view, Switch switchView) {
        if (view == coloredView) {
            coloredIcons = !coloredIcons;
            syncSwitch(switchView, coloredIcons);
            prefs.setColorizedLegacyTreatment(coloredIcons);
            updateWhite(coloredIcons);
        } else if (view == shapeLessView) {
            shapeLess = !shapeLess;
            syncSwitch(switchView, shapeLess);
            prefs.setForceShapeless(shapeLess);
        } else if (view == legacyView) {
            legacy = !legacy;
            syncSwitch(switchView, legacy);
            prefs.setEnableLegacyTreatment(legacy);
            if (!legacy) {
                updateColoredBackground(false);
                updateAdaptive(false);
                updateWhite(false);
            } else {
                updateColoredBackground(true);
                updateAdaptive(true);
                updateWhite(true);
            }
        } else if (view == whiteView) {
            white = !white;
            syncSwitch(switchView, white);
            prefs.setEnableWhiteOnlyTreatment(white);
        } else if (view == adaptiveView) {
            adaptive = !adaptive;
            syncSwitch(switchView, adaptive);

            prefs.setAdaptifyIconPacks(adaptive);
        }
    }

    private void updateColoredBackground(boolean state) {
        if (!state) {
            coloredView.setClickable(false);
            prefs.setColorizedLegacyTreatment(false);
            coloredView.findViewById(R.id.switchWidget).setEnabled(false);
        } else {
            coloredView.setClickable(true);
            coloredView.findViewById(R.id.switchWidget).setEnabled(true);
        }
    }

    private void updateWhite(boolean state) {
        if (!state) {
            whiteView.setClickable(false);
            prefs.setEnableWhiteOnlyTreatment(false);
            whiteView.findViewById(R.id.switchWidget).setEnabled(false);
        } else {
            whiteView.setClickable(true);
            whiteView.findViewById(R.id.switchWidget).setEnabled(true);
        }
    }

    private void updateAdaptive(boolean state) {
        if (!state) {
            adaptiveView.setClickable(false);
            prefs.setAdaptifyIconPacks(false);
            adaptiveView.findViewById(R.id.switchWidget).setEnabled(false);
        } else {
            adaptiveView.setClickable(true);
            adaptiveView.findViewById(R.id.switchWidget).setEnabled(true);
        }
    }

    private void syncSwitch(Switch switchCompat, boolean checked) {
        switchCompat.setChecked(checked);
    }
}