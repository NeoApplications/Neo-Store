/*
 * This file is part of Neo Launcher
 * Copyright (c) 2022   Neo Launcher Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saggitt.omega.override

import android.content.Context
import com.android.launcher3.LauncherSettings.Favorites.ITEM_TYPE_DEEP_SHORTCUT
import com.android.launcher3.LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT
import com.android.launcher3.Utilities
import com.android.launcher3.model.ModelWriter
import com.android.launcher3.model.data.WorkspaceItemInfo
import com.saggitt.omega.util.SingletonHolder
import com.saggitt.omega.util.ensureOnMainThread
import com.saggitt.omega.util.useApplicationContext

class ShortcutInfoProvider(context: Context) : CustomInfoProvider<WorkspaceItemInfo>(context) {

    override fun getTitle(info: WorkspaceItemInfo): String {
        return (info.customTitle ?: info.title).toString()
    }

    override fun getDefaultTitle(info: WorkspaceItemInfo): String {
        return info.title.toString()
    }

    override fun getCustomTitle(info: WorkspaceItemInfo): String? {
        return info.customTitle?.toString()
    }

    override fun setTitle(info: WorkspaceItemInfo, title: String?, modelWriter: ModelWriter) {
        info.setTitle(title, modelWriter)
    }

    override fun supportsSwipeUp(info: WorkspaceItemInfo) = false

    override fun setSwipeUpAction(info: WorkspaceItemInfo, action: String?) {
        info.setSwipeUpAction(context, action)
    }

    override fun getSwipeUpAction(info: WorkspaceItemInfo): String? {
        return info.swipeUpAction
    }

    override fun supportsBadgeVisible(info: WorkspaceItemInfo) = when (info.itemType) {
        ITEM_TYPE_SHORTCUT, ITEM_TYPE_DEEP_SHORTCUT -> {
            Utilities.getOmegaPrefs(context).notificationCount
        }
        else -> false
    }

    companion object : SingletonHolder<ShortcutInfoProvider, Context>(
        ensureOnMainThread(
            useApplicationContext(::ShortcutInfoProvider)
        )
    )
}
