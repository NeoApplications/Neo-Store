package com.saggitt.omega.util

import android.content.Context
import com.saggitt.omega.dash.actionprovider.AllAppsShortcut
import com.saggitt.omega.dash.actionprovider.AudioPlayer
import com.saggitt.omega.dash.actionprovider.ChangeWallpaper
import com.saggitt.omega.dash.actionprovider.DeviceSettings
import com.saggitt.omega.dash.actionprovider.EditDash
import com.saggitt.omega.dash.actionprovider.LaunchAssistant
import com.saggitt.omega.dash.actionprovider.ManageApps
import com.saggitt.omega.dash.actionprovider.ManageVolume
import com.saggitt.omega.dash.actionprovider.OmegaSettings
import com.saggitt.omega.dash.actionprovider.SleepDevice
import com.saggitt.omega.dash.actionprovider.Torch
import com.saggitt.omega.dash.controlprovider.AutoRotation
import com.saggitt.omega.dash.controlprovider.Bluetooth
import com.saggitt.omega.dash.controlprovider.Location
import com.saggitt.omega.dash.controlprovider.MobileData
import com.saggitt.omega.dash.controlprovider.Sync
import com.saggitt.omega.dash.controlprovider.Wifi

fun getDashActionProviders(context: Context) = listOf(
    EditDash(context),
    ChangeWallpaper(context),
    OmegaSettings(context),
    ManageVolume(context),
    DeviceSettings(context),
    ManageApps(context),
    AllAppsShortcut(context),
    SleepDevice(context),
    LaunchAssistant(context),
    Torch(context),
    AudioPlayer(context)
)

fun getDashControlProviders(context: Context) = listOf(
    Wifi(context),
    MobileData(context),
    Location(context),
    Bluetooth(context),
    AutoRotation(context),
    Sync(context)
)