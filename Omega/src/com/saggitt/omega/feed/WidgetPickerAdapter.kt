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
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.UserHandle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.launcher3.R

class WidgetPickerAdapter(private val mContext: Context) :
    RecyclerView.Adapter<WidgetPickerAdapter.WidgetsViewHolder>() {
    private var mItems: List<Item> = ArrayList()
    private val mOnClickListener: OnClickListener = mContext as OnClickListener

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): WidgetsViewHolder {
        val view = LayoutInflater.from(mContext).inflate(
            R.layout.item_all_widget, viewGroup,
            false
        )
        val widgetsViewHolder = WidgetsViewHolder(view)
        widgetsViewHolder.itemView.setOnClickListener {
            val position = widgetsViewHolder.bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                mOnClickListener.onClick(mItems[position])
            }
        }
        return widgetsViewHolder
    }

    override fun onBindViewHolder(widgetsViewHolder: WidgetsViewHolder, i: Int) {
        val info = mItems[i]
        widgetsViewHolder.icon.setImageDrawable(info.icon)
        widgetsViewHolder.label.text = info.label
    }

    override fun getItemCount(): Int {
        return mItems.size
    }

    fun setItems(items: List<Item>) {
        mItems = items
        notifyDataSetChanged()
    }

    class WidgetsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var icon: ImageView = itemView.findViewById(R.id.widget_icon)
        var label: TextView = itemView.findViewById(R.id.widget_label)
    }

    class Item {
        var profile: UserHandle? = null
        var label: CharSequence?
        var icon: Drawable?
        var packageName: String? = null
        var className: String? = null
        var extras: Bundle? = null

        /**
         * Create a list item from given label and icon.
         */
        internal constructor(label: CharSequence?, icon: Drawable?) {
            this.label = label
            this.icon = icon
        }

        /**
         * Create a list item and fill it with details from the given
         * [ResolveInfo] object.
         */
        internal constructor(pm: PackageManager?, resolveInfo: ResolveInfo) {
            label = resolveInfo.loadLabel(pm)
            if (label == null && resolveInfo.activityInfo != null) {
                label = resolveInfo.activityInfo.name
            }
            icon = resolveInfo.loadIcon(pm)
            packageName = resolveInfo.activityInfo.applicationInfo.packageName
            className = resolveInfo.activityInfo.name
        }

        /**
         * Build the [Intent] described by this item. If this item
         * can't create a valid [android.content.ComponentName], it will return
         * [Intent.ACTION_CREATE_SHORTCUT] filled with the item label.
         * TODO: replace with ShortcutManager.createShortcutIntent()
         */
        fun getIntent(baseIntent: Intent?): Intent {
            val intent = Intent(baseIntent)
            if (packageName != null && className != null) {
                // Valid package and class, so fill details as normal intent
                intent.setClassName(packageName!!, className!!)
                if (extras != null) {
                    intent.putExtras(extras!!)
                }
            } else {
                // No valid package or class, so treat as shortcut with label
                intent.action = Intent.ACTION_CREATE_SHORTCUT
                intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, label)
            }
            return intent
        }
    }

    internal interface OnClickListener {
        fun onClick(item: Item?)
    }
}