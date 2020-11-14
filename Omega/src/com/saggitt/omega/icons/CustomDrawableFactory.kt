/*
 *  Copyright (c) 2020 Omega Launcher
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
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