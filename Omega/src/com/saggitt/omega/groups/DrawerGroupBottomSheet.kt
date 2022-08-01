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

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatButton
import com.android.launcher3.Launcher
import com.android.launcher3.R
import com.saggitt.omega.util.applyColor
import com.saggitt.omega.util.omegaPrefs
import com.saggitt.omega.views.BaseBottomSheet
import com.saggitt.omega.views.SettingsBottomSheet

/* Crea la vista principal para crear Folders o Tabs */
@SuppressLint("ViewConstructor")
class DrawerGroupBottomSheet(context: Context, config: AppGroups.Group.CustomizationMap,
                             private val callback: (Boolean) -> Unit) : FrameLayout(context), View.OnClickListener {

    init {
        View.inflate(context, R.layout.drawer_folder_bottom_sheet, this)

        val container = findViewById<ViewGroup>(R.id.customization_container)
        config.sortedEntries.reversed().forEach { entry ->
            entry.createRow(context, container)?.let { container.addView(it, 0) }
        }

        findViewById<AppCompatButton>(R.id.save).apply {
            applyColor(context.omegaPrefs.accentColor)
            setTextColor(context.omegaPrefs.accentColor)
            setOnClickListener(this@DrawerGroupBottomSheet)
        }
        findViewById<AppCompatButton>(R.id.cancel).apply {
            setOnClickListener(this@DrawerGroupBottomSheet)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.save -> callback(true)
            R.id.cancel -> callback(false)
        }
    }

    companion object {

        fun show(context: Context, config: AppGroups.Group.CustomizationMap, animate: Boolean, callback: () -> Unit) {
            val sheet = SettingsBottomSheet.inflate(context)
            sheet.show(DrawerGroupBottomSheet(context, config) {
                if (it) {
                    callback()
                }
                sheet.close(true)
            }, animate)
        }

        fun show(launcher: Launcher, config: AppGroups.Group.CustomizationMap, animate: Boolean, callback: () -> Unit) {
            val sheet = BaseBottomSheet.inflate(launcher)
            sheet.show(DrawerGroupBottomSheet(launcher, config) {
                if (it) {
                    callback()
                }
                sheet.close(true)
            }, animate)
        }

        fun newGroup(context: Context, emptyGroup: AppGroups.Group, animate: Boolean, callback: (AppGroups.Group.CustomizationMap) -> Unit) {
            val config = emptyGroup.customizations
            show(context, config, animate) {
                callback(config)
            }
        }

        fun edit(context: Context, group: AppGroups.Group, callback: () -> Unit) {
            val config = AppGroups.Group.CustomizationMap(group.customizations)
            show(context, config, true) {
                group.customizations.applyFrom(config)
                callback()
            }
        }

        fun editTab(launcher: Launcher, group: DrawerTabs.Tab) {
            val config = AppGroups.Group.CustomizationMap(group.customizations)
            edit(launcher, config, group, true) {
                launcher.omegaPrefs.drawerTabs.saveToJson()
            }
        }

        fun editFolder(launcher: Launcher, group: DrawerFolders.Folder) {
            val config = AppGroups.Group.CustomizationMap(group.customizations)
            edit(launcher, config, group, true) {
                launcher.omegaPrefs.drawerAppGroupsManager.drawerFolders.saveToJson()
            }
        }

        fun edit(launcher: Launcher, config: AppGroups.Group.CustomizationMap,
                 group: AppGroups.Group, animate: Boolean = true, callback: () -> Unit) {
            show(launcher, config, animate) {
                group.customizations.applyFrom(config)
                callback()
            }
        }
    }
}
