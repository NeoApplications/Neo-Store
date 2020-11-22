/*
 *  Copyright (c) 2020 Omega Launcher
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
 *
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
            ColorStateList iconColor = ColorStateList.valueOf(Themes.getAttrColor(mContext, R.attr.dashIconTint));
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
