/*
 * This file is part of Omega Launcher
 * Copyright (c) 2022   Omega Launcher Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saggitt.omega.search

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import com.android.launcher3.DeviceProfile
import com.android.launcher3.LauncherAppState
import com.android.launcher3.R
import com.android.launcher3.qsb.QsbContainerView
import com.saggitt.omega.util.Config


class HotseatQsbLayout(context: Context, attrs: AttributeSet? = null) :
    AbstractQsbLayout(context, attrs) {

    var assistantIcon: ImageView? = null
    var gIcon: ImageView? = null
    var lensIcon: ImageView? = null

    var mContext: Context = context

    override fun onFinishInflate() {
        super.onFinishInflate()

        assistantIcon = findViewById(R.id.mic_icon)
        gIcon = findViewById(R.id.g_icon)
        lensIcon = findViewById(R.id.lens_icon)

        assistantIcon?.setImageResource(R.drawable.ic_mic_color)
        gIcon?.setImageResource(R.drawable.ic_super_g_color)
        lensIcon?.setImageResource(R.drawable.ic_lens_color)

        val searchPackage = QsbContainerView.getSearchWidgetPackageName(mContext)
        setOnClickListener { view: View? ->
            mContext.startActivity(
                Intent("android.search.action.GLOBAL_SEARCH").addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
                ).setPackage(searchPackage)
            )
        }
        if (searchPackage == Config.GOOGLE_QSB) {
            setupLensIcon()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val requestedWidth = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        val idp = LauncherAppState.getIDP(mContext)!!
        val dp: DeviceProfile = idp.getDeviceProfile(mContext)
        val cellWidth = DeviceProfile.calculateCellWidth(
            requestedWidth,
            dp.cellLayoutBorderSpacingPx,
            dp.numShownHotseatIcons
        )
        val width = requestedWidth - (cellWidth - Math.round(dp.iconSizePx * 0.92f))
        setMeasuredDimension(width, height)

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child != null) {
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0)
            }
        }
    }

    private fun setupLensIcon() {
        val lensIntent = Intent.makeMainActivity(
            ComponentName(
                Config.LENS_PACKAGE,
                Config.LENS_ACTIVITY
            )
        ).addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        )
        if (context.packageManager.resolveActivity(lensIntent, 0) == null) {
            return
        }
        lensIcon?.visibility = VISIBLE
        lensIcon?.setOnClickListener { view: View? ->
            mContext.startActivity(
                lensIntent
            )
        }
    }

}