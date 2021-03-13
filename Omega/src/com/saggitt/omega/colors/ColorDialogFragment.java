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

package com.saggitt.omega.colors;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.viewpager.widget.ViewPager;

import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.google.android.material.tabs.TabLayout;
import com.saggitt.omega.util.CustomPagerAdapter;

public class ColorDialogFragment extends RelativeLayout {

    public ColorDialogFragment(Context context) {
        this(context, null);
    }

    public ColorDialogFragment(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorDialogFragment(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.dialog_color_selector, this);
        View tabCustom = findViewById(R.id.tab_custom);
        View tabPresets = findViewById(R.id.tab_presets);
        View[] views = {tabCustom, tabPresets};
        String[] titles = {context.getString(R.string.custom), context.getString(R.string.color_presets)};

        ViewPager viewPager = findViewById(R.id.color_pager);
        viewPager.setOffscreenPageLimit(1);
        viewPager.setAdapter(new CustomPagerAdapter(views, titles));

        TabLayout tabLayout = findViewById(R.id.color_tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setSelectedTabIndicatorColor(Utilities.getOmegaPrefs(context).getAccentColor());
    }
}
