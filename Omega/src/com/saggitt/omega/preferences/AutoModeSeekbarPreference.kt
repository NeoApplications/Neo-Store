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
package com.saggitt.omega.preferences

import android.content.Context
import android.util.AttributeSet
import com.android.launcher3.R

class AutoModeSeekbarPreference @JvmOverloads constructor(
    private val mContext: Context,
    attrs: AttributeSet? = null, defStyleAttr: Int = 0
) :
    SeekbarPreference(mContext, attrs, defStyleAttr) {

    private var low = min
    public override fun updateSummary() {
        if (current < low) {
            mValueText!!.text = mContext.getString(R.string.automatic_short)
        } else {
            super.updateSummary()
        }
    }

    init {
        min -= (max - min) / steps
        steps += 1
        defaultValue = min
    }
}