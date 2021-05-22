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
package com.saggitt.omega.dash

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import com.android.launcher3.AbstractFloatingView
import com.android.launcher3.Launcher
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.util.Themes
import java.util.*

class DashItemAdapter(
    inflater: LayoutInflater,
    items: List<DashProvider>,
    private val mContext: Context
) {
    val allViews: ArrayList<View> = arrayListOf()
    private var circularItemChangeListener: DashItemChangeListener? = null
    val count: Int
        get() = allViews.size

    fun getItemAt(i: Int): View {
        return allViews[i]
    }

    fun removeItemAt(i: Int) {
        if (allViews.size > 0) {
            allViews.removeAt(i)
            notifyItemChange()
        }
    }

    fun addItem(view: View) {
        allViews.add(view)
        notifyItemChange()
    }

    fun setOnItemChangeListener(listener: DashItemChangeListener?) {
        circularItemChangeListener = listener
    }

    fun notifyItemChange() {
        circularItemChangeListener!!.onDashItemChange()
    }

    interface DashItemChangeListener {
        fun onDashItemChange()
    }

    init {
        for (dashItem in items) {
            val view = inflater.inflate(R.layout.dash_item, null)
            val itemView = view.findViewById<ImageView>(R.id.bt_item)
            val backgroundColor =
                ColorStateList.valueOf(Themes.getAttrColor(mContext, R.attr.dashIconBackground))
            itemView.backgroundTintList = backgroundColor
            itemView.setImageDrawable(dashItem.icon)
            val iconColor = ColorStateList.valueOf(Utilities.getOmegaPrefs(mContext).accentColor)
            itemView.imageTintList = iconColor
            itemView.setOnClickListener {
                dashItem.runAction(mContext)
                AbstractFloatingView.closeAllOpenViews(Launcher.getLauncher(mContext))
            }
            allViews.add(view)
        }
    }
}