package com.looker.droidify.entity

import com.looker.droidify.database.entity.Installed
import com.looker.droidify.database.entity.Release
import com.looker.droidify.utility.extension.text.nullIfEmpty
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class Product(
    var repositoryId: Long,
    val packageName: String,
    val name: String,
    val summary: String,
    var description: String,
    val whatsNew: String,
    val icon: String,
    val metadataIcon: String,
    val author: Author,
    val source: String,
    val changelog: String,
    val web: String,
    val tracker: String,
    val added: Long,
    val updated: Long,
    val suggestedVersionCode: Long,
    val categories: List<String>,
    val antiFeatures: List<String>,
    val licenses: List<String>,
    val donates: List<Donate>,
    val screenshots: List<Screenshot>,
    val releases: List<Release>,
) {
    // Same releases with different signatures
    val selectedReleases: List<Release>
        get() = releases.filter { it.selected }

    val displayRelease: Release?
        get() = selectedReleases.firstOrNull() ?: releases.firstOrNull()

    val version: String
        get() = displayRelease?.version.orEmpty()

    val versionCode: Long
        get() = selectedReleases.firstOrNull()?.versionCode ?: 0L

    val compatible: Boolean
        get() = selectedReleases.firstOrNull()?.incompatibilities?.isEmpty() == true

    val signatures: List<String>
        get() = selectedReleases.mapNotNull { it.signature.nullIfEmpty() }.distinct().toList()

    fun item(): ProductItem {
        return ProductItem(
            repositoryId,
            packageName,
            name,
            summary,
            icon,
            metadataIcon,
            version,
            "",
            compatible,
            false,
            0
        )
    }

    fun canUpdate(installed: Installed?): Boolean {
        return installed != null && compatible && versionCode > installed.version_code &&
                installed.signature in signatures
    }

    fun toJSON() = Json.encodeToString(this)

    companion object {
        fun fromJson(json: String) = Json.decodeFromString<Product>(json)

        fun <T> findSuggested(
            products: List<T>,
            installed: Installed?,
            extract: (T) -> Product,
        ): T? {
            return products.maxWithOrNull(compareBy({
                extract(it).compatible &&
                        (installed == null || installed.signature in extract(it).signatures)
            }, { extract(it).versionCode }))
        }
    }
}
