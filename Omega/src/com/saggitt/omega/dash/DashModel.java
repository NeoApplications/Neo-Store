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

package com.saggitt.omega.dash;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.provider.Settings;

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

    public DashModel() {
    }

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
                context.startActivity(Intent.createChooser(new Intent(Intent.ACTION_SET_WALLPAPER), context.getString(R.string.wallpaper_pick)));
                break;

            case "DeviceSettings":
                context.startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
                break;
            case "LauncherSettings":
                Launcher launcher = Launcher.getLauncher(context);
                launcher.startActivity(new Intent(Intent.ACTION_APPLICATION_PREFERENCES)
                        .setPackage(launcher.getPackageName())
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
