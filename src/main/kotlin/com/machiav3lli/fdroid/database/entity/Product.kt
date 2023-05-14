package com.machiav3lli.fdroid.database.entity

import androidx.room.Entity
import com.machiav3lli.fdroid.ROW_PACKAGE_NAME
import com.machiav3lli.fdroid.ROW_REPOSITORY_ID
import com.machiav3lli.fdroid.TABLE_PRODUCT
import com.machiav3lli.fdroid.TABLE_PRODUCT_TEMP
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.entity.AntiFeature
import com.machiav3lli.fdroid.entity.Author
import com.machiav3lli.fdroid.entity.Donate
import com.machiav3lli.fdroid.entity.ProductItem
import com.machiav3lli.fdroid.entity.Screenshot
import com.machiav3lli.fdroid.utility.extension.text.nullIfEmpty
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// TODO Add Product Extras to handle favorite lists etc..
@Entity(tableName = TABLE_PRODUCT, primaryKeys = [ROW_REPOSITORY_ID, ROW_PACKAGE_NAME])
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
    var releases: List<Release> = emptyList()
    var categories: List<String> = emptyList()
    var antiFeatures: List<String> = emptyList()
    var licenses: List<String> = emptyList()
    var donates: List<Donate> = emptyList()
    var screenshots: List<Screenshot> = emptyList()
    var versionCode: Long = 0L
    var suggestedVersionCode: Long = 0L
    var signatures: List<String> = emptyList()
    var compatible: Boolean = false
    var author: Author = Author()
    var source: String = ""
    var web: String = ""
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
    ) : this(repositoryId, packageName) {
        this.label = label
        this.summary = summary
        this.description = description
        this.added = added
        this.updated = updated
        this.icon = icon
        this.metadataIcon = metadataIcon
        this.releases = releases
        this.categories = categories
        this.antiFeatures = antiFeatures
        this.licenses = licenses
        this.donates = donates
        this.screenshots = screenshots
        this.versionCode = selectedReleases.firstOrNull()?.versionCode ?: 0L
        this.suggestedVersionCode = suggestedVersionCode
        this.signatures = selectedReleases.mapNotNull { it.signature.nullIfEmpty() }.distinct()
        this.compatible = selectedReleases.firstOrNull()?.incompatibilities?.isEmpty() == true
        this.author = author
        this.source = source
        this.web = web
        this.tracker = tracker
        this.changelog = changelog
        this.whatsNew = whatsNew
    }

    val selectedReleases: List<Release>
        get() = releases.filter { it.selected }

    val displayRelease: Release?
        get() = selectedReleases.firstOrNull() ?: releases.firstOrNull()

    val version: String
        get() = displayRelease?.version.orEmpty()

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

    fun refreshVariables() {
        this.versionCode = selectedReleases.firstOrNull()?.versionCode ?: 0L
        this.signatures = selectedReleases.mapNotNull { it.signature.nullIfEmpty() }.distinct()
        this.compatible = selectedReleases.firstOrNull()?.incompatibilities?.isEmpty() == true
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

data class Licenses(
    val licenses: List<String>,
)

data class IconDetails(
    var packageName: String,
    var icon: String = "",
    var metadataIcon: String = "",
)