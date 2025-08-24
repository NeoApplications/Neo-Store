package com.machiav3lli.fdroid.data.database.entity

import androidx.room.ColumnInfo
import com.machiav3lli.fdroid.data.entity.Author
import com.machiav3lli.fdroid.data.entity.Donate
import com.machiav3lli.fdroid.utils.extension.android.Android
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


// TODO unify with Product
@Serializable
open class IndexProduct(
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
    var screenshots: List<String> = emptyList()
    var suggestedVersionCode: Long = 0L
    var author: Author = Author()
    var source: String = ""
    var web: String = ""

    @ColumnInfo(defaultValue = "")
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
        releases: List<Release>,
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
        this.releases = releases
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

    fun toV2(): Product = Product(
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
        whatsNew = whatsNew,
    )

    fun refreshReleases(
        features: Set<String>,
        unstable: Boolean,
    ) {
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
            }.toMutableList()

        val predicate: (Release) -> Boolean = {
            unstable || (!it.releaseChannels.contains("Beta") && suggestedVersionCode <= 0) ||
                    it.versionCode <= suggestedVersionCode
        }
        val firstCompatibleReleaseIndex =
            releasePairs.indexOfFirst { it.second.isEmpty() && predicate(it.first) }
        val firstReleaseIndex =
            if (firstCompatibleReleaseIndex >= 0) firstCompatibleReleaseIndex else
                releasePairs.indexOfFirst { predicate(it.first) }
        val firstSelected = if (firstReleaseIndex >= 0) releasePairs[firstReleaseIndex] else null

        releases = releasePairs.map { (release, incompatibilities) ->
            release.copy(
                incompatibilities = incompatibilities,
                selected = firstSelected
                    ?.let { it.first.versionCode == release.versionCode && it.second == incompatibilities } == true,
            )
        }
    }

    fun toJSON() = Json.encodeToString(this)

    companion object {
        fun fromJson(json: String) = Json.decodeFromString<IndexProduct>(json)
    }
}
