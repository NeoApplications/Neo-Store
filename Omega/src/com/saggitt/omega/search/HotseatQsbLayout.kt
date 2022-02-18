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

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.app.ActivityOptionsCompat
import com.android.launcher3.DeviceProfile
import com.android.launcher3.LauncherAppState
import com.android.launcher3.R
import com.saggitt.omega.util.Config


class HotseatQsbLayout(context: Context, attrs: AttributeSet? = null) :
    AbstractQsbLayout(context, attrs) {

    override fun onFinishInflate() {
        super.onFinishInflate()
        findViewById<AppCompatImageView?>(R.id.search_engine_logo).apply {
            setOnClickListener {
                if (searchProvider.supportsFeed) {
                    searchProvider.startFeed { intent ->
                        mContext.startActivity(intent)
                    }
                } else {
                    controller.searchProvider.startSearch { intent: Intent? ->
                        context.startActivity(
                            intent
                            //intent, ActivityOptionsCompat.makeClipRevealAnimation(this, 0, 0, width, height).toBundle()
                        )
                    }
                }
            }
        }
        setOnClickListener {
            startHotseatSearch()
        }
    }

    private fun startHotseatSearch() {
        val controller = SearchProviderController.getInstance(mContext)
        val provider = controller.searchProvider

        if (provider.packageName == Config.GOOGLE_QSB) {
            mContext.startActivity(
                Intent("android.search.action.GLOBAL_SEARCH").addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
                ).setPackage(searchProvider.packageName)
            )
        } else {
            controller.searchProvider.startSearch { intent: Intent? ->
                context.startActivity(
                    intent,
                    ActivityOptionsCompat.makeClipRevealAnimation(this, 0, 0, width, height)
                        .toBundle()
                )
            }
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
}