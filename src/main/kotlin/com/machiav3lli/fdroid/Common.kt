package com.machiav3lli.fdroid

import com.machiav3lli.fdroid.entity.PermissionGroup

const val NOTIFICATION_CHANNEL_SYNCING = "syncing"
const val NOTIFICATION_CHANNEL_UPDATES = "updates"
const val NOTIFICATION_CHANNEL_DOWNLOADING = "downloading"
const val NOTIFICATION_CHANNEL_INSTALLER = "installed"
const val NOTIFICATION_CHANNEL_VULNS = "vulnerabilities"

const val NOTIFICATION_ID_SYNCING = 1
const val NOTIFICATION_ID_UPDATES = 2
const val NOTIFICATION_ID_DOWNLOADING = 3
const val NOTIFICATION_ID_INSTALLER = 4
const val NOTIFICATION_ID_VULNS = 5

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
const val NETWORK_TYPE_WIFI = 16
const val EXODUS_TRACKERS_SYNC = -22L

const val PREFS_LANGUAGE = "languages"
const val PREFS_LANGUAGE_DEFAULT = "system"

const val FILTER_CATEGORY_ALL = "All"

const val EXTRA_REPOSITORY_ID = "repositoryId"
const val EXTRA_REPOSITORY_EDIT = "editMode"
const val EXTRA_PAGE_ROUTE = "pageRoute"
const val EXTRA_INTENT_HANDLED = "intentHandled"
const val INTENT_ACTION_BINARY_EYE = "com.google.zxing.client.android.SCAN"

const val HELP_SOURCECODE = "https://github.com/NeoApplications/Neo-Store"
const val HELP_CHANGELOG = "https://github.com/NeoApplications/Neo-Store/blob/master/CHANGELOG.md"
const val HELP_TELEGRAM = "https://t.me/neo_android_store"
const val HELP_MATRIX = "https://matrix.to/#/#neo-store:matrix.org"
const val HELP_LICENSE = "https://github.com/NeoApplications/Neo-Store/blob/master/COPYING"

const val PERMISSION_GROUP_INTERNET = "android.permission-group.INTERNET"
const val PERMISSION_READ_CELL_BROADCASTS = "android.permission.READ_CELL_BROADCASTS"

const val TC_PACKAGENAME = "net.kollnig.missioncontrol"
const val TC_PACKAGENAME_FDROID = "net.kollnig.missioncontrol.fdroid"
const val TC_INTENT_EXTRA_SEARCH = "Search"

const val NAV_MAIN = 0
const val NAV_PREFS = 1

// Permissions based on Risk

val LOW_RISK_PERMISSIONS = listOf(
    android.Manifest.permission.ACCEPT_HANDOVER,
    android.Manifest.permission.ACCOUNT_MANAGER,
    android.Manifest.permission.ADD_VOICEMAIL,
    android.Manifest.permission.ANSWER_PHONE_CALLS,
    android.Manifest.permission.BATTERY_STATS,
    android.Manifest.permission.BIND_CALL_REDIRECTION_SERVICE,
    android.Manifest.permission.BIND_CARRIER_MESSAGING_CLIENT_SERVICE,
    android.Manifest.permission.BIND_CARRIER_MESSAGING_SERVICE,
    android.Manifest.permission.BIND_CARRIER_SERVICES,
    android.Manifest.permission.BIND_DEVICE_ADMIN,
    android.Manifest.permission.BIND_WALLPAPER,
    android.Manifest.permission.BROADCAST_WAP_PUSH,
    android.Manifest.permission.CHANGE_NETWORK_STATE,
    android.Manifest.permission.CHANGE_WIFI_MULTICAST_STATE,
    android.Manifest.permission.CHANGE_WIFI_STATE,
    android.Manifest.permission.DELETE_CACHE_FILES,
    android.Manifest.permission.DELETE_PACKAGES,
    android.Manifest.permission.GET_ACCOUNTS,
    android.Manifest.permission.GET_ACCOUNTS,
    android.Manifest.permission.GET_ACCOUNTS_PRIVILEGED,
    android.Manifest.permission.MANAGE_ONGOING_CALLS,
    android.Manifest.permission.MEDIA_CONTENT_CONTROL,
    android.Manifest.permission.MODIFY_AUDIO_SETTINGS,
    android.Manifest.permission.MODIFY_PHONE_STATE,
    android.Manifest.permission.MOUNT_FORMAT_FILESYSTEMS,
    android.Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
    android.Manifest.permission.PACKAGE_USAGE_STATS,
    android.Manifest.permission.PROCESS_OUTGOING_CALLS,
    android.Manifest.permission.NFC,
    android.Manifest.permission.NFC_PREFERRED_PAYMENT_INFO,
    android.Manifest.permission.NFC_TRANSACTION_EVENT,
    android.Manifest.permission.QUERY_ALL_PACKAGES,
    android.Manifest.permission.READ_CALENDAR,
    android.Manifest.permission.READ_CALL_LOG,
    android.Manifest.permission.READ_CONTACTS,
    android.Manifest.permission.READ_LOGS,
    android.Manifest.permission.READ_PHONE_NUMBERS,
    android.Manifest.permission.READ_PHONE_STATE,
    android.Manifest.permission.READ_PRECISE_PHONE_STATE,
    android.Manifest.permission.READ_VOICEMAIL,
    android.Manifest.permission.RECEIVE_WAP_PUSH,
    android.Manifest.permission.REQUEST_COMPANION_PROFILE_COMPUTER,
    android.Manifest.permission.REQUEST_COMPANION_PROFILE_WATCH,
    android.Manifest.permission.REQUEST_COMPANION_RUN_IN_BACKGROUND,
    android.Manifest.permission.REQUEST_COMPANION_SELF_MANAGED,
    android.Manifest.permission.REQUEST_COMPANION_START_FOREGROUND_SERVICES_FROM_BACKGROUND,
    android.Manifest.permission.REQUEST_COMPANION_USE_DATA_IN_BACKGROUND,
    android.Manifest.permission.REQUEST_DELETE_PACKAGES,
    android.Manifest.permission.REQUEST_INSTALL_PACKAGES,
    android.Manifest.permission.SCHEDULE_EXACT_ALARM,
    android.Manifest.permission.SET_ALARM,
    android.Manifest.permission.START_FOREGROUND_SERVICES_FROM_BACKGROUND,
    android.Manifest.permission.USE_SIP,
    android.Manifest.permission.UWB_RANGING,
    android.Manifest.permission.WRITE_CALENDAR,
    android.Manifest.permission.WRITE_CALL_LOG,
    android.Manifest.permission.WRITE_CONTACTS,
    android.Manifest.permission.WRITE_VOICEMAIL,
)

val MEDIUM_RISK_PERMISSIONS = listOf(
    android.Manifest.permission.ACCESS_COARSE_LOCATION,
    android.Manifest.permission.ACCESS_MEDIA_LOCATION,
    android.Manifest.permission.ACCESS_NETWORK_STATE,
    android.Manifest.permission.ACCESS_WIFI_STATE,
    android.Manifest.permission.BIND_NFC_SERVICE,
    android.Manifest.permission.BIND_VPN_SERVICE,
    android.Manifest.permission.BLUETOOTH,
    android.Manifest.permission.BLUETOOTH_ADMIN,
    android.Manifest.permission.BROADCAST_SMS,
    android.Manifest.permission.CAPTURE_AUDIO_OUTPUT,
    android.Manifest.permission.INTERNET,
    android.Manifest.permission.MANAGE_DOCUMENTS,
    android.Manifest.permission.MANAGE_MEDIA,
    android.Manifest.permission.MANAGE_WIFI_INTERFACES,
    android.Manifest.permission.MANAGE_WIFI_NETWORK_SELECTION,
    android.Manifest.permission.NEARBY_WIFI_DEVICES,
    android.Manifest.permission.OVERRIDE_WIFI_CONFIG,
    PERMISSION_READ_CELL_BROADCASTS,
    android.Manifest.permission.READ_MEDIA_AUDIO,
    android.Manifest.permission.READ_MEDIA_IMAGES,
    android.Manifest.permission.READ_MEDIA_VIDEO,
    android.Manifest.permission.READ_SMS,
    android.Manifest.permission.REBOOT,
    android.Manifest.permission.RECEIVE_MMS,
    android.Manifest.permission.RECEIVE_SMS,
    android.Manifest.permission.SEND_SMS,
    android.Manifest.permission.SMS_FINANCIAL_TRANSACTIONS,
)

val HIGH_RISK_PERMISSIONS = listOf(
    android.Manifest.permission.ACCESS_BACKGROUND_LOCATION,
    android.Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
    android.Manifest.permission.ACCESS_FINE_LOCATION,
    android.Manifest.permission.ACTIVITY_RECOGNITION,
    android.Manifest.permission.BLUETOOTH_ADVERTISE,
    android.Manifest.permission.BLUETOOTH_CONNECT,
    android.Manifest.permission.BLUETOOTH_SCAN,
    android.Manifest.permission.BLUETOOTH_PRIVILEGED,
    android.Manifest.permission.BODY_SENSORS,
    android.Manifest.permission.CAMERA,
    android.Manifest.permission.MANAGE_EXTERNAL_STORAGE,
    android.Manifest.permission.READ_EXTERNAL_STORAGE,
    android.Manifest.permission.RECORD_AUDIO,
    android.Manifest.permission.USE_FINGERPRINT,
    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
)

// Permissions based on Group

val CONTACTS_PERMISSIONS = listOf(
    android.Manifest.permission.READ_CALL_LOG,
    android.Manifest.permission.READ_CONTACTS,
    android.Manifest.permission.READ_PHONE_NUMBERS,
    android.Manifest.permission.READ_VOICEMAIL,
    android.Manifest.permission.WRITE_CALL_LOG,
    android.Manifest.permission.WRITE_CONTACTS,
    android.Manifest.permission.WRITE_VOICEMAIL,
)

val CALENDAR_PERMISSIONS = listOf(
    android.Manifest.permission.READ_CALENDAR,
    android.Manifest.permission.WRITE_CALENDAR,
)

val SMS_PERMISSIONS = listOf(
    android.Manifest.permission.BROADCAST_SMS,
    android.Manifest.permission.READ_SMS,
    android.Manifest.permission.RECEIVE_SMS,
    android.Manifest.permission.RECEIVE_MMS,
    android.Manifest.permission.RECEIVE_WAP_PUSH,
    android.Manifest.permission.SEND_SMS,
    android.Manifest.permission.SMS_FINANCIAL_TRANSACTIONS,
    PERMISSION_READ_CELL_BROADCASTS,
)

val STORAGE_PERMISSIONS = listOf(
    android.Manifest.permission.DELETE_CACHE_FILES,
    android.Manifest.permission.MANAGE_DOCUMENTS,
    android.Manifest.permission.MANAGE_EXTERNAL_STORAGE,
    android.Manifest.permission.MANAGE_MEDIA,
    android.Manifest.permission.MOUNT_FORMAT_FILESYSTEMS,
    android.Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
    android.Manifest.permission.READ_EXTERNAL_STORAGE,
    android.Manifest.permission.READ_MEDIA_AUDIO,
    android.Manifest.permission.READ_MEDIA_IMAGES,
    android.Manifest.permission.READ_MEDIA_VIDEO,
    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
)

val PHONE_PERMISSIONS = listOf(
    android.Manifest.permission.ACCEPT_HANDOVER,
    android.Manifest.permission.ACCOUNT_MANAGER,
    android.Manifest.permission.ADD_VOICEMAIL,
    android.Manifest.permission.ANSWER_PHONE_CALLS,
    android.Manifest.permission.BATTERY_STATS,
    android.Manifest.permission.BIND_CALL_REDIRECTION_SERVICE,
    android.Manifest.permission.BIND_CARRIER_MESSAGING_CLIENT_SERVICE,
    android.Manifest.permission.BIND_CARRIER_MESSAGING_SERVICE,
    android.Manifest.permission.BIND_CARRIER_SERVICES,
    android.Manifest.permission.BIND_DEVICE_ADMIN,
    android.Manifest.permission.BIND_WALLPAPER,
    android.Manifest.permission.CALL_COMPANION_APP,
    android.Manifest.permission.CALL_PHONE,
    android.Manifest.permission.CALL_PRIVILEGED,
    android.Manifest.permission.DELETE_PACKAGES,
    android.Manifest.permission.GET_ACCOUNTS,
    android.Manifest.permission.GET_ACCOUNTS_PRIVILEGED,
    android.Manifest.permission.MANAGE_ONGOING_CALLS,
    android.Manifest.permission.MEDIA_CONTENT_CONTROL,
    android.Manifest.permission.MODIFY_AUDIO_SETTINGS,
    android.Manifest.permission.MODIFY_PHONE_STATE,
    android.Manifest.permission.PACKAGE_USAGE_STATS,
    android.Manifest.permission.PROCESS_OUTGOING_CALLS,
    android.Manifest.permission.QUERY_ALL_PACKAGES,
    android.Manifest.permission.READ_LOGS,
    android.Manifest.permission.READ_PHONE_STATE,
    android.Manifest.permission.READ_PRECISE_PHONE_STATE,
    android.Manifest.permission.REBOOT,
    android.Manifest.permission.REQUEST_COMPANION_PROFILE_COMPUTER,
    android.Manifest.permission.REQUEST_COMPANION_PROFILE_WATCH,
    android.Manifest.permission.REQUEST_COMPANION_RUN_IN_BACKGROUND,
    android.Manifest.permission.REQUEST_COMPANION_SELF_MANAGED,
    android.Manifest.permission.REQUEST_COMPANION_START_FOREGROUND_SERVICES_FROM_BACKGROUND,
    android.Manifest.permission.REQUEST_COMPANION_USE_DATA_IN_BACKGROUND,
    android.Manifest.permission.REQUEST_DELETE_PACKAGES,
    android.Manifest.permission.REQUEST_INSTALL_PACKAGES,
    android.Manifest.permission.SCHEDULE_EXACT_ALARM,
    android.Manifest.permission.SET_ALARM,
    android.Manifest.permission.START_FOREGROUND_SERVICES_FROM_BACKGROUND,
    android.Manifest.permission.USE_SIP,
)

val LOCATION_PERMISSIONS = listOf(
    android.Manifest.permission.ACCESS_MEDIA_LOCATION,
    android.Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
    android.Manifest.permission.ACCESS_FINE_LOCATION,
    android.Manifest.permission.ACCESS_COARSE_LOCATION,
    android.Manifest.permission.ACCESS_BACKGROUND_LOCATION,
)

val MICROPHONE_PERMISSIONS = listOf(
    android.Manifest.permission.RECORD_AUDIO,
    android.Manifest.permission.CAPTURE_AUDIO_OUTPUT,
)

val CAMERA_PERMISSIONS = listOf(
    android.Manifest.permission.CAMERA,
)

val NEARBY_DEVICES_PERMISSIONS = listOf(
    android.Manifest.permission.ACTIVITY_RECOGNITION,
    android.Manifest.permission.BIND_NFC_SERVICE,
    android.Manifest.permission.BLUETOOTH,
    android.Manifest.permission.BLUETOOTH_ADMIN,
    android.Manifest.permission.BLUETOOTH_ADVERTISE,
    android.Manifest.permission.BLUETOOTH_CONNECT,
    android.Manifest.permission.BLUETOOTH_SCAN,
    android.Manifest.permission.BLUETOOTH_PRIVILEGED,
    android.Manifest.permission.BROADCAST_WAP_PUSH,
    android.Manifest.permission.BODY_SENSORS,
    android.Manifest.permission.CHANGE_WIFI_MULTICAST_STATE,
    android.Manifest.permission.CHANGE_WIFI_STATE,
    android.Manifest.permission.MANAGE_WIFI_INTERFACES,
    android.Manifest.permission.MANAGE_WIFI_NETWORK_SELECTION,
    android.Manifest.permission.NFC,
    android.Manifest.permission.NFC_PREFERRED_PAYMENT_INFO,
    android.Manifest.permission.NFC_TRANSACTION_EVENT,
    android.Manifest.permission.NEARBY_WIFI_DEVICES,
    android.Manifest.permission.OVERRIDE_WIFI_CONFIG,
    android.Manifest.permission.USE_FINGERPRINT,
    android.Manifest.permission.UWB_RANGING,
)

val INTERNET_PERMISSIONS = listOf(
    android.Manifest.permission.ACCESS_NETWORK_STATE,
    android.Manifest.permission.ACCESS_WIFI_STATE,
    android.Manifest.permission.BIND_VPN_SERVICE,
    android.Manifest.permission.CHANGE_NETWORK_STATE,
    android.Manifest.permission.INTERNET,
)

// Trackers special groups

val WIDESPREAD_TRACKERS = listOf(
    49, // Google Firebase Analytics
    312, // Google AdMob
    27, // Google Crashlytics
    67, // Facebook Login
    70, // Facebook Share
    66, // Facebook Analytics
    48, // Google Analytics
    65, // Facebook Ads
    105, // Google Tag Manager
    121, // Unity3D Ads
    72, // AppLovin
    328, // IAB Open Measurement
    69, // Facebook Places
    12, // AppsFlyer
    106, // Inmobi
    25, // Flurry (Yahoo)
    146, // IronSource
    90, // AdColony
    169, // Vungle (Liftoff)
    61, // Moat (Oracle)
    193, // OneSignal
    92, // Amazon Advertisement
    52, // Adjust (AppLovin)
    35, // Twitter MoPub
    53, // ChartBoost
    // MAGMA complements
    240, // Google Analytics Plugin (Cordova)
    387, // Anvato (Google)
    5, // Google DoubleClick
    95, // Amazon Insights
    423, // Amplify (Amazon Mobile Analytics)
    93, // Amazon Mobile Associates
    238, // Microsoft Visual Studio App Center Crashes
    243, // Microsoft Visual Studio App Center Analytics
    392, // Facebook Flipper
    68, // Facebook Notifications
    47, // Facebook Audience
)

val NON_FREE_COUNTRIES_TRACKERS = listOf(
    124, // Yandex Ad
    333, // Huawei Mobile Services Core
    363, // Pangle (TikTok)
    200, // Mintegral
    140, // Appmetrica (Yandex)
    198, // myTarget (Mail.Ru)
    336, // Mai.Ru
)

// PermissionGroup groups

val PHYSICAL_DATA_PERMISSIONS = listOf(
    PermissionGroup.Location,
    PermissionGroup.Camera,
    PermissionGroup.Microphone,
    PermissionGroup.NearbyDevices,
)

val IDENTIFICATION_DATA_PERMISSIONS = listOf(
    PermissionGroup.Contacts,
    PermissionGroup.Calendar,
    PermissionGroup.Phone,
    PermissionGroup.SMS,
    PermissionGroup.Storage,
    PermissionGroup.Internet,
)

interface RepoManager {
    fun onDeleteConfirm(repoId: Long)
}
