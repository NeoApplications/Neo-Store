/*
 *     This file is part of Lawnchair Launcher.
 *
 *     Lawnchair Launcher is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Lawnchair Launcher is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Lawnchair Launcher.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.saggitt.omega.preferences

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.ImageView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.android.launcher3.R
import com.saggitt.omega.util.forEachIndexed

open class StyledIconPreference : Preference {
    var count = 1
    var index = 0

    @JvmOverloads
    constructor (context: Context) : super(context)

    @JvmOverloads
    constructor (context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        holder.findViewById(androidx.appcompat.R.id.icon)?.let { it as? ImageView }?.apply {
            val size = resources.getDimensionPixelSize(R.dimen.dashboard_tile_image_size)
            layoutParams = ViewGroup.LayoutParams(size, size)
        }
    }

    override fun onAttached() {
        super.onAttached()
        parent?.forEachIndexed { i, pref ->
            if (pref.key == key) index = i
            if (pref is StyledIconPreference) count++
        }
    }
}
