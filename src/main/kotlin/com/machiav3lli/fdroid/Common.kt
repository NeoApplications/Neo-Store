package com.machiav3lli.fdroid

const val NOTIFICATION_CHANNEL_SYNCING = "syncing"
const val NOTIFICATION_CHANNEL_UPDATES = "updates"
const val NOTIFICATION_CHANNEL_DOWNLOADING = "downloading"
const val NOTIFICATION_CHANNEL_INSTALLER = "installed"

const val NOTIFICATION_ID_SYNCING = 1
const val NOTIFICATION_ID_UPDATES = 2
const val NOTIFICATION_ID_DOWNLOADING = 3
const val NOTIFICATION_ID_INSTALLER = 4

const val TABLE_CATEGORY = "category"
const val TABLE_CATEGORY_NAME = "category"
const val TABLE_CATEGORY_TEMP_NAME = "temporary_category"
const val TABLE_INSTALLED = "installed"
const val TABLE_INSTALLED_NAME = "memory_installed"
const val TABLE_PRODUCT = "product"
const val TABLE_PRODUCT_NAME = "product"
const val TABLE_PRODUCT_TEMP_NAME = "temporary_product"
const val TABLE_RELEASE = "release"
const val TABLE_RELEASE_NAME = "release"
const val TABLE_REPOSITORY = "repository"
const val TABLE_REPOSITORY_NAME = "repository"
const val TABLE_EXTRAS = "extras"
const val TABLE_EXTRAS_NAME = "extras"

const val ROW_REPOSITORY_ID = "repositoryId"
const val ROW_PACKAGE_NAME = "packageName"
const val ROW_LABEL = "label"
const val ROW_SUMMARY = "summary"
const val ROW_DESCRIPTION = "description"
const val ROW_ADDED = "added"
const val ROW_UPDATED = "updated"
const val ROW_VERSION_CODE = "versionCode"
const val ROW_SIGNATURES = "signatures"
const val ROW_COMPATIBLE = "compatible"
const val ROW_ICON = "icon"
const val ROW_METADATA_ICON = "metadataIcon"
const val ROW_RELEASES = "releases"
const val ROW_CATEGORIES = "categories"
const val ROW_ANTIFEATURES = "antiFeatures"
const val ROW_LICENSES = "licenses"
const val ROW_DONATES = "donates"
const val ROW_SCREENSHOTS = "screenshots"
const val ROW_SIGNATURE = "signature"
const val ROW_ID = "_id"
const val ROW_ENABLED = "enabled"
const val ROW_AUTHOR = "author"
const val ROW_SUGGESTED_VERSION_CODE = "suggestedVersionCode"
const val ROW_SOURCE = "source"
const val ROW_WEB = "web"
const val ROW_TRACKER = "tracker"
const val ROW_CHANGELOG = "changelog"
const val ROW_WHATS_NEW = "whatsNew"
const val ROW_CAN_UPDATE = "can_update"
const val ROW_IGNORED_VERSION = "ignoredVersion"
const val ROW_IGNORE_UPDATES = "ignoreUpdates"
const val ROW_FAVORITE = "favorite"

const val RELEASE_STATE_NONE = 0
const val RELEASE_STATE_SUGGESTED = 1
const val RELEASE_STATE_INSTALLED = 2

const val JOB_ID_SYNC = 1
const val EXODUS_TRACKERS_SYNC = -22L

const val PREFS_LANGUAGE = "languages"
const val PREFS_LANGUAGE_DEFAULT = "system"

const val EXTRA_REPOSITORY_ID = "repositoryId"
const val EXTRA_PAGE_ROUTE = "pageRoute"
const val EXTRA_INTENT_HANDLED = "intentHandled"
const val INTENT_ACTION_BINARY_EYE = "com.google.zxing.client.android.SCAN"

const val HELP_SOURCECODE = "https://github.com/NeoApplications/Neo-Store"
const val HELP_CHANGELOG = "https://github.com/NeoApplications/Neo-Store/blob/master/CHANGELOG.md"
const val HELP_TELEGRAM = "https://t.me/neo_android_store"
const val HELP_MATRIX = "https://matrix.to/#/#neo-store:matrix.org"
const val HELP_LICENSE = "https://github.com/NeoApplications/Neo-Store/blob/master/COPYING"

const val NAV_MAIN = 0
const val NAV_PREFS = 1

interface RepoManager {
    fun onDeleteConfirm()
}
