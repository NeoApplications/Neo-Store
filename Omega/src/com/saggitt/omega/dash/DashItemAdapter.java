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

package com.saggitt.omega.dash;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.android.launcher3.AbstractFloatingView;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.util.Themes;

import java.util.ArrayList;

public class DashItemAdapter {
    private final ArrayList<View> mItemViews;
    private DashItemChangeListener circularItemChangeListener;
    private final Context mContext;

    public DashItemAdapter(LayoutInflater inflater, ArrayList<DashModel> items, Context context) {
        mContext = context;
        mItemViews = new ArrayList<>();
        for (DashModel dashItem : items) {
            View view = inflater.inflate(R.layout.dash_item, null);
            ImageView itemView = view.findViewById(R.id.bt_item);
            ColorStateList backgroundColor = ColorStateList.valueOf(Themes.getAttrColor(mContext, R.attr.dashIconBackground));
            itemView.setBackgroundTintList(backgroundColor);
            itemView.setImageDrawable(dashItem.getIcon());
            ColorStateList iconColor = ColorStateList.valueOf(Utilities.getOmegaPrefs(mContext).getAccentColor());
            itemView.setImageTintList(iconColor);

            itemView.setOnClickListener((parent) -> {
                dashItem.RunAction(dashItem.getAction(), mContext);
                AbstractFloatingView.closeAllOpenViews(Launcher.getLauncher(mContext));
            });
            mItemViews.add(view);
        }
    }

    public int getCount() {
        return mItemViews.size();
    }

    public View getItemAt(int i) {
        return mItemViews.get(i);
    }

    public ArrayList<View> getAllViews() {
        return mItemViews;
    }

    public void removeItemAt(int i) {
        if (mItemViews.size() > 0) {
            mItemViews.remove(i);
            notifyItemChange();
        }
    }

    public void addItem(View view) {
        mItemViews.add(view);
        notifyItemChange();
    }

    public void setOnItemChangeListener(DashItemChangeListener listener) {
        circularItemChangeListener = listener;
    }

    public void notifyItemChange() {
        circularItemChangeListener.onDashItemChange();
    }

    interface DashItemChangeListener {
        void onDashItemChange();
    }
}
