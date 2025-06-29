package com.machiav3lli.fdroid.data.index.v2

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.InputStream

@Serializable
data class IndexV2(
    val repo: Repo,
    val packages: IdMap<Package>
) {
    @Serializable
    data class Entry(
        val timestamp: Long,
        val version: Long,
        val index: File,
        val diffs: Map<Long, File>
    ) {
        fun getDiff(timestamp: Long): File? {
            return if (this.timestamp == timestamp) null
            else diffs[timestamp]
        }

        fun toJSON() = Json.encodeToString(this)

        companion object {
            private val jsonConfig = Json { ignoreUnknownKeys = true }

            @OptIn(ExperimentalSerializationApi::class)
            fun fromJsonStream(inputStream: InputStream) =
                jsonConfig.decodeFromStream<Entry>(inputStream)
        }
    }

    @Serializable
    data class Repo(
        val name: Localized<String> = emptyMap(),
        val description: Localized<String> = emptyMap(),
        val icon: Localized<File>? = null,
        val address: String,
        val mirrors: List<Mirror> = emptyList(),
        val webBaseUrl: String? = null,
        val timestamp: Long,
        val antiFeatures: IdMap<AntiFeature> = emptyMap(),
        val categories: IdMap<Category> = emptyMap(),
        // Optional
        val releaseChannels: IdMap<ReleaseChannel>? = null,
    )

    @Serializable
    data class Package(
        val metadata: Metadata,
        val versions: IdMap<Version>
    )

    @Serializable
    data class Metadata(
        val added: Long,
        val name: Localized<String>? = null,
        val summary: Localized<String>? = null,
        val description: Localized<String>? = null,
        val icon: Localized<File>? = null,
        val categories: List<String>,
        val changelog: String? = null,
        val issueTracker: String? = null,
        val lastUpdated: Long = 0,
        val license: String = "",
        val sourceCode: String? = null,
        val webSite: String? = null,
        val translation: String? = null,
        val screenshots: Screenshots? = null,
        val featureGraphic: Localized<File>? = null,
        val promoGraphic: Localized<File>? = null,
        val video: Localized<String>? = null,
        val authorName: String? = null,
        val authorEmail: String? = null,
        val authorWebsite: String? = null,
        val donate: List<String> = emptyList(),
        val bitcoin: String? = null,
        val liberapay: String? = null,
        val litecoin: String? = null,
        val openCollective: String? = null,
        val preferredSigner: String? = null,
    )

    @Serializable
    data class Version(
        val added: Long,
        val file: File,
        val src: File? = null,
        val manifest: Manifest,
        val whatsNew: Localized<String>? = null,
        val antiFeatures: IdMap<Localized<String>> = emptyMap(),
    )

    @Serializable
    data class Manifest(
        val nativecode: List<String> = emptyList(),
        val versionName: String,
        val versionCode: Long,
        val usesSdk: UsesSdk? = null,
        val signer: Signer? = null,
        val usesPermission: List<Permission>? = null,
        val usesPermissionSdk23: List<Permission>? = null,
    )

    @Serializable
    data class UsesSdk(
        val minSdkVersion: Int = 0,
        val targetSdkVersion: Int = 0,
    )

    @Serializable
    data class Signer(
        val sha256: List<String>,
    )

    @Serializable
    data class Permission(
        val name: String,
        val maxSdkVersion: Int = 0,
    )

    @Serializable
    data class Mirror(
        val isPrimary: Boolean = false,
        val url: String,
        val countryCode: String? = null,
    )

    @Serializable
    data class AntiFeature(
        val name: Localized<String>,
        val description: Localized<String> = emptyMap(),
        val icon: Localized<File> = emptyMap(),
    )

    @Serializable
    data class Category(
        val icon: Localized<File>? = null,
        val name: Localized<String>,
    )

    @Serializable
    data class ReleaseChannel(
        val description: Localized<String> = emptyMap(),
        val name: Localized<String> = emptyMap(),
    )

    @Serializable
    data class Screenshots(
        val phone: Localized<List<File>>? = null,
        val sevenInch: Localized<List<File>>? = null,
        val tenInch: Localized<List<File>>? = null,
        val wear: Localized<List<File>>? = null,
        val tv: Localized<List<File>>? = null,
    ) {
        val isEmpty: Boolean = listOfNotNull(phone, sevenInch, tenInch, wear, tv).isEmpty()
    }

    @Serializable
    data class File(
        val name: String,
        val sha256: String? = null,
        val size: Long? = null,
    )
}

// keys are ISO locale codes
internal typealias Localized<T> = Map<String, T>
// keys are identifiers used as foreign keys
internal typealias IdMap<T> = Map<String, T>