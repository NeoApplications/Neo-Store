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

import android.annotation.SuppressLint;
import android.content.Intent;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import com.android.launcher3.Launcher;
import com.android.launcher3.R;

public class QsbActionMode implements ActionMode.Callback {
    final AbstractQsbLayout qsbLayout;
    private final String clipboardText;
    private final Intent settingsBroadcast;
    private final Intent settingsIntent;

    public QsbActionMode(AbstractQsbLayout layout, String clipboardText, Intent settingBroadcast, Intent settingsIntent) {
        this.qsbLayout = layout;
        this.clipboardText = clipboardText;
        this.settingsBroadcast = settingBroadcast;
        this.settingsIntent = settingsIntent;
    }

    @SuppressLint("ResourceType")
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        actionMode.setTitle(null);
        actionMode.setSubtitle(null);
        actionMode.setTitleOptionalHint(true);
        actionMode.setTag(Launcher.AUTO_CANCEL_ACTION_MODE);
        if (clipboardText != null) {
            menu.add(0, 16908322, 0, 17039371).setShowAsAction(1);
        }
        if (settingsBroadcast != null || settingsIntent != null) {
            menu.add(0, R.id.hotseat_qsb_menu_item, 0, R.string.customize).setShowAsAction(8);
        }
        return clipboardText != null || settingsBroadcast != null || settingsIntent != null;
    }

    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return true;
    }

    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        if (menuItem.getItemId() == 16908322 && !TextUtils.isEmpty(clipboardText)) {
            qsbLayout.startSearch(clipboardText, 3);
            actionMode.finish();
            return true;
        } else if (menuItem.getItemId() == R.id.hotseat_qsb_menu_item && !(this.settingsBroadcast == null && this.settingsIntent == null)) {
            if (settingsBroadcast != null) {
                this.qsbLayout.getContext().sendBroadcast(this.settingsBroadcast);
            } else if (settingsIntent != null) {
                this.qsbLayout.getContext().startActivity(settingsIntent);
            }
            actionMode.finish();
            return true;
        }
        return false;
    }

    public void onDestroyActionMode(ActionMode actionMode) {
    }
}
