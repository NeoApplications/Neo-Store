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
            drawable.setTint(mContext.getColor(R.color.transparentish))
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