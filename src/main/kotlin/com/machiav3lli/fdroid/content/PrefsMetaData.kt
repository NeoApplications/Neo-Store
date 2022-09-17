package com.machiav3lli.fdroid.content

import com.machiav3lli.fdroid.R

val BooleanPrefsMeta = mapOf(
    Preferences.Key.ShowScreenshots to Pair(
        R.string.show_screenshots,
        R.string.show_screenshots_description
    ),
    Preferences.Key.InstallAfterSync to Pair(
        R.string.install_after_sync,
        R.string.install_after_sync_summary
    ),
    Preferences.Key.UpdateNotify to Pair(
        R.string.notify_about_updates,
        R.string.notify_about_updates_summary
    ),
    Preferences.Key.UpdateUnstable to Pair(
        R.string.unstable_updates,
        R.string.unstable_updates_summary
    ),
    Preferences.Key.IncompatibleVersions to Pair(
        R.string.incompatible_versions,
        R.string.incompatible_versions_summary
    ),
    Preferences.Key.RootPermission to Pair(
        R.string.root_permission,
        R.string.root_permission_description
    ),
    Preferences.Key.RootSessionInstaller to Pair(
        R.string.root_session_installer,
        R.string.root_session_installer_description
    ),
)

val NonBooleanPrefsMeta = mapOf(
    Preferences.Key.Language to R.string.prefs_language_title,
    Preferences.Key.Theme to R.string.theme,
    Preferences.Key.DefaultTab to R.string.default_tab,
    Preferences.Key.UpdatedApps to R.string.prefs_updated_apps,
    Preferences.Key.NewApps to R.string.prefs_new_apps,
    Preferences.Key.AutoSync to R.string.sync_repositories_automatically,
    Preferences.Key.AutoSyncInterval to R.string.auto_sync_interval,
    Preferences.Key.ReleasesCacheRetention to R.string.releases_cache_retention,
    Preferences.Key.ImagesCacheRetention to R.string.images_cache_retention,
    Preferences.Key.ProxyType to R.string.proxy_type,
    Preferences.Key.ProxyHost to R.string.proxy_host,
    Preferences.Key.ProxyPort to R.string.proxy_port,
)

val PrefsDependencies = mapOf(
    Preferences.Key.RootSessionInstaller to Preferences.Key.RootPermission,
)
