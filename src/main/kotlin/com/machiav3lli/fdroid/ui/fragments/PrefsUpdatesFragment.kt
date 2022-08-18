package com.machiav3lli.fdroid.ui.fragments

import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.android.material.circularreveal.CircularRevealFrameLayout
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.content.Preferences

class PrefsUpdatesFragment : PrefsNavFragmentX() {

    override fun setupPrefs(scrollLayout: CircularRevealFrameLayout) {
        val preferences = LinearLayout(scrollLayout.context)
        preferences.orientation = LinearLayout.VERTICAL
        scrollLayout.addView(
            preferences,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        preferences.addCategory(getString(R.string.updates)) {
            addEnumeration(
                Preferences.Key.AutoSync,
                getString(R.string.sync_repositories_automatically)
            ) {
                when (it) {
                    Preferences.AutoSync.Never -> getString(R.string.never)
                    Preferences.AutoSync.Wifi -> getString(R.string.only_on_wifi)
                    Preferences.AutoSync.WifiBattery -> getString(R.string.only_on_wifi_and_battery)
                    Preferences.AutoSync.Always -> getString(R.string.always)
                }
            }
            addEditInt(
                Preferences.Key.ImagesCacheRetention,
                getString(R.string.images_cache_retention),
                1..365
            )
            addEditInt(
                Preferences.Key.ReleasesCacheRetention,
                getString(R.string.releases_cache_retention),
                0..365
            )
            addEditInt(
                Preferences.Key.AutoSyncInterval,
                getString(R.string.auto_sync_interval),
                1..1440
            )
            addSwitch(
                Preferences.Key.InstallAfterSync, getString(R.string.install_after_sync),
                getString(R.string.install_after_sync_summary)
            )
            addSwitch(
                Preferences.Key.UpdateNotify, getString(R.string.notify_about_updates),
                getString(R.string.notify_about_updates_summary)
            )
            addSwitch(
                Preferences.Key.UpdateUnstable, getString(R.string.unstable_updates),
                getString(R.string.unstable_updates_summary)
            )
            addSwitch(
                Preferences.Key.IncompatibleVersions, getString(R.string.incompatible_versions),
                getString(R.string.incompatible_versions_summary)
            )
        }
        preferences.addCategory(getString(R.string.install_types)) {
            addSwitch(
                Preferences.Key.RootPermission, getString(R.string.root_permission),
                getString(R.string.root_permission_description)
            )
            addSwitch(
                Preferences.Key.RootSessionInstaller, getString(R.string.root_session_installer),
                getString(R.string.root_session_installer_description)
            )
        }
    }
}
