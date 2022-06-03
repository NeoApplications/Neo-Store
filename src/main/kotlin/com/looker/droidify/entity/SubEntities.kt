package com.looker.droidify.entity

import android.content.Context
import android.content.pm.PermissionGroupInfo
import android.content.pm.PermissionInfo
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Launch
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.ui.graphics.vector.ImageVector
import com.looker.droidify.R
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class Author(val name: String = "", val email: String = "", val web: String = "") {
    fun toJSON() = Json.encodeToString(this)

    companion object {
        fun fromJson(json: String) = Json.decodeFromString<Author>(json)
    }
}

@Serializable
sealed class Donate {
    @Serializable
    data class Regular(val url: String) : Donate()

    @Serializable
    data class Bitcoin(val address: String) : Donate()

    @Serializable
    data class Litecoin(val address: String) : Donate()

    @Serializable
    data class Flattr(val id: String) : Donate()

    @Serializable
    data class Liberapay(val id: String) : Donate()

    @Serializable
    data class OpenCollective(val id: String) : Donate()

    fun toJSON() = Json.encodeToString(this)

    companion object {
        fun fromJson(json: String) = Json.decodeFromString<Donate>(json)
    }
}

@Serializable
class Screenshot(val locale: String, val type: Type, val path: String) {
    enum class Type(val jsonName: String) {
        PHONE("phone"),
        SMALL_TABLET("smallTablet"),
        LARGE_TABLET("largeTablet")
    }

    val identifier: String
        get() = "$locale.${type.name}.$path"

    fun toJSON() = Json.encodeToString(this)

    companion object {
        fun fromJson(json: String) = Json.decodeFromString<Screenshot>(json)
    }
}

enum class AntiFeature(val key: String, @StringRes val titleResId: Int) {
    ADS("Ads", R.string.has_advertising),
    DEBUGGABLE("ApplicationDebuggable", R.string.compiled_for_debugging),
    DISABLED_ALGORITHM("DisabledAlgorithm", R.string.signed_using_unsafe_algorithm),
    KNOWN_VULN("KnownVuln", R.string.has_security_vulnerabilities),
    NO_SOURCE_SINCE("NoSourceSince", R.string.source_code_no_longer_available),
    NON_FREE_ADD("NonFreeAdd", R.string.promotes_non_free_software),
    NON_FREE_ASSETS("NonFreeAssets", R.string.contains_non_free_media),
    NON_FREE_DEP("NonFreeDep", R.string.has_non_free_dependencies),
    NON_FREE_NET("NonFreeNet", R.string.promotes_non_free_network_services),
    TRACKING("Tracking", R.string.tracks_or_reports_your_activity),
    NON_FREE_UPSTREAM("UpstreamNonFree", R.string.upstream_source_code_is_not_free),
    NSFW("NSFW", R.string.not_safe_for_work)
}

sealed interface ComponentState {
    val icon: ImageVector
    val textId: Int
}

sealed class DownloadState(
    @StringRes override val textId: Int,
    override val icon: ImageVector = Icons.Rounded.Close
) : ComponentState {

    object Pending : DownloadState(R.string.pending)
    object Connecting : DownloadState(R.string.connecting)
    class Downloading(val downloaded: Long, val total: Long?) :
        DownloadState(R.string.downloading)

    object Installing : DownloadState(R.string.installing)
}

sealed class ActionState(
    @StringRes override val textId: Int,
    override val icon: ImageVector = Icons.Rounded.Download
) : ComponentState {

    object Install : ActionState(R.string.install, Icons.Rounded.Download)
    object Update : ActionState(R.string.update, Icons.Rounded.Download)
    object Uninstall : ActionState(R.string.uninstall, Icons.Rounded.Delete)
    object Launch : ActionState(R.string.launch, Icons.Rounded.Launch)
    object Details : ActionState(R.string.details, Icons.Rounded.Tune)
    object Share : ActionState(R.string.share, Icons.Rounded.Share)
    class Cancel(@StringRes stateId: Int) : ActionState(stateId, Icons.Rounded.Close)
    object NoAction : ActionState(R.string.no_action_possible, Icons.Rounded.Close)
}

open class LinkType(
    @DrawableRes val iconResId: Int,
    val title: String,
    val link: Uri? = null
)

class DonateType(donate: Donate, context: Context) : LinkType(
    iconResId = when (donate) {
        is Donate.Regular -> R.drawable.ic_donate_regular
        is Donate.Bitcoin -> R.drawable.ic_donate_bitcoin
        is Donate.Litecoin -> R.drawable.ic_donate_litecoin
        is Donate.Flattr -> R.drawable.ic_donate_flattr
        is Donate.Liberapay -> R.drawable.ic_donate_liberapay
        is Donate.OpenCollective -> R.drawable.ic_donate_opencollective
    },
    title = when (donate) {
        is Donate.Regular -> context.getString(R.string.website)
        is Donate.Bitcoin -> "Bitcoin"
        is Donate.Litecoin -> "Litecoin"
        is Donate.Flattr -> "Flattr"
        is Donate.Liberapay -> "Liberapay"
        is Donate.OpenCollective -> "Open Collective"
    },
    link = when (donate) {
        is Donate.Regular -> Uri.parse(donate.url)
        is Donate.Bitcoin -> Uri.parse("bitcoin:${donate.address}")
        is Donate.Litecoin -> Uri.parse("litecoin:${donate.address}")
        is Donate.Flattr -> Uri.parse("https://flattr.com/thing/${donate.id}")
        is Donate.Liberapay -> Uri.parse("https://liberapay.com/~${donate.id}")
        is Donate.OpenCollective -> Uri.parse("https://opencollective.com/${donate.id}")
    }
)

class PermissionsType(
    val group: PermissionGroupInfo?,
    val permissions: List<PermissionInfo>,
)
