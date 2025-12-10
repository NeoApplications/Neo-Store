package com.machiav3lli.fdroid.data.database.entity

import android.util.Base64
import android.util.Log
import android.util.Xml
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.machiav3lli.fdroid.ROW_ADDRESS
import com.machiav3lli.fdroid.ROW_ENABLED
import com.machiav3lli.fdroid.ROW_ID
import com.machiav3lli.fdroid.ROW_UPDATED
import com.machiav3lli.fdroid.TABLE_REPOSITORY
import com.machiav3lli.fdroid.data.database.DatabaseX.Companion.TAG
import com.machiav3lli.fdroid.utils.Utils.calculateSHA256
import com.machiav3lli.fdroid.utils.extension.text.nullIfEmpty
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.xmlpull.v1.XmlPullParser
import java.io.File
import java.io.FileInputStream
import java.net.URL
import java.nio.charset.Charset

@Entity(
    tableName = TABLE_REPOSITORY,
    indices = [
        Index(value = [ROW_ID], unique = true),
        Index(value = [ROW_ENABLED]),
        Index(value = [ROW_ADDRESS]),
        Index(value = [ROW_UPDATED, ROW_ENABLED]),
    ]
)
@Serializable
data class Repository(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val address: String = "",
    // TODO add support for countryCode and isPrimary
    val mirrors: List<String> = emptyList(),
    val name: String = "",
    val description: String = "",
    val version: Int = 21,
    val enabled: Boolean = false,
    val fingerprint: String = "",
    val lastModified: String = "",
    @ColumnInfo(defaultValue = "")
    val entryLastModified: String = "",
    val entityTag: String = "",
    @ColumnInfo(defaultValue = "")
    val entryEntityTag: String = "",
    val updated: Long = 0L,
    val timestamp: Long = 0L,
    val authentication: String = "",
    @ColumnInfo(defaultValue = "")
    val webBaseUrl: String = "",
    @ColumnInfo(defaultValue = "0")
    val mirrorRotation: Boolean = false,
) {
    val intentAddress: String
        get() = "${address.trimEnd('/')}?fingerprint=$fingerprint"

    fun edit(address: String, fingerprint: String, authentication: String): Repository = let {
        val changed = this.address != address || this.fingerprint != fingerprint
        copy(
            timestamp = if (changed) 0L else timestamp,
            lastModified = if (changed) "" else lastModified,
            entryLastModified = if (changed) "" else entryLastModified,
            entityTag = if (changed) "" else entityTag,
            entryEntityTag = if (changed) "" else entryEntityTag,
            address = address,
            fingerprint = fingerprint,
            authentication = authentication,
        )
    }

    fun update(
        mirrors: List<String>, name: String, description: String, version: Int,
        lastModified: String, entityTag: String, timestamp: Long, webBaseUrl: String?,
        entryLastModified: String?, entryEntityTag: String?,
    ): Repository = copy(
        mirrors = mirrors,
        name = name.nullIfEmpty() ?: this.name,
        description = description.nullIfEmpty() ?: this.description,
        version = if (version >= 0) version else this.version,
        lastModified = lastModified,
        entryLastModified = entryLastModified ?: this.entryLastModified,
        entityTag = entityTag,
        entryEntityTag = entryEntityTag ?: this.entryEntityTag,
        updated = System.currentTimeMillis(),
        timestamp = timestamp,
        webBaseUrl = webBaseUrl ?: this.webBaseUrl,
    )

    fun enable(enabled: Boolean): Repository = copy(
        enabled = enabled,
        timestamp = 0L,
        lastModified = "",
        entryLastModified = "",
        entityTag = "",
        entryEntityTag = "",
    )

    val authenticationPair: Pair<String?, String?>
        get() = authentication.nullIfEmpty()
            ?.let { if (it.startsWith("Basic ")) it.substring(6) else null }
            ?.let {
                try {
                    Base64.decode(it, Base64.NO_WRAP).toString(Charset.defaultCharset())
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
            ?.let {
                val index = it.indexOf(':')
                if (index >= 0) Pair(
                    it.substring(0, index),
                    it.substring(index + 1)
                ) else null
            }
            ?: Pair(null, null)

    val downloadAddress: String
        get() = if (!mirrorRotation) address
        else mirrors.filter { address.contains(".onion/") || !it.contains(".onion/") }.random()

    fun toJSON() = Json.encodeToString(this)

    companion object {
        fun fromJson(json: String) = Json.decodeFromString<Repository>(json)

        fun authentication(username: String?, password: String?) = username?.let { u ->
            password
                ?.let { p ->
                    Base64.encodeToString(
                        "$u:$p".toByteArray(Charset.defaultCharset()),
                        Base64.NO_WRAP
                    )
                }
        }
            ?.let { "Basic $it" }.orEmpty()

        fun newRepository(
            address: String = "",
            fallbackName: String = "",
            fingerprint: String = "",
            authentication: String = "",
        ): Repository {
            val name = try {
                URL(address).let { "${it.host}${it.path}" }
            } catch (e: Exception) {
                fallbackName
            }
            return Repository(
                address = address,
                name = name,
                fingerprint = fingerprint,
                authentication = authentication
            )
        }

        fun parsePresetReposXML(additionalReposFile: File): List<Repository> = runCatching {
            val repoItems = mutableListOf<String>()
            FileInputStream(additionalReposFile).use { input ->
                val parser: XmlPullParser = Xml.newPullParser()
                parser.setInput(input, null)

                parser.apply {
                    var isItem = false
                    while (next() != XmlPullParser.END_DOCUMENT) {
                        when (eventType) {
                            XmlPullParser.START_TAG -> if (name == "item") isItem = true
                            XmlPullParser.END_TAG   -> isItem = false
                            XmlPullParser.TEXT      -> if (isItem) repoItems.add(parser.text)
                        }
                    }
                }
            }

            // additional_repos: each object seems to have 7 items:
            // name (str), address (str), description (str),
            // version (int), enabled (0/1), push requests (ignore?), pubkey (hex)
            return if (repoItems.size % 7 == 0) {
                repoItems.chunked(7).mapNotNull { itemsSet ->
                    fromXML(itemsSet)?.let {
                        Log.w(
                            TAG,
                            ("Preset Repositories: Successfully loaded ${it.name}: ${it.address}")
                        )
                        it
                    }
                }
            } else {
                Log.e(
                    TAG,
                    ("Preset Repositories: Invalid source $additionalReposFile with false number of items: ${repoItems.size}")
                )
                emptyList()
            }
        }.fold(
            onSuccess = { it },
            onFailure = {
                Log.e(
                    TAG,
                    ("Preset Repositories: Failed parsing preset repositories from $additionalReposFile: ${it.message}")
                )
                emptyList()
            }
        )

        fun fromXML(xml: List<String>) = runCatching {
            defaultRepository(
                name = xml[0],
                address = xml[1],
                description = xml[2].replace(Regex("\\s+"), " ").trim(),
                version = xml[3].toInt(),
                enabled = xml[4].toInt() > 0 && xml[1].startsWith("http"),
                fingerprint = xml[6].let {
                    if (it.length > 32) calculateSHA256(it)
                    else it
                },
                authentication = "",
            )
        }.getOrNull()

        private fun defaultRepository(
            address: String, name: String, description: String, version: Int,
            enabled: Boolean, fingerprint: String, authentication: String, webBaseUrl: String = "",
        ): Repository = Repository(
            0, address, emptyList(), name, description, version, enabled,
            fingerprint, "", "", "", "",
            0L, 0L, authentication, webBaseUrl,
        )

        private val F_DROID = defaultRepository(
            "https://f-droid.org/repo",
            "F-Droid",
            "The official F-Droid Free Software repository. " +
                    "Everything in this repository is always built from the source code.",
            21,
            true,
            "43238D512C1E5EB2D6569F4A3AFBF5523418B82E0A3ED1552770ABB9A9C9CCAB", "",
            "https://f-droid.org/packages/",
        )
        private val F_DROID_ARCHIVE =
            defaultRepository(
                "https://f-droid.org/archive",
                "F-Droid Archive",
                "The archive of the official F-Droid Free " +
                        "Software repository. Apps here are old and can contain known vulnerabilities and security issues!",
                21,
                false,
                "43238D512C1E5EB2D6569F4A3AFBF5523418B82E0A3ED1552770ABB9A9C9CCAB", "",
            )
        private val GUARDIAN = defaultRepository(
            "https://guardianproject.info/fdroid/repo",
            "Guardian Project Official Releases",
            "The " +
                    "official repository of The Guardian Project apps for use with the F-Droid client. Applications in this " +
                    "repository are official binaries built by the original application developers and signed by the same key as " +
                    "the APKs that are released in the Google Play Store.",
            21,
            true,
            "B7C2EEFD8DAC7806AF67DFCD92EB18126BC08312A7F2D6F3862E46013C7A6135", "",
        )
        private val GUARDIAN_ARCHIVE = defaultRepository(
            "https://guardianproject.info/fdroid/archive",
            "Guardian Project Archive",
            "The official " +
                    "repository of The Guardian Project apps for use with the F-Droid client. This contains older versions of " +
                    "applications from the main repository.",
            21,
            false,
            "B7C2EEFD8DAC7806AF67DFCD92EB18126BC08312A7F2D6F3862E46013C7A6135", "",
        )
        private val IZZY = defaultRepository(
            "https://apt.izzysoft.de/fdroid/repo", "IzzyOnDroid F-Droid Repo",
            "This is a " +
                    "repository of apps to be used with F-Droid the original application developers, taken from the resp. " +
                    "repositories (mostly GitHub). At this moment I cannot give guarantees on regular updates for all of them, " +
                    "though most are checked multiple times a week ",
            21, true,
            "3BF0D6ABFEAE2F401707B6D966BE743BF0EEE49C2561B9BA39073711F628937A", "",
            "https://apt.izzysoft.de/fdroid/index/apk/",
        )
        private val MICRO_G = defaultRepository(
            "https://microg.org/fdroid/repo", "MicroG Project",
            "Official repository of the open-source implementation of Google Play Services.",
            21, false, "9BD06727E62796C0130EB6DAB39B73157451582CBD138E86C468ACC395D14165", ""
        )
        private val BROMITE = defaultRepository(
            "https://fdroid.bromite.org/fdroid/repo", "Bromite",
            "Bromite is a Chromium plus ad blocking and enhanced privacy; take back your browser.",
            21, false, "E1EE5CD076D7B0DC84CB2B45FB78B86DF2EB39A3B6C56BA3DC292A5E0C3B9504", ""
        )
        private val NEWPIPE = defaultRepository(
            "https://archive.newpipe.net/fdroid/repo", "NewPipe",
            "NewPipe's official independent repository.",
            21, false, "E2402C78F9B97C6C89E97DB914A2751FDA1D02FE2039CC0897A462BDB57E7501", ""
        )
        private val LIBRETRO = defaultRepository(
            "https://fdroid.libretro.com/repo", "LibRetro",
            "The official canary repository for this great retro emulators hub.",
            21, false, "3F05B24D497515F31FEAB421297C79B19552C5C81186B3750B7C131EF41D733D", ""
        )
        private val KDE_RELEASE = defaultRepository(
            "https://cdn.kde.org/android/stable-releases/fdroid/repo", "KDE Android Release",
            "The official release repository for KDE Android apps.",
            21, false, "13784BA6C80FF4E2181E55C56F961EED5844CEA16870D3B38D58780B85E1158F", ""
        )
        private val KDE_NIGHTLY = defaultRepository(
            "https://cdn.kde.org/android/fdroid/repo", "KDE Android Nightly",
            "The official nightly repository for KDE Android apps.",
            21, false, "B3EBE10AFA6C5C400379B34473E843D686C61AE6AD33F423C98AF903F056523F", ""
        )
        private val CALYX_OS_TEST = defaultRepository(
            "https://calyxos.gitlab.io/calyx-fdroid-repo/fdroid/repo", "Calyx OS Repo - Testing",
            "The official Calyx Labs F-Droid repository.",
            21, false, "C44D58B4547DE5096138CB0B34A1CC99DAB3B4274412ED753FCCBFC11DC1B7B6", ""
        )
        private val DIVEST_OS = defaultRepository(
            "https://divestos.org/fdroid/official", "Divest OS Repo",
            "The official Divest OS F-Droid repository.",
            21, false, "E4BE8D6ABFA4D9D4FEEF03CDDA7FF62A73FD64B75566F6DD4E5E577550BE8467", ""
        )
        private val COLLABORA = defaultRepository(
            "https://www.collaboraoffice.com/downloads/fdroid/repo", "Collabora Office",
            "Collabora Office is an office suite based on LibreOffice.",
            21, false, "573258C84E149B5F4D9299E7434B2B69A8410372921D4AE586BA91EC767892CC", ""
        )
        private val NETSYMS = defaultRepository(
            "https://repo.netsyms.com/fdroid/repo", "Netsyms Technologies",
            "Open-source apps created by Netsyms Technologies.",
            21, false, "2581BA7B32D3AB443180C4087CAB6A7E8FB258D3A6E98870ECB3C675E4D64489", ""
        )
        private val FEDILAB = defaultRepository(
            "https://fdroid.fedilab.app/repo", "Fedilab",
            "Fedilab's official F-Droid repository.",
            21, false, "11F0A69910A4280E2CD3CCC3146337D006BE539B18E1A9FEACE15FF757A94FEB", ""
        )
        private val NETHUNTER = defaultRepository(
            "https://store.nethunter.com/repo", "Kali Nethunter",
            "Kali Nethunter's official selection of original binaries.",
            21, false, "7E418D34C3AD4F3C37D7E6B0FACE13332364459C862134EB099A3BDA2CCF4494", ""
        )
        private val NANODROID = defaultRepository(
            "https://nanolx.org/fdroid/repo", "NanoDroid",
            "A companion repository to microG's installer.",
            21, false, "862ED9F13A3981432BF86FE93D14596B381D75BE83A1D616E2D44A12654AD015", ""
        )
        private val FROSTNERD = defaultRepository(
            "https://fdroid.frostnerd.com/fdroid/repo/", "Frostnerd",
            "A repository with a group of apps with public code.",
            21, false, "74BB580F263EC89E15C207298DEC861B5069517550FE0F1D852F16FA611D2D26", ""
        )
        private val FROSTNERD_ARCHIVE = defaultRepository(
            "https://fdroidarchive.frostnerd.com/", "Frostnerd Archive",
            "An archive repository of Frostnerd.",
            21, false, "74BB580F263EC89E15C207298DEC861B5069517550FE0F1D852F16FA611D2D26", ""
        )
        private val BITWARDEN = defaultRepository(
            "https://mobileapp.bitwarden.com/fdroid/repo", "Bitwarden",
            "The official repository for Bitwarden.",
            21, false, "BC54EA6FD1CD5175BCCCC47C561C5726E1C3ED7E686B6DB4B18BAC843A3EFE6C", ""
        )
        private val MOLLY = defaultRepository(
            "https://molly.im/fdroid/foss/fdroid/repo", "Molly",
            "Molly is a fork of Signal focused on security.",
            21, false, "5198DAEF37FC23C14D5EE32305B2AF45787BD7DF2034DE33AD302BDB3446DF74", ""
        )
        private val BRIAR = defaultRepository(
            "https://briarproject.org/fdroid/repo", "Briar",
            "An serverless/offline messenger that hides your metadata.",
            21, false, "1FB874BEE7276D28ECB2C9B06E8A122EC4BCB4008161436CE474C257CBF49BD6", ""
        )
        private val SIMPLEX_CHAT = defaultRepository(
            "https://app.simplex.chat/fdroid/repo/", "SimpleX Chat",
            "SimpleX Chat official F-Droid repository.",
            21, false, "9F358FF284D1F71656A2BFAF0E005DEAE6AA14143720E089F11FF2DDCFEB01BA", ""
        )
        private val ANONYMOUS_MESSENGER = defaultRepository(
            "https://anonymousmessenger.ly/fdroid/repo", "Anonymous Messenger",
            "Official repository for Anonymous Messenger, a peer to peer private anonymous and secure messenger that works over TOR.",
            21, false, "E065F74B35C920C7713012F45A0C8AD4758DD2776CFF6214F7D8064B285C044B", ""
        )
        private val PI2P = defaultRepository(
            "https://fdroid.i2pd.xyz/fdroid/repo", "PurpleI2P",
            "A repository of I2P apps.",
            21, false, "2B9564B0895EEAC039E854C6B065291B01E6A9CA02939CEDD0D35CF44BEE78E0", ""
        )
        private val UNOFFICIAL_FIREFOX = defaultRepository(
            "https://rfc2822.gitlab.io/fdroid-firefox/fdroid/repo", "Unofficial Firefox",
            "An unofficial repository with some of the most well known FOSS apps not on F-Droid.",
            21, false, "8F992BBBA0340EFE6299C7A410B36D9C8889114CA6C58013C3587CDA411B4AED", ""
        )
        private val BEOCODE = defaultRepository(
            "https://fdroid.beocode.eu/fdroid/repo", "BeoCode Repo",
            "An Fdroid repo for apps developed by BeoCode.",
            21, false, "28360DDEBA00922B156A9B03B5C96FEA39D239F88F1FBFA5A8157291749AA05A", ""
        )
        private val UMBRELLA = defaultRepository(
            "https://secfirst.org/fdroid/repo", "Umbrella",
            "Security advices, tutorials, tools etc..",
            21, false, "39EB57052F8D684514176819D1645F6A0A7BD943DBC31AB101949006AC0BC228", ""
        )
        private val WIND = defaultRepository(
            "https://guardianproject-wind.s3.amazonaws.com/fdroid/repo", "Wind Project",
            "A collection of interesting offline/serverless apps.",
            21, false, "182CF464D219D340DA443C62155198E399FEC1BC4379309B775DD9FC97ED97E1", ""
        )
        private val PATCHED = defaultRepository(
            "https://thecapslock.gitlab.io/fdroid-patched-apps/fdroid/repo", "Patched Apps",
            "A collection of patched applications to provide better compatibility, privacy etc..",
            21, false, "313D9E6E789FF4E8E2D687AAE31EEF576050003ED67963301821AC6D3763E3AC", ""
        )
        private val ELEMENT_DEV_FDROID = defaultRepository(
            "https://fdroid.krombel.de/element-dev-fdroid/fdroid/repo/",
            "Unofficial Element-FDroid-dev",
            "An unofficial repository with Element-dev builds for devices with F-Droid.",
            21,
            false,
            "FD146EF30FA9F8F075BDCD9F02F069D22061B1DF7CC90E90821750A7184BF53D",
            ""
        )
        private val ELEMENT_DEV_GPLAY = defaultRepository(
            "https://fdroid.krombel.de/element-dev-gplay/fdroid/repo/",
            "Unofficial Element-GPlay-dev",
            "An unofficial repository with Element-dev builds for devices with Google Service.",
            21,
            false,
            "5564AB4D4BF9461AF7955449246F12D7E792A8D65165EBB2C0E90E65E77D5095",
            ""
        )
        private val SESSION = defaultRepository(
            "https://fdroid.getsession.org/fdroid/repo/",
            "Session Messenger",
            "A mirror repository for Session messenger.",
            21,
            false,
            "DB0E5297EB65CC22D6BD93C869943BDCFCB6A07DC69A48A0DD8C7BA698EC04E6",
            ""
        )
        private val CALYX_OS = defaultRepository(
            "https://fdroid-repo.calyxinstitute.org/fdroid/repo", "Calyx OS Repo",
            "The official Calyx Labs F-Droid repository.",
            21,
            false,
            "5DA90117C91B0011AE44314CCC456CDFE406FBCE3BF880072FD7C0B073E20DF3",
            ""
        )
        private val THREEMA = defaultRepository(
            "https://releases.threema.ch/fdroid/repo",
            "Official Threema repository.",
            "The official repository to get Threema's builds.",
            21,
            false,
            "5734E753899B25775D90FE85362A49866E05AC4F83C05BEF5A92880D2910639E",
            ""
        )
        private val REVOLT = defaultRepository(
            "https://fdroid.revolt.chat/repo/",
            "Official Revolt repository.",
            "The official repository to get Revolt's builds for Android.",
            21,
            false,
            "0A9D2F61C8659801711E22177862F84C8134966F427973037A8FDFACFF07C4F2",
            ""
        )
        private val GITJOURNAL = defaultRepository(
            "https://gitjournal.io/fdroid/repo",
            "Official GitJournal repository.",
            "The official repository to get GitJournal's builds for Android.",
            21,
            false,
            "E2EE4AA4380F0D3B3CF81EB17F5E48F827C3AA77122D9AD330CC441650894574",
            ""
        )
        private val CRYPTOMATOR = defaultRepository(
            "https://static.cryptomator.org/android/fdroid/repo",
            "Cryptomator",
            "The official repository for Cryptomator.",
            21,
            false,
            "F7C3EC3B0D588D3CB52983E9EB1A7421C93D4339A286398E71D7B651E8D8ECDD",
            ""
        )
        private val TWIN_HELIX = defaultRepository(
            "https://fdroid.twinhelix.com/fdroid/repo",
            "TwinHelix's Signal-FOSS",
            "A fork of Signal for Android with proprietary Google binary blobs removed. Uses OpenStreetMap for maps and a websocket server connection, instead of Google Maps and Firebase Cloud Messaging.",
            21,
            false,
            "7B03B0232209B21B10A30A63897D3C6BCA4F58FE29BC3477E8E3D8CF8E304028",
            ""
        )
        private val STACK_WALLET = defaultRepository(
            "https://fdroid.stackwallet.com",
            "Stack Wallet",
            "An open-source, non-custodial, privacy-preserving cryptocurrency wallet.",
            21,
            false,
            "764B4262F75750A5F620A205CEE2886F18635FBDA18DF40758F5A1A45A950F84",
            ""
        )
        private val C_GEO = defaultRepository(
            "https://fdroid.cgeo.org",
            "c:geo",
            "An open-source, full-featured, always ready-to-go Geocaching application. This is the stable channel.",
            21,
            false,
            "370BB4D550C391D5DCCB6C81FD82FDA4892964764E085A09B7E075E9BAD5ED98",
            ""
        )
        private val C_GEO_NIGHTLY = defaultRepository(
            "https://fdroid.cgeo.org/nightly",
            "c:geo nightly",
            "An open-source, full-featured, always ready-to-go Geocaching application. This is the nightly channel.",
            21,
            false,
            "370BB4D550C391D5DCCB6C81FD82FDA4892964764E085A09B7E075E9BAD5ED98",
            ""
        )
        private val PETER_CXY = defaultRepository(
            "https://fdroid.typeblog.net/",
            "PeterCxy's Shelter repo",
            "Shelter is a Free and Open-Source (FOSS) app that leverages the \"Work Profile\" feature of Android to provide an isolated space that you can install or clone apps into.",
            21,
            false,
            "1A7E446C491C80BC2F83844A26387887990F97F2F379AE7B109679FEAE3DBC8C",
            ""
        )
        private val JAK_LINUX = defaultRepository(
            "https://jak-linux.org/fdroid/repo",
            "Julian Andres Klode's repo",
            "The official repository for DNS66.",
            21,
            false,
            "C00A81E44BFF606530C4C7A2137BAC5F1C03D2FDEF6DB3B84C71386EA9BFD225",
            ""
        )
        private val INVISV = defaultRepository(
            "https://fdroid.invisv.com/",
            "INVISV F-Droid Repo",
            "The official F-Droid repository for INVISV Android apps.",
            21,
            false,
            "EE79926D09C88A49D6CCE9EE2D79B950AE269FDB19240F8DD6A8AC658BEDF83A",
            ""
        )
        private val MONERUJO = defaultRepository(
            "https://f-droid.monerujo.io/fdroid/repo",
            "Official Monerujo F-Droid repo",
            "The official Monerujo monero wallet F-Droid repo.",
            21,
            false,
            "A82C68E14AF0AA6A2EC20E6B272EFF25E5A038F3F65884316E0F5E0D91E7B713",
            ""
        )
        private val IODE = defaultRepository(
            "https://raw.githubusercontent.com/iodeOS/fdroid/master/fdroid/repo",
            "iodéOS F-Droid repo",
            "The official iodéOS F-Droid repo.",
            21,
            false,
            "EC43610D9ACCA5D2426EB2D5EB74331930014DE79D3C3ACBC17DFE58AA12605F",
            ""
        )
        private val SPIRIT_CROC = defaultRepository(
            "https://s2.spiritcroc.de/fdroid/repo",
            "SpiritCroc's F-Droid repository",
            "While some of my apps are available from the official F-Droid repository, I also maintain my own repository for a small selection of apps. These might be forks of other apps with only minor changes, or apps that are not published on the Play Store for other reasons. In contrast to the official F-Droid repos, these might also include proprietary libraries, e.g. for push notifications.",
            21,
            false,
            "6612ADE7E93174A589CF5BA26ED3AB28231A789640546C8F30375EF045BC9242",
            ""
        )
        private val SPIRIT_CROC_TEST = defaultRepository(
            "https://s2.spiritcroc.de/testing/fdroid/repo",
            "SpiritCroc's Test F-Droid repository",
            "SpiritCroc.de Test Builds",
            21,
            false,
            "52D03F2FAB785573BB295C7AB270695E3A1BDD2ADC6A6DE8713250B33F231225",
            ""
        )
        private val DIVEST_OS_UNOFFICIAL = defaultRepository(
            "https://divestos.org/apks/unofficial/fdroid/repo/",
            "DivestOS Unofficial F-Droid repo",
            "This repository contains unofficial builds of open source apps that are not included in the other repos.",
            21,
            false,
            "A18CDB92F40EBFBBF778A54FD12DBD74D90F1490CB9EF2CC6C7E682DD556855D",
            ""
        )
        private val FUNKWHALE = defaultRepository(
            "https://fdroid.funkwhale.audio/fdroid/repo",
            "Funkwhale F-Droid repo",
            "This main source to install Funkwhale Android client.",
            21,
            false,
            "103063BC7189C91CE727DBF8266B07662518096E1686B6A088253933A3D0788F",
            ""
        )
        private val DIVOLT = defaultRepository(
            "https://fdroid.ggtyler.dev/",
            "Divolt F-Droid repo",
            "Repository of the fork of Revolt's Android TWA for Divolt, a self-hosted instance of Revolt",
            21,
            false,
            "12D4F647710DBB0FAFDE0FBE9C2127ECB53CFE0E8B1CB2DE56B704FCE330A0F8",
            ""
        )
        private val CROMITE = defaultRepository(
            "https://www.cromite.org/fdroid/repo/",
            "Cromite Browser",
            "Repository of the fork of Bromite, the privacy focused Chromium fork.",
            21,
            false,
            "49F37E74DEE483DCA2B991334FB5A0200787430D0B5F9A783DD5F13695E9517B",
            ""
        )
        private val ANIYOMI = defaultRepository(
            "https://fdroid.aniyomi.org",
            "Aniyomi",
            "Fork of Tachiyomi for anime",
            21,
            false,
            "2A01E80EBB8B50B5D4C0BF7DF45F12479C475F34B3F3F5AA57975C7E1BC0B9C3",
            ""
        )
        private val KOYU = defaultRepository(
            "https://fdroid.koyu.space/fdroid/repo",
            "Koyu Space",
            "FOSS apps of the koyu.space global network",
            21,
            false,
            "0ACF19C0ACEA755934E00676D52EB2F9D68F300C052A565B459FE2F5E98D237E",
            ""
        )
        private val KUSCHKU = defaultRepository(
            "https://repo.kuschku.de/fdroid/repo",
            "Kuschku",
            "Official Repository for all kuschku.de applications, including Quasseldroid",
            21,
            false,
            "A0CBC2C29E38ED9542F86A1188412A60C5A756FC4D7A31C4C622242D7AD021F2",
            ""
        )
        private val KVAESITSO = defaultRepository(
            "https://fdroid.mm20.de/repo",
            "Kvaesitso Launcher",
            "Apps developed and distributed by MM20",
            21,
            false,
            "156FBAB952F6996415F198F3F29628D24B30E725B0F07A2B49C3A9B5161EEE1A",
            ""
        )
        private val ETOPA = defaultRepository(
            "https://ltheinrich.de/fdroid/repo",
            "Etopa(OTP app)",
            "Time-based one-time password authenticator",
            21,
            false,
            "B90FC7691EC5BE977DCBBCB18C3984C794CCAFA5BB8712ED2D64F9FD8703B636",
            ""
        )
        private val METATRANS_APPS = defaultRepository(
            "https://fdroid.metatransapps.com/fdroid/repo",
            "Metatrans Apps",
            "Chess & Educational Games",
            21,
            false,
            "214027CD55300B837A93B43717B190DD4867CDB20FAABD8853DEF55BD0FF6A0B",
            ""
        )
        private val GADGETBRIDGE = defaultRepository(
            "https://freeyourgadget.codeberg.page/fdroid/repo",
            "Gadgetbridge nightly apps",
            "Gadgetbridge repository of nightly releases",
            21,
            false,
            "CD381ECCC465AB324E21BCC335895615E07E70EE11E9FD1DF3C020C5194F00B2",
            ""
        )
        private val GROBOX = defaultRepository(
            "https://grobox.de/fdroid/repo",
            "Grobox Testing Repo ",
            " F-Droid Maintainer Torsten Grote‘s Testing repo",
            21,
            false,
            "28E14FB3B280BCE8FF1E0F8E82726FF46923662CECFF2A0689108CE19E8B347C",
            ""
        )
        private val FAIRFAX = defaultRepository(
            "https://julianfairfax.gitlab.io/fdroid-repo/fdroid/repo",
            "Julian Fairfax's F-Droid Repo",
            "Repository for installing apps more easily (Proton, GrapheneOS).",
            21,
            false,
            "83ABB548CAA6F311CE3591DDCA466B65213FD0541352502702B1908F0C84206D",
            ""
        )
        private val ZIMBELSTERN = defaultRepository(
            "https://zimbelstern.eu/fdroid/repo",
            "Zimbelstern's F-Droid repository",
            "This is the official repository of apps from zimbelstern.eu",
            21,
            false,
            "285158DECEF37CB8DE7C5AF14818ACBF4A9B1FBE63116758EFC267F971CA23AA",
            ""
        )
        private val FUTO = defaultRepository(
            "https://app.futo.org/fdroid/repo",
            "FUTO F-Droid Repo",
            "Software created by FUTO",
            21,
            false,
            "39D47869D29CBFCE4691D9F7E6946A7B6D7E6FF4883497E6E675744ECDFA6D6D",
            ""
        )
        private val CAKE_WALLET = defaultRepository(
            "https://fdroid.cakelabs.com/fdroid/repo",
            "Cake Wallet F-Droid Repo",
            "Official F-Droid repository for Cake Labs applications",
            21,
            false,
            "EA44EFAEE0B641EE7A032D397D5D976F9C4E5E1ED26E11C75702D064E55F8755",
            ""
        )
        private val RBOARD = defaultRepository(
            "https://raw.githubusercontent.com/GboardThemes/Repo/master/repo",
            "Rboard Theme Manager repository",
            "Download themes and enable hidden Gboard features",
            21,
            false,
            "F6910227B3A8294F9F0739D9FC1A6A2EB27A041276DD0A1CA531318D680B6915",
            ""
        )
        private val INVIZBOX = defaultRepository(
            "https://update.invizbox.com/fdroid/repo",
            "InvizBox F-Droid Repository",
            "This is a repository of InvizBox apps to be used with F-Droid. Applications in this repository are official binaries built by the original application developers.",
            21,
            false,
            "FFA1A810B48608135EEFD7C692F2306172FB578549C4111A70266374A119D189",
            ""
        )
        private val LAGRANGE = defaultRepository(
            "https://skyjake.github.io/fdroid/repo",
            "Lagrange Pre-Release",
            "Testing alpha/beta builds of the Lagrange Gemini Browser.",
            21,
            false,
            "46AEA2F2D86047AD65DA955126C6532F79B05AF2BFEFFC5CF1B467A79E686F86",
            ""
        )
        private val ROHIT = defaultRepository(
            "https://thedoc.eu.org/fdroid/repo",
            "Rohit's F-Droid Repository",
            "This is a repository for personal apps built by Rohit.",
            21,
            false,
            "B1358F5B942E5676B2935B83F39E3BAA363F3FDA9E53DB62113551D14B09A173",
            ""
        )
        private val TAGESSCHAU = defaultRepository(
            "https://fdroid.tagesschau.de/repo",
            "Tagesschau F-Droid Repository",
            "Die F-Droid Repository für die Tagesschau.",
            21,
            false,
            "D7E4043234FCB5EAACB1F4D43759A0BF2ED412DC718B8FBB07444078C6E0070C",
            ""
        )
        private val SAUNAREPO = defaultRepository(
            "https://repo.the-sauna.icu/repo",
            "The sauna F-Droid Repository",
            "Minimal F-Droid repo for the sauna & other (beta, unreleased / lost) apps.",
            21,
            false,
            "4420FFD2354D0E08ACB2F1F478E06A4200B3CFF5F55515B08E8308DBD2B6EFCE",
            ""
        )
        private val LIBRECHURCH = defaultRepository(
            "https://repo.librechurch.org/fdroid/repo",
            "F-Droid Repo LUKi e.V.",
            "Ein Projekt zur Verstärkung digitaler Freiheit im digitalen Kontext.",
            21,
            false,
            "281049CDA52EAE483EE3E3137DA232ED48B14EFFD6ED6D331D522293CC0E67C5",
            ""
        )
        private val MOBILSICHER = defaultRepository(
            "https://repo.mobilsicher.de/fdroid/repo",
            "mobilsicher Apps Repo",
            "Dies ist das mobilsicher F-Droid Repo der Ethischen Apps, die durch das ITUJ e.V. veröffentlicht wurden. Die Apps in dem Repo wurden von den App-Betreibern zur Verfügung gestellt, um sie der Öffentlichkeit zugänglich zu machen.",
            21,
            false,
            "D8EE660B3812AA2DF68D8A9740C02B8C5315C3BB0911E3C223A527A3C7A97495",
            ""
        )
        private val LUBL = defaultRepository(
            "https://f.lubl.de/repo",
            "lubl fdroid repo",
            "This is a repository of apps to be used with F-Droid. Applications in this repository are official binaries built by the original application developers.",
            21,
            false,
            "C77FF604CD54E84A3ABDDFF17301E5753451BA8476EEDF438FF25554D2D6A0BE",
            ""
        )
        private val PIXELFED = defaultRepository(
            "https://fdroid.pixelfed.net/fdroid/repo",
            "Pixelfed F-Droid Repo",
            "This is a repository of official Pixelfed apps.",
            21,
            false,
            "FAE6C2292348F0BF910397A08916CCF4B2601B010327768B314951CD300DCA6E",
            ""
        )
        private val VIDELIBRI = defaultRepository(
            "https://fdroid.videlibri.de/repo",
            "VideLibri F-Droid Repo",
            "An app to access library catalogs and OPACs from your local device, e.g. for (automatic) renewing of all lend books or searching for other books available in the library.",
            21,
            false,
            "388988E3A53B6F91D1AE39784EA4B5F1A12B22D34F8E3363E5C7F70A1C57A6F4",
            ""
        )
        private val WOZ = defaultRepository(
            "https://fdroid.woz.ch/fdroid/repo",
            "WOZ App auf F‑Droid",
            "The F-droid repository for the swiss newspaper, WOZ.",
            21,
            false,
            "F19BB95E4DD3B74906E2EF0BA41EA6F34AB82C44F78E1B9505FE207DD5E60CC2",
            ""
        )
        private val NAILYK = defaultRepository(
            "https://releases.nailyk.fr/repo",
            "nailyk's repository",
            "This repository is managed by nailyk to keep some app up to date, provide upgrade on some unmaintained official fdroid repo.",
            21,
            false,
            "05F26958DE412482FC8681B4B34EECA37FC064DF98B8EFDC98ECEBAB8584F078",
            ""
        )
        private val OBFUSK = defaultRepository(
            "https://obfusk.dev/fdroid/repo",
            "Fay's f-droid repo",
            "Fay Stegerman's repository with her apps.",
            21,
            false,
            "2A21B7FFC93B878724B1991C05DAE113C72B93A556C193F49B5D3342884798B7",
            ""
        )
        private val MAXXIS = defaultRepository(
            "https://pili.qi0.de/fdroid/repo",
            "Maxxis F-Droid Repo",
            "This is the F-Droid Repo for maxxis apps.",
            21,
            false,
            "83161D8D5EC84BA32666ECE62E40D578342CAD3B03EAEECA2E75E396125FDAA0",
            ""
        )
        private val TWOBR = defaultRepository(
            "https://raw.githubusercontent.com/2br-2b/Fdroid-repo/master/fdroid/repo",
            "2BR's F-Droid Repo",
            "An Fdroid repository for the various apps 2BR make.",
            21,
            false,
            "90B5B0DDE20A84FB42CB960F41D279B0D3BB86578A42E773364A5534C0E8D27A",
            ""
        )
        private val NUCLEUS = defaultRepository(
            "https://raw.githubusercontent.com/nucleus-ffm/Nucleus-F-Droid-Repo/master/fdroid/repo",
            "Nucleus' F-Droid Repo",
            "An Fdroid repository for the various apps Nucleus make.",
            21,
            false,
            "A32DE9127A6961C5BEBF412C2128312CDFE70F2D7AD444091538432694B776FF",
            ""
        )
        private val XARANTOLUS = defaultRepository(
            "https://raw.githubusercontent.com/xarantolus/fdroid/main/fdroid/repo",
            "xarantolus' F-Droid Repo",
            "This repository hosts an F-Droid repo for xarantolus apps.",
            21,
            false,
            "080898AE4309AECEB58915E43A4B7C4A3E2CDA40C91738E2C02F58339AB2FBD7",
            ""
        )
        private val ONIONSHARE_NIGHTLY = defaultRepository(
            "https://raw.githubusercontent.com/onionshare/onionshare-android-nightly/master/fdroid/repo",
            "OnionShare Nightly F-Droid Repo",
            "Onionshare insecure nightly builds (for testing only!) are available in this F-Droid repository.",
            21,
            false,
            "7E04F902940A2AEDAC30E491A5CE7ADCC74A3F73B43459E4448222F3EEE629EF",
            ""
        )
        private val OBERNBERGER = defaultRepository(
            "https://codeberg.org/florian-obernberger/fdroid-repo/raw/branch/main/repo",
            "Florian Obernberger's F-Droid Repo",
            "This is Florian's personal F-Droid repository.",
            21,
            false,
            "5E9181C818BD1D28E8642821971C569438DF632E671505FD6E9DB6940722B56F",
            ""
        )
        private val SYLKEVICIOUS = defaultRepository(
            "https://codeberg.org/silkevicious/apkrepo/raw/branch/master/fdroid/repo",
            "Sylke Vicious's F-Droid Repo",
            "This is silkevicious's personal F-Droid repository.",
            21,
            false,
            "DFDB0A58E78704CAEB609389B81AB2BE6A090662F860635D760E76ACBC700AF8",
            ""
        )
        private val INSPORATION = defaultRepository(
            "https://jhass.github.io/insporation/fdroid/repo",
            "insporation* Repository",
            "Repository for builds of the insporation* Android client for diaspora*.",
            21,
            false,
            "EC792A58B39DF9FBB466FB100E30E3842F229FDBC6E28D32C417F6A5B30ECCAE",
            ""
        )
        private val RWTH = defaultRepository(
            "https://app.rwth-aachen.de/fdroid/repo",
            "RWTH Aachen University Repository",
            "The FDroid repository for apps of RWTH Aachen University.",
            21,
            false,
            "ACD7C554DED6B9A80BF43F7D766EE75C07DEBB89E83AB55799B94234AC05044C",
            ""
        )
        private val F_DROID_CLASSIC = defaultRepository(
            "https://bubu1.eu/fdroidclassic/fdroid/repo",
            "F-Droid Classic Repository",
            "The FDroid Classic beta versions repository.",
            21,
            false,
            "5187CFD99F084FFAB2AD60D9D10B39203B89A46DD4862397FE1B1A4F3D46627A",
            ""
        )
        private val OFFICIAL_I2P = defaultRepository(
            "https://eyedeekay.github.io/fdroid/repo",
            "I2P F-Droid Repo",
            "This is a repository of official I2P apps.",
            21,
            false,
            "22658CC69F48D63F63C3D64E2041C81714E2749F3F6E5445C825297A00DDC5B6",
            ""
        )
        private val JUWELERKASSA = defaultRepository(
            "https://juwelierkassa.at/fdroid/repo",
            "Juwelierkassa F-Droid Repo",
            "This is a repository for Juwelierkassa apps.",
            21,
            false,
            "930270248A02202D517518474D0C0215070A537DE3681D8721D84A9B1693915E",
            ""
        )
        private val KAFFEEMITKOFFEIN = defaultRepository(
            "https://kaffeemitkoffein.de/fdroid/repo",
            "kaffeemitkoffein.de F-Droid Repo",
            "This is a repository of apps to be used with F-Droid.",
            21,
            false,
            "2F275CB83735FE7975449CE800CA8407B5019D5F7B6ABCB30F3651C520A37261",
            ""
        )
        private val LTTRS = defaultRepository(
            "https://ltt.rs/fdroid/repo",
            "Ltt.rs F-Droid Repo",
            "This repository contains the latest version of Ltt.rs for Android build directly by the developer.",
            21,
            false,
            "9C2E57C85C279E5E1A427F6E87927FC1E2278F62D61D7FCEFDE9346E568CCF86",
            ""
        )
        private val LUBLIN = defaultRepository(
            "https://lublin.se/fdroid/repo",
            "Lublin's F-Droid Repo",
            "This repository is primarily used for distributing frequent releases of apps that Lublin maintains.",
            21,
            false,
            "4FE75AB58C310E9778FBA716A0D1D66E8F49F697737BC0EE2437AEAE278CF64C",
            ""
        )
        private val CLOUDBURST = defaultRepository(
            "https://c10udburst.github.io/fdroid/repo",
            "Cloudburst's F-Droid Repo",
            "This is Cloudburst's personal F-Droid repository.",
            21,
            false,
            "0E2D249AB2545EC52DCF67AB43464FB2F7B11EEC71F6D8891108FDD8034A58A5",
            ""
        )
        private val HUIZENGEK = defaultRepository(
            "https://repo.vitune.app/fdroid/repo/",
            "Huizengek's F-Droid Repo",
            "This is Huizengek's personal F-Droid repository.",
            21,
            false,
            "A9F4730F35858B40CD7ED86E46030644120FB5DFBC1B3366D05CE38A7BDB5C79",
            ""
        )
        private val F5A = defaultRepository(
            "https://f5a.torus.icu/fdroid/repo",
            "Fcitx 5 For Android F-Droid Repo",
            "Out-of-tree fcitx5-android plugins.",
            21,
            false,
            "5D87CE1FAD3772425C2A7ED987A57595A20B07543B9595A7FD2CED25DFF3CF12",
            ""
        )
        private val IRONFOX = defaultRepository(
            "https://fdroid.ironfoxoss.org/fdroid/repo",
            "IronFox",
            "The official repository for IronFox: A privacy and security-oriented Firefox-based browser for Android.",
            21,
            false,
            "C5E291B5A571F9C8CD9A9799C2C94E02EC9703948893F2CA756D67B94204F904",
            ""
        )
        private val BRAVE = defaultRepository(
            "https://brave-browser-apk-release.s3.brave.com/fdroid/repo",
            "Brave Browser",
            "The official repository for Brave Browser: A privacy and security-oriented Chromium-based browser for Android.",
            21,
            false,
            "3C60DE135AA19EC949E998469C908F7171885C1E2805F39EB403DDB0F37B4BD2",
            ""
        )
        private val BREEZY = defaultRepository(
            "https://breezy-weather.github.io/fdroid-repo/fdroid/repo",
            "Breezy Weather",
            "The F-Droid repo for Breezy Weather.",
            21,
            false,
            "3480A7BB2A296D8F98CB90D2309199B5B9519C1B31978DBCD877ADB102AF35EE",
            ""
        )
        private val THUNDERBIRD = defaultRepository(
            "https://thunderbird.github.io/fdroid-thunderbird/repo",
            "Thunderbird F-Droid repository",
            "This repository hosts Android Apps provided by Thunderbird.",
            21,
            false,
            "8B86E5D48983F0875F7EB7A1B2F91B225EE5B997E463E3D63D0E2556E53666BE",
            ""
        )
        private val AXPOS = defaultRepository(
            "https://apps.axpos.org/repo/",
            "AXP.OS",
            "Out-of-band updates, for the AOSmium based System Webview but also provide other apps build/made by the AXP.OS project.",
            21,
            false,
            "569A3FA3E603D68F214A468FB8A368FFBE5F281F57C6CD4B836057318BC37798",
            ""
        )

        val defaultRepositories = listOf(
            F_DROID, IZZY,
            GUARDIAN, ONIONSHARE_NIGHTLY, MICRO_G,
            CROMITE, IRONFOX, BRAVE, THUNDERBIRD,
            NEWPIPE, BITWARDEN, GITJOURNAL,
            CALYX_OS, CALYX_OS_TEST, IODE, AXPOS,
            KDE_RELEASE, KDE_NIGHTLY, NANODROID, NETSYMS,
            FUTO, KVAESITSO, C_GEO, C_GEO_NIGHTLY, COLLABORA,
            PIXELFED, VIDELIBRI, GADGETBRIDGE, BREEZY,
            THREEMA, SESSION, MOLLY, BRIAR, ANONYMOUS_MESSENGER,
            FEDILAB, NETHUNTER, INSPORATION,
            SIMPLEX_CHAT, REVOLT, TWIN_HELIX,
            PI2P, OFFICIAL_I2P,
            F_DROID_CLASSIC,
            ETOPA, METATRANS_APPS,
            LTTRS, KAFFEEMITKOFFEIN,
            SAUNAREPO, PATCHED,
            UMBRELLA, CRYPTOMATOR,
            RWTH, TAGESSCHAU, WOZ, PETER_CXY,
            LAGRANGE, ROHIT, NAILYK, LUBL,
            SYLKEVICIOUS, OBERNBERGER, NUCLEUS,
            XARANTOLUS, TWOBR, OBFUSK, MAXXIS,
            JAK_LINUX, MONERUJO,
            SPIRIT_CROC, SPIRIT_CROC_TEST,
            KUSCHKU,
            STACK_WALLET,
            GROBOX, ZIMBELSTERN,
            LIBRECHURCH, JUWELERKASSA,
            CLOUDBURST, HUIZENGEK,
            F5A,
        )

        val addedReposV9 = listOf(
            SIMPLEX_CHAT,
        )

        val addedReposV10 = listOf(
            SESSION, THREEMA, CALYX_OS
        )

        val addedReposV11 = listOf(
            REVOLT, GITJOURNAL,
        )

        val addedReposV12 = listOf(
            CRYPTOMATOR, TWIN_HELIX
        )

        val addedReposV14 = listOf(
            ANONYMOUS_MESSENGER
        )

        val addedReposV15 = listOf(
            KDE_RELEASE, STACK_WALLET,
            C_GEO, C_GEO_NIGHTLY, PETER_CXY
        )

        val addedReposV17 = listOf(
            JAK_LINUX, C_GEO, C_GEO_NIGHTLY,
        )
        val addedReposV18 = listOf(
            MONERUJO, IODE, SPIRIT_CROC,
        )

        val addedReposV19 = listOf(
            CROMITE,
        )

        val addedReposV20 = listOf(
            SPIRIT_CROC_TEST, KUSCHKU, KVAESITSO,
            ETOPA, METATRANS_APPS, GADGETBRIDGE, FUTO,
            GROBOX, ZIMBELSTERN, PI2P,
        )

        val addedReposV21 = listOf(
            CAKE_WALLET, RBOARD,
        )

        val addedReposV22 = listOf(
            LAGRANGE, ROHIT, TAGESSCHAU,
            LTTRS, KAFFEEMITKOFFEIN, OFFICIAL_I2P, F_DROID_CLASSIC,
            RWTH, INSPORATION, SYLKEVICIOUS, OBERNBERGER,
            NUCLEUS, ONIONSHARE_NIGHTLY, XARANTOLUS, TWOBR,
            OBFUSK, MAXXIS, NAILYK, WOZ,
            VIDELIBRI, PIXELFED, LUBL,
            SAUNAREPO, LIBRECHURCH, JUWELERKASSA,
        )

        val addedReposV23 = listOf(
            CLOUDBURST, HUIZENGEK,
        )

        val archiveRepos = listOf(
            F_DROID_ARCHIVE,
            GUARDIAN_ARCHIVE,
        )

        val removedReposV28 = listOf(
            FUNKWHALE, DIVOLT, INVISV, BROMITE
        )

        val addedReposV29 = listOf(
            F5A
        )

        val addedReposV30 = listOf(
            IRONFOX
        )

        val removedReposV29 = listOf(
            DIVEST_OS, DIVEST_OS_UNOFFICIAL,
        )

        val removedReposV31 = listOf(
            MOBILSICHER
        )

        val addedReposV1102 = listOf(
            BRAVE, BREEZY
        )

        val addedReposV1107 = listOf(
            THUNDERBIRD, AXPOS
        )

        val removedReposV1107 = listOf(
            LUBLIN, FAIRFAX, BEOCODE,
            WIND, INVIZBOX, ANIYOMI, KOYU,
            UNOFFICIAL_FIREFOX, ELEMENT_DEV_GPLAY, ELEMENT_DEV_FDROID,
        )
    }
}

data class LatestSyncs(
    var latest: Long,
    var latestAll: Long,
)
