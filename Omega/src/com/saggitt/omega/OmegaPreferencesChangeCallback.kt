/*
 *  This file is part of Omega Launcher.
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

import com.android.launcher3.compat.UserManagerCompat
import com.android.launcher3.pageindicators.WorkspacePageIndicator
import com.saggitt.omega.blur.BlurWallpaperProvider

class OmegaPreferencesChangeCallback(val launcher: OmegaLauncher) {
    fun recreate() {
        if (launcher.shouldRecreate()) launcher.recreate()
    }

    fun reloadApps() {
        UserManagerCompat.getInstance(launcher.mContext).userProfiles.forEach { launcher.model.onPackagesReload(it) }
    }

    fun reloadAll() {
        launcher.model.forceReload()
    }

    fun reloadDrawer() {
        launcher.appsView.appsLists.forEach { it.reset() }
    }

    fun restart() {
        launcher.scheduleRestart()
    }

    fun refreshGrid() {
        launcher.refreshGrid()
    }

    fun updateBlur() {
        BlurWallpaperProvider.getInstance(launcher).updateAsync()
    }

    fun updateSmartspaceProvider() {
        launcher.omegaApp.smartspace.onProviderChanged()
    }

    fun updateWeatherData() {
        launcher.omegaApp.smartspace.forceUpdateWeather()
    }

    fun forceReloadApps() {
        UserManagerCompat.getInstance(launcher).userProfiles.forEach { launcher.model.forceReload() }
    }

    fun updatePageIndicator() {
        val indicator = launcher.workspace.pageIndicator
        if (indicator is WorkspacePageIndicator) {
            indicator.updateLineHeight()
        }
    }
}