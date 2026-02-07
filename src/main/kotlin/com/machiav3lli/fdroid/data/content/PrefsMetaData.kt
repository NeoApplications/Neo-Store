package com.machiav3lli.fdroid.data.content

import android.os.Build
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ArrowsClockwise
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Bell
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Browser
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CircleWavyQuestion
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CircleWavyWarning
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CirclesThreePlus
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Clock
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ClockCounterClockwise
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CloudArrowDown
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Compass
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CrosshairSimple
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.DeviceMobile
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.DotsThreeOutline
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Flask
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.FolderNotch
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.GearSix
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Hash
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Image
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Lock
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Robot
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ShieldSlash
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Swatches
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.TagSimple
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Textbox
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Translate
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.TwoCircle
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.VideoConference
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Wrench
import com.machiav3lli.fdroid.utils.extension.android.Android

val BooleanPrefsMeta = mapOf(
    Preferences.Key.ShowScreenshots to Triple(
        R.string.show_screenshots,
        R.string.show_screenshots_description,
        Phosphor.Image
    ),
    Preferences.Key.ShowTrackers to Triple(
        R.string.show_trackers,
        R.string.show_trackers_description,
        Phosphor.CrosshairSimple
    ),
    Preferences.Key.AltNavBarItem to Triple(
        R.string.alt_navbar_item,
        R.string.alt_navbar_item_description,
        Phosphor.DotsThreeOutline
    ),
    Preferences.Key.AltNewApps to Triple(
        R.string.alt_new_apps,
        R.string.alt_new_apps_description,
        Phosphor.CirclesThreePlus
    ),
    Preferences.Key.HideNewApps to Triple(
        R.string.hide_new_apps,
        R.string.hide_new_apps_description,
        Phosphor.CircleWavyWarning
    ),
    Preferences.Key.AltBlockLayout to Triple(
        R.string.alt_block_layout,
        R.string.alt_block_layout_summary,
        Phosphor.Browser
    ),
    Preferences.Key.AndroidInsteadOfSDK to Triple(
        R.string.android_instead_of_sdk,
        R.string.android_instead_of_sdk_summary,
        Phosphor.Robot
    ),
    Preferences.Key.InstallAfterSync to Triple(
        R.string.install_after_sync,
        R.string.install_after_sync_summary,
        Phosphor.CloudArrowDown
    ),
    Preferences.Key.UpdateNotify to Triple(
        R.string.notify_about_updates,
        R.string.notify_about_updates_summary,
        Phosphor.Bell
    ),
    Preferences.Key.KeepInstallNotification to Triple(
        R.string.keep_install_notification,
        R.string.keep_install_notification_summary,
        Phosphor.CircleWavyWarning
    ),
    Preferences.Key.DisableDownloadVersionCheck to Triple(
        R.string.disable_download_version_check,
        R.string.disable_download_version_check_summary,
        Phosphor.ShieldSlash
    ),
    Preferences.Key.UpdateUnstable to Triple(
        R.string.unstable_updates,
        R.string.unstable_updates_summary,
        Phosphor.Flask
    ),
    Preferences.Key.IncompatibleVersions to Triple(
        R.string.incompatible_versions,
        R.string.incompatible_versions_summary,
        Phosphor.ShieldSlash
    ),
    Preferences.Key.DisableSignatureCheck to Triple(
        R.string.disable_signature_check,
        R.string.disable_signature_check_summary,
        Phosphor.ShieldSlash
    ),
    Preferences.Key.DisablePermissionsCheck to Triple(
        R.string.disable_permissions_check,
        R.string.disable_permissions_check_summary,
        Phosphor.CircleWavyQuestion
    ),
    Preferences.Key.RootSessionInstaller to Triple(
        R.string.root_session_installer,
        R.string.root_session_installer_description,
        Phosphor.TagSimple
    ),
    Preferences.Key.RootAllowDowngrades to Triple(
        R.string.root_allow_downgrades,
        R.string.root_allow_downgrades_description,
        Phosphor.ClockCounterClockwise
    ),
    Preferences.Key.RootAllowInstallingOldApps to Triple(
        R.string.root_allow_installing_old_apps,
        R.string.root_allow_installing_old_apps_description,
        Phosphor.Clock
    ),
    Preferences.Key.EnableDownloadDirectory to Triple(
        R.string.enable_download_directory,
        R.string.enable_download_directory_summary,
        Phosphor.FolderNotch
    ),
    Preferences.Key.DownloadManager to Triple(
        R.string.download_manager,
        R.string.download_manager_summary,
        Phosphor.CloudArrowDown
    ),
    Preferences.Key.IndexV2 to Triple(
        R.string.index_v2,
        R.string.index_v2_summary,
        Phosphor.TwoCircle
    ),
    Preferences.Key.DownloadShowDialog to Triple(
        R.string.download_show_dialog,
        R.string.download_show_dialog_summary,
        Phosphor.TagSimple
    ),
    Preferences.Key.BottomSearchBar to Triple(
        R.string.bottom_search_bar,
        R.string.bottom_search_bar_summary,
        Phosphor.DeviceMobile
    ),
    Preferences.Key.DisableListDetail to Triple(
        R.string.disable_list_detail,
        R.string.disable_list_detail_summary,
        Phosphor.VideoConference
    ),
    Preferences.Key.KidsMode to Triple(
        R.string.kids_mode,
        if (Preferences[Preferences.Key.KidsMode]) R.string.kids_mode_summary
        else R.string.kids_mode_summary_full,
        Phosphor.Lock
    ),
    Preferences.Key.DisableCertificateValidation to Triple(
        R.string.disable_certificate_check,
        R.string.disable_certificate_check_summary,
        Phosphor.ShieldSlash
    ),
)

val NonBooleanPrefsMeta = mapOf(
    Preferences.Key.Language to Pair(
        R.string.prefs_language_title,
        Phosphor.Translate
    ),
    Preferences.Key.Theme to Pair(
        R.string.theme,
        Phosphor.Swatches
    ),
    Preferences.Key.DefaultTab to Pair(
        R.string.default_tab,
        Phosphor.DeviceMobile
    ),
    Preferences.Key.UpdatedApps to Pair(
        R.string.prefs_updated_apps,
        Phosphor.Hash
    ),
    Preferences.Key.NewApps to Pair(
        R.string.prefs_new_apps,
        Phosphor.Hash
    ),
    Preferences.Key.AutoSync to Pair(
        R.string.sync_repositories_automatically,
        Phosphor.ArrowsClockwise
    ),
    Preferences.Key.AutoSyncInterval to Pair(
        R.string.auto_sync_interval_hours,
        Phosphor.Clock
    ),
    Preferences.Key.Installer to Pair(
        R.string.prefs_installer,
        Phosphor.Wrench
    ),
    Preferences.Key.ActionLockDialog to Pair(
        R.string.action_lock_dialog,
        Phosphor.Lock
    ),
    Preferences.Key.DownloadDirectory to Pair(
        R.string.custom_download_directory,
        Phosphor.FolderNotch
    ),
    Preferences.Key.ReleasesCacheRetention to Pair(
        R.string.releases_cache_retention,
        Phosphor.Clock
    ),
    Preferences.Key.ImagesCacheRetention to Pair(
        R.string.images_cache_retention,
        Phosphor.Clock
    ),
    Preferences.Key.ProxyType to Pair(
        R.string.proxy_type,
        Phosphor.GearSix
    ),
    Preferences.Key.ProxyUrl to Pair(
        R.string.proxy_url,
        Phosphor.Textbox
    ),
    Preferences.Key.ProxyHost to Pair(
        R.string.proxy_host,
        Phosphor.Textbox
    ),
    Preferences.Key.ProxyPort to Pair(
        R.string.proxy_port,
        Phosphor.TagSimple
    ),
    Preferences.Key.MaxIdleConnections to Pair(
        R.string.max_idle_connections_description,
        Phosphor.Hash
    ),
    Preferences.Key.MaxParallelDownloads to Pair(
        R.string.max_parallel_downloads,
        Phosphor.Hash
    ),
    Preferences.Key.RBProvider to Pair(
        R.string.rb_provider,
        Phosphor.Compass
    ),
    Preferences.Key.DLStatsProvider to Pair(
        R.string.dlstats_provider,
        Phosphor.Compass
    ),
)

val PrefsEntries = mapOf(
    Preferences.Key.Theme to mutableMapOf(
        Preferences.Theme.Light to R.string.light,
        Preferences.Theme.Dark to R.string.dark,
        Preferences.Theme.Black to R.string.amoled,
        Preferences.Theme.LightMediumContrast to R.string.light_medium_contrast,
        Preferences.Theme.DarkMediumContrast to R.string.dark_medium_contrast,
        Preferences.Theme.BlackMediumContrast to R.string.black_medium_contrast,
        Preferences.Theme.LightHighContrast to R.string.light_high_contrast,
        Preferences.Theme.DarkHighContrast to R.string.dark_high_contrast,
        Preferences.Theme.BlackHighContrast to R.string.black_high_contrast,
    ).apply {
        if (Android.sdk(Build.VERSION_CODES.Q)) {
            put(Preferences.Theme.System, R.string.system)
            put(Preferences.Theme.SystemBlack, R.string.system_black)
        }
        if (Android.sdk(Build.VERSION_CODES.S)) {
            put(Preferences.Theme.Dynamic, R.string.dynamic)
            put(Preferences.Theme.DynamicLight, R.string.dynamic_light)
            put(Preferences.Theme.DynamicDark, R.string.dynamic_dark)
            put(Preferences.Theme.DynamicBlack, R.string.dynamic_black)
        }
    },
    Preferences.Key.DefaultTab to mapOf(
        Preferences.DefaultTab.Latest to R.string.latest,
        Preferences.DefaultTab.Explore to R.string.explore,
        Preferences.DefaultTab.Installed to R.string.installed,
    ),
    Preferences.Key.ActionLockDialog to mapOf(
        Preferences.ActionLock.None to R.string.action_lock_none,
        Preferences.ActionLock.Device to R.string.action_lock_device,
        Preferences.ActionLock.Biometric to R.string.action_lock_biometric,
    ),
    Preferences.Key.Installer to mapOf(
        Preferences.Installer.Default to R.string.default_installer,
        Preferences.Installer.Root to R.string.root_installer,
        Preferences.Installer.Legacy to R.string.legacy_installer,
        Preferences.Installer.AM to R.string.am_installer,
        Preferences.Installer.System to R.string.system_installer,
        Preferences.Installer.Shizuku to R.string.shizuku_installer,
    ),
    Preferences.Key.AutoSync to mapOf(
        Preferences.AutoSync.Wifi to R.string.only_on_wifi,
        Preferences.AutoSync.WifiBattery to R.string.only_on_wifi_and_battery,
        Preferences.AutoSync.Battery to R.string.only_on_battery,
        Preferences.AutoSync.Always to R.string.always,
        Preferences.AutoSync.Never to R.string.never,
    ),
    Preferences.Key.ProxyType to mapOf(
        Preferences.ProxyType.Direct to R.string.no_proxy,
        Preferences.ProxyType.Http to R.string.http_proxy,
        Preferences.ProxyType.Socks to R.string.socks_proxy,
    ),
    Preferences.Key.RBProvider to mapOf(
        Preferences.RBProvider.None to R.string.rb_none,
        Preferences.RBProvider.IzzyOnDroid to R.string.rb_izzyondroid,
        Preferences.RBProvider.BG443 to R.string.rb_bg443,
    ),
    Preferences.Key.DLStatsProvider to mapOf(
        Preferences.DLStatsProvider.None to R.string.dlstats_none,
        Preferences.DLStatsProvider.IzzyOnDroid to R.string.dlstats_izzyondroid,
    ),
)

val IntPrefsRanges = mapOf(
    Preferences.Key.UpdatedApps to 1..1000,
    Preferences.Key.NewApps to 1..300,
    Preferences.Key.AutoSyncInterval to 1..720,
    Preferences.Key.ReleasesCacheRetention to 0..365,
    Preferences.Key.ImagesCacheRetention to 0..365,
    Preferences.Key.ProxyPort to 1..65535,
    Preferences.Key.MaxIdleConnections to 1..32,
    Preferences.Key.MaxParallelDownloads to 1..32,
)

val PrefsDependencies = mapOf(
    Preferences.Key.RootSessionInstaller to Pair(
        Preferences.Key.Installer,
        listOf(Preferences.Installer.Root)
    ),
    Preferences.Key.RootAllowDowngrades to Pair(
        Preferences.Key.Installer,
        listOf(Preferences.Installer.Root)
    ),
    Preferences.Key.RootAllowInstallingOldApps to Pair(
        Preferences.Key.Installer,
        listOf(Preferences.Installer.Root)
    ),
    Preferences.Key.DownloadDirectory to Pair(
        Preferences.Key.EnableDownloadDirectory,
        listOf(true)
    ),
    Preferences.Key.ProxyUrl to Pair(
        Preferences.Key.ProxyType,
        listOf(Preferences.ProxyType.Http)
    ),
    Preferences.Key.ProxyHost to Pair(
        Preferences.Key.ProxyType,
        listOf(Preferences.ProxyType.Socks)
    ),
    Preferences.Key.ProxyPort to Pair(
        Preferences.Key.ProxyType,
        listOf(Preferences.ProxyType.Socks)
    ),
    Preferences.Key.ActionLockDialog to Pair(
        Preferences.Key.DownloadShowDialog,
        listOf(true)
    ),
    Preferences.Key.KidsMode to Pair(
        Preferences.Key.KidsMode,
        listOf(false)
    ),
)
