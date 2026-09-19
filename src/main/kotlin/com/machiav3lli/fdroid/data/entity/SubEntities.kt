package com.machiav3lli.fdroid.data.entity

import Sword
import android.content.Context
import android.content.pm.PermissionGroupInfo
import android.content.pm.PermissionInfo
import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.net.toUri
import com.machiav3lli.fdroid.FILTER_CATEGORY_ALL
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.data.database.entity.Release
import com.machiav3lli.fdroid.ui.compose.icons.Icon
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.icon.IcDonateLiberapay
import com.machiav3lli.fdroid.ui.compose.icons.icon.IcDonateLitecoin
import com.machiav3lli.fdroid.ui.compose.icons.icon.IcDonateOpencollective
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.AddressBook
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ApplePodcastsLogo
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ArrowSquareOut
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Asterisk
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Barbell
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.BatteryCharging
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Bell
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.BookBookmark
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Books
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Brain
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Broadcast
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Browser
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Calendar
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Camera
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Cardholder
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ChartLine
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Chat
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Chats
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CheckCircle
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CheckSquare
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Checkerboard
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ChefHat
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CircleWavyWarning
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CirclesFour
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CirclesThreePlus
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Clock
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CloudArrowDown
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CloudSun
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Club
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Code
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Command
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Compass
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CrosshairSimple
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CurrencyBTC
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CurrencyDollarSimple
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.DiceThree
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Download
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Envelope
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Flask
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.FolderNotch
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.GameController
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.GearSix
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Ghost
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Globe
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.GlobeSimple
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Graph
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.HeartStraight
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.HeartStraightFill
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.House
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Image
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Images
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Key
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Keyboard
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Leaf
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Lightbulb
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.MapPin
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.MathOperations
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Megaphone
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Metronome
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Microphone
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Newspaper
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.NotePencil
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Nut
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.PaintBrush
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Password
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.PenNib
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Phone
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Pizza
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.PlayCircle
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.PuzzlePiece
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Robot
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.RssSimple
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Scales
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ScribbleLoop
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ShareNetwork
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ShieldCheck
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ShieldStar
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ShieldWarning
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ShoppingCart
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.SlidersHorizontal
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.SoccerBall
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Storefront
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Strategy
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Swatches
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.TelevisionSimple
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Textbox
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.TrainSimple
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Translate
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.TrashSimple
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.VideoConference
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Wallet
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.WifiHigh
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Wrench
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.X
import kotlinx.serialization.Serializable
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
    data class Liberapay(val id: String) : Donate()

    @Serializable
    data class OpenCollective(val id: String) : Donate()

    fun toJSON() = Json.encodeToString(this)

    companion object {
        fun fromJson(json: String) = Json.decodeFromString<Donate>(json)
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

fun String.toAntiFeature(): AntiFeature? = AntiFeature.entries.find { it.key == this }

@Immutable
sealed interface ComponentState {
    val icon: ImageVector
    val textId: Int
}

@Immutable
sealed class ActionState(
    @StringRes override val textId: Int,
    override val icon: ImageVector = Phosphor.Download,
) : ComponentState {

    data class Install(override val repoName: String = "") : Download(repoName, R.string.install)
    data class Update(override val repoName: String = "") : Download(repoName, R.string.update)
    open class Download(open val repoName: String = "", override val textId: Int) :
        ActionState(textId, Phosphor.Download)

    data object Uninstall : ActionState(R.string.uninstall, Phosphor.TrashSimple)
    data object Launch : ActionState(R.string.launch, Phosphor.ArrowSquareOut)
    data object Details : ActionState(R.string.details, Phosphor.SlidersHorizontal)
    data object Share : ActionState(R.string.share, Phosphor.ShareNetwork)
    data object CancelPending : ActionState(R.string.pending, Phosphor.X)
    data object CancelConnecting : ActionState(R.string.connecting, Phosphor.X)
    data object CancelDownloading : ActionState(R.string.downloading, Phosphor.X)
    data object NoAction : ActionState(R.string.no_action_possible, Phosphor.X)
    data object Bookmark : ActionState(R.string.favorite_add, Phosphor.HeartStraight)
    data object Bookmarked : ActionState(R.string.favorite_remove, Phosphor.HeartStraightFill)
}

open class LinkType(
    val icon: ImageVector,
    val title: String,
    val link: Uri? = null,
)

class DonateType(donate: Donate, context: Context) : LinkType(
    icon = when (donate) {
        is Donate.Regular        -> Phosphor.CurrencyDollarSimple
        is Donate.Bitcoin        -> Phosphor.CurrencyBTC
        is Donate.Litecoin       -> Icon.IcDonateLitecoin
        is Donate.Liberapay      -> Icon.IcDonateLiberapay
        is Donate.OpenCollective -> Icon.IcDonateOpencollective
    },
    title = when (donate) {
        is Donate.Regular        -> context.getString(R.string.website)
        is Donate.Bitcoin        -> "Bitcoin"
        is Donate.Litecoin       -> "Litecoin"
        is Donate.Liberapay      -> "Liberapay"
        is Donate.OpenCollective -> "Open Collective"
    },
    link = when (donate) {
        is Donate.Regular        -> donate.url.toUri()
        is Donate.Bitcoin        -> "bitcoin:${donate.address}".toUri()
        is Donate.Litecoin       -> "litecoin:${donate.address}".toUri()
        is Donate.Liberapay      -> "https://liberapay.com/${donate.id}".toUri()
        is Donate.OpenCollective -> "https://opencollective.com/${donate.id}".toUri()
    }
)

data class Request(
    val id: Int,
    val installed: Boolean,
    val updates: Boolean,
    val updateCategory: UpdateCategory,
    val section: Section,
    val order: Order,
    val ascending: Boolean,
    val category: String,
    val filteredOutRepos: Set<String>,
    val filteredAntiFeatures: Set<String>,
    val filteredLicenses: Set<String>,
    val filteredPackages: Set<String>,
    val numberOfItems: Int = 0,
    val minMinSDK: Int = 0,
    val maxMinSDK: Int = 0,
    val minTargetSDK: Int = 0,
    val maxTargetSDK: Int = 0,
) {
    companion object {
        val ALL: Request
            get() = Request(
                id = Source.AVAILABLE.ordinal,
                installed = false,
                updates = false,
                updateCategory = UpdateCategory.ALL,
                section = Section.All,
                order = Order.NAME,
                ascending = true,
                category = FILTER_CATEGORY_ALL,
                filteredOutRepos = emptySet(),
                filteredAntiFeatures = emptySet(),
                filteredLicenses = emptySet(),
                filteredPackages = emptySet(),
            )

        val EXPLORE: Request
            get() = Request(
                id = Source.AVAILABLE.ordinal,
                installed = false,
                updates = false,
                updateCategory = UpdateCategory.ALL,
                section = Section.All,
                order = Preferences[Preferences.Key.SortOrderExplore].order,
                ascending = Preferences[Preferences.Key.SortOrderAscendingExplore],
                category = Preferences[Preferences.Key.CategoriesFilterExplore],
                filteredOutRepos = Preferences[Preferences.Key.ReposFilterExplore],
                filteredAntiFeatures = Preferences[Preferences.Key.AntifeaturesFilterExplore],
                filteredLicenses = Preferences[Preferences.Key.LicensesFilterExplore],
                filteredPackages = Preferences[Preferences.Key.PackagesBlocklist],
                minMinSDK = Preferences[Preferences.Key.MinMinSDKExplore].ordinal,
                maxMinSDK = Preferences[Preferences.Key.MaxMinSDKExplore].ordinal,
                minTargetSDK = Preferences[Preferences.Key.MinTargetSDKExplore].ordinal,
                maxTargetSDK = Preferences[Preferences.Key.MaxTargetSDKExplore].ordinal,
            )

        val Favorites: Request
            get() = Request(
                id = Source.FAVORITES.ordinal,
                installed = false,
                updates = false,
                updateCategory = UpdateCategory.ALL,
                section = Section.FAVORITE,
                order = Preferences[Preferences.Key.SortOrderExplore].order,
                ascending = Preferences[Preferences.Key.SortOrderAscendingExplore],
                category = Preferences[Preferences.Key.CategoriesFilterExplore],
                filteredOutRepos = Preferences[Preferences.Key.ReposFilterExplore],
                filteredAntiFeatures = Preferences[Preferences.Key.AntifeaturesFilterExplore],
                filteredLicenses = Preferences[Preferences.Key.LicensesFilterExplore],
                filteredPackages = Preferences[Preferences.Key.PackagesBlocklist],
            )

        val Search: Request
            get() = Request(
                id = Source.SEARCH.ordinal,
                installed = false,
                updates = false,
                updateCategory = UpdateCategory.ALL,
                section = Section.All,
                order = Preferences[Preferences.Key.SortOrderSearch].order,
                ascending = Preferences[Preferences.Key.SortOrderAscendingSearch],
                category = Preferences[Preferences.Key.CategoriesFilterSearch],
                filteredOutRepos = Preferences[Preferences.Key.ReposFilterSearch],
                filteredAntiFeatures = Preferences[Preferences.Key.AntifeaturesFilterSearch],
                filteredLicenses = Preferences[Preferences.Key.LicensesFilterSearch],
                filteredPackages = Preferences[Preferences.Key.PackagesBlocklist],
                minMinSDK = Preferences[Preferences.Key.MinMinSDKSearch].ordinal,
                maxMinSDK = Preferences[Preferences.Key.MaxMinSDKSearch].ordinal,
                minTargetSDK = Preferences[Preferences.Key.MinTargetSDKSearch].ordinal,
                maxTargetSDK = Preferences[Preferences.Key.MaxTargetSDKSearch].ordinal,
            )

        val SearchFavorites: Request
            get() = Request(
                id = Source.SEARCH_FAVORITES.ordinal,
                installed = false,
                updates = false,
                updateCategory = UpdateCategory.ALL,
                section = Section.FAVORITE,
                order = Preferences[Preferences.Key.SortOrderSearch].order,
                ascending = Preferences[Preferences.Key.SortOrderAscendingSearch],
                category = Preferences[Preferences.Key.CategoriesFilterSearch],
                filteredOutRepos = Preferences[Preferences.Key.ReposFilterSearch],
                filteredAntiFeatures = Preferences[Preferences.Key.AntifeaturesFilterSearch],
                filteredLicenses = Preferences[Preferences.Key.LicensesFilterSearch],
                filteredPackages = Preferences[Preferences.Key.PackagesBlocklist],
                minMinSDK = Preferences[Preferences.Key.MinMinSDKSearch].ordinal,
                maxMinSDK = Preferences[Preferences.Key.MaxMinSDKSearch].ordinal,
                minTargetSDK = Preferences[Preferences.Key.MinTargetSDKSearch].ordinal,
                maxTargetSDK = Preferences[Preferences.Key.MaxTargetSDKSearch].ordinal,
            )

        val Installed: Request
            get() = Request(
                id = Source.INSTALLED.ordinal,
                installed = true,
                updates = false,
                updateCategory = UpdateCategory.ALL,
                section = Section.All,
                order = Preferences[Preferences.Key.SortOrderInstalled].order,
                ascending = Preferences[Preferences.Key.SortOrderAscendingInstalled],
                category = Preferences[Preferences.Key.CategoriesFilterInstalled],
                filteredOutRepos = Preferences[Preferences.Key.ReposFilterInstalled],
                filteredAntiFeatures = Preferences[Preferences.Key.AntifeaturesFilterInstalled],
                filteredLicenses = Preferences[Preferences.Key.LicensesFilterInstalled],
                filteredPackages = Preferences[Preferences.Key.PackagesBlocklist],
                minMinSDK = Preferences[Preferences.Key.MinMinSDKInstalled].ordinal,
                maxMinSDK = Preferences[Preferences.Key.MaxMinSDKInstalled].ordinal,
                minTargetSDK = Preferences[Preferences.Key.MinTargetSDKInstalled].ordinal,
                maxTargetSDK = Preferences[Preferences.Key.MaxTargetSDKInstalled].ordinal,
            )

        val SearchInstalled: Request
            get() = Request(
                id = Source.SEARCH_INSTALLED.ordinal,
                installed = true,
                updates = false,
                updateCategory = UpdateCategory.ALL,
                section = Section.All,
                order = Preferences[Preferences.Key.SortOrderSearch].order,
                ascending = Preferences[Preferences.Key.SortOrderAscendingSearch],
                category = Preferences[Preferences.Key.CategoriesFilterSearch],
                filteredOutRepos = Preferences[Preferences.Key.ReposFilterSearch],
                filteredAntiFeatures = Preferences[Preferences.Key.AntifeaturesFilterSearch],
                filteredLicenses = Preferences[Preferences.Key.LicensesFilterSearch],
                filteredPackages = Preferences[Preferences.Key.PackagesBlocklist],
                minMinSDK = Preferences[Preferences.Key.MinMinSDKSearch].ordinal,
                maxMinSDK = Preferences[Preferences.Key.MaxMinSDKSearch].ordinal,
                minTargetSDK = Preferences[Preferences.Key.MinTargetSDKSearch].ordinal,
                maxTargetSDK = Preferences[Preferences.Key.MaxTargetSDKSearch].ordinal,
            )

        val Updates: Request
            get() = Request(
                id = Source.UPDATES.ordinal,
                installed = true,
                updates = true,
                updateCategory = UpdateCategory.ALL,
                section = Section.All,
                order = Order.NAME,
                ascending = true,
                category = FILTER_CATEGORY_ALL,
                filteredOutRepos = emptySet(),
                filteredAntiFeatures = emptySet(),
                filteredLicenses = emptySet(),
                filteredPackages = Preferences[Preferences.Key.PackagesBlocklist],
            )

        val Updated: Request
            get() = Request(
                id = Source.UPDATED.ordinal,
                installed = false,
                updates = false,
                updateCategory = if (Preferences[Preferences.Key.HideNewApps]) UpdateCategory.ALL
                else UpdateCategory.UPDATED,
                section = Section.All,
                order = Preferences[Preferences.Key.SortOrderLatest].order,
                ascending = Preferences[Preferences.Key.SortOrderAscendingLatest],
                category = Preferences[Preferences.Key.CategoriesFilterLatest],
                filteredOutRepos = Preferences[Preferences.Key.ReposFilterLatest],
                filteredAntiFeatures = Preferences[Preferences.Key.AntifeaturesFilterLatest],
                filteredLicenses = Preferences[Preferences.Key.LicensesFilterLatest],
                filteredPackages = Preferences[Preferences.Key.PackagesBlocklist],
                numberOfItems = Preferences[Preferences.Key.UpdatedApps],
                minMinSDK = Preferences[Preferences.Key.MinMinSDKLatest].ordinal,
                maxMinSDK = Preferences[Preferences.Key.MaxMinSDKLatest].ordinal,
                minTargetSDK = Preferences[Preferences.Key.MinTargetSDKLatest].ordinal,
                maxTargetSDK = Preferences[Preferences.Key.MaxTargetSDKLatest].ordinal,
            )

        val New: Request
            get() = Request(
                id = Source.NEW.ordinal,
                installed = false,
                updates = false,
                updateCategory = UpdateCategory.NEW,
                section = Section.All,
                order = Order.DATE_ADDED,
                ascending = false,
                category = FILTER_CATEGORY_ALL,
                filteredOutRepos = emptySet(),
                filteredAntiFeatures = emptySet(),
                filteredLicenses = emptySet(),
                filteredPackages = Preferences[Preferences.Key.PackagesBlocklist],
                numberOfItems = Preferences[Preferences.Key.NewApps],
                minMinSDK = Preferences[Preferences.Key.MinMinSDKLatest].ordinal,
                maxMinSDK = Preferences[Preferences.Key.MaxMinSDKLatest].ordinal,
                minTargetSDK = Preferences[Preferences.Key.MinTargetSDKLatest].ordinal,
                maxTargetSDK = Preferences[Preferences.Key.MaxTargetSDKLatest].ordinal,
            )

        val SearchNew: Request
            get() = Request(
                id = Source.SEARCH.ordinal,
                installed = false,
                updates = false,
                updateCategory = UpdateCategory.NEW,
                section = Section.All,
                order = Order.DATE_ADDED,
                ascending = false,
                category = Preferences[Preferences.Key.CategoriesFilterSearch],
                filteredOutRepos = Preferences[Preferences.Key.ReposFilterSearch],
                filteredAntiFeatures = Preferences[Preferences.Key.AntifeaturesFilterSearch],
                filteredPackages = Preferences[Preferences.Key.PackagesBlocklist],
                filteredLicenses = Preferences[Preferences.Key.LicensesFilterSearch],
                minMinSDK = Preferences[Preferences.Key.MinMinSDKSearch].ordinal,
                maxMinSDK = Preferences[Preferences.Key.MaxMinSDKSearch].ordinal,
                minTargetSDK = Preferences[Preferences.Key.MinTargetSDKSearch].ordinal,
                maxTargetSDK = Preferences[Preferences.Key.MaxTargetSDKSearch].ordinal,
            )

        val None: Request
            get() = Request(
                id = Source.NONE.ordinal,
                installed = false,
                updates = false,
                updateCategory = UpdateCategory.ALL,
                section = Section.NONE,
                order = Order.DATE_ADDED,
                ascending = false,
                category = FILTER_CATEGORY_ALL,
                filteredOutRepos = emptySet(),
                filteredAntiFeatures = emptySet(),
                filteredLicenses = emptySet(),
                filteredPackages = Preferences[Preferences.Key.PackagesBlocklist],
                numberOfItems = 0,
            )
    }
}

sealed class DialogKey {
    data object None : DialogKey()
    data object PermissionBatteryOptimization : DialogKey()
    data class Link(val uri: Uri) : DialogKey()
    open class Action(
        val label: String,
        val action: () -> Unit
    ) : DialogKey()

    class Download(
        label: String,
        action: () -> Unit
    ) : Action(label, action)

    class Uninstall(
        label: String,
        action: () -> Unit
    ) : Action(label, action)

    class BatchDownload(
        val labels: List<String>,
        val action: () -> Unit
    ) : DialogKey()

    data class ReleaseIncompatible(
        val incompatibilities: List<Release.Incompatibility>,
        val platforms: List<String>,
        val minSdkVersion: Int,
        val maxSdkVersion: Int,
    ) : DialogKey()

    data class ReleaseIssue(val resId: Int) : DialogKey()
    data class Launch(
        val packageName: String,
        val launcherActivities: List<Pair<String, String>>,
    ) : DialogKey()
}

data class Permission(
    val nameId: Int,
    val icon: ImageVector,
    val descriptionId: Int,
    val warningTextId: Int = -1,
    val ignorePref: Preferences.Key<Boolean>? = null,
) {
    companion object {
        val BatteryOptimization = Permission(
            R.string.ignore_battery_optimization_title,
            Phosphor.Leaf,
            R.string.ignore_battery_optimization_message,
            R.string.warning_disable_battery_optimization,
            Preferences.Key.IgnoreDisableBatteryOptimization,
        )
        val PostNotifications = Permission(
            R.string.post_notifications_permission_title,
            Phosphor.CircleWavyWarning,
            R.string.post_notifications_permission_message,
            R.string.warning_show_notification,
            Preferences.Key.IgnoreShowNotifications,
        )
        val InstallPackages = Permission(
            R.string.install_packages_permission_title,
            Phosphor.Download,
            R.string.install_packages_permission_message,
            -1,
        )
    }
}

class PermissionsType(
    val group: PermissionGroupInfo?,
    val permissions: List<PermissionInfo>,
)

val String.appCategoryIcon: ImageVector
    get() = when (this.lowercase()) {
        FILTER_CATEGORY_ALL.lowercase() -> Phosphor.CirclesFour
        "action game"                   -> Phosphor.Sword
        "alarm clock"                   -> Phosphor.Clock
        "ambient sound"                 -> Phosphor.Broadcast
        "app store & updater"           -> Phosphor.Storefront
        "app manager"                   -> Phosphor.GearSix
        "audio"                         -> Phosphor.Microphone
        "audiovideo"                    -> Phosphor.PlayCircle
        "audiobook"                     -> Phosphor.BookBookmark
        "automation"                    -> Phosphor.Robot
        "ai chat"                       -> Phosphor.Robot
        "battery"                       -> Phosphor.BatteryCharging
        "board game"                    -> Phosphor.Checkerboard
        "bookmark"                      -> Phosphor.BookBookmark
        "browser"                       -> Phosphor.Browser
        "camera"                        -> Phosphor.Camera
        "card game"                     -> Phosphor.Club
        "cast"                          -> Phosphor.Broadcast
        "casual game"                   -> Phosphor.GameController
        "clock"                         -> Phosphor.Clock
        "code & forge"                  -> Phosphor.Code
        "connectivity"                  -> Phosphor.WifiHigh
        "contact"                       -> Phosphor.AddressBook
        "communication"                 -> Phosphor.Chat
        "calculator"                    -> Phosphor.MathOperations
        "calendar"                      -> Phosphor.Calendar
        "calendar & agenda"             -> Phosphor.Calendar
        "cloud storage & file sync"     -> Phosphor.CloudArrowDown // fix
        "default"                       -> Phosphor.Asterisk
        "development"                   -> Phosphor.Code
        "dice"                          -> Phosphor.DiceThree
        "diet"                          -> Phosphor.Leaf
        "dns & hosts"                   -> Phosphor.ShieldCheck
        "draw"                          -> Phosphor.ScribbleLoop
        "download"                      -> Phosphor.Download
        "education"                     -> Phosphor.Brain
        "educational game"              -> Phosphor.Brain
        "ebook reader"                  -> Phosphor.BookBookmark
        "email"                         -> Phosphor.Envelope
        "emergency action"              -> Phosphor.ShieldWarning
        "emulator"                      -> Phosphor.GameController
        "fdroid"                        -> Phosphor.Asterisk
        "fedilab"                       -> Phosphor.Graph
        "feed"                          -> Phosphor.RssSimple
        "file encryption & vault"       -> Phosphor.Key
        "file manager"                  -> Phosphor.FolderNotch
        "file transfer"                 -> Phosphor.ShareNetwork
        "finance manager"               -> Phosphor.CurrencyDollarSimple
        "firewall"                      -> Phosphor.ShieldCheck
        "flashlight"                    -> Phosphor.Lightbulb
        "food"                          -> Phosphor.Pizza
        "forum"                         -> Phosphor.Chats
        "game"                          -> Phosphor.GameController
        "game helper"                   -> Phosphor.Wrench
        "gallery"                       -> Phosphor.Images
        "games"                         -> Phosphor.GameController
        "graphics"                      -> Phosphor.PaintBrush
        "guardian project"              -> Phosphor.ShieldStar
        "habit tracker"                 -> Phosphor.CheckCircle
        "health manager"                -> Phosphor.HeartStraight
        "icon pack"                     -> Phosphor.CirclesThreePlus
        "internet"                      -> Phosphor.Globe
        "inventory"                     -> Phosphor.Storefront
        "kde"                           -> Phosphor.Code
        "keyboard & ime"                -> Phosphor.Keyboard
        "kidsgame"                      -> Phosphor.GameController
        "launcher"                      -> Phosphor.House
        "local media player"            -> Phosphor.PlayCircle
        "location tracker & sharer"     -> Phosphor.MapPin
        "lyrics"                        -> Phosphor.NotePencil
        "market & price"                -> Phosphor.CurrencyDollarSimple
        "math"                          -> Phosphor.MathOperations
        "medication"                    -> Phosphor.Flask
        "meditation"                    -> Phosphor.Leaf
        "mental health"                 -> Phosphor.Brain
        "messaging"                     -> Phosphor.Chat
        "money"                         -> Phosphor.CurrencyDollarSimple
        "multimedia"                    -> Phosphor.PlayCircle
        "music practice tool"           -> Phosphor.Metronome
        "navigation"                    -> Phosphor.Compass
        "network"                       -> Phosphor.GlobeSimple
        "network analyzer"              -> Phosphor.ChartLine
        "news"                          -> Phosphor.Newspaper
        "note"                          -> Phosphor.NotePencil
        "notification"                  -> Phosphor.Bell
        "ocr"                           -> Phosphor.Textbox
        "office"                        -> Phosphor.Books
        "offline"                       -> Phosphor.Leaf
        "online media player"           -> Phosphor.PlayCircle
        "osmand"                        -> Phosphor.Compass
        "party game"                    -> Phosphor.GameController
        "pass wallet"                   -> Phosphor.Cardholder
        "password & 2fa"                -> Phosphor.Password
        "platformer game"               -> Phosphor.GameController
        "phone & sms"                   -> Phosphor.Phone
        "podcast"                       -> Phosphor.ApplePodcastsLogo
        "productivity"                  -> Phosphor.Compass
        "public transport"              -> Phosphor.TrainSimple
        "push"                          -> Phosphor.Bell
        "puzzle game"                   -> Phosphor.PuzzlePiece
        "qt"                            -> Phosphor.Books
        "radio"                         -> Phosphor.Broadcast
        "reading"                       -> Phosphor.BookBookmark
        "recipe manager"                -> Phosphor.ChefHat
        "recorder"                      -> Phosphor.Microphone
        "religion"                      -> Phosphor.Command
        "remote access"                 -> Phosphor.Globe
        "remote controller"             -> Phosphor.Broadcast
        "role-playing game"             -> Phosphor.Sword
        "schedule"                      -> Phosphor.Calendar
        "science"                       -> Phosphor.Brain
        "science & education"           -> Phosphor.Brain
        "security"                      -> Phosphor.ShieldStar
        "shooter game"                  -> Phosphor.CrosshairSimple
        "shopping list"                 -> Phosphor.ShoppingCart
        "social network"                -> Phosphor.Graph
        "speech recognizer"             -> Phosphor.Microphone
        "sport game"                    -> Phosphor.SoccerBall
        "sports & health"               -> Phosphor.Barbell
        "stopwatch"                     -> Phosphor.Clock
        "strategy game"                 -> Phosphor.Strategy
        "system"                        -> Phosphor.Nut
        "task"                          -> Phosphor.CheckSquare
        "text editor"                   -> Phosphor.NotePencil
        "text encryption"               -> Phosphor.Textbox
        "text to speech"                -> Phosphor.Megaphone
        "theming"                       -> Phosphor.Swatches
        "time"                          -> Phosphor.Clock
        "time tracker"                  -> Phosphor.Clock
        "timer"                         -> Phosphor.Clock
        "translation & dictionary"      -> Phosphor.Translate
        "tv"                            -> Phosphor.TelevisionSimple
        "unit convertor"                -> Phosphor.Scales
        "utility"                       -> Phosphor.Wrench
        "video"                         -> Phosphor.PlayCircle
        "visual novel"                  -> Phosphor.PlayCircle
        "voice & video chat"            -> Phosphor.VideoConference
        "volume"                        -> Phosphor.Microphone
        "vpn & proxy"                   -> Phosphor.Ghost
        "wallet"                        -> Phosphor.Wallet
        "wallpaper"                     -> Phosphor.Image
        "weather"                       -> Phosphor.CloudSun
        "word game"                     -> Phosphor.Checkerboard
        "workout"                       -> Phosphor.Barbell
        "writing"                       -> Phosphor.PenNib
        "xposed"                        -> Phosphor.ShieldStar
        else                            -> Phosphor.Asterisk
    }
/*
- Default (default apps from ROMs)
 */