package com.machiav3lli.fdroid.index.v0

import android.util.Xml
import com.machiav3lli.fdroid.database.entity.Product
import com.machiav3lli.fdroid.database.entity.Release
import com.machiav3lli.fdroid.entity.Author
import com.machiav3lli.fdroid.entity.Donate
import com.machiav3lli.fdroid.utility.extension.android.Android
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class IndexV0Parser(private val repositoryId: Long, private val callback: Callback) {
    companion object {
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            .apply { timeZone = TimeZone.getTimeZone("UTC") }

        private fun String.parseDate(): Long {
            return try {
                dateFormat.parse(this)?.time ?: 0L
            } catch (e: Exception) {
                0L
            }
        }

        internal fun validateIcon(icon: String): String {
            return if (icon.endsWith(".xml")) "" else icon
        }
    }

    interface Callback {
        fun onRepository(
            mirrors: List<String>, name: String, description: String,
            certificate: String, version: Int, timestamp: Long,
        )

        fun onProduct(product: Product)
    }

    internal object DonateComparator : Comparator<Donate> {
        private val classes = listOf(
            Donate.Regular::class,
            Donate.Bitcoin::class,
            Donate.Litecoin::class,
            Donate.Flattr::class,
            Donate.Liberapay::class,
            Donate.OpenCollective::class
        )

        override fun compare(donate1: Donate, donate2: Donate): Int {
            val index1 = classes.indexOf(donate1::class)
            val index2 = classes.indexOf(donate2::class)
            return when {
                index1 >= 0 && index2 == -1 -> -1
                index2 >= 0 && index1 == -1 -> 1
                else                        -> index1.compareTo(index2)
            }
        }
    }

    private class RepositoryBuilder {
        var address = ""
        val mirrors = mutableListOf<String>()
        var name = ""
        var description = ""
        var certificate = ""
        var version = -1
        var timestamp = 0L
    }

    private class ProductBuilder(val repositoryId: Long, val packageName: String) {
        var name = ""
        var summary = ""
        var description = ""
        var icon = ""
        var authorName = ""
        var authorEmail = ""
        var source = ""
        var changelog = ""
        var web = ""
        var tracker = ""
        var added = 0L
        var updated = 0L
        var suggestedVersionCode = 0L
        val categories = linkedSetOf<String>()
        val antiFeatures = linkedSetOf<String>()
        val licenses = mutableListOf<String>()
        val donates = mutableListOf<Donate>()
        val releases = mutableListOf<Release>()

        fun build(): Product = Product(
            repositoryId = repositoryId,
            packageName = packageName,
            label = name,
            summary = summary,
            description = description,
            added = added,
            updated = updated,
            icon = icon,
            metadataIcon = "",
            releases = releases,
            categories = categories.toList(),
            antiFeatures = antiFeatures.toList(),
            licenses = licenses,
            donates = donates.sortedWith(DonateComparator),
            screenshots = emptyList(),
            suggestedVersionCode = suggestedVersionCode,
            author = Author(authorName, authorEmail, ""),
            source = source,
            web = web,
            tracker = tracker,
            changelog = changelog,
            whatsNew = ""
        )
    }

    private class ReleaseBuilder(val repositoryId: Long) {
        var packageName = ""
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
        var hashType = ""
        var signature = ""
        var obbMain = ""
        var obbMainHash = ""
        var obbPatch = ""
        var obbPatchHash = ""
        val permissions = linkedSetOf<String>()
        val features = linkedSetOf<String>()
        val platforms = linkedSetOf<String>()

        fun build(): Release {
            val hashType = if (hash.isNotEmpty() && hashType.isEmpty()) "sha256" else hashType
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
                features.toList(),
                platforms.toList(),
                emptyList()
            )
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(inputStream: InputStream) {
        inputStream.use { stream ->
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(stream, null)
            parser.nextTag()
            parser.readFdroid()
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun XmlPullParser.readFdroid() {
        require(XmlPullParser.START_TAG, null, "fdroid")
        while (next() != XmlPullParser.END_TAG) {
            if (eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (this.name) {
                "repo"        -> readRepo()
                "application" -> readApplication()
                else          -> skip()
            }
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun XmlPullParser.readRepo() {
        require(XmlPullParser.START_TAG, null, "repo")
        val repositoryBuilder = RepositoryBuilder()
        repositoryBuilder.address = getAttributeValue(null, "url")
        repositoryBuilder.name = getAttributeValue(null, "name")
        repositoryBuilder.description = getAttributeValue(null, "description")
        repositoryBuilder.certificate = getAttributeValue(null, "pubkey")
        repositoryBuilder.version = getAttributeValue(null, "version")?.toIntOrNull() ?: 0
        repositoryBuilder.timestamp =
            (getAttributeValue(null, "timestamp")?.toLongOrNull() ?: 0L) * 1000L

        while (next() != XmlPullParser.END_TAG) {
            if (eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (name) {
                "description" -> repositoryBuilder.description = readText()
                "mirror"      -> repositoryBuilder.mirrors += readText()
                else          -> skip()
            }
        }

        callback.onRepository(
            repositoryBuilder.mirrors,
            repositoryBuilder.name,
            repositoryBuilder.description,
            repositoryBuilder.certificate,
            repositoryBuilder.version,
            repositoryBuilder.timestamp
        )
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun XmlPullParser.readApplication() {
        require(XmlPullParser.START_TAG, null, "application")
        val packageName = getAttributeValue(null, "id")
        val productBuilder = ProductBuilder(repositoryId, packageName)

        while (next() != XmlPullParser.END_TAG) {
            if (eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (this.name) {
                "name"
                     -> productBuilder.name = readText()

                "summary"
                     -> productBuilder.summary = readText()

                "description"
                     -> productBuilder.description = "<p>${readText()}</p>"

                "desc"
                     -> productBuilder.description = readText().replace("\n", "<br/>")

                "icon"
                     -> productBuilder.icon = validateIcon(readText())

                "author"
                     -> productBuilder.authorName = readText()

                "email"
                     -> productBuilder.authorEmail = readText()

                "source"
                     -> productBuilder.source = readText()

                "changelog"
                     -> productBuilder.changelog = readText()

                "web"
                     -> productBuilder.web = readText()

                "tracker"
                     -> productBuilder.tracker = readText()

                "added"
                     -> productBuilder.added = readText().parseDate()

                "lastupdated"
                     -> productBuilder.updated = readText().parseDate()

                "marketvercode"
                     -> productBuilder.suggestedVersionCode = readText().toLongOrNull() ?: 0L

                "categories"
                     -> productBuilder.categories += readText().split(',')
                    .filter { it.isNotEmpty() }

                "antifeatures"
                     -> productBuilder.antiFeatures += readText().split(',')
                    .filter { it.isNotEmpty() }

                "license"
                     -> productBuilder.licenses += readText().split(',')
                    .filter { it.isNotEmpty() }

                "donate"
                     -> productBuilder.donates += Donate.Regular(readText())

                "bitcoin"
                     -> productBuilder.donates += Donate.Bitcoin(readText())

                "litecoin"
                     -> productBuilder.donates += Donate.Litecoin(readText())

                "flattr"
                     -> productBuilder.donates += Donate.Flattr(readText())

                "liberapay"
                     -> productBuilder.donates += Donate.Liberapay(readText())

                "openCollective"
                     -> productBuilder.donates += Donate.OpenCollective(readText())

                "package"
                     -> readPackage(productBuilder)

                else -> skip()
            }
        }

        callback.onProduct(productBuilder.build())
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun XmlPullParser.readPackage(productBuilder: ProductBuilder) {
        require(XmlPullParser.START_TAG, null, "package")
        val releaseBuilder = ReleaseBuilder(repositoryId)
        releaseBuilder.packageName = productBuilder.packageName

        while (next() != XmlPullParser.END_TAG) {
            if (eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (this.name) {
                "version"
                       -> releaseBuilder.version = readText()

                "versioncode"
                       -> releaseBuilder.versionCode =
                    readText().toLongOrNull() ?: 0L

                "added"
                       -> releaseBuilder.added = readText().parseDate()

                "size"
                       -> releaseBuilder.size =
                    readText().toLongOrNull() ?: 0L

                "sdkver"
                       -> releaseBuilder.minSdkVersion =
                    readText().toIntOrNull() ?: 0

                "targetSdkVersion"
                       -> releaseBuilder.targetSdkVersion =
                    readText().toIntOrNull() ?: 0

                "maxsdkver"
                       -> releaseBuilder.maxSdkVersion =
                    readText().toIntOrNull() ?: 0

                "srcname"
                       -> releaseBuilder.source = readText()

                "apkname"
                       -> releaseBuilder.release = readText()

                "hash" -> {
                    releaseBuilder.hashType = getAttributeValue(null, "type")
                    releaseBuilder.hash = readText()
                }

                "sig"
                       -> releaseBuilder.signature = readText()

                "obbMainFile"
                       -> releaseBuilder.obbMain = readText()

                "obbMainFileSha256"
                       -> releaseBuilder.obbMainHash = readText()

                "obbPatchFile"
                       -> releaseBuilder.obbPatch = readText()

                "obbPatchFileSha256"
                       -> releaseBuilder.obbPatchHash = readText()

                "uses-permission"
                       -> readPermission(releaseBuilder)

                "uses-permission-sdk-23"
                       -> readPermission(releaseBuilder)

                "features"
                       -> releaseBuilder.features += readText().split(',')
                    .filter { it.isNotEmpty() }

                "nativecode"
                       -> releaseBuilder.platforms += readText().split(',')
                    .filter { it.isNotEmpty() }

                else   -> skip()
            }
        }

        productBuilder.releases.add(releaseBuilder.build())
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun XmlPullParser.readText(): String {
        var result = ""
        if (next() == XmlPullParser.TEXT) {
            result = this.text
            nextTag()
        }
        return result
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun XmlPullParser.readPermission(releaseBuilder: ReleaseBuilder) {
        require(XmlPullParser.START_TAG, null, this.name)
        val minSdkVersion = if (this.name == "uses-permission-sdk-23") 23 else 0
        val maxSdkVersion = getAttributeValue(null, "maxSdkVersion")?.toIntOrNull()
            ?: Int.MAX_VALUE
        val name = getAttributeValue(null, "name")
        if (Android.sdk in minSdkVersion..maxSdkVersion) {
            releaseBuilder.permissions.add(name)
        }
        nextTag()
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun XmlPullParser.skip() {
        check(eventType == XmlPullParser.START_TAG)
        var depth = 1
        while (depth != 0) {
            when (next()) {
                XmlPullParser.END_TAG   -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }
}
