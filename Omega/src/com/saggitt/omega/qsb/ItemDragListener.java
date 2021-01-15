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

package com.saggitt.omega.qsb;

import android.content.pm.LauncherActivityInfo;
import android.graphics.Rect;
import android.view.View;

import com.android.launcher3.InstallShortcutReceiver;
import com.android.launcher3.ItemInfo;
import com.android.launcher3.WorkspaceItemInfo;
import com.android.launcher3.compat.ShortcutConfigActivityInfo;
import com.android.launcher3.dragndrop.BaseItemDragListener;
import com.android.launcher3.userevent.nano.LauncherLogProto;
import com.android.launcher3.widget.PendingAddShortcutInfo;
import com.android.launcher3.widget.PendingItemDragHelper;

public class ItemDragListener extends BaseItemDragListener {
    private final LauncherActivityInfo mActivityInfo;

    public ItemDragListener(LauncherActivityInfo activityInfo, Rect rect) {
        super(rect, rect.width(), rect.width());
        mActivityInfo = activityInfo;
    }

    protected PendingItemDragHelper createDragHelper() {
        PendingAddShortcutInfo tag = new PendingAddShortcutInfo(new ShortcutConfigActivityInfo.ShortcutConfigActivityInfoVO(mActivityInfo) {

            public WorkspaceItemInfo createShortcutInfo() {
                return InstallShortcutReceiver.fromActivityInfo(mActivityInfo, mLauncher);
            }
        });
        View view = new View(mLauncher);
        view.setTag(tag);
        return new PendingItemDragHelper(view);
    }

    @Override
    public void fillInLogContainerData(View v, ItemInfo info, LauncherLogProto.Target target, LauncherLogProto.Target targetParent) {
    }
}
