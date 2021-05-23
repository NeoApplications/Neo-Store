/*
 *     This file is part of Lawnchair Launcher.
 *
 *     Lawnchair Launcher is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Lawnchair Launcher is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Lawnchair Launcher.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saggitt.omega.groups

import com.android.launcher3.Launcher
import com.android.launcher3.allapps.AllAppsStore
import com.android.launcher3.model.ModelWriter
import com.android.launcher3.model.data.FolderInfo
import com.saggitt.omega.util.omegaPrefs

class DrawerFolderInfo(private val drawerFolder: DrawerFolders.Folder) : FolderInfo() {

    private var changed = false
    lateinit var appsStore: AllAppsStore

    override fun setTitle(title: CharSequence?, modelWriter: ModelWriter) {
        super.setTitle(title, modelWriter)
        changed = true
        drawerFolder.title = title.toString()
    }

    override fun onIconChanged() {
        super.onIconChanged()
        drawerFolder.context.omegaPrefs.withChangeCallback {
            it.reloadDrawer()
        }
    }

    fun onCloseComplete() {
        if (changed) {
            changed = false
            drawerFolder.context.omegaPrefs.appGroupsManager.drawerFolders.saveToJson()
        }
    }

    fun showEdit(launcher: Launcher) {
        DrawerTabEditBottomSheet.editFolder(launcher, drawerFolder)
    }
}
