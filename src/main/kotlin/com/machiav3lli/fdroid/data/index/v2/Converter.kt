package com.machiav3lli.fdroid.data.index.v2

import android.content.res.Resources
import androidx.core.os.ConfigurationCompat.getLocales
import androidx.core.os.LocaleListCompat
import com.machiav3lli.fdroid.data.database.entity.IndexProduct
import com.machiav3lli.fdroid.data.database.entity.Release
import com.machiav3lli.fdroid.data.entity.AntiFeature
import com.machiav3lli.fdroid.data.entity.Author
import com.machiav3lli.fdroid.data.entity.Donate
import com.machiav3lli.fdroid.data.index.IndexContentMerger
import com.machiav3lli.fdroid.data.index.v2.IndexV2.File
import com.machiav3lli.fdroid.utils.LocaleChooser.LocaleConfig
import com.machiav3lli.fdroid.utils.extension.android.Android
import java.util.Locale

internal fun IndexV2.Package.toProduct(repositoryId: Long, packageName: String, locales: LocaleConfig) = IndexProduct(
    repositoryId = repositoryId,
    packageName = packageName,
    label = metadata.name.findLocalized(locales, packageName),
    summary = metadata.summary.findLocalized(locales, ""),
    description = metadata.description.findLocalized(locales, "")
        .removeSurrounding("\n")
        .replace("\n", "<br/>"),
    added = metadata.added,
    updated = metadata.lastUpdated,
    icon = metadata.icon.findLocalized(locales, File("")).name,
    metadataIcon = metadata.icon.findLocalized(locales, File("")).name,
    releases = emptyList(),
    categories = metadata.categories,
    antiFeatures = versions.entries.maxBy { it.value.added }.value.antiFeatures.keys.toList(),
    licenses = metadata.license.split(',').filter { it.isNotEmpty() },
    donates = listOfNotNull(
        *metadata.donate.map { Donate.Regular(it) }.toTypedArray(),
        metadata.bitcoin?.let { Donate.Bitcoin(it) },
        metadata.openCollective?.let { Donate.OpenCollective(it) },
        metadata.liberapay?.let { Donate.Liberapay(it) },
        metadata.litecoin?.let { Donate.Litecoin(it) },
    )
        .sortedWith(IndexContentMerger.DonateComparator),
    screenshots = with(metadata.screenshots) {
        listOfNotNull(this?.phone, this?.sevenInch, this?.tenInch, this?.wear, this?.tv)
            .fold(mutableMapOf<String, List<File>>()) { acc, map ->
                map.forEach { (key, value) ->
                    acc.merge(key, value) { oldList, newList ->
                        oldList + newList
                    }
                }
                acc
            }
            .findLocalized(locales, emptyList())
            .map(File::name)
    },
    suggestedVersionCode = 0L,
    author = Author(
        metadata.authorName.orEmpty(),
        metadata.authorEmail.orEmpty(),
        metadata.authorWebsite.orEmpty()
    ),
    source = metadata.sourceCode.orEmpty(),
    web = metadata.webSite.orEmpty(),
    video = metadata.video.findLocalized(locales, ""),
    tracker = metadata.issueTracker.orEmpty(),
    translation = metadata.translation.orEmpty(),
    changelog = metadata.changelog.orEmpty(),
    // TODO convert usage to Map<VersionCode,Changelog>
    whatsNew = versions.entries
        .maxBy { it.value.added }.value
        .whatsNew.findLocalized(locales, ""),
)

internal fun IndexV2.Version.toRelease(
    repositoryId: Long,
    packageName: String,
) = Release(
    packageName = packageName,
    repositoryId = repositoryId,
    selected = false,
    version = manifest.versionName,
    versionCode = manifest.versionCode,
    added = added,
    size = file.size ?: 0L,
    minSdkVersion = manifest.usesSdk?.minSdkVersion ?: 0,
    targetSdkVersion = manifest.usesSdk?.targetSdkVersion ?: 0,
    maxSdkVersion = 0,
    source = src?.name.orEmpty(),
    release = file.name.removePrefix("/"),
    hash = file.sha256.orEmpty(),
    hashType = "SHA-256",
    signature = manifest.signer?.sha256?.first().orEmpty(),
    obbMain = "",
    obbMainHash = "",
    obbMainHashType = "",
    obbPatch = "",
    obbPatchHash = "",
    obbPatchHashType = "",
    permissions = manifest.usesPermission.orEmpty().plus(manifest.usesPermissionSdk23.orEmpty())
        .mapNotNull {
            it.takeIf { it.name.isNotEmpty() && (it.maxSdkVersion <= 0 || Android.sdk <= it.maxSdkVersion) }
                ?.name
        },
    features = emptyList(),
    platforms = manifest.nativecode,
    incompatibilities = emptyList(),
    isCompatible = true,
    releaseChannels = releaseChannels,
    hasVulnerability = antiFeatures.keys.contains(AntiFeature.KNOWN_VULN.key),
)

internal fun <T> Localized<T>?.findLocalized(locales: LocaleConfig, fallback: T): T =
    getBestLocale(locales) ?: fallback

private fun <T> Localized<T>?.getBestLocale(locales: LocaleConfig): T? {
    if (isNullOrEmpty()) return null

    var bestMatch: T? = null
    var bestWeight = -1
    for (match in entries.iterator()) {
        val locale = match.key
        val weight = locales.getWeight(locale)
        if (weight == -1) continue
        if (bestWeight == -1 || weight < bestWeight) {
            bestMatch = match.value
            bestWeight = weight
        }
    }
    return bestMatch ?: entries.first().value
}
