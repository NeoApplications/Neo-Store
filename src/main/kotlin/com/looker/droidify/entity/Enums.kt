package com.looker.droidify.entity

import com.looker.droidify.R

enum class Order(val titleResId: Int) {
    NAME(R.string.name),
    DATE_ADDED(R.string.whats_new),
    LAST_UPDATE(R.string.recently_updated)
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