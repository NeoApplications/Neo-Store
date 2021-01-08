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
            icon.setBackground(drawable)
            name.setText(item.getItemName())
        }
    }
}