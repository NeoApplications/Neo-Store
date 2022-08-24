package com.saggitt.omega.compose.objects

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.android.launcher3.R
import com.saggitt.omega.compose.navigation.Routes

open class PageItem(
    @StringRes val titleId: Int,
    @DrawableRes val iconId: Int = -1,
    val route: String
) {
    companion object {
        val PrefsProfile = PageItem(
            R.string.title__general_profile,
            R.drawable.ic_style,
            Routes.PREFS_PROFILE
        )
        val PrefsDesktop = PageItem(
            R.string.title__general_desktop,
            R.drawable.ic_desktop,
            Routes.PREFS_DESKTOP
        )
        val PrefsDock = PageItem(
            R.string.title__general_dock,
            R.drawable.ic_dock,
            Routes.PREFS_DOCK
        )
        val PrefsDrawer = PageItem(
            R.string.title__general_drawer,
            R.drawable.ic_apps_colored,
            Routes.PREFS_DRAWER
        )

        val PrefsWidgetsNotifications = PageItem(
            R.string.title__general_widgets_notifications,
            R.drawable.ic_widgets,
            Routes.PREFS_WIDGETS
        )
        val PrefsSearchFeed = PageItem(
            R.string.title__general_search_feed,
            R.drawable.ic_search_colored,
            Routes.PREFS_SEARCH
        )
        val PrefsGesturesDash = PageItem(
            R.string.title__general_gestures_dash,
            R.drawable.ic_gesture,
            Routes.PREFS_GESTURES
        )

        val PrefsBackup = PageItem(
            R.string.backups,
            R.drawable.ic_import_export,
            Routes.PREFS_BACKUPS
        )
        val PrefsDesktopMode = PageItem(
            R.string.pref_desktop_mode,
            R.drawable.ic_desktop,
            Routes.PREFS_DM
        )
        val PrefsDeveloper = PageItem(
            R.string.developer_options_title,
            R.drawable.ic_code,
            Routes.PREFS_DEV
        )
        val PrefsAbout = PageItem(
            R.string.title__general_about,
            R.drawable.ic_info,
            Routes.ABOUT
        )
        val AboutTranslators = PageItem(
            titleId = R.string.about_translators,
            iconId = R.drawable.ic_language,
            route = Routes.TRANSLATORS,
        )
        val AboutLicense = PageItem(
            titleId = R.string.category__about_licenses,
            iconId = R.drawable.ic_copyright,
            route = Routes.LICENSE,
        )
        val AboutChangelog = PageItem(
            titleId = R.string.title__about_changelog,
            iconId = R.drawable.ic_list,
            route = Routes.CHANGELOG,
        )
    }
}