package com.machiav3lli.fdroid.data.database.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Relation
import com.machiav3lli.fdroid.ROW_ADDED
import com.machiav3lli.fdroid.ROW_AUTHOR
import com.machiav3lli.fdroid.ROW_ICON
import com.machiav3lli.fdroid.ROW_LABEL
import com.machiav3lli.fdroid.ROW_LICENSES
import com.machiav3lli.fdroid.ROW_METADATA_ICON
import com.machiav3lli.fdroid.ROW_PACKAGE_NAME
import com.machiav3lli.fdroid.ROW_REPOSITORY_ID
import com.machiav3lli.fdroid.ROW_UPDATED
import com.machiav3lli.fdroid.TABLE_PRODUCT
import com.machiav3lli.fdroid.TABLE_PRODUCT_TEMP
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.data.entity.Author
import com.machiav3lli.fdroid.data.entity.Donate
import com.machiav3lli.fdroid.data.entity.ProductItem
import com.machiav3lli.fdroid.utils.extension.android.Android
import com.machiav3lli.fdroid.utils.extension.text.nullIfEmpty
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Entity(
    tableName = TABLE_PRODUCT,
    primaryKeys = [ROW_REPOSITORY_ID, ROW_PACKAGE_NAME],
    indices = [
        Index(value = [ROW_PACKAGE_NAME]),
        Index(value = [ROW_REPOSITORY_ID, ROW_PACKAGE_NAME], unique = true),
        Index(value = [ROW_LABEL]),
        Index(value = [ROW_ADDED]),
        Index(value = [ROW_UPDATED]),
        Index(value = [ROW_AUTHOR]),
        Index(value = [ROW_PACKAGE_NAME, ROW_ICON, ROW_METADATA_ICON]),
        Index(value = [ROW_LICENSES]),
    ]
)
@Serializable
open class Product(
    var repositoryId: Long,
    var packageName: String,
) {
    var label: String = ""
    var summary: String = ""
    var description: String = ""
    var added: Long = 0L
    var updated: Long = 0L
    var icon: String = ""
    var metadataIcon: String = ""
    var categories: List<String> = emptyList()
    var antiFeatures: List<String> = emptyList()
    var licenses: List<String> = emptyList()
    var donates: List<Donate> = emptyList()
    var screenshots: List<String> = emptyList()
    var suggestedVersionCode: Long = 0L
    var author: Author = Author()
    var source: String = ""
    var web: String = ""
    var video: String = ""
    var tracker: String = ""
    var changelog: String = ""
    var whatsNew: String = ""

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
        categories: List<String>,
        antiFeatures: List<String>,
        licenses: List<String>,
        donates: List<Donate>,
        screenshots: List<String>,
        suggestedVersionCode: Long = 0L,
        author: Author = Author(),
        source: String = "",
        web: String = "",
        video: String = "",
        tracker: String = "",
        changelog: String = "",
        whatsNew: String = "",
    ) : this(repositoryId, packageName) {
        this.label = label
        this.summary = summary
        this.description = description
        this.added = added
        this.updated = updated
        this.icon = icon
        this.metadataIcon = metadataIcon
        this.categories = categories
        this.antiFeatures = antiFeatures
        this.licenses = licenses
        this.donates = donates
        this.screenshots = screenshots
        this.suggestedVersionCode = suggestedVersionCode
        this.author = author
        this.source = source
        this.web = web
        this.video = video
        this.tracker = tracker
        this.changelog = changelog
        this.whatsNew = whatsNew
    }

    fun toJSON() = Json.encodeToString(this)

    companion object {
        fun fromJson(json: String) = Json.decodeFromString<Product>(json)
    }
}


@Entity(tableName = TABLE_PRODUCT_TEMP)
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
    categories: List<String>,
    antiFeatures: List<String>,
    licenses: List<String>,
    donates: List<Donate>,
    screenshots: List<String>,
    suggestedVersionCode: Long = 0L,
    author: Author = Author(),
    source: String = "",
    web: String = "",
    video: String = "",
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
    categories = categories,
    antiFeatures = antiFeatures,
    licenses = licenses,
    donates = donates,
    screenshots = screenshots,
    suggestedVersionCode = suggestedVersionCode,
    author = author,
    source = source,
    web = web,
    video = video,
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
    categories = categories,
    antiFeatures = antiFeatures,
    licenses = licenses,
    donates = donates,
    screenshots = screenshots,
    suggestedVersionCode = suggestedVersionCode,
    author = author,
    source = source,
    web = web,
    video = video,
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

data class EmbeddedProduct(
    @Embedded val product: Product,
    @Relation(
        parentColumn = ROW_PACKAGE_NAME,
        entityColumn = ROW_PACKAGE_NAME,
    )
    val releases: List<Release> = emptyList(),
) {
    val selectedReleases: List<Release>
        get() = releases
            .filter { it.selected }
            .distinctBy(Release::identifier)
            .sortedByDescending { it.versionCode }

    val displayRelease: Release?
        get() = selectedReleases.firstOrNull() ?: releases.firstOrNull()

    val version: String
        get() = displayRelease?.version.orEmpty()

    val versionCode: Long
        get() = selectedReleases.firstOrNull()?.versionCode ?: 0L

    val productSignatures: List<String>
        get() = selectedReleases
            .filter { it.repositoryId == product.repositoryId }
            .mapNotNull { it.signature.nullIfEmpty() }
            .distinct()

    val compatible: Boolean
        get() = selectedReleases.firstOrNull()?.incompatibilities?.isEmpty() == true

    fun canUpdate(installed: Installed?): Boolean = installed != null &&
            compatible &&
            (selectedReleases.filter { it.signature in installed.signatures }
                .any { it.versionCode > installed.versionCode } ||
                    (versionCode > installed.versionCode && Preferences[Preferences.Key.DisableSignatureCheck]))

    fun refreshReleases(
        features: Set<String>,
        unstable: Boolean,
    ): List<Release> {
        val releasePairs = releases.distinctBy { it.identifier }
            .sortedByDescending { it.versionCode }
            .map { release ->
                val incompatibilities = mutableListOf<Release.Incompatibility>()
                if (release.minSdkVersion > 0 && Android.sdk < release.minSdkVersion) {
                    incompatibilities += Release.Incompatibility.MinSdk
                }
                if (release.maxSdkVersion > 0 && Android.sdk > release.maxSdkVersion) {
                    incompatibilities += Release.Incompatibility.MaxSdk
                }
                if (release.platforms.isNotEmpty() && release.platforms.intersect(Android.platforms)
                        .isEmpty()
                ) {
                    incompatibilities += Release.Incompatibility.Platform
                }
                incompatibilities += (release.features - features).sorted()
                    .map { Release.Incompatibility.Feature(it) }
                Pair(release, incompatibilities as List<Release.Incompatibility>)
            }.toImmutableList()

        val predicate: (Release) -> Boolean = {
            unstable || (!it.releaseChannels.contains("Beta") && product.suggestedVersionCode <= 0) ||
                    it.versionCode <= product.suggestedVersionCode
        }
        val firstCompatibleReleaseIndex =
            releasePairs.indexOfFirst { it.second.isEmpty() && predicate(it.first) }
        val firstReleaseIndex =
            if (firstCompatibleReleaseIndex >= 0) firstCompatibleReleaseIndex else
                releasePairs.indexOfFirst { predicate(it.first) }
        val firstSelected = if (firstReleaseIndex >= 0) releasePairs[firstReleaseIndex] else null

        // TODO update releases
        return releasePairs.map { (release, incompatibilities) ->
            release
                .copy(
                    incompatibilities = incompatibilities, selected = firstSelected
                        ?.let { it.first.versionCode == release.versionCode && it.second == incompatibilities } == true)
        }
    }

    fun toItem(installed: Installed? = null): ProductItem =
        ProductItem(
            repositoryId = product.repositoryId,
            packageName = product.packageName,
            name = product.label,
            developer = product.author.name,
            summary = product.summary,
            icon = product.icon,
            metadataIcon = product.metadataIcon,
            version = version,
            installedVersion = installed?.version ?: "",
            compatible = compatible,
            canUpdate = canUpdate(installed),
            launchable = !installed?.launcherActivities.isNullOrEmpty(),
            matchRank = 0
        )
}
