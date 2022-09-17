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

val PrefsDependencies = mapOf(
    Preferences.Key.RootSessionInstaller to Preferences.Key.RootPermission,
)
