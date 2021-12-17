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
import android.view.ViewGroup
import com.android.launcher3.AbstractFloatingView
import com.android.launcher3.Launcher
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.databinding.DashControlItemBinding
import com.android.launcher3.util.Themes
import com.mikepenz.fastadapter.binding.AbstractBindingItem

class DashControlItem(val context: Context, val provider: DashControlProvider) :
    AbstractBindingItem<DashControlItemBinding>() {

    override val type: Int
        get() = R.id.fastadapter_item

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?) =
        DashControlItemBinding.inflate(inflater, parent, false)

    override fun bindView(binding: DashControlItemBinding, payloads: List<Any>) {
        val backgroundColor =
            ColorStateList.valueOf(Themes.getAttrColor(context, R.attr.dashIconBackground))
        val sheetColor =
            ColorStateList.valueOf(Themes.getAttrColor(context, R.attr.dashSheetBackground))
        val activeColor = ColorStateList.valueOf(Utilities.getOmegaPrefs(context).accentColor)
        binding.root.backgroundTintList = if (provider.state) activeColor else backgroundColor
        binding.itemIcon.setImageDrawable(provider.icon)
        binding.itemName.text = provider.name
        binding.itemIcon.tooltipText = provider.description
        binding.itemIcon.imageTintList = if (provider.state) sheetColor else activeColor
        binding.itemName.setTextColor(if (provider.state) sheetColor else activeColor)
        binding.root.setOnClickListener {
            provider.state = !provider.state
            AbstractFloatingView.closeAllOpenViews(Launcher.getLauncher(context))
        }
    }
}
