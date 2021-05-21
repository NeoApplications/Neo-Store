/*
 *  This file is part of Omega Launcher.
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
package com.saggitt.omega.qsb

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.saggitt.omega.allapps.BlurQsbLayout
import com.saggitt.omega.util.getLauncherOrNull

class SearchWidget
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    FrameLayout(context, attrs, defStyleAttr) {

    override fun addView(child: View) {
        super.addView(child)
        if (child is BlurQsbLayout) {
            child.setWidgetMode(true)
            child.scrimView =
                context.getLauncherOrNull()!!.findViewById(com.android.launcher3.R.id.scrim_view)
        }
    }
}