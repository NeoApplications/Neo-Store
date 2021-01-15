/*
 *  This file is part of Omega Launcher.
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
import android.widget.Switch;

import androidx.preference.PreferenceViewHolder;
import androidx.preference.SwitchPreference;

import com.android.launcher3.Utilities;
import com.saggitt.omega.util.OmegaUtilsKt;

public class StyledSwitchPreference extends SwitchPreference {
    public StyledSwitchPreference(Context context) {
        super(context);
    }

    public StyledSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StyledSwitchPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        Switch checkableView = (Switch) holder.findViewById(android.R.id.switch_widget);
        OmegaUtilsKt.applyColor(checkableView, Utilities.getOmegaPrefs(getContext()).getAccentColor());
    }
}
