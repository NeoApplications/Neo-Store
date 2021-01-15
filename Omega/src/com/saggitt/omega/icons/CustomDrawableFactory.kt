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
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Process
import android.os.UserHandle
import androidx.annotation.RequiresApi
import com.android.launcher3.FastBitmapDrawable
import com.android.launcher3.ItemInfoWithIcon
import com.android.launcher3.WorkspaceItemInfo
import com.android.launcher3.graphics.DrawableFactory
import com.android.launcher3.icons.BitmapInfo
import com.saggitt.omega.iconpack.IconPackManager
import com.saggitt.omega.icons.clock.CustomClock
import com.saggitt.omega.icons.clock.DynamicClock

class CustomDrawableFactory(context: Context) : DrawableFactory() {
    private val iconPackManager = IconPackManager.getInstance(context)
    val customClockDrawer by lazy { CustomClock(context) }
    private val mDynamicClockDrawer by lazy { DynamicClock(context) }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun newIcon(context: Context, info: ItemInfoWithIcon): FastBitmapDrawable {

        if (info.usingLowResIcon()) {
            return super.newIcon(context, info)
        }
        return iconPackManager.newIcon((info as? WorkspaceItemInfo)?.customIcon ?: info.iconBitmap,
                info, this).also { it.setIsDisabled(info.isDisabled) }
    }

    override fun newIcon(context: Context?, icon: BitmapInfo, info: ActivityInfo): FastBitmapDrawable? {
        return if (DynamicClock.DESK_CLOCK.packageName == info.packageName &&
                UserHandle.getUserHandleForUid(info.applicationInfo.uid) == Process.myUserHandle()) {
            mDynamicClockDrawer.drawIcon(icon.icon)
        } else super.newIcon(context, icon, info)
    }
}