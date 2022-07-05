package com.machiav3lli.fdroid.entity

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.machiav3lli.fdroid.R

enum class Order(@StringRes val titleResId: Int, @DrawableRes val iconResId: Int) {
    NAME(R.string.name,R.drawable.ic_placeholder),
    DATE_ADDED(R.string.whats_new,R.drawable.ic_placeholder),
    LAST_UPDATE(R.string.recently_updated,R.drawable.ic_placeholder)
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