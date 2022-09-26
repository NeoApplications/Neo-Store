package com.machiav3lli.fdroid.entity

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.machiav3lli.fdroid.HELP_CHANGELOG
import com.machiav3lli.fdroid.HELP_LICENSE
import com.machiav3lli.fdroid.HELP_MATRIX
import com.machiav3lli.fdroid.HELP_SOURCECODE
import com.machiav3lli.fdroid.HELP_TELEGRAM
import com.machiav3lli.fdroid.R

enum class Order(@StringRes val titleResId: Int, @DrawableRes val iconResId: Int) {
    NAME(R.string.name, R.drawable.ic_placeholder),
    DATE_ADDED(R.string.date_added, R.drawable.ic_placeholder),
    LAST_UPDATE(R.string.date_updated, R.drawable.ic_placeholder)
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
    INSTALLED(false, true),
    UPDATES(false, false),
    UPDATED(false, true),
    NEW(false, true)
}

enum class LinkRef(
    @StringRes val titleId: Int,
    val url: String? = null
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
