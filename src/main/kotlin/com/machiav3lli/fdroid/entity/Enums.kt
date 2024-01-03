package com.machiav3lli.fdroid.entity

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.machiav3lli.fdroid.HELP_CHANGELOG
import com.machiav3lli.fdroid.HELP_LICENSE
import com.machiav3lli.fdroid.HELP_MATRIX
import com.machiav3lli.fdroid.HELP_SOURCECODE
import com.machiav3lli.fdroid.HELP_TELEGRAM
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CalendarPlus
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CalendarX
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.TagSimple

enum class InstallerType(@StringRes val titleResId: Int) {
    DEFAULT(R.string.default_installer),
    ROOT(R.string.root_installer),
    LEGACY(R.string.legacy_installer),
    AM(R.string.am_installer),
}

enum class Contrast(val themes: List<Preferences.Theme>) {
    NORMAL(
        listOf(
            Preferences.Theme.Light,
            Preferences.Theme.Dark,
            Preferences.Theme.Black
        )
    ),
    MEDIUM(
        listOf(
            Preferences.Theme.LightMediumContrast,
            Preferences.Theme.DarkMediumContrast,
            Preferences.Theme.BlackMediumContrast
        )
    ),
    HIGH(
        listOf(
            Preferences.Theme.LightHighContrast,
            Preferences.Theme.DarkHighContrast,
            Preferences.Theme.BlackHighContrast
        )
    )
}

enum class Order(@StringRes val titleResId: Int, val icon: ImageVector) {
    NAME(R.string.name, Phosphor.TagSimple),
    DATE_ADDED(R.string.date_added, Phosphor.CalendarX),
    LAST_UPDATE(R.string.date_updated, Phosphor.CalendarPlus)
}

enum class UpdateCategory(val id: Int) {
    ALL(0),
    UPDATED(1),
    NEW(2)
}

enum class InstallState {
    INSTALL,
    INSTALLING,
    INSTALLED,
    PENDING
}

enum class Source(val sections: Boolean, val order: Boolean) {
    AVAILABLE(true, true),
    SEARCH(false, true),
    INSTALLED(false, true),
    UPDATES(false, false),
    UPDATED(false, true),
    NEW(false, true)
}

enum class LinkRef(
    @StringRes val titleId: Int,
    val url: String? = null,
) {
    Sourcecode(
        titleId = R.string.source_code,
        url = HELP_SOURCECODE
    ),
    Changelog(
        titleId = R.string.changelog,
        url = HELP_CHANGELOG
    ),
    Telegram(
        titleId = R.string.group_telegram,
        url = HELP_TELEGRAM
    ),
    Matrix(
        titleId = R.string.group_matrix,
        url = HELP_MATRIX
    ),
    License(
        titleId = R.string.license,
        url = HELP_LICENSE
    ),
}
