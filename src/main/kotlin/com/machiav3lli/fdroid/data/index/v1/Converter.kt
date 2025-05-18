package com.machiav3lli.fdroid.data.index.v1

import android.content.res.Resources
import androidx.core.os.ConfigurationCompat.getLocales
import androidx.core.os.LocaleListCompat
import com.machiav3lli.fdroid.data.database.entity.IndexProduct
import com.machiav3lli.fdroid.data.database.entity.Release
import com.machiav3lli.fdroid.data.entity.Author
import com.machiav3lli.fdroid.data.entity.Donate
import com.machiav3lli.fdroid.data.index.v0.IndexV0Parser
import com.machiav3lli.fdroid.utils.extension.Quintuple
import com.machiav3lli.fdroid.utils.extension.android.Android
import com.machiav3lli.fdroid.utils.extension.text.nullIfEmpty
import okhttp3.internal.toLongOrDefault
import java.util.Locale

internal fun IndexV1.App.toProduct(repositoryId: Long) = IndexProduct(
    repositoryId = repositoryId,
    packageName = packageName,
    label = localized.findLocalizedString(name) { _, localized -> localized.name },
    summary = localized.findLocalizedString(summary) { _, localized -> localized.summary },
    description = localized.findLocalizedString(description) { _, localized -> localized.description }
        .removeSurrounding("\n")
        .replace("\n", "<br/>"),
    added = added,
    updated = lastUpdated,
    icon = localized.findLocalizedString("/icons/$icon") { key, localized ->
        localized.localeIcon(key).nullIfEmpty()?.let { "/$packageName/$it" } ?: ""
    },
    metadataIcon = localized.findLocalizedString("") { key, localized -> localized.localeIcon(key) },
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
        .sortedWith(IndexV0Parser.DonateComparator),
    screenshots = localized
        .find { key, localized ->
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
    video = localized.findLocalizedString("") { _, localized -> localized.video },
    tracker = issueTracker,
    changelog = changelog,
    whatsNew = localized.findLocalizedString("") { _, localized -> localized.whatsNew }
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
    obbMainHashType = obbMainFileSha256
        .takeIf { it.isNotEmpty() }
        ?.let { "sha256" } ?: "",
    obbPatch = obbPatchFile,
    obbPatchHash = obbPatchFileSha256,
    obbPatchHashType = obbPatchFileSha256
        .takeIf { it.isNotEmpty() }
        ?.let { "sha256" } ?: "",
    permissions = (usesPermission + usesPermissionSdk23).mapNotNull {
        it.takeIf { it.name.isNotEmpty() && (it.maxSdk <= 0 || Android.sdk <= it.maxSdk) }
            ?.name
    },
    features = features,
    platforms = nativecode,
    incompatibilities = emptyList(),
    isCompatible = true,
)

internal fun Map<String, IndexV1.Localized>.findLocalizedString(
    fallback: String,
    callback: (String, IndexV1.Localized) -> String,
): String {
    // @BLumia: it's possible a key of a certain Localized object is empty, so we still need a fallback
    return (findLocalized { key, localized -> callback(key, localized).trim().nullIfEmpty() }
        ?: findString(fallback, callback)).trim()
}

private fun <T> Map<String, IndexV1.Localized>.findLocalized(callback: (String, IndexV1.Localized) -> T?): T? {
    return getBestLocale(getLocales(Resources.getSystem().configuration))?.let {
        callback(
            it.first,
            it.second
        )
    }
}

/**
 * Gets the best localization for the given [localeList]
 * from collections.
 */
private fun <T> Map<String, T>?.getBestLocale(localeList: LocaleListCompat): Pair<String, T>? {
    if (isNullOrEmpty()) return null
    val defLocale = Locale.getDefault()
    val defTag = defLocale.toLanguageTag()
    val sysLocaleMatch = localeList.getFirstMatch(keys.toTypedArray()) ?: return null
    val sysTag = sysLocaleMatch.toLanguageTag()
    // try the user-set default language
    return entries.find { it.key == defTag }?.toPair()
        ?: run {
            // split away stuff like script and try language and region only
            val langCountryTag = "${defLocale.language}-${defLocale.country}"
            (getOrStartsWith(langCountryTag) ?: run {
                // split away region tag and try language only
                val langTag = defLocale.language
                // try language, then English and then just take the first of the list
                getOrStartsWith(langTag)
            })?.toPair()
        }
        // now try first matched system tag (usually has region tag, e.g. de-DE)
        ?: entries.find { it.key == sysTag }?.toPair()
        ?: run {
            // split away stuff like script and try language and region only
            val langCountryTag = "${sysLocaleMatch.language}-${sysLocaleMatch.country}"
            (getOrStartsWith(langCountryTag) ?: run {
                // split away region tag and try language only
                val langTag = sysLocaleMatch.language
                // try language, then English and then just take the first of the list
                getOrStartsWith(langTag)
                    ?: entries.find { it.key == "en-US" }
                    ?: entries.find { it.key == "en" }
                    ?: entries.first()
            }).toPair()
        }
}

/**
 * Returns the value from the map with the given key or if that key is not contained in the map,
 * tries the first map key that starts with the given key.
 * If nothing matches, null is returned.
 *
 * This is useful when looking for a language tag like `fr_CH` and falling back to `fr`
 * in a map that has `fr_FR` as a key.
 */
private fun <T> Map<String, T>.getOrStartsWith(s: String): Map.Entry<String, T>? =
    entries.find { it.key == s } ?: run {
        entries.forEach { entry ->
            if (entry.key.startsWith(s)) return entry
        }
        return null
    }

private fun Map<String, IndexV1.Localized>.findString(
    fallback: String,
    callback: (String, IndexV1.Localized) -> String,
): String {
    return (find { key, localized -> callback(key, localized).nullIfEmpty() } ?: fallback).trim()
}

private fun <T> Map<String, IndexV1.Localized>.find(callback: (String, IndexV1.Localized) -> T?): T? {
    return getAndCall("en-US", callback)
        ?: getAndCall("en", callback)
}

private fun <T> Map<String, IndexV1.Localized>.getAndCall(
    key: String,
    callback: (String, IndexV1.Localized) -> T?,
): T? {
    return this[key]?.let { callback(key, it) }
}