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
import android.widget.Button
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.android.launcher3.R
import com.android.launcher3.Utilities
import java.util.*

class IconShapeAdapter(context: Context) : RecyclerView.Adapter<IconShapeAdapter.Holder>() {
    private val adapterItems = ArrayList<ShapeModel>()
    private val mContext = context

    init {
        val shapeItems = context.resources.getStringArray(R.array.icon_shape_values)
        for (shape in shapeItems) {
            adapterItems.add(ShapeModel(shape, Utilities.getOmegaPrefs(mContext).iconShape == shape))
        }
        adapterItems.removeAt(0)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(adapterItems[position], position)
    }

    override fun getItemCount(): Int {
        return adapterItems.count()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return createHolder(parent, R.layout.item_icon_shape, ::Holder)
    }

    private inline fun createHolder(parent: ViewGroup, resource: Int, creator: (View) -> Holder): Holder {
        return creator(LayoutInflater.from(parent.context).inflate(resource, parent, false))
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconButton = itemView.findViewById<Button>(R.id.shape_icon)
        private val check = itemView.findViewById<ImageView>(R.id.check_mark)
        private var prefs = Utilities.getOmegaPrefs(mContext)

        fun bind(item: ShapeModel, itemPosition: Int) {
            val drawable = item.getIcon(mContext, item.shapeName)
            if (prefs.iconShape == item.shapeName) {
                drawable.setTint(prefs.accentColor)
                item.isSelected = true
                check.drawable.setTint(Color.WHITE)
                check.visibility = View.VISIBLE
            } else {
                drawable.setTint(mContext.getColor(R.color.transparentish))
                item.isSelected = false
                check.visibility = View.INVISIBLE
            }

            iconButton.background = drawable
            iconButton.setOnClickListener {
                adapterItems.get(itemPosition).isSelected = true
                prefs.iconShape = item.shapeName
                notifyDataSetChanged()
            }
        }
    }
}