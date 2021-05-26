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
package com.saggitt.omega.feed

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.launcher3.R

class AddedWidgetsAdapter(private val mContext: Context, private val mDensity: Int) :
    RecyclerView.Adapter<AddedWidgetsAdapter.WidgetsViewHolder>() {
    private var mAppWidgetProviderInfos: MutableList<Widget> = ArrayList()
    private val mOnActionClickListener: OnActionClickListener = mContext as OnActionClickListener

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): WidgetsViewHolder {
        val view = LayoutInflater.from(mContext).inflate(
            R.layout.item_added_widget, viewGroup,
            false
        )
        val widgetsViewHolder = WidgetsViewHolder(view)
        widgetsViewHolder.actionBtn.setImageResource(R.drawable.ic_remove_widget_red_24dp)
        widgetsViewHolder.actionBtn.setOnClickListener {
            val position = widgetsViewHolder.bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val widget = mAppWidgetProviderInfos[position]
                mAppWidgetProviderInfos.removeAt(position)
                mOnActionClickListener.removeWidget(widget.id)
                notifyItemRemoved(position)
            }
        }
        return widgetsViewHolder
    }

    override fun onBindViewHolder(widgetsViewHolder: WidgetsViewHolder, i: Int) {
        val info = mAppWidgetProviderInfos[i].info
        widgetsViewHolder.icon.setImageDrawable(info?.loadIcon(mContext, mDensity))
        widgetsViewHolder.label.text = info?.loadLabel(mContext.packageManager)
    }

    override fun getItemCount(): Int {
        return mAppWidgetProviderInfos.size
    }

    fun setAppWidgetProviderInfos(appWidgetProviderInfos: MutableList<Widget>) {
        mAppWidgetProviderInfos = appWidgetProviderInfos
        notifyDataSetChanged()
    }

    class WidgetsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var icon: ImageView = itemView.findViewById(R.id.widget_icon)
        var label: TextView = itemView.findViewById(R.id.widget_label)
        var actionBtn: ImageView = itemView.findViewById(R.id.action_image_view)
    }

    internal interface OnActionClickListener {
        fun removeWidget(id: Int)
    }
}