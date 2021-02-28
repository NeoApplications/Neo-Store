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

package com.saggitt.omega.popup

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherApps
import android.net.Uri
import android.view.View
import com.android.launcher3.*
import com.android.launcher3.LauncherSettings.Favorites.ITEM_TYPE_APPLICATION
import com.android.launcher3.model.data.*
import com.android.launcher3.model.data.ItemInfoWithIcon.FLAG_SYSTEM_YES
import com.android.launcher3.popup.SystemShortcut
import com.saggitt.omega.override.CustomInfoProvider
import com.saggitt.omega.util.OmegaSingletonHolder
import com.saggitt.omega.util.hasFlag
import com.saggitt.omega.util.omegaPrefs
import com.saggitt.omega.views.CustomBottomSheet
import java.net.URISyntaxException

class OmegaShortcut(private val context: Context) {

    private val shortcuts = listOf(
            ShortcutEntry("edit", Edit.FACTORY, true),
            ShortcutEntry("info", SystemShortcut.APP_INFO, true),
            ShortcutEntry("widgets", SystemShortcut.WIDGETS, true),
            ShortcutEntry("install", SystemShortcut.INSTALL, true),
            ShortcutEntry("remove", Remove.FACTORY, false),
            ShortcutEntry("uninstall", Uninstall.FACTORY, false))

    inner class ShortcutEntry(key: String, val shortcut: SystemShortcut.Factory<in Launcher>, enabled: Boolean) {
        val enabled by context.omegaPrefs.BooleanPref("pref_iconPopup_$key", enabled)
    }

    val enabledShortcuts get() = shortcuts.filter { it.enabled }.map { it.shortcut }

    class Uninstall(private val target: BaseDraggingActivity, private val itemInfo: ItemInfo,
                    private val componentName: ComponentName) :
            SystemShortcut<BaseDraggingActivity>(R.drawable.ic_uninstall_no_shadow,
                    R.string.uninstall_drop_target_label, target, itemInfo) {

        override fun onClick(v: View?) {
            AbstractFloatingView.closeAllOpenViews(target)
            val i = Intent.parseUri(target.getString(R.string.delete_package_intent), 0)
                    .setData(Uri.fromParts("package", componentName.packageName, componentName.className))
                    .putExtra(Intent.EXTRA_USER, itemInfo.user)
            target.startActivity(i)
        }

        companion object {

            val FACTORY = Factory<BaseDraggingActivity> { launcher, itemInfo ->
                if (itemInfo is ItemInfoWithIcon && itemInfo.runtimeStatusFlags.hasFlag(FLAG_SYSTEM_YES)) {
                    null
                } else {
                    getUninstallTarget(launcher, itemInfo)?.let { cn ->
                        Uninstall(launcher, itemInfo, cn)
                    }
                }
            }

            private fun getUninstallTarget(target: BaseDraggingActivity, item: ItemInfo): ComponentName? {
                if (item.itemType == ITEM_TYPE_APPLICATION && item.id == ItemInfo.NO_ID) {
                    val intent = item.intent
                    val user = item.user
                    if (intent != null) {
                        val info = target.getSystemService(LauncherApps::class.java).resolveActivity(intent, user)
                        if (info != null && !info.applicationInfo.flags.hasFlag(ApplicationInfo.FLAG_SYSTEM)) {
                            return info.componentName
                        }
                    }
                }
                return null
            }
        }
    }

    class Remove(private val target: Launcher, private val itemInfo: ItemInfo) :
            SystemShortcut<Launcher>(R.drawable.ic_remove_no_shadow, R.string.remove_drop_target_label, target,
                    itemInfo) {
        override fun onClick(v: View?) {
            AbstractFloatingView.closeAllOpenViews(target)
            target.removeItem(null, itemInfo, true)
            target.model.forceReload()
            target.workspace.stripEmptyScreens()
        }

        companion object {
            val FACTORY = Factory<Launcher> { target, itemInfo ->
                when {
                    itemInfo.id == ItemInfo.NO_ID -> null
                    itemInfo is WorkspaceItemInfo || itemInfo is LauncherAppWidgetInfo || itemInfo is FolderInfo -> Remove(target, itemInfo)
                    else -> null
                }
            }
        }
    }

    class Edit(private val target: Launcher, private val itemInfo: ItemInfo) :
            SystemShortcut<Launcher>(R.drawable.ic_edit_no_shadow, R.string.action_preferences, target, itemInfo) {
        override fun onClick(v: View?) {
            AbstractFloatingView.closeAllOpenViews(target)
            CustomBottomSheet.show(target, itemInfo)
        }

        companion object {
            val FACTORY = Factory<Launcher> { target, itemInfo ->
                if (!target.omegaPrefs.lockDesktop && CustomInfoProvider.isEditable(itemInfo)) {
                    Edit(target, itemInfo)
                } else null
            }
        }
    }

    companion object : OmegaSingletonHolder<OmegaShortcut>(::OmegaShortcut)

}