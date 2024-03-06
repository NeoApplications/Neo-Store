package com.machiav3lli.fdroid.database.entity

import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.entity.AntiFeature
import com.machiav3lli.fdroid.entity.Author
import com.machiav3lli.fdroid.entity.Donate
import com.machiav3lli.fdroid.entity.ProductItem
import com.machiav3lli.fdroid.entity.Screenshot
import com.machiav3lli.fdroid.utility.extension.android.Android
import com.machiav3lli.fdroid.utility.extension.text.nullIfEmpty
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.serializers.RealmListKSerializer
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
open class Product(
) : RealmObject {
    var repositoryId: Long = -1
    var packageName: String = ""

    @PrimaryKey
    private var primaryKey: String = ""

    @Serializable(RealmListKSerializer::class)
    var releases: RealmList<Release> = realmListOf()

    var label: String = ""
    var summary: String = ""
    var description: String = ""
    var added: Long = 0L
    var updated: Long = 0L
    var icon: String = ""
    var metadataIcon: String = ""
    @Serializable(RealmListKSerializer::class)
    var categories: RealmList<Category> = realmListOf()
    @Serializable(RealmListKSerializer::class)
    var antiFeatures: RealmList<String> = realmListOf()
    @Serializable(RealmListKSerializer::class)
    var licenses: RealmList<String> = realmListOf()
    @Serializable(RealmListKSerializer::class)
    var donates: RealmList<Donate> = realmListOf()
    @Serializable(RealmListKSerializer::class)
    var screenshots: RealmList<Screenshot> = realmListOf()
    var suggestedVersionCode: Long = 0L
    var author: Author = Author()
    var source: String = ""
    var web: String = ""
    var tracker: String = ""
    var changelog: String = ""
    var whatsNew: String = ""

    init {
        primaryKey = "$repositoryId/$packageName"
    }

    constructor(
        repositoryId: Long,
        packageName: String,
        label: String,
        summary: String,
        description: String,
        added: Long,
        updated: Long,
        icon: String,
        metadataIcon: String,
        releases: List<Release>,
        categories: List<String>,
        antiFeatures: List<String>,
        licenses: List<String>,
        donates: List<Donate>,
        screenshots: List<Screenshot>,
        suggestedVersionCode: Long = 0L,
        author: Author = Author(),
        source: String = "",
        web: String = "",
        tracker: String = "",
        changelog: String = "",
        whatsNew: String = "",
    ) : this() {
        this.repositoryId = repositoryId
        this.packageName = packageName
        this.label = label
        this.summary = summary
        this.description = description
        this.added = added
        this.updated = updated
        this.icon = icon
        this.metadataIcon = metadataIcon
        this.releases = releases.toRealmList()
        this.categories = categories.map(::Category).toRealmList()
        this.antiFeatures = antiFeatures.toRealmList()
        this.licenses = licenses.toRealmList()
        this.donates = donates.toRealmList()
        this.screenshots = screenshots.toRealmList()
        this.suggestedVersionCode = suggestedVersionCode
        this.author = author
        this.source = source
        this.web = web
        this.tracker = tracker
        this.changelog = changelog
        this.whatsNew = whatsNew
        primaryKey = "$repositoryId/$packageName"
    }

    val selectedReleases: List<Release>
        get() = releases.filter { it.selected }

    val displayRelease: Release?
        get() = selectedReleases.firstOrNull() ?: releases.firstOrNull()

    val version: String
        get() = displayRelease?.version.orEmpty()

    val versionCode: Long
        get() = selectedReleases.firstOrNull()?.versionCode ?: 0L

    val signatures: List<String>
        get() = selectedReleases.mapNotNull { it.signature.nullIfEmpty() }.distinct()

    val compatible: Boolean
        get() = selectedReleases.firstOrNull()?.incompatibilities?.isEmpty() == true
    val otherAntiFeatures: List<String>
        get() = antiFeatures
            .filterNot {
                it in listOf(
                    AntiFeature.NO_SOURCE_SINCE,
                    AntiFeature.NON_FREE_DEP,
                    AntiFeature.NON_FREE_ASSETS,
                    AntiFeature.NON_FREE_UPSTREAM,
                    AntiFeature.NON_FREE_NET,
                    AntiFeature.TRACKING
                ).map(AntiFeature::key)
            }

    fun toItem(installed: Installed? = null): ProductItem =
        ProductItem(
            repositoryId = repositoryId,
            packageName = packageName,
            name = label,
            developer = author.name,
            summary = summary,
            icon = icon,
            metadataIcon = metadataIcon,
            version = version,
            installedVersion = installed?.version ?: "",
            compatible = compatible,
            canUpdate = canUpdate(installed),
            matchRank = 0
        )

    fun canUpdate(installed: Installed?): Boolean = installed != null &&
            compatible &&
            versionCode > installed.versionCode &&
            (installed.signature in signatures || Preferences[Preferences.Key.DisableSignatureCheck])

    fun refreshReleases(
        features: Set<String>,
        unstable: Boolean,
    ) {
        val releasePairs = releases.distinctBy { it.identifier }
            .sortedByDescending { it.versionCode }
            .map { release ->
                val incompatibilities = mutableListOf<Incompatibility>()
                if (release.minSdkVersion > 0 && Android.sdk < release.minSdkVersion) {
                    incompatibilities += Incompatibility.MinSdk
                }
                if (release.maxSdkVersion > 0 && Android.sdk > release.maxSdkVersion) {
                    incompatibilities += Incompatibility.MaxSdk
                }
                if (release.platforms.isNotEmpty() && release.platforms.intersect(Android.platforms)
                        .isEmpty()
                ) {
                    incompatibilities += Incompatibility.Platform
                }
                incompatibilities += (release.features - features).sorted()
                    .map { Incompatibility.Feature(it) }
                Pair(release, incompatibilities as List<Incompatibility>)
            }.toMutableList()

        val predicate: (Release) -> Boolean = {
            unstable || suggestedVersionCode <= 0 ||
                    it.versionCode <= suggestedVersionCode
        }
        val firstCompatibleReleaseIndex =
            releasePairs.indexOfFirst { it.second.isEmpty() && predicate(it.first) }
        val firstReleaseIndex =
            if (firstCompatibleReleaseIndex >= 0) firstCompatibleReleaseIndex else
                releasePairs.indexOfFirst { predicate(it.first) }
        val firstSelected = if (firstReleaseIndex >= 0) releasePairs[firstReleaseIndex] else null

        releases = releasePairs.map { (release, incompatibilities) ->
            release
                .copy(incompatibilities = incompatibilities, selected = firstSelected
                    ?.let { it.first.versionCode == release.versionCode && it.second == incompatibilities } == true)
        }.toRealmList()
    }

    fun toJSON() = Json.encodeToString(this)

    companion object {
        fun fromJson(json: String) = Json.decodeFromString<Product>(json)
    }
}

class ProductTemp(
    repositoryId: Long,
    packageName: String,
    label: String,
    summary: String,
    description: String,
    added: Long,
    updated: Long,
    icon: String,
    metadataIcon: String,
    releases: List<Release>,
    categories: List<String>,
    antiFeatures: List<String>,
    licenses: List<String>,
    donates: List<Donate>,
    screenshots: List<Screenshot>,
    suggestedVersionCode: Long = 0L,
    author: Author = Author(),
    source: String = "",
    web: String = "",
    tracker: String = "",
    changelog: String = "",
    whatsNew: String = "",
) : Product(
    repositoryId = repositoryId,
    packageName = packageName,
    label = label,
    summary = summary,
    description = description,
    added = added,
    updated = updated,
    icon = icon,
    metadataIcon = metadataIcon,
    releases = releases,
    categories = categories,
    antiFeatures = antiFeatures,
    licenses = licenses,
    donates = donates,
    screenshots = screenshots,
    suggestedVersionCode = suggestedVersionCode,
    author = author,
    source = source,
    web = web,
    tracker = tracker,
    changelog = changelog,
    whatsNew = whatsNew
)

fun Product.asProductTemp(): ProductTemp = ProductTemp(
    repositoryId = repositoryId,
    packageName = packageName,
    label = label,
    summary = summary,
    description = description,
    added = added,
    updated = updated,
    icon = icon,
    metadataIcon = metadataIcon,
    releases = releases,
    categories = categories.map { it.label },
    antiFeatures = antiFeatures,
    licenses = licenses,
    donates = donates,
    screenshots = screenshots,
    suggestedVersionCode = suggestedVersionCode,
    author = author,
    source = source,
    web = web,
    tracker = tracker,
    changelog = changelog,
    whatsNew = whatsNew
)

data class Licenses(
    val licenses: List<String>,
)

data class IconDetails(
    var packageName: String,
    var icon: String = "",
    var metadataIcon: String = "",
)