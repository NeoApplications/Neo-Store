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
import com.saggitt.omega.OmegaLauncher
import com.saggitt.omega.compose.components.BaseComposeBottomSheet
import com.saggitt.omega.theme.OmegaAppTheme

const val DASH_LINE_DEFAULT = 6 // 2-6

class DashSheet(val context: Context) {

    companion object {
        fun show(launcher: OmegaLauncher, animate: Boolean = true) {
            BaseComposeBottomSheet.show(launcher, animate) {
                OmegaAppTheme { DashPage() }
            }
        }
    }
}