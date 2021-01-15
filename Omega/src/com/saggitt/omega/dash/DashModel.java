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

package com.saggitt.omega.dash;

import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.provider.Settings;
import android.widget.Toast;

import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.saggitt.omega.settings.SettingsActivity;

import java.io.Serializable;
import java.util.Objects;

import static com.android.launcher3.LauncherState.ALL_APPS;

public class DashModel implements Serializable {

    private int id;
    private String title;
    private String description;
    private String action;
    private boolean isEnabled;
    private Drawable icon;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String name) {
        this.title = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public void RunAction(String action, Context context) {

        switch (action) {
            case "MobileNetworkSettings":
                context.startActivity(new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS));
                break;

            case "EditDash":
                String fragment = "com.saggitt.omega.dash.DashFragment";
                SettingsActivity.startFragment(context, fragment, R.string.edit_dash);
                break;

            case "AppSettings":
                context.startActivity(new Intent(Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS));
                break;

            case "SetWallpaper":
                try {
                    context.startActivity(
                            Intent.createChooser(new Intent(Intent.ACTION_SET_WALLPAPER),
                                    context.getString(R.string.wallpaper_pick)));
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(context, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
                }
                break;

            case "DeviceSettings":
                context.startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
                break;

            case "LauncherSettings":
                context.startActivity(new Intent(Intent.ACTION_APPLICATION_PREFERENCES)
                        .setPackage(context.getPackageName())
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                break;

            case "AppDrawer":
                if (!Launcher.getLauncher(context).isInState(ALL_APPS)) {
                    Launcher.getLauncher(context).getStateManager().goToState(ALL_APPS);
                }
                break;

            case "VolumeDialog":
                try {
                    AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                    Objects.requireNonNull(audioManager).setStreamVolume(AudioManager.STREAM_RING, audioManager.getStreamVolume(AudioManager.STREAM_RING), AudioManager.FLAG_SHOW_UI);
                } catch (Exception e) {
                    NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    if (!Objects.requireNonNull(mNotificationManager).isNotificationPolicyAccessGranted()) {
                        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                        context.startActivity(intent);
                    }
                }
                break;
        }
    }

}
