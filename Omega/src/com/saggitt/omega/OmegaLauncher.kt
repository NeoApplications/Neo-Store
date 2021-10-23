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
package com.saggitt.omega

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import com.android.launcher3.Utilities
import com.android.launcher3.uioverrides.QuickstepLauncher
import com.saggitt.omega.preferences.OmegaPreferences
import com.saggitt.omega.preferences.OmegaPreferencesChangeCallback

class OmegaLauncher : QuickstepLauncher() {
    private var paused = false
    private var sRestart = false
    private val prefCallback = OmegaPreferencesChangeCallback(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mPrefs = Utilities.getOmegaPrefs(this)
        mPrefs.registerCallback(prefCallback)
    }

    override fun onDestroy() {
        super.onDestroy()

        Utilities.getOmegaPrefs(this).unregisterCallback()

        if (sRestart) {
            sRestart = false
            OmegaPreferences.destroyInstance()
        }
    }

    fun scheduleRestart() {
        if (paused) {
            sRestart = true
        } else {
            Utilities.restartLauncher(this)
        }
    }

    fun shouldRecreate() = !sRestart

    private val customLayoutInflater by lazy {
        OmegaLayoutInflater(
            super.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater,
            this
        )
    }
}