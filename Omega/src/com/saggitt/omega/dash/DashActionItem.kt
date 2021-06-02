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
import com.android.launcher3.databinding.DashItemBinding
import com.android.launcher3.util.Themes
import com.mikepenz.fastadapter.binding.AbstractBindingItem

class DashActionItem(val context: Context, val provider: DashActionProvider) :
    AbstractBindingItem<DashItemBinding>() {

    override val type: Int
        get() = R.id.fastadapter_item

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?) =
        DashItemBinding.inflate(inflater, parent, false)

    override fun bindView(binding: DashItemBinding, payloads: List<Any>) {
        val backgroundColor =
            ColorStateList.valueOf(Themes.getAttrColor(context, R.attr.dashIconBackground))
        binding.itemIcon.backgroundTintList = backgroundColor
        binding.itemIcon.setImageDrawable(provider.icon)
        binding.itemIcon.tooltipText = provider.name
        val iconColor = ColorStateList.valueOf(Utilities.getOmegaPrefs(context).accentColor)
        binding.itemIcon.imageTintList = iconColor
        binding.itemIcon.setOnClickListener {
            provider.runAction(context)
            AbstractFloatingView.closeAllOpenViews(Launcher.getLauncher(context))
        }
    }
}
