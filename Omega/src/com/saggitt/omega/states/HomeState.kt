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

package com.saggitt.omega.states

import android.content.Context
import com.android.launcher3.Launcher
import com.android.launcher3.LauncherState
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.saggitt.omega.util.SingletonHolder
import com.saggitt.omega.util.omegaPrefs

open class HomeState(id: Int, containerType: Int, transitionDuration: Int, flags: Int) :
        LauncherState(id, containerType, transitionDuration, flags) {

    override fun getScrimProgress(launcher: Launcher): Float {
        if (!launcher.omegaPrefs.dockGradientStyle) {
            return getNormalProgress(launcher)
        }
        return super.getScrimProgress(launcher)
    }

    companion object {

        private val shelfOffset = SingletonHolder<Int, Context> { it.resources.getDimensionPixelSize(R.dimen.shelf_surface_offset) }

        fun getNormalProgress(launcher: Launcher): Float {
            return 1 - (getScrimHeight(launcher) / launcher.allAppsController.shiftRange)
        }

        private fun getScrimHeight(launcher: Launcher): Float {
            val dp = launcher.deviceProfile
            val prefs = Utilities.getOmegaPrefs(launcher)

            return if (prefs.dockHide) {
                dp.allAppsCellHeightPx - dp.allAppsIconTextSizePx
            } else {
                val rangeDelta = dp.heightPx - launcher.allAppsController.shiftRange
                val lp = launcher.hotseat.layoutParams
                -rangeDelta + lp.height + dp.insets.top - shelfOffset.getInstance(launcher)
            }
        }
    }
}
