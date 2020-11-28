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

package com.saggitt.omega.feed;

import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.launcher3.R;

import java.util.ArrayList;
import java.util.List;

public class AddedWidgetsAdapter extends
        RecyclerView.Adapter<AddedWidgetsAdapter.WidgetsViewHolder> {

    private Context mContext;
    private int mDensity;
    private List<Widget> mAppWidgetProviderInfos = new ArrayList<>();

    private OnActionClickListener mOnActionClickListener;
    private static final String TAG = "AddedWidgetsAdapter";

    public AddedWidgetsAdapter(Context context, int density) {
        this.mContext = context;
        mDensity = density;
        mOnActionClickListener = (OnActionClickListener) mContext;
    }

    @NonNull
    @Override
    public WidgetsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_added_widget, viewGroup,
                false);
        WidgetsViewHolder widgetsViewHolder = new WidgetsViewHolder(view);
        widgetsViewHolder.actionBtn.setImageResource(R.drawable.ic_remove_widget_red_24dp);
        widgetsViewHolder.actionBtn.setOnClickListener(
                v -> {
                    int position = widgetsViewHolder.getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Widget widget = mAppWidgetProviderInfos.get(position);
                        mAppWidgetProviderInfos.remove(position);
                        mOnActionClickListener.removeWidget(widget.id);
                        notifyItemRemoved(position);
                    }
                });
        return widgetsViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull WidgetsViewHolder widgetsViewHolder, int i) {
        AppWidgetProviderInfo info = mAppWidgetProviderInfos.get(i).info;
        widgetsViewHolder.icon.setImageDrawable(info.loadIcon(mContext, mDensity));
        widgetsViewHolder.label.setText(info.loadLabel(mContext.getPackageManager()));
    }

    @Override
    public int getItemCount() {
        return mAppWidgetProviderInfos.size();
    }

    public void setAppWidgetProviderInfos(List<Widget> appWidgetProviderInfos) {
        this.mAppWidgetProviderInfos = appWidgetProviderInfos;
        notifyDataSetChanged();
    }

    public static class WidgetsViewHolder extends RecyclerView.ViewHolder {

        ImageView icon;
        TextView label;
        ImageView actionBtn;

        public WidgetsViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.widget_icon);
            label = itemView.findViewById(R.id.widget_label);
            actionBtn = itemView.findViewById(R.id.action_image_view);
        }
    }

    interface OnActionClickListener {
        void removeWidget(int id);
    }
}

