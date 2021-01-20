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

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LauncherActivityInfo;
import android.os.Bundle;

import com.android.launcher3.compat.LauncherAppsCompat;
import com.android.launcher3.util.ComponentKey;
import com.google.android.apps.nexuslauncher.NexusLauncherActivity;
import com.google.android.apps.nexuslauncher.search.AppSearchProvider;

import java.lang.ref.WeakReference;

public class LongClickReceiver extends BroadcastReceiver {
    private static WeakReference<NexusLauncherActivity> bR = new WeakReference<>(null);

    public static void bq(final NexusLauncherActivity nexusLauncherActivity) {
        LongClickReceiver.bR = new WeakReference<>(nexusLauncherActivity);
    }

    public void onReceive(final Context context, final Intent intent) {
        final NexusLauncherActivity launcher = LongClickReceiver.bR.get();
        if (launcher != null) {
            final ComponentKey dl = AppSearchProvider.uriToComponent(intent.getData(), context);
            final LauncherActivityInfo resolveActivity = LauncherAppsCompat.getInstance(context).resolveActivity(new Intent(Intent.ACTION_MAIN).setComponent(dl.componentName), dl.user);
            if (resolveActivity == null) {
                return;
            }
            final ItemDragListener onDragListener = new ItemDragListener(resolveActivity, intent.getSourceBounds());
            onDragListener.init(launcher, false);
            launcher.getDragLayer().setOnDragListener(onDragListener);
            final ClipData clipData = new ClipData(new ClipDescription("", new String[]{onDragListener.getMimeType()}), new ClipData.Item(""));
            final Bundle bundle = new Bundle();
            bundle.putParcelable("clip_data", clipData);
            this.setResult(-1, null, bundle);
        }
    }
}
