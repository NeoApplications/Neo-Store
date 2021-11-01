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

import android.content.Context.USER_SERVICE
import android.os.UserManager
import com.saggitt.omega.OmegaLauncher
import com.saggitt.omega.blur.BlurWallpaperProvider

class OmegaPreferencesChangeCallback(val launcher: OmegaLauncher) {
    fun recreate() {
        if (launcher.shouldRecreate()) launcher.recreate()
    }

    fun reloadApps() {
        (launcher.applicationContext.getSystemService(
            USER_SERVICE
        ) as UserManager).userProfiles.forEach {
            launcher.model.onPackagesReload(it)
        }
    }

    fun restart() {
        launcher.scheduleRestart()
    }

    fun updateBlur() {
        BlurWallpaperProvider.getInstance(launcher).updateAsync()
    }
}