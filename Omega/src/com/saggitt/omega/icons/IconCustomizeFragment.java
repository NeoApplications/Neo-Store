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
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.saggitt.omega.OmegaPreferences;
import com.saggitt.omega.adaptive.IconShapeCustomizeView;
import com.saggitt.omega.theme.ThemeOverride;
import com.saggitt.omega.theme.ThemedContextProvider;
import com.saggitt.omega.util.OmegaUtilsKt;

import org.jetbrains.annotations.NotNull;

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
    private IconShapeAdapter adapter;

    private IconShapeCustomizeView customizeView;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

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
        shapeView.setLayoutManager(new GridLayoutManager(mContext, 4));
        adapter = new IconShapeAdapter(Objects.requireNonNull(mContext));
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

        customizeView = view.findViewById(R.id.customizeView);
    }

    public void showDialog() {
        ContextThemeWrapper themedContext = new ThemedContextProvider(getContext(), null, new ThemeOverride.Settings()).get();
        AlertDialog.Builder dialog = new AlertDialog.Builder(themedContext, new ThemeOverride.AlertDialog().getTheme(getContext()));
        dialog.setTitle(R.string.menu_icon_shape);
        dialog.setView(R.layout.icon_shape_customize_view);
        dialog.setPositiveButton(android.R.string.ok, (dialog1, which) -> new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                prefs.setIconShape(customizeView.getCurrentShape().getHashString());
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        dialog.setNegativeButton(android.R.string.cancel, null);
        dialog.create();
        dialog.show();
    }

    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_icon_shape, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_custom_shape) {
            showDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
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