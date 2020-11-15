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