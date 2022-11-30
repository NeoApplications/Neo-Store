package com.machiav3lli.fdroid.entity

import android.content.pm.PermissionInfo
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.machiav3lli.backup.ui.compose.icons.phosphor.GitPullRequest
import com.machiav3lli.fdroid.IDENTIFICATION_DATA_PERMISSIONS
import com.machiav3lli.fdroid.PERMISSION_GROUP_INTERNET
import com.machiav3lli.fdroid.PHYSICAL_DATA_PERMISSIONS
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.database.entity.Tracker
import com.machiav3lli.fdroid.ui.compose.icons.Icon
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.icon.Opensource
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.AddressBook
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Broadcast
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Bug
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Calendar
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ChartLine
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Chat
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Copyleft
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Copyright
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CurrencyCircleDollar
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.EyeSlash
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.FolderNotch
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Globe
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.MapPin
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Microphone
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Phone
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ShieldStar
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.User
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.UserFocus

class PrivacyData(
    val permissions: Map<PermissionGroup?, List<PermissionInfo>>,
    val trackers: List<Tracker>,
    val antiFeatures: List<AntiFeature>,
) {
    val physicalDataPermissions: Map<PermissionGroup?, List<PermissionInfo>>
        get() = permissions.filter { it.key in PHYSICAL_DATA_PERMISSIONS }
    val identificationDataPermissions: Map<PermissionGroup?, List<PermissionInfo>>
        get() = permissions.filter { it.key in IDENTIFICATION_DATA_PERMISSIONS }
    val otherPermissions: Map<PermissionGroup?, List<PermissionInfo>>
        get() = permissions.filterNot { it.key in (PHYSICAL_DATA_PERMISSIONS + IDENTIFICATION_DATA_PERMISSIONS) }

}

class PrivacyNote(
    val permissionsNote: Int,
    val trackersNote: Int,
    val sourceType: SourceType,
)

class SourceType(
    val open: Boolean,
    val free: Boolean,
    val independent: Boolean,
)

open class PermissionGroup(
    val name: String,
    @StringRes val labelId: Int,
    val icon: ImageVector,
) {
    object Contacts : PermissionGroup(
        android.Manifest.permission_group.CONTACTS,
        R.string.permission_contacts,
        Phosphor.AddressBook
    )

    object Calendar : PermissionGroup(
        android.Manifest.permission_group.CALENDAR,
        R.string.permission_calendar,
        Phosphor.Calendar
    )

    object SMS : PermissionGroup(
        android.Manifest.permission_group.SMS,
        R.string.permission_sms,
        Phosphor.Chat
    )

    object Storage : PermissionGroup(
        android.Manifest.permission_group.STORAGE,
        R.string.permission_storage,
        Phosphor.FolderNotch
    )

    object Phone : PermissionGroup(
        android.Manifest.permission_group.PHONE,
        R.string.permission_phone,
        Phosphor.Phone
    )

    object Location : PermissionGroup(
        android.Manifest.permission_group.LOCATION,
        R.string.permission_location,
        Phosphor.MapPin
    )

    object Camera : PermissionGroup(
        android.Manifest.permission_group.CAMERA,
        R.string.permission_contacts,
        Phosphor.AddressBook
    )

    object Microphone : PermissionGroup(
        android.Manifest.permission_group.MICROPHONE,
        R.string.permission_microphone,
        Phosphor.Microphone
    )

    object NearbyDevices : PermissionGroup(
        android.Manifest.permission_group.NEARBY_DEVICES,
        R.string.permission_nearby_devices,
        Phosphor.Broadcast
    )

    object Internet : PermissionGroup(
        PERMISSION_GROUP_INTERNET,
        R.string.permission_internet,
        Phosphor.Globe
    )

    object Other : PermissionGroup(
        "",
        R.string.permission_other,
        Phosphor.ShieldStar
    )

    companion object {
        fun String.getPermissionGroup() = when (this) {
            android.Manifest.permission_group.CONTACTS -> Contacts
            android.Manifest.permission_group.CALENDAR -> Calendar
            android.Manifest.permission_group.SMS -> SMS
            android.Manifest.permission_group.STORAGE -> Storage
            android.Manifest.permission_group.PHONE -> Phone
            android.Manifest.permission_group.MICROPHONE -> Microphone
            android.Manifest.permission_group.LOCATION -> Location
            android.Manifest.permission_group.CAMERA -> Camera
            android.Manifest.permission_group.NEARBY_DEVICES -> NearbyDevices
            PERMISSION_GROUP_INTERNET -> Internet
            else -> Other
        }
    }
}

open class TrackersGroup(
    @StringRes labelId: Int,
    @StringRes descriptionId: Int,
    icon: ImageVector,
) {
    object Identification : TrackersGroup(
        R.string.trackers_identification,
        R.string.trackers_identification_description,
        Phosphor.User
    )

    object Analytics : TrackersGroup(
        R.string.trackers_analytics,
        R.string.trackers_analytics_description,
        Phosphor.ChartLine
    )

    object Ads : TrackersGroup(
        R.string.trackers_ads,
        R.string.trackers_ads_description,
        Phosphor.CurrencyCircleDollar
    )

    object Profiling : TrackersGroup(
        R.string.trackers_profiling,
        R.string.trackers_profiling_description,
        Phosphor.UserFocus
    )

    object BugReporting : TrackersGroup(
        R.string.trackers_bug,
        R.string.trackers_bug_description,
        Phosphor.Bug
    )
}

open class SourceInfo(
    @StringRes labelId: Int,
    @StringRes descriptionId: Int,
    icon: ImageVector,
) {
    object Proprietary : SourceInfo(
        R.string.source_proprietary,
        R.string.source_proprietary_description,
        Phosphor.EyeSlash
    )

    object Open : SourceInfo(
        R.string.source_open,
        R.string.source_open_description,
        Icon.Opensource
    )

    object Copyleft : SourceInfo(
        R.string.source_copyleft,
        R.string.source_copyleft_description,
        Phosphor.Copyleft
    )

    object Copyright : SourceInfo(
        R.string.source_copyright,
        R.string.source_copyright_description,
        Phosphor.Copyright
    )

    object Dependency : SourceInfo(
        R.string.source_dependencies,
        R.string.source_dependencies_description,
        Phosphor.GitPullRequest
    )

}