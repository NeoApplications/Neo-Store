package com.machiav3lli.fdroid.data.index.v1

import com.machiav3lli.fdroid.data.database.entity.IndexProduct
import com.machiav3lli.fdroid.data.database.entity.Release
import com.machiav3lli.fdroid.data.entity.AntiFeature
import com.machiav3lli.fdroid.data.entity.Author
import com.machiav3lli.fdroid.data.entity.Donate
import com.machiav3lli.fdroid.data.index.IndexContentMerger
import com.machiav3lli.fdroid.utils.extension.Quintuple
import com.machiav3lli.fdroid.utils.extension.android.Android
import com.machiav3lli.fdroid.utils.extension.text.nullIfEmpty
import okhttp3.internal.toLongOrDefault

internal fun IndexV1.App.toProduct(repositoryId: Long, locales: IndexV1Parser.LocalesCallback) =
    IndexProduct(
        repositoryId = repositoryId,
        packageName = packageName,
        label = name.nullIfEmpty()
            ?: localized.findLocalizedString(locales, name) { _, localized -> localized.name },
        summary = localized.findLocalizedString(
            locales,
            summary
        ) { _, localized -> localized.summary },
        description = localized.findLocalizedString(
            locales,
            description
        ) { _, localized -> localized.description }
            .removeSurrounding("\n")
            .replace("\n", "<br/>"),
        added = added,
        updated = lastUpdated,
        icon = localized.findLocalizedString(locales, "/icons/$icon") { key, localized ->
            localized.localeIcon(key).nullIfEmpty()?.let { "/$packageName/$it" } ?: ""
        },
        metadataIcon = localized.findLocalizedString(
            locales,
            ""
        ) { key, localized -> localized.localeIcon(key) },
        releases = emptyList(),
        categories = categories,
        antiFeatures = antiFeatures,
        licenses = license.split(',').filter { it.isNotEmpty() },
        donates = listOfNotNull(
            donate?.let { Donate.Regular(it) },
            bitcoin?.let { Donate.Bitcoin(it) },
            openCollective?.let { Donate.OpenCollective(it) },
            liberapay?.let { Donate.Liberapay(it) },
            litecoin?.let { Donate.Litecoin(it) },
        )
            .sortedWith(IndexContentMerger.DonateComparator),
        screenshots = localized
            .find(locales) { key, localized ->
                Quintuple(
                    localized.phoneScreenshots,
                    localized.sevenInchScreenshots,
                    localized.tenInchScreenshots,
                    localized.tvScreenshots,
                    localized.wearScreenshots,
                )
                    .takeIf {
                        it.first.isNotEmpty() || it.second.isNotEmpty() || it.third.isNotEmpty() || it.fourth.isNotEmpty() || it.fifth.isNotEmpty()
                    }
                    ?.let { Pair(key, it) }
            }?.let { (key, screenshots) ->
                screenshots.first.map { "/$packageName/$key/phoneScreenshots/$it" } +
                        screenshots.second.map { "/$packageName/$key/sevenInchScreenshots/$it" } +
                        screenshots.third.map { "/$packageName/$key/tenInchScreenshots/$it" } +
                        screenshots.fourth.map { "/$packageName/$key/tvScreenshots/$it" } +
                        screenshots.fifth.map { "/$packageName/$key/wearScreenshots/$it" }
            }
            .orEmpty().toList(),
        suggestedVersionCode = suggestedVersionCode.toLongOrDefault(0L),
        author = Author(authorName, authorEmail, authorWebSite),
        source = sourceCode,
        web = webSite,
        video = localized.findLocalizedString(locales, "") { _, localized -> localized.video },
        tracker = issueTracker,
        translation = translation,
        changelog = changelog,
        whatsNew = localized.findLocalizedString(locales, "") { _, localized -> localized.whatsNew }
            .removeSurrounding("\n")
            .replace("\n", "<br/>"),
    )

internal fun IndexV1.Package.toRelease(
    repositoryId: Long,
    packageName: String
) = Release(
    packageName = packageName,
    repositoryId = repositoryId,
    selected = false,
    version = versionName,
    versionCode = versionCode,
    added = added,
    size = size,
    minSdkVersion = minSdkVersion,
    targetSdkVersion = targetSdkVersion,
    maxSdkVersion = maxSdkVersion,
    source = srcname,
    release = apkName,
    hash = hash,
    hashType = hashType,
    signature = signer,
    obbMain = obbMainFile,
    obbMainHash = obbMainFileSha256,
    obbMainHashType = if (obbMainFileSha256.isNotBlank()) "sha256" else "",
    obbPatch = obbPatchFile,
    obbPatchHash = obbPatchFileSha256,
    obbPatchHashType = if (obbPatchFileSha256.isNotBlank()) "sha256" else "",
    permissions = (usesPermission + usesPermissionSdk23).mapNotNull {
        it.takeIf { it.name.isNotEmpty() && (it.maxSdk <= 0 || Android.sdk <= it.maxSdk) }
            ?.name
    },
    features = features,
    platforms = nativecode,
    incompatibilities = emptyList(),
    isCompatible = true,
    releaseChannels = emptyList(),
    hasVulnerability = antiFeatures.contains(AntiFeature.KNOWN_VULN.key),
)

internal fun Map<String, IndexV1.Localized>.findLocalizedString(
    locales: IndexV1Parser.LocalesCallback,
    fallback: String,
    callback: (String, IndexV1.Localized) -> String,
): String {
    // @BLumia: it's possible a key of a certain Localized object is empty, so we still need a fallback
    return locales.findLocalized { key -> callback(key, this[key]!!).trim().nullIfEmpty() }
        ?: fallback.trim()
}

private fun <T> Map<String, IndexV1.Localized>.find(
    locales: IndexV1Parser.LocalesCallback,
    callback: (String, IndexV1.Localized) -> T?,
): T? {
    return locales.findLocalized { callback(it, this[it]!!) }
}
