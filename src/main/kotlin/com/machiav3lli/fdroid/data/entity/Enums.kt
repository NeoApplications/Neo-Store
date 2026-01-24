package com.machiav3lli.fdroid.data.entity

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.machiav3lli.fdroid.HELP_CHANGELOG
import com.machiav3lli.fdroid.HELP_CHANNEL
import com.machiav3lli.fdroid.HELP_LICENSE
import com.machiav3lli.fdroid.HELP_MATRIX
import com.machiav3lli.fdroid.HELP_SOURCECODE
import com.machiav3lli.fdroid.HELP_TELEGRAM
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.BracketsSquare
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CalendarPlus
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CalendarX
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Copyleft
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.GithubLogo
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ListBullets
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Megaphone
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.TagSimple
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.TelegramLogo

enum class InstallerType(@StringRes val titleResId: Int) {
    DEFAULT(R.string.default_installer),
    ROOT(R.string.root_installer),
    LEGACY(R.string.legacy_installer),
    AM(R.string.am_installer),
    SYSTEM(R.string.system_installer),
    SHIZUKU(R.string.shizuku_installer),
}

enum class Contrast(val themes: List<Preferences.Theme>) {
    NORMAL(
        listOf(
            Preferences.Theme.Light,
            Preferences.Theme.Dark,
            Preferences.Theme.Black
        )
    ),
    MEDIUM(
        listOf(
            Preferences.Theme.LightMediumContrast,
            Preferences.Theme.DarkMediumContrast,
            Preferences.Theme.BlackMediumContrast
        )
    ),
    HIGH(
        listOf(
            Preferences.Theme.LightHighContrast,
            Preferences.Theme.DarkHighContrast,
            Preferences.Theme.BlackHighContrast
        )
    )
}

enum class Order(@StringRes val titleResId: Int, val icon: ImageVector) {
    NAME(R.string.name, Phosphor.TagSimple),
    DATE_ADDED(R.string.date_added, Phosphor.CalendarX),
    LAST_UPDATE(R.string.date_updated, Phosphor.CalendarPlus)
}

enum class UpdateCategory(val id: Int) {
    ALL(0),
    UPDATED(1),
    NEW(2),
}

enum class AndroidVersion(override val valueString: String) : Preferences.EnumEnumeration {
    Unknown("Non"),
    Base("1.0"),
    BASE_1_1("1.1"),
    Cupcake("1.5"),
    Donut("1.6"),
    Eclair("2.0"),
    Eclair_0_1("2.0.1"),
    Eclair_MR1("2.1"),
    Froyo("2.2"),
    Gingerbread("2.3"),
    Gingerbread_MR1("2.3.3"),
    Honeycomb("3.0"),
    Honeycomb_MR1("3.1"),
    Honeycomb_MR2("3.2"),
    IceCreamSandwich("4.0"),
    IceCreamSandwich_MR1("4.0.3"),
    JellyBean("4.1"),
    JellyBean_MR1("4.2"),
    JellyBean_MR2("4.3"),
    KitKat("4.4"),
    KitKat_Watch("4.4W"),
    Lollipop("5.0"),
    Lollipop_MR1("5.1"),
    Marshmallow("6.0"),
    Nougat("7.0"),
    Nougat_MR1("7.1"),
    Oreo("8.0"),
    Oreo_MR1("8.1"),
    Pie("9"),
    Q("10"),
    R("11"),
    S("12.0"),
    S_V2("12.1"),
    Tiramisu("13"),
    UpsideDownCake("14"),
    VanillaIceCream("15"),
    Baklava("16"),
}


enum class TopDownloadType(val key: String, @StringRes val displayString: Int) {
    TOTAL_RECENT("_total", R.string.total_recent_downloads),
    TOTAL_ALLTIME("_total_alltime", R.string.total_alltime_downloads),
    NEO_STORE("Neo Store", R.string.trending_from_neostore),
    DROIDIFY("Droid-ify", R.string.trending_from_droidify),
    FDROID("F-Droid", R.string.trending_from_fdroid),
    FDROID_CLASSIC("F-Droid Classic", R.string.trending_from_fdroid_classic),
    FLICKY("Flicky", R.string.trending_from_flicky),
    UNKNOWN("_unknown", R.string.trending_from_unknown),
}

enum class Source {
    AVAILABLE,
    FAVORITES,
    SEARCH,
    SEARCH_INSTALLED,
    SEARCH_NEW,
    INSTALLED,
    UPDATES,
    UPDATED,
    NEW,
    NONE,
}

enum class Section {
    All,
    FAVORITE,
    NONE,
}

enum class Page {
    LATEST,
    EXPLORE,
    SEARCH,
    INSTALLED,
}

enum class ColoringState {
    Positive,
    Negative,
    Neutral,
}

enum class LinkRef(
    val icon: ImageVector,
    @StringRes val titleId: Int,
    val url: String,
) {
    Sourcecode(
        icon = Phosphor.GithubLogo,
        titleId = R.string.source_code,
        url = HELP_SOURCECODE,
    ),
    Changelog(
        icon = Phosphor.ListBullets,
        titleId = R.string.changelog,
        url = HELP_CHANGELOG,
    ),
    Channel(
        icon = Phosphor.Megaphone,
        titleId = R.string.about_channel,
        url = HELP_CHANNEL,
    ),
    Telegram(
        icon = Phosphor.TelegramLogo,
        titleId = R.string.group_telegram,
        url = HELP_TELEGRAM,
    ),
    Matrix(
        icon = Phosphor.BracketsSquare,
        titleId = R.string.group_matrix,
        url = HELP_MATRIX,
    ),
    License(
        icon = Phosphor.Copyleft,
        titleId = R.string.license,
        url = HELP_LICENSE,
    ),
}
