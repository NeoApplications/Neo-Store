package com.machiav3lli.fdroid.index

import android.content.res.Resources
import androidx.core.os.ConfigurationCompat.getLocales
import androidx.core.os.LocaleListCompat
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.machiav3lli.fdroid.database.entity.Product
import com.machiav3lli.fdroid.database.entity.Release
import com.machiav3lli.fdroid.entity.Author
import com.machiav3lli.fdroid.entity.Donate
import com.machiav3lli.fdroid.entity.Screenshot
import com.machiav3lli.fdroid.utility.extension.android.Android
import com.machiav3lli.fdroid.utility.extension.json.collectDistinctNotEmptyStrings
import com.machiav3lli.fdroid.utility.extension.json.collectNotNull
import com.machiav3lli.fdroid.utility.extension.json.forEach
import com.machiav3lli.fdroid.utility.extension.json.forEachKey
import com.machiav3lli.fdroid.utility.extension.json.illegal
import com.machiav3lli.fdroid.utility.extension.text.nullIfEmpty
import okhttp3.internal.toLongOrDefault
import java.io.InputStream

class IndexV1Parser(private val repositoryId: Long, private val callback: Callback) {
    interface Callback {
        fun onRepository(
            mirrors: List<String>,
            name: String,
            description: String,
            version: Int,
            timestamp: Long,
        )

        fun onProduct(product: Product)
        fun onReleases(packageName: String, releases: List<Release>)
    }

    private class Screenshots(
        val phone: List<String>,
        val smallTablet: List<String>,
        val largeTablet: List<String>,
    )

    private class Localized(
        val name: String, val summary: String, val description: String,
        val whatsNew: String, val metadataIcon: String, val screenshots: Screenshots?,
    )

    class ParsingException(message: String, cause: Throwable? = null) : Exception(message, cause)

    private fun <T> Map<String, Localized>.getAndCall(
        key: String,
        callback: (String, Localized) -> T?,
    ): T? {
        return this[key]?.let { callback(key, it) }
    }

    /**
     * Gets the best localization for the given [localeList]
     * from collections.
     */
    private fun <T> Map<String, T>?.getBestLocale(localeList: LocaleListCompat): T? {
        if (isNullOrEmpty()) return null
        val firstMatch = localeList.getFirstMatch(keys.toTypedArray()) ?: return null
        val tag = firstMatch.toLanguageTag()
        // try first matched tag first (usually has region tag, e.g. de-DE)
        return get(tag) ?: run {
            // split away stuff like script and try language and region only
            val langCountryTag = "${firstMatch.language}-${firstMatch.country}"
            getOrStartsWith(langCountryTag) ?: run {
                // split away region tag and try language only
                val langTag = firstMatch.language
                // try language, then English and then just take the first of the list
                getOrStartsWith(langTag) ?: get("en-US") ?: get("en") ?: values.first()
            }
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
    private fun <T> Map<String, T>.getOrStartsWith(s: String): T? = get(s) ?: run {
        entries.forEach { (key, value) ->
            if (key.startsWith(s)) return value
        }
        return null
    }

    private fun <T> Map<String, Localized>.find(callback: (String, Localized) -> T?): T? {
        return getAndCall("en-US", callback) ?: getAndCall("en_US", callback) ?: getAndCall(
            "en",
            callback
        )
    }

    private fun <T> Map<String, Localized>.findLocalized(callback: (Localized) -> T?): T? {
        return getBestLocale(getLocales(Resources.getSystem().configuration))?.let { callback(it) }
    }

    private fun Map<String, Localized>.findString(
        fallback: String,
        callback: (Localized) -> String,
    ): String {
        return (find { _, localized -> callback(localized).nullIfEmpty() } ?: fallback).trim()
    }

    private fun Map<String, Localized>.findLocalizedString(
        fallback: String,
        callback: (Localized) -> String,
    ): String {
        // @BLumia: it's possible a key of a certain Localized object is empty, so we still need a fallback
        return (findLocalized { localized -> callback(localized).trim().nullIfEmpty() }
            ?: findString(fallback, callback)).trim()
    }

    private val jsonFactory = JsonFactory()

    fun parse(inputStream: InputStream) {
        try {
            val parser = jsonFactory.createParser(inputStream)
            parser.parseJson()
        } catch (e: Exception) {
            throw ParsingException("Error parsing index", e)
        }
    }

    private fun JsonParser.parseJson() {
        if (nextToken() != JsonToken.START_OBJECT) {
            illegal()
        } else {
            forEachKey { it ->
                when {
                    it.dictionary("repo")     -> parseRepository()

                    it.array("apps")          -> forEach(JsonToken.START_OBJECT) {
                        val product = parseProduct()
                        callback.onProduct(product)
                    }

                    it.dictionary("packages") -> forEachKey {
                        if (it.token == JsonToken.START_ARRAY) {
                            val packageName = it.key
                            val releases = collectNotNull(JsonToken.START_OBJECT) {
                                parseRelease(packageName)
                            }
                            callback.onReleases(packageName, releases)
                        } else {
                            skipChildren()
                        }
                    }

                    else                      -> skipChildren()
                }
            }
        }
    }

    private fun JsonParser.parseRepository() {
        var mirrors = emptyList<String>()
        var address = ""
        var name = ""
        var description = ""
        var version = 0
        var timestamp = 0L

        while (nextToken() != JsonToken.END_OBJECT) {
            when (this.currentName()) {
                "address"     -> address = text
                "mirrors"     -> mirrors = collectDistinctNotEmptyStrings()
                "name"        -> name = text
                "description" -> description = text
                "version"     -> version = valueAsInt
                "timestamp"   -> timestamp = valueAsLong
            }
        }

        val realMirrors = if (address.isNotEmpty()) listOf(address).plus(mirrors).distinct()
        else mirrors
        callback.onRepository(realMirrors, name, description, version, timestamp)
    }

    private fun JsonParser.parseProduct(): Product {
        var packageName = ""
        var nameFallback = ""
        var summaryFallback = ""
        var descriptionFallback = ""
        var icon = ""
        var authorName = ""
        var authorEmail = ""
        var authorWeb = ""
        var source = ""
        var changelog = ""
        var web = ""
        var tracker = ""
        var added = 0L
        var updated = 0L
        var suggestedVersionCode = 0L
        var categories = emptyList<String>()
        var antiFeatures = emptyList<String>()
        val licenses = mutableListOf<String>()
        val donates = mutableListOf<Donate>()
        val localizedMap = mutableMapOf<String, Localized>()
        forEachKey { it ->
            when {
                it.string("packageName")
                     -> packageName = text

                it.string("name")
                     -> nameFallback = text

                it.string("summary")
                     -> summaryFallback = text

                it.string("description")
                     -> descriptionFallback = text

                it.string("icon")
                     -> icon = IndexV0Parser.validateIcon(text)

                it.string("authorName")
                     -> authorName = text

                it.string("authorEmail")
                     -> authorEmail = text

                it.string("authorWebSite")
                     -> authorWeb = text

                it.string("sourceCode")
                     -> source = text

                it.string("changelog")
                     -> changelog = text

                it.string("webSite")
                     -> web = text

                it.string("issueTracker")
                     -> tracker = text

                it.number("added")
                     -> added = valueAsLong

                it.number("lastUpdated")
                     -> updated = valueAsLong

                it.string("suggestedVersionCode")
                     -> suggestedVersionCode = text.toLongOrDefault(0L)

                it.array("categories")
                     -> categories = collectDistinctNotEmptyStrings()

                it.array("antiFeatures")
                     -> antiFeatures = collectDistinctNotEmptyStrings()

                it.string("license")
                     -> licenses += text.split(',').filter { it.isNotEmpty() }

                it.string("donate")
                     -> donates += Donate.Regular(text)

                it.string("bitcoin")
                     -> donates += Donate.Bitcoin(text)

                it.string("flattrID")
                     -> donates += Donate.Flattr(text)

                it.string("liberapayID")
                     -> donates += Donate.Liberapay(text)

                it.string("openCollective")
                     -> donates += Donate.OpenCollective(text)

                it.dictionary("localized")
                     -> forEachKey { keyToken ->
                    if (keyToken.token == JsonToken.START_OBJECT) {
                        val locale = keyToken.key
                        var name = ""
                        var summary = ""
                        var description = ""
                        var whatsNew = ""
                        var metadataIcon = ""
                        var phone = emptyList<String>()
                        var smallTablet = emptyList<String>()
                        var largeTablet = emptyList<String>()
                        forEachKey {
                            when {
                                it.string("name")
                                     -> name = text

                                it.string("summary")
                                     -> summary = text

                                it.string("description")
                                     -> description = text

                                it.string("whatsNew")
                                     -> whatsNew = text

                                it.string("icon")
                                     -> metadataIcon = text

                                it.array("phoneScreenshots")
                                     -> phone = collectDistinctNotEmptyStrings()

                                it.array("sevenInchScreenshots")
                                     -> smallTablet = collectDistinctNotEmptyStrings()

                                it.array("tenInchScreenshots")
                                     -> largeTablet = collectDistinctNotEmptyStrings()

                                else -> skipChildren()
                            }
                        }
                        val screenshots =
                            if (sequenceOf(phone, smallTablet, largeTablet).any { it.isNotEmpty() })
                                Screenshots(phone, smallTablet, largeTablet) else null
                        localizedMap[locale] = Localized(
                            name, summary, description, whatsNew,
                            metadataIcon.nullIfEmpty()?.let { "$locale/$it" }.orEmpty(), screenshots
                        )
                    } else {
                        skipChildren()
                    }
                }

                else -> skipChildren()
            }
        }
        val name = localizedMap.findLocalizedString(nameFallback) { it.name }
        val summary = localizedMap.findLocalizedString(summaryFallback) { it.summary }
        val description =
            localizedMap.findLocalizedString(descriptionFallback) { it.description }
                .removeSurrounding("\n")
                .replace("\n", "<br/>")
        val whatsNew = localizedMap.findLocalizedString("") { it.whatsNew }
            .removeSurrounding("\n")
            .replace("\n", "<br/>")
        val metadataIcon = localizedMap.findLocalizedString("") { it.metadataIcon }
        val screenshotPairs =
            localizedMap.find { key, localized -> localized.screenshots?.let { Pair(key, it) } }
        val screenshots = screenshotPairs
            ?.let { (key, screenshots) ->
                screenshots.phone.asSequence()
                    .map { Screenshot(key, Screenshot.Type.PHONE, it) } +
                        screenshots.smallTablet.asSequence()
                            .map { Screenshot(key, Screenshot.Type.SMALL_TABLET, it) } +
                        screenshots.largeTablet.asSequence()
                            .map { Screenshot(key, Screenshot.Type.LARGE_TABLET, it) }
            }
            .orEmpty().toList()
        return Product(
            repositoryId = repositoryId,
            packageName = packageName,
            label = name,
            summary = summary,
            description = description,
            added = added,
            updated = updated,
            icon = icon,
            metadataIcon = metadataIcon,
            releases = emptyList(),
            categories = categories,
            antiFeatures = antiFeatures,
            licenses = licenses,
            donates = donates.sortedWith(IndexV0Parser.DonateComparator),
            screenshots = screenshots,
            suggestedVersionCode = suggestedVersionCode,
            author = Author(authorName, authorEmail, authorWeb),
            source = source,
            web = web,
            tracker = tracker,
            changelog = changelog,
            whatsNew = whatsNew
        )
    }

    private fun JsonParser.parseRelease(packageName: String): Release {
        var version = ""
        var versionCode = 0L
        var added = 0L
        var size = 0L
        var minSdkVersion = 0
        var targetSdkVersion = 0
        var maxSdkVersion = 0
        var source = ""
        var release = ""
        var hash = ""
        var hashTypeCandidate = ""
        var signature = ""
        var obbMain = ""
        var obbMainHash = ""
        var obbPatch = ""
        var obbPatchHash = ""
        val permissions = linkedSetOf<String>()
        var features = emptyList<String>()
        var platforms = emptyList<String>()
        forEachKey {
            when {
                it.string("versionName")           -> version = text
                it.number("versionCode")           -> versionCode = valueAsLong
                it.number("added")                 -> added = valueAsLong
                it.number("size")                  -> size = valueAsLong
                it.number("minSdkVersion")         -> minSdkVersion = valueAsInt
                it.number("targetSdkVersion")      -> targetSdkVersion = valueAsInt
                it.number("maxSdkVersion")         -> maxSdkVersion = valueAsInt
                it.string("srcname")               -> source = text
                it.string("apkName")               -> release = text
                it.string("hash")                  -> hash = text
                it.string("hashType")              -> hashTypeCandidate = text
                it.string("sig")                   -> signature = text
                it.string("obbMainFile")           -> obbMain = text
                it.string("obbMainFileSha256")     -> obbMainHash = text
                it.string("obbPatchFile")          -> obbPatch = text
                it.string("obbPatchFileSha256")    -> obbPatchHash = text
                it.array("uses-permission")        -> collectPermissions(permissions, 0)
                it.array("uses-permission-sdk-23") -> collectPermissions(permissions, 23)
                it.array("features")               -> features = collectDistinctNotEmptyStrings()
                it.array("nativecode")             -> platforms = collectDistinctNotEmptyStrings()
                else                               -> skipChildren()
            }
        }
        val hashType =
            if (hash.isNotEmpty() && hashTypeCandidate.isEmpty()) "sha256" else hashTypeCandidate
        val obbMainHashType = if (obbMainHash.isNotEmpty()) "sha256" else ""
        val obbPatchHashType = if (obbPatchHash.isNotEmpty()) "sha256" else ""
        return Release(
            packageName,
            repositoryId,
            false,
            version,
            versionCode,
            added,
            size,
            minSdkVersion,
            targetSdkVersion,
            maxSdkVersion,
            source,
            release,
            hash,
            hashType,
            signature,
            obbMain,
            obbMainHash,
            obbMainHashType,
            obbPatch,
            obbPatchHash,
            obbPatchHashType,
            permissions.toList(),
            features,
            platforms,
            emptyList()
        )
    }

    private fun JsonParser.collectPermissions(permissions: LinkedHashSet<String>, minSdk: Int) {
        forEach(JsonToken.START_ARRAY) {
            val firstToken = nextToken()
            val permission = if (firstToken == JsonToken.VALUE_STRING) valueAsString else ""
            if (firstToken != JsonToken.END_ARRAY) {
                val secondToken = nextToken()
                val maxSdk = if (secondToken == JsonToken.VALUE_NUMBER_INT) valueAsInt else 0
                if (permission.isNotEmpty() && Android.sdk >= minSdk && (maxSdk <= 0 || Android.sdk <= maxSdk)) {
                    permissions.add(permission)
                }
                if (secondToken != JsonToken.END_ARRAY) {
                    while (true) {
                        val token = nextToken()
                        if (token == JsonToken.END_ARRAY) {
                            break
                        } else if (token.isStructStart) {
                            skipChildren()
                        }
                    }
                }
            }
        }
    }
}
