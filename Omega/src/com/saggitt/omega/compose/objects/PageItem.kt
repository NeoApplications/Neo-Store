package com.saggitt.omega.compose.objects

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.android.launcher3.R

open class PageItem(
    @StringRes val titleId: Int,
    @DrawableRes val iconId: Int = -1,
    val route: String
) {
    companion object {
        val PrefsProfile = PageItem(
            R.string.title__general_profile,
            R.drawable.ic_style,
            "prefs_profile"
        )
        val PrefsDesktop = PageItem(
            R.string.title__general_desktop,
            R.drawable.ic_desktop,
            "prefs_desktop"
        )
        val PrefsDock = PageItem(
            R.string.title__general_dock,
            R.drawable.ic_dock,
            "prefs_dock"
        )
        val PrefsDrawer = PageItem(
            R.string.title__general_drawer,
            R.drawable.ic_apps_colored,
            "prefs_drawer"
        )

        val PrefsWidgetsNotifications = PageItem(
            R.string.title__general_widgets_notifications,
            R.drawable.ic_widgets,
            "prefs_widgets"
        )
        val PrefsSearchFeed = PageItem(
            R.string.title__general_search_feed,
            R.drawable.ic_search_colored,
            "prefs_search"
        )
        val PrefsGesturesDash = PageItem(
            R.string.title__general_gestures_dash,
            R.drawable.ic_gesture,
            "prefs_gestures"
        )

        val PrefsBackup = PageItem(
            R.string.backups,
            R.drawable.ic_import_export,
            "prefs_backup"
        )
        val PrefsDesktopMode = PageItem(
            R.string.pref_desktop_mode,
            R.drawable.ic_desktop,
            "prefs_desktop_mode"
        )
        val PrefsDeveloper = PageItem(
            R.string.developer_options_title,
            R.drawable.ic_code,
            "prefs_developer"
        )
    }
}