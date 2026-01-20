package com.machiav3lli.fdroid.utils

import android.Manifest
import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
import android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE
import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import android.content.pm.Signature
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.format.DateUtils
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.biometric.BiometricManager
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import androidx.fragment.app.FragmentManager
import com.machiav3lli.derdiedas.config.BuildConfig
import com.machiav3lli.fdroid.AM_PACKAGENAME
import com.machiav3lli.fdroid.AM_PACKAGENAME_DEBUG
import com.machiav3lli.fdroid.PREFS_LANGUAGE_DEFAULT
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.data.database.entity.EmbeddedProduct
import com.machiav3lli.fdroid.data.database.entity.Installed
import com.machiav3lli.fdroid.data.database.entity.Product
import com.machiav3lli.fdroid.data.database.entity.Release
import com.machiav3lli.fdroid.data.database.entity.Repository
import com.machiav3lli.fdroid.data.entity.AndroidVersion
import com.machiav3lli.fdroid.data.entity.Contrast
import com.machiav3lli.fdroid.data.entity.LinkType
import com.machiav3lli.fdroid.data.entity.PermissionGroup
import com.machiav3lli.fdroid.manager.work.DownloadWorker
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ArrowsClockwise
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.At
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Bug
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Copyleft
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.GlobeSimple
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Translate
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.User
import com.machiav3lli.fdroid.ui.dialog.LaunchDialog
import com.machiav3lli.fdroid.utils.extension.android.Android
import com.machiav3lli.fdroid.utils.extension.android.signerSHA256Signatures
import com.machiav3lli.fdroid.utils.extension.android.versionCodeCompat
import com.machiav3lli.fdroid.utils.extension.isInstalled
import com.machiav3lli.fdroid.utils.extension.text.hex
import com.machiav3lli.fdroid.utils.extension.text.nullIfEmpty
import com.topjohnwu.superuser.Shell
import io.ktor.http.HttpStatusCode
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuProvider
import rikka.sui.Sui
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

object Utils {
    fun PackageInfo.toInstalledItem(launcherActivities: List<Pair<String, String>> = emptyList()): Installed {
        return Installed(
            packageName,
            versionName.orEmpty(),
            versionCodeCompat,
            signerSHA256Signatures,
            applicationInfo?.flags?.and(ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM,
            launcherActivities
        )
    }

    fun calculateSHA256(signature: Signature): String {
        return MessageDigest.getInstance("SHA-256").digest(signature.toByteArray())
            .hex()
    }

    fun calculateSHA256(hexadecString: String): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(
                hexadecString
                    .chunked(2)
                    .mapNotNull { byteStr ->
                        try {
                            byteStr.toInt(16).toByte()
                        } catch (_: NumberFormatException) {
                            null
                        }
                    }
                    .toByteArray()
            ).hex()
    }

    fun startUpdate(
        packageName: String,
        installed: Installed?,
        products: List<Pair<EmbeddedProduct, Repository>>,
    ) {
        val productRepository = findSuggestedProduct(products, installed) { it.first }
        val selectedRelease = getCompatibleReleases(productRepository, installed)
            .getBestRelease()

        if (productRepository != null && selectedRelease != null) {
            DownloadWorker.enqueue(
                packageName,
                productRepository.first.product.label,
                productRepository.second,
                selectedRelease,
            )
        }
    }

    private fun getCompatibleReleases(
        productRepository: Pair<EmbeddedProduct, Repository>?,
        installed: Installed?
    ): List<Release> {
        val includeIncompatible = Preferences[Preferences.Key.IncompatibleVersions]
        val ignoreSigCheck = Preferences[Preferences.Key.DisableSignatureCheck]

        return productRepository?.first?.releases.orEmpty()
            .filter {
                it.repositoryId == productRepository?.second?.id
                        && (includeIncompatible || it.incompatibilities.isEmpty())
                        && (installed == null || it.signature in installed.signatures || ignoreSigCheck)
            }
            .sortedByDescending { it.versionCode }
    }

    private fun List<Release>.getBestRelease(): Release? {
        if (isEmpty()) return null
        if (size == 1) return first()

        return filter { it.platforms.contains(Android.primaryPlatform) }
            .minByOrNull { it.platforms.size }
            ?: maxByOrNull { it.platforms.size }
    }

    fun Context.setLanguage(): Configuration {
        var setLocalCode = Preferences[Preferences.Key.Language]
        if (setLocalCode == PREFS_LANGUAGE_DEFAULT) {
            setLocalCode = Locale.getDefault().toString()
        }
        val config = resources.configuration
        val sysLocale = config.locales[0]
        if (setLocalCode != sysLocale.toString() || setLocalCode != "${sysLocale.language}-r${sysLocale.country}") {
            val newLocale = getLocaleOfCode(setLocalCode)
            Locale.setDefault(newLocale)
            config.setLocale(newLocale)
        }
        return config
    }

    val languagesList: List<String>
        get() {
            val entryVals = arrayOfNulls<String>(1)
            entryVals[0] = PREFS_LANGUAGE_DEFAULT
            return entryVals.plus(BuildConfig.DETECTED_LOCALES.sorted()).filterNotNull()
        }

    fun translateLocale(locale: Locale): String {
        val country = locale.getDisplayCountry(locale)
        val language = locale.getDisplayLanguage(locale)
        return (language.replaceFirstChar { it.uppercase(Locale.getDefault()) }
                + (if (country.isNotEmpty() && country.compareTo(language, true) != 0)
            "($country)" else ""))
    }

    fun Context.getLocaleOfCode(localeCode: String): Locale = when {
        localeCode.isEmpty()
             -> resources.configuration.locales[0]

        localeCode.contains("-r")
             -> Locale(
            localeCode.substringBefore("-r"),
            localeCode.substringAfter("-r")
        )

        localeCode.contains("_")
             -> Locale(
            localeCode.substringBefore("_"),
            localeCode.substringAfter("_")
        )

        else -> Locale(localeCode)
    }

    /**
     * Checks if app is currently considered to be in the foreground by Android.
     */
    fun inForeground(): Boolean {
        val appProcessInfo = ActivityManager.RunningAppProcessInfo()
        ActivityManager.getMyMemoryState(appProcessInfo)
        val importance = appProcessInfo.importance
        return ((importance == IMPORTANCE_FOREGROUND) or (importance == IMPORTANCE_VISIBLE))
    }

    private val charactersToBeEscaped = Regex("""[\\${'$'}"`]""")

    fun quotePath(parameter: String): String =
        "\"${parameter.replace(charactersToBeEscaped) { "\\${it.value}" }}\""
}

fun <T> findSuggestedProduct(
    products: List<T>,
    installed: Installed?,
    extract: (T) -> EmbeddedProduct,
): T? {
    return products.maxWithOrNull(
        compareBy(
            {
                extract(it).compatible && (
                        installed == null ||
                                installed.signatures.intersect(extract(it).productSignatures.toSet())
                                    .isNotEmpty() ||
                                Preferences[Preferences.Key.DisableSignatureCheck]
                        )
            },
            { extract(it).versionCode },
        )
    )
}

val Context.isDarkTheme: Boolean
    get() = when (Preferences[Preferences.Key.Theme]) {
        is Preferences.Theme.Light,
        is Preferences.Theme.LightMediumContrast,
        is Preferences.Theme.LightHighContrast,
        is Preferences.Theme.DynamicLight,
             -> false

        is Preferences.Theme.Dark,
        is Preferences.Theme.DarkMediumContrast,
        is Preferences.Theme.DarkHighContrast,
        is Preferences.Theme.Black,
        is Preferences.Theme.BlackMediumContrast,
        is Preferences.Theme.BlackHighContrast,
        is Preferences.Theme.DynamicDark,
        is Preferences.Theme.DynamicBlack,
             -> true

        else -> resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }

val isBlackTheme: Boolean
    get() = when (Preferences[Preferences.Key.Theme]) {
        is Preferences.Theme.Black,
        is Preferences.Theme.BlackMediumContrast,
        is Preferences.Theme.BlackHighContrast,
        is Preferences.Theme.SystemBlack,
        is Preferences.Theme.DynamicBlack,
             -> true

        else -> false
    }

fun getGetThemeContrast(): Contrast = when (Preferences[Preferences.Key.Theme]) {
    is Preferences.Theme.LightMediumContrast,
    is Preferences.Theme.DarkMediumContrast,
    is Preferences.Theme.BlackMediumContrast,
         -> Contrast.MEDIUM

    is Preferences.Theme.LightHighContrast,
    is Preferences.Theme.DarkHighContrast,
    is Preferences.Theme.BlackHighContrast,
         -> Contrast.HIGH

    else -> Contrast.NORMAL
}

val isDynamicColorsTheme: Boolean
    get() = when (Preferences[Preferences.Key.Theme]) {
        is Preferences.Theme.Dynamic,
        is Preferences.Theme.DynamicLight,
        is Preferences.Theme.DynamicDark,
        is Preferences.Theme.DynamicBlack,
             -> true

        else -> false
    }

fun Context.showBatteryOptimizationDialog() {
    AlertDialog.Builder(this)
        .setTitle(R.string.ignore_battery_optimization_title)
        .setMessage(R.string.ignore_battery_optimization_message)
        .setPositiveButton(R.string.dialog_approve) { _, _ ->
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.data = ("package:" + this.packageName).toUri()
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(
                    this,
                    R.string.ignore_battery_optimization_not_supported,
                    Toast.LENGTH_LONG
                ).show()
                Preferences[Preferences.Key.IgnoreDisableBatteryOptimization] = true
            }
        }
        .show()
}

fun PackageManager.getLaunchActivities(packageName: String): List<Pair<String, String>> =
    queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0)
        .mapNotNull { resolveInfo -> resolveInfo.activityInfo }
        .filter { activityInfo -> activityInfo.packageName == packageName }
        .mapNotNull { activityInfo ->
            val label = try {
                activityInfo.loadLabel(this).toString()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
            label?.let { labelName ->
                Pair(
                    activityInfo.name,
                    labelName
                )
            }
        }
        .toList()

fun Context.onLaunchClick(installed: Installed, fragmentManager: FragmentManager) {
    if (installed.launcherActivities.size >= 2) {
        LaunchDialog(installed.packageName, installed.launcherActivities)
            .show(fragmentManager, LaunchDialog::class.java.name)
    } else {
        installed.launcherActivities.firstOrNull()
            ?.let { startLauncherActivity(installed.packageName, it.first) }
    }
}

fun Context.startLauncherActivity(packageName: String, name: String) {
    try {
        startActivity(
            Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LAUNCHER)
                .setComponent(ComponentName(packageName, name))
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Context.shareIntent(packageName: String, appName: String, repoWebUrl: String) {
    val shareIntent = Intent(Intent.ACTION_SEND)
    val extraText = when {
        repoWebUrl.isNotBlank()
            -> "${repoWebUrl.trimEnd('/')}/$packageName"

        else
            -> "https://f-droid.org/packages/${packageName}/"
    }

    shareIntent.type = "text/plain"
    shareIntent.putExtra(Intent.EXTRA_TITLE, appName)
    shareIntent.putExtra(Intent.EXTRA_SUBJECT, appName)
    shareIntent.putExtra(Intent.EXTRA_TEXT, extraText)

    startActivity(Intent.createChooser(shareIntent, "Where to Send?"))
}

fun Context.shareReleaseIntent(appName: String, address: String) {
    val shareIntent = Intent(Intent.ACTION_SEND)
    shareIntent.type = "text/plain"
    shareIntent.putExtra(Intent.EXTRA_TITLE, appName)
    shareIntent.putExtra(Intent.EXTRA_SUBJECT, appName)
    shareIntent.putExtra(Intent.EXTRA_TEXT, address)

    startActivity(Intent.createChooser(shareIntent, "Where to share?"))
}

fun Context.shareText(title: String, content: String) {
    val shareIntent = Intent(Intent.ACTION_SEND)
    shareIntent.type = "text/plain"
    shareIntent.putExtra(Intent.EXTRA_TITLE, title)
    shareIntent.putExtra(Intent.EXTRA_SUBJECT, title)
    shareIntent.putExtra(Intent.EXTRA_TEXT, content)

    startActivity(Intent.createChooser(shareIntent, "Where to share?"))
}

fun Int.dmReasonToHttpResponse() = when (this) {
    DownloadManager.ERROR_UNKNOWN             -> HttpStatusCode.NotImplemented
    DownloadManager.ERROR_FILE_ERROR          -> HttpStatusCode.Conflict
    DownloadManager.ERROR_UNHANDLED_HTTP_CODE -> HttpStatusCode.NotImplemented
    DownloadManager.ERROR_HTTP_DATA_ERROR     -> HttpStatusCode.BadRequest
    DownloadManager.ERROR_TOO_MANY_REDIRECTS  -> HttpStatusCode.GatewayTimeout
    DownloadManager.ERROR_INSUFFICIENT_SPACE  -> HttpStatusCode.InsufficientStorage
    DownloadManager.ERROR_DEVICE_NOT_FOUND    -> HttpStatusCode.NotFound
    DownloadManager.ERROR_CANNOT_RESUME       -> HttpStatusCode.RequestedRangeNotSatisfiable
    DownloadManager.ERROR_FILE_ALREADY_EXISTS -> HttpStatusCode.NotModified
    else                                      -> HttpStatusCode.OK
}

fun Context.openPermissionPage(packageName: String) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(
        Uri.fromParts("package", packageName, null)
    )
    startActivity(intent)
}

fun Product.generateLinks(context: Context): List<LinkType> {
    val links = mutableListOf<LinkType>()
    if (author.name.isNotEmpty() || author.web.isNotEmpty()) {
        links.add(
            LinkType(
                icon = Phosphor.User,
                title = author.name,
                link = author.web.nullIfEmpty()?.let(String::toUri)
            )
        )
    }
    author.email.nullIfEmpty()?.let {
        links.add(
            LinkType(
                Phosphor.At,
                context.getString(R.string.author_email),
                "mailto:$it".toUri()
            )
        )
    }
    translation.nullIfEmpty()?.let {
        links.add(
            LinkType(
                Phosphor.Translate,
                context.getString(R.string.translation),
                it.toUri()
            )
        )
    }
    links.addAll(licenses.map {
        LinkType(
            Phosphor.Copyleft,
            it,
            "https://spdx.org/licenses/$it.html".toUri()
        )
    })
    tracker.nullIfEmpty()?.let {
        links.add(
            LinkType(
                Phosphor.Bug,
                context.getString(R.string.bug_tracker),
                it.toUri()
            )
        )
    }
    changelog.nullIfEmpty()?.let {
        links.add(
            LinkType(
                Phosphor.ArrowsClockwise,
                context.getString(R.string.changelog),
                it.toUri()
            )
        )
    }
    web.nullIfEmpty()
        ?.let {
            links.add(
                LinkType(
                    Phosphor.GlobeSimple,
                    context.getString(R.string.project_website),
                    it.toUri()
                )
            )
        }
    return links
}

fun Release.generatePermissionGroups(context: Context): Map<PermissionGroup, List<PermissionInfo>> {
    val packageManager = context.packageManager
    return permissions
        .asSequence().mapNotNull {
            try {
                packageManager.getPermissionInfo(it, 0)
            } catch (e: Exception) {
                null
            }
        }
        .groupBy(PackageItemResolver::getPermissionGroup)
}

fun List<PermissionInfo>.getLabelsAndDescriptions(context: Context): List<String> {
    val localCache = PackageItemResolver.LocalCache()

    return map { permission ->
        val labelFromPackage =
            PackageItemResolver.loadLabel(context, localCache, permission)
        val label = labelFromPackage ?: run {
            val prefixes =
                listOf("android.permission.", "com.android.browser.permission.")
            prefixes.find { permission.name.startsWith(it) }?.let { prefix ->
                val transform = permission.name.substring(prefix.length)
                if (transform.matches("[A-Z_]+".toRegex())) {
                    transform.split("_")
                        .joinToString(separator = " ") { it.lowercase(Locale.US) }
                } else {
                    null
                }
            }
        }
        val description =
            PackageItemResolver.loadDescription(context, localCache, permission)
                ?.nullIfEmpty()?.let { if (it == permission.name) null else it }

        if (description.isNullOrEmpty()) (label ?: permission.name).toString()
        else "${label ?: permission.name}: $description"
    }
}

fun Context.getLocaleDateString(time: Long): String {
    val date = Date(time)
    val format = if (DateUtils.isToday(date.time)) DateUtils.FORMAT_SHOW_TIME else
        DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_SHOW_DATE
    return DateUtils.formatDateTime(this, date.time, format)
}

fun Collection<EmbeddedProduct>.matchSearchQuery(searchQuery: String): List<EmbeddedProduct> {
    if (searchQuery.isBlank()) return toList()
    val now = System.currentTimeMillis()
    return filter {
        listOf(
            it.product.label,
            it.product.packageName,
            it.product.author.name,
            it.product.summary,
            it.product.description
        )
            .any { literal ->
                literal.contains(searchQuery, true)
            }
    }.sortedByDescending {
        (if ("${it.product.label} ${it.product.packageName}".contains(
                searchQuery,
                true
            )
        ) 7 else 0) or
                (if (isDifferenceMoreThanOneYear(it.product.updated, now)) 0 else 3) or
                (if ("${it.product.summary} ${it.product.author.name}".contains(
                        searchQuery,
                        true
                    )
                ) 1 else 0)
    }
}

fun isDifferenceMoreThanOneYear(time1: Long, time2: Long): Boolean {
    val difference = abs(time1 - time2)
    val oneYearInMilliseconds = 365 * 24 * 60 * 60 * 1000L
    return difference > oneYearInMilliseconds
}

val Context.isRunningOnTV: Boolean
    get() = packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)

val currentTimestamp: String
    get() {
        val simpleDateFormat = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US)
        return simpleDateFormat.format(Date())
    }

val shellIsRoot: Boolean
    get() = Shell.getCachedShell()?.isRoot ?: Shell.getShell().isRoot

val Context.installedAM: Intent?
    get() = packageManager.getLaunchIntentForPackage(AM_PACKAGENAME)
        ?: packageManager.getLaunchIntentForPackage(AM_PACKAGENAME_DEBUG)

val Context.amInstalled: Boolean
    get() = installedAM != null

val Context.hasShizukuOrSui: Boolean
    get() = Android.sdk(Build.VERSION_CODES.O) &&
            (packageManager.isInstalled(ShizukuProvider.MANAGER_APPLICATION_ID) || Sui.isSui())

fun hasShizukuPermission(): Boolean =
    Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED

fun isShizukuRunning() = Shizuku.pingBinder()

fun Context.getHasSystemInstallPermission(): Boolean =
    ActivityCompat.checkSelfPermission(this, Manifest.permission.INSTALL_PACKAGES) ==
            PackageManager.PERMISSION_GRANTED

fun Context.isBiometricLockAvailable(): Boolean =
    BiometricManager.from(this).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) ==
            BiometricManager.BIOMETRIC_SUCCESS

fun Context.isBiometricLockEnabled(): Boolean =
    isBiometricLockAvailable() &&
            Preferences[Preferences.Key.ActionLockDialog] == Preferences.ActionLock.Biometric

fun getAndroidVersionName(versionCode: Int): String =
    AndroidVersion.entries.getOrNull(versionCode)?.valueString ?: "Unknown sdk: $versionCode"
