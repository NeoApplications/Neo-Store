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
package com.saggitt.omega.views

import android.app.FragmentManager
import android.content.Context
import android.util.AttributeSet
import com.android.launcher3.Launcher
import com.android.launcher3.R

// TODO: replace the Donations buttons with Google play dialog
// this should be completed before the app is available in google play
class GBillingDialog @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0
) :
    BaseBottomSheet(context, attrs, defStyleAttr) {
    private val mFragmentManager: FragmentManager = mLauncher.fragmentManager
    fun populateAndShow() {}

    companion object {
        fun show(launcher: Launcher) {
            val gbd = launcher.layoutInflater
                .inflate(R.layout.billing_dialog, launcher.dragLayer, false) as GBillingDialog
            gbd.populateAndShow()
        }
    }
}