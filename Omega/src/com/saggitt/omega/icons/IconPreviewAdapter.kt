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

package com.saggitt.omega.icons

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.launcher3.R
import com.android.launcher3.Utilities
import java.util.*

class IconPreviewAdapter(context: Context) : RecyclerView.Adapter<IconPreviewAdapter.Holder>() {
    private val adapterItems = ArrayList<PreviewIconModel>()
    private val mContext = context
    private val iconColor = Color.WHITE
    private val previewIcons = intArrayOf(R.drawable.ic_google_photos, R.drawable.ic_instagram_color,
            R.drawable.ic_youtube_color, R.drawable.ic_whatsapp_color)
    private val previewNames = intArrayOf(R.string.preview_app_google_photos, R.string.preview_app_instagram,
            R.string.preview_app_youtube, R.string.preview_app_whatsapp)

    init {
        for (i in 0..3) {
            adapterItems.add(PreviewIconModel(previewIcons[i], previewNames[i], iconColor))
        }
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(adapterItems[position], position)
    }

    override fun getItemCount(): Int {
        return adapterItems.count()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return createHolder(parent, R.layout.item_icon_preview, ::Holder)
    }

    private inline fun createHolder(parent: ViewGroup, resource: Int, creator: (View) -> Holder): Holder {
        return creator(LayoutInflater.from(parent.context).inflate(resource, parent, false))
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon = itemView.findViewById<ImageView>(R.id.item_icon)
        private val name = itemView.findViewById<TextView>(R.id.item_name)
        private var prefs = Utilities.getOmegaPrefs(mContext)

        fun bind(item: PreviewIconModel, position: Int) {
            icon.setImageDrawable(item.getItemIcon(mContext))
            val drawable = item.getShape(mContext, prefs.iconShape, item.itemColor)
            icon.background = drawable
            name.setText(item.itemName)
        }
    }
}