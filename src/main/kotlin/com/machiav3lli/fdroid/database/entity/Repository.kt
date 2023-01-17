package com.machiav3lli.fdroid.database.entity

import android.util.Base64
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.machiav3lli.fdroid.ROW_ID
import com.machiav3lli.fdroid.TABLE_REPOSITORY_NAME
import com.machiav3lli.fdroid.utility.extension.text.nullIfEmpty
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URL
import java.nio.charset.Charset

@Entity(tableName = TABLE_REPOSITORY_NAME)
@Serializable
data class Repository(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = ROW_ID)
    var id: Long = 0,
    var address: String = "",
    var mirrors: List<String> = emptyList(),
    var name: String = "",
    var description: String = "",
    var version: Int = 21,
    var enabled: Boolean = false,
    var fingerprint: String = "",
    var lastModified: String = "",
    var entityTag: String = "",
    var updated: Long = 0L,
    var timestamp: Long = 0L,
    var authentication: String = "",
) {
    fun edit(address: String, fingerprint: String, authentication: String): Repository = apply {
        val changed = this.address != address || this.fingerprint != fingerprint
        this.lastModified = if (changed) "" else lastModified
        this.entityTag = if (changed) "" else entityTag
        this.address = address
        this.fingerprint = fingerprint
        this.authentication = authentication
    }

    fun update(
        mirrors: List<String>, name: String, description: String, version: Int,
        lastModified: String, entityTag: String, timestamp: Long,
    ): Repository = apply {
        this.mirrors = mirrors
        this.name = name
        this.description = description
        this.version = if (version >= 0) version else this.version
        this.lastModified = lastModified
        this.entityTag = entityTag
        this.updated = System.currentTimeMillis()
        this.timestamp = timestamp
    }

    fun enable(enabled: Boolean): Repository = apply {
        this.enabled = enabled
        this.lastModified = ""
        this.entityTag = ""
    }

    fun setAuthentication(username: String?, password: String?) {
        this.authentication = username?.let { u ->
            password
                ?.let { p ->
                    Base64.encodeToString(
                        "$u:$p".toByteArray(Charset.defaultCharset()),
                        Base64.NO_WRAP
                    )
                }
        }
            ?.let { "Basic $it" }.orEmpty()
    }

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

    fun toJSON() = Json.encodeToString(this)

    companion object {
        fun fromJson(json: String) = Json.decodeFromString<Repository>(json)

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

        private fun defaultRepository(
            address: String, name: String, description: String,
            version: Int, enabled: Boolean, fingerprint: String, authentication: String,
        ): Repository = Repository(
            0, address, emptyList(), name, description, version, enabled,
            fingerprint, "", "", 0L, 0L, authentication
        )

        private val F_DROID = defaultRepository(
            "https://f-droid.org/repo",
            "F-Droid",
            "The official F-Droid Free Software repository. " +
                    "Everything in this repository is always built from the source code.",
            21,
            true,
            "43238D512C1E5EB2D6569F4A3AFBF5523418B82E0A3ED1552770ABB9A9C9CCAB",
            ""
        )
        private val F_DROID_ARCHIVE =
            defaultRepository(
                "https://f-droid.org/archive",
                "F-Droid Archive",
                "The archive of the official F-Droid Free " +
                        "Software repository. Apps here are old and can contain known vulnerabilities and security issues!",
                21,
                false,
                "43238D512C1E5EB2D6569F4A3AFBF5523418B82E0A3ED1552770ABB9A9C9CCAB",
                ""
            )
        private val GUARDIAN = defaultRepository(
            "https://guardianproject.info/fdroid/repo",
            "Guardian Project Official Releases",
            "The " +
                    "official repository of The Guardian Project apps for use with the F-Droid client. Applications in this " +
                    "repository are official binaries built by the original application developers and signed by the same key as " +
                    "the APKs that are released in the Google Play Store.",
            21,
            false,
            "B7C2EEFD8DAC7806AF67DFCD92EB18126BC08312A7F2D6F3862E46013C7A6135",
            ""
        )
        private val GUARDIAN_ARCHIVE = defaultRepository(
            "https://guardianproject.info/fdroid/archive",
            "Guardian Project Archive",
            "The official " +
                    "repository of The Guardian Project apps for use with the F-Droid client. This contains older versions of " +
                    "applications from the main repository.",
            21,
            false,
            "B7C2EEFD8DAC7806AF67DFCD92EB18126BC08312A7F2D6F3862E46013C7A6135",
            ""
        )
        private val IZZY = defaultRepository(
            "https://apt.izzysoft.de/fdroid/repo", "IzzyOnDroid F-Droid Repo", "This is a " +
                    "repository of apps to be used with F-Droid the original application developers, taken from the resp. " +
                    "repositories (mostly GitHub). At this moment I cannot give guarantees on regular updates for all of them, " +
                    "though most are checked multiple times a week ", 21, true,
            "3BF0D6ABFEAE2F401707B6D966BE743BF0EEE49C2561B9BA39073711F628937A", ""
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
        private val UNGOOGLED_CHROMIUM = defaultRepository(
            "https://www.droidware.info/fdroid/repo", "Ungoogled Chromium",
            "Chromium sans dependency on Google web services. It also features some enhancments to privacy, control & transparency",
            21, false, "2144449AB1DD270EC31B6087409B5D0EA39A75A9F290DA62AC1B238A0EAAF851", ""
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
        private val KDE = defaultRepository(
            "https://cdn.kde.org/android/fdroid/repo", "KDE Android",
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
        private val FLUFFY_CHAT = defaultRepository(
            "https://fluffychat.im/repo/stable/repo/", "FluffyChat",
            "A repository to easily distribute the cute matrix messanger client.",
            21, false, "8E2637AEF6697CC6DD486AF044A6EE45B1A742AE3EF56566E748CDE8BC65C1FB", ""
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
        private val I2P = defaultRepository(
            "https://f-droid.i2p.io/repo/", "i2p",
            "A repository of I2P apps.",
            21, false, "22658CC69F48D63F63C3D64E2041C81714E2749F3F6E5445C825297A00DDC5B6", ""
        )
        private val UNOFFICIAL_FIREFOX = defaultRepository(
            "https://rfc2822.gitlab.io/fdroid-firefox/fdroid/repo", "Unofficial Firefox",
            "An unofficial repository with some of the most well known FOSS apps not on F-Droid.",
            21, false, "8F992BBBA0340EFE6299C7A410B36D9C8889114CA6C58013C3587CDA411B4AED", ""
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

        val defaultRepositories = listOf(
            F_DROID, F_DROID_ARCHIVE,
            GUARDIAN, GUARDIAN_ARCHIVE,
            IZZY,
            MICRO_G,
            BROMITE, UNGOOGLED_CHROMIUM,
            NEWPIPE, LIBRETRO, BITWARDEN, GITJOURNAL,
            CALYX_OS, CALYX_OS_TEST, DIVEST_OS, KDE, NANODROID,
            NETSYMS, FEDILAB, NETHUNTER,
            THREEMA, SESSION, MOLLY, BRIAR,
            FLUFFY_CHAT, SIMPLEX_CHAT, REVOLT,
            I2P, COLLABORA,
            ELEMENT_DEV_FDROID, ELEMENT_DEV_GPLAY,
            FROSTNERD, FROSTNERD_ARCHIVE,
            UNOFFICIAL_FIREFOX, PATCHED, WIND, UMBRELLA,
            CRYPTOMATOR, TWIN_HELIX
        )

        val addedReposV9 = listOf(
            FLUFFY_CHAT, SIMPLEX_CHAT, I2P,
            ELEMENT_DEV_FDROID, ELEMENT_DEV_GPLAY,
            FROSTNERD, FROSTNERD_ARCHIVE
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
    }
}
