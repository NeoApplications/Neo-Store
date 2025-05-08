package com.machiav3lli.fdroid.data.index.v2

import android.content.res.Resources
import androidx.core.os.ConfigurationCompat.getLocales
import androidx.core.os.LocaleListCompat
import com.machiav3lli.fdroid.data.database.entity.Product
import com.machiav3lli.fdroid.data.database.entity.Release
import com.machiav3lli.fdroid.data.entity.Author
import com.machiav3lli.fdroid.data.entity.Donate
import com.machiav3lli.fdroid.data.entity.Screenshot
import com.machiav3lli.fdroid.data.index.v0.IndexV0Parser
import com.machiav3lli.fdroid.data.index.v2.IndexV2.File
import com.machiav3lli.fdroid.utils.extension.android.Android

internal fun IndexV2.Package.toProduct(repositoryId: Long, packageName: String) = Product(
    repositoryId = repositoryId,
    packageName = packageName,
    label = metadata.name.findLocalized(packageName),
    summary = metadata.summary.findLocalized(""),
    description = metadata.description.findLocalized("")
        .removeSurrounding("\n")
        .replace("\n", "<br/>"),
    added = metadata.added,
    updated = metadata.lastUpdated,
    icon = metadata.icon.findLocalized(File("")).name,
    metadataIcon = metadata.icon.findLocalized(File("")).name,
    releases = emptyList(),
    categories = metadata.categories,
    // TODO add full anti-features with declarations
    antiFeatures = versions.entries.maxBy { it.value.added }.value.antiFeatures.keys.toList(),
    licenses = metadata.license.split(',').filter { it.isNotEmpty() },
    donates = listOfNotNull(
        *metadata.donate.map { Donate.Regular(it) }.toTypedArray(),
        metadata.bitcoin?.let { Donate.Bitcoin(it) },
        metadata.openCollective?.let { Donate.OpenCollective(it) },
        metadata.liberapay?.let { Donate.Liberapay(it) },
        metadata.litecoin?.let { Donate.Litecoin(it) },
    )
        .sortedWith(IndexV0Parser.DonateComparator),
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
            .findLocalized(emptyList())
            .map {
                Screenshot(null, null, it.name)
            }
    },
    suggestedVersionCode = 0L,
    author = Author(
        metadata.authorName.orEmpty(),
        metadata.authorEmail.orEmpty(),
        metadata.authorWebsite.orEmpty()
    ),
    source = metadata.sourceCode.orEmpty(),
    web = metadata.webSite.orEmpty(),
    video = metadata.video.findLocalized(""),
    tracker = metadata.issueTracker.orEmpty(),
    changelog = metadata.changelog.orEmpty(),
    whatsNew = versions.entries
        .maxBy { it.value.added }.value
        .whatsNew.findLocalized(""),
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
)

internal fun <T> Localized<T>?.findLocalized(fallback: T): T =
    getBestLocale(getLocales(Resources.getSystem().configuration)) ?: fallback

private fun <T> Localized<T>?.getBestLocale(localeList: LocaleListCompat): T? {
    if (isNullOrEmpty()) return null
    val firstMatch = localeList.getFirstMatch(keys.toTypedArray()) ?: return null
    val tag = firstMatch.toLanguageTag()
    // try first matched tag first (usually has region tag, e.g. de-DE)
    return entries.find { it.key == tag }?.value ?: run {
        // split away stuff like script and try language and region only
        val langCountryTag = "${firstMatch.language}-${firstMatch.country}"
        (getOrStartsWith(langCountryTag) ?: run {
            // split away region tag and try language only
            val langTag = firstMatch.language
            // try language, then English and then just take the first of the list
            getOrStartsWith(langTag)
                ?: get("en-US")
                ?: getOrStartsWith("en")
                ?: entries.first().value
        })
    }
}

private fun <T> Map<String, T>.getOrStartsWith(s: String): T? =
    entries.find { it.key == s }?.value ?: run {
        entries.forEach { entry ->
            if (entry.key.startsWith(s)) return entry.value
        }
        return null
    }
