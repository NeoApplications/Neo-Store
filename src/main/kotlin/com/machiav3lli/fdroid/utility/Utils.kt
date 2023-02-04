package com.machiav3lli.fdroid.utility

import android.app.Activity
import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
import android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import android.content.pm.Signature
import android.content.res.Configuration
import android.net.Uri
import android.provider.Settings
import android.text.SpannableStringBuilder
import android.text.style.BulletSpan
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.text.util.Linkify
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.text.HtmlCompat
import androidx.core.text.util.LinkifyCompat
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavDestination
import com.machiav3lli.fdroid.BuildConfig
import com.machiav3lli.fdroid.PREFS_LANGUAGE_DEFAULT
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.database.entity.Installed
import com.machiav3lli.fdroid.database.entity.Product
import com.machiav3lli.fdroid.database.entity.Release
import com.machiav3lli.fdroid.database.entity.Repository
import com.machiav3lli.fdroid.entity.LinkType
import com.machiav3lli.fdroid.entity.PermissionGroup
import com.machiav3lli.fdroid.service.Connection
import com.machiav3lli.fdroid.service.DownloadService
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ArrowsClockwise
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.At
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Bug
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Copyleft
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.GlobeSimple
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.User
import com.machiav3lli.fdroid.ui.compose.utils.Callbacks
import com.machiav3lli.fdroid.ui.dialog.LaunchDialog
import com.machiav3lli.fdroid.ui.navigation.NavItem
import com.machiav3lli.fdroid.utility.extension.android.Android
import com.machiav3lli.fdroid.utility.extension.android.singleSignature
import com.machiav3lli.fdroid.utility.extension.android.versionCodeCompat
import com.machiav3lli.fdroid.utility.extension.text.hex
import com.machiav3lli.fdroid.utility.extension.text.nullIfEmpty
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.flow.MutableStateFlow
import java.lang.ref.WeakReference
import java.security.MessageDigest
import java.security.cert.Certificate
import java.security.cert.CertificateEncodingException
import java.text.SimpleDateFormat
import java.util.*

object Utils {
    fun PackageInfo.toInstalledItem(launcherActivities: List<Pair<String, String>> = emptyList()): Installed {
        val signatureString = singleSignature?.let(Utils::calculateHash).orEmpty()
        return Installed(
            packageName,
            versionName.orEmpty(),
            versionCodeCompat,
            signatureString,
            applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == ApplicationInfo.FLAG_SYSTEM,
            launcherActivities
        )
    }

    fun calculateHash(signature: Signature): String {
        return MessageDigest.getInstance("MD5").digest(signature.toCharsString().toByteArray())
            .hex()
    }

    fun calculateFingerprint(certificate: Certificate): String {
        val encoded = try {
            certificate.encoded
        } catch (e: CertificateEncodingException) {
            null
        }
        return encoded?.let(::calculateFingerprint).orEmpty()
    }

    fun calculateFingerprint(key: ByteArray): String {
        return if (key.size >= 256) {
            try {
                val fingerprint = MessageDigest.getInstance("SHA-256").digest(key)
                val builder = StringBuilder()
                for (byte in fingerprint) {
                    builder.append("%02X".format(Locale.US, byte.toInt() and 0xff))
                }
                builder.toString()
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            }
        } else {
            ""
        }
    }

    suspend fun startUpdate(
        packageName: String,
        installed: Installed?,
        products: List<Pair<Product, Repository>>,
        downloadConnection: Connection<DownloadService.Binder, DownloadService>,
    ) {
        val productRepository = findSuggestedProduct(products, installed) { it.first }
        val compatibleReleases = productRepository?.first?.selectedReleases.orEmpty()
            .filter { installed == null || installed.signature == it.signature }
        val releaseFlow = MutableStateFlow(compatibleReleases.firstOrNull())
        if (compatibleReleases.size > 1) {
            releaseFlow.emit(
                compatibleReleases
                    .filter { it.platforms.contains(Android.primaryPlatform) }
                    .minByOrNull { it.platforms.size }
                    ?: compatibleReleases.minByOrNull { it.platforms.size }
                    ?: compatibleReleases.firstOrNull()
            )
        }
        val binder = downloadConnection.binder
        releaseFlow.collect {
            if (productRepository != null && it != null && binder != null) {
                binder.enqueue(
                    packageName,
                    productRepository.first.label,
                    productRepository.second,
                    it
                )
            }
        }
    }

    fun Context.setLanguage(): Configuration {
        var setLocalCode = Preferences[Preferences.Key.Language]
        if (setLocalCode == PREFS_LANGUAGE_DEFAULT) {
            setLocalCode = Locale.getDefault().toString()
        }
        val config = resources.configuration
        val sysLocale = if (Android.sdk(24)) config.locales[0] else config.locale
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
        localeCode.isEmpty()      -> if (Android.sdk(24)) {
            resources.configuration.locales[0]
        } else {
            resources.configuration.locale
        }
        localeCode.contains("-r") -> Locale(
            localeCode.substring(0, 2),
            localeCode.substring(4)
        )
        localeCode.contains("_")  -> Locale(
            localeCode.substring(0, 2),
            localeCode.substring(3)
        )
        else                      -> Locale(localeCode)
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

}

fun <T> findSuggestedProduct(
    products: List<T>,
    installed: Installed?,
    extract: (T) -> Product,
): T? {
    return products.maxWithOrNull(
        compareBy(
            {
                extract(it).compatible &&
                        (installed == null || installed.signature in extract(it).signatures)
            },
            { extract(it).versionCode },
        )
    )
}

val isDarkTheme: Boolean
    get() = when (Preferences[Preferences.Key.Theme]) {
        is Preferences.Theme.Light -> false
        is Preferences.Theme.Dark  -> true
        is Preferences.Theme.Black -> true
        else                       -> false
    }

val isBlackTheme: Boolean
    get() = when (Preferences[Preferences.Key.Theme]) {
        is Preferences.Theme.Black       -> true
        is Preferences.Theme.SystemBlack -> true
        else                             -> false
    }

val isDynamicColorsTheme: Boolean
    get() = when (Preferences[Preferences.Key.Theme]) {
        is Preferences.Theme.Dynamic -> true
        else                         -> false
    }

fun Context.showBatteryOptimizationDialog() {
    AlertDialog.Builder(this)
        .setTitle(R.string.ignore_battery_optimization_title)
        .setMessage(R.string.ignore_battery_optimization_message)
        .setPositiveButton(R.string.dialog_approve) { _, _ ->
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.data = Uri.parse("package:" + this.packageName)
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(
                    this,
                    R.string.ignore_battery_optimization_not_supported,
                    Toast.LENGTH_LONG
                ).show()
                Preferences[Preferences.Key.IgnoreIgnoreBatteryOptimization] = true
            }
        }
        .setNeutralButton(R.string.dialog_refuse) { _: DialogInterface?, _: Int ->
            Preferences[Preferences.Key.IgnoreIgnoreBatteryOptimization] = true
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

fun Product.generateLinks(context: Context): List<LinkType> {
    val links = mutableListOf<LinkType>()
    if (author.name.isNotEmpty() || author.web.isNotEmpty()) {
        links.add(
            LinkType(
                icon = Phosphor.User,
                title = author.name,
                link = author.web.nullIfEmpty()?.let(Uri::parse)
            )
        )
    }
    author.email.nullIfEmpty()?.let {
        links.add(
            LinkType(
                Phosphor.At,
                context.getString(R.string.author_email),
                Uri.parse("mailto:$it")
            )
        )
    }
    links.addAll(licenses.map {
        LinkType(
            Phosphor.Copyleft,
            it,
            Uri.parse("https://spdx.org/licenses/$it.html")
        )
    })
    tracker.nullIfEmpty()
        ?.let {
            links.add(
                LinkType(
                    Phosphor.Bug,
                    context.getString(R.string.bug_tracker),
                    Uri.parse(it)
                )
            )
        }
    changelog.nullIfEmpty()?.let {
        links.add(
            LinkType(
                Phosphor.ArrowsClockwise,
                context.getString(R.string.changelog),
                Uri.parse(it)
            )
        )
    }
    web.nullIfEmpty()
        ?.let {
            links.add(
                LinkType(
                    Phosphor.GlobeSimple,
                    context.getString(R.string.project_website),
                    Uri.parse(it)
                )
            )
        }
    return links
}

fun Release.generatePermissionGroups(context: Context): Map<PermissionGroup, List<PermissionInfo>> { // TODO other permissions as last group
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

fun List<PermissionInfo>.getLabels(context: Context): List<String> {
    val localCache = PackageItemResolver.LocalCache()

    val labels = map { permission ->
        val labelFromPackage =
            PackageItemResolver.loadLabel(context, localCache, permission)
        val label = labelFromPackage ?: run {
            val prefixes =
                listOf("android.permission.", "com.android.browser.permission.")
            prefixes.find { permission.name.startsWith(it) }?.let { it ->
                val transform = permission.name.substring(it.length)
                if (transform.matches("[A-Z_]+".toRegex())) {
                    transform.split("_")
                        .joinToString(separator = " ") { it.lowercase(Locale.US) }
                } else {
                    null
                }
            }
        }
        if (label == null) {
            Pair(false, permission.name)
        } else {
            Pair(true, label.first().uppercaseChar() + label.substring(1, label.length))
        }
    }
    return labels.sortedBy { it.first }.map { it.second }
}

fun List<PermissionInfo>.getLabelsAndDescriptions(context: Context): List<String> {
    val localCache = PackageItemResolver.LocalCache()

    return map { permission ->
        val labelFromPackage =
            PackageItemResolver.loadLabel(context, localCache, permission)
        val label = labelFromPackage ?: run {
            val prefixes =
                listOf("android.permission.", "com.android.browser.permission.")
            prefixes.find { permission.name.startsWith(it) }?.let { it ->
                val transform = permission.name.substring(it.length)
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

fun Collection<Product>.matchSearchQuery(searchQuery: String): List<Product> {
    if (searchQuery.isBlank()) return toList()
    val searchRegex = Regex(searchQuery, RegexOption.IGNORE_CASE)
    return filter {
        listOf(it.label, it.packageName, it.author.name, it.summary, it.description)
            .any { literal ->
                literal.contains(searchRegex)
            }
    }.sortedByDescending {
        (if ("${it.label} ${it.packageName}".contains(searchRegex)) 7 else 0) or
                (if ("${it.summary} ${it.author.name}".contains(searchRegex)) 3 else 0) or
                (if (it.description.contains(searchRegex)) 1 else 0)
    }
}

// TODO move to a new file

private class LinkSpan(private val url: String, callbacks: Callbacks) :
    ClickableSpan() {
    private val callbacksReference = WeakReference(callbacks)

    override fun onClick(view: View) {
        val callbacks = callbacksReference.get()
        val uri = try {
            Uri.parse(url)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
        if (callbacks != null && uri != null) {
            callbacks.onUriClick(uri, true)
        }
    }
}

fun formatHtml(text: String, callbacks: Callbacks): SpannableStringBuilder {
    val html = HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_COMPACT)
    val builder = run {
        val builder = SpannableStringBuilder(html)
        val last = builder.indexOfLast { it != '\n' }
        val first = builder.indexOfFirst { it != '\n' }
        if (last >= 0) {
            builder.delete(last + 1, builder.length)
        }
        if (first in 1 until last) {
            builder.delete(0, first - 1)
        }
        generateSequence(builder) {
            val index = it.indexOf("\n\n\n")
            if (index >= 0) it.delete(index, index + 1) else null
        }.last()
    }
    LinkifyCompat.addLinks(builder, Linkify.WEB_URLS or Linkify.EMAIL_ADDRESSES)
    val urlSpans = builder.getSpans(0, builder.length, URLSpan::class.java).orEmpty()
    for (span in urlSpans) {
        val start = builder.getSpanStart(span)
        val end = builder.getSpanEnd(span)
        val flags = builder.getSpanFlags(span)
        builder.removeSpan(span)
        builder.setSpan(LinkSpan(span.url, callbacks), start, end, flags)
    }
    val bulletSpans = builder.getSpans(0, builder.length, BulletSpan::class.java).orEmpty()
        .asSequence().map { Pair(it, builder.getSpanStart(it)) }
        .sortedByDescending { it.second }
    for (spanPair in bulletSpans) {
        val (span, start) = spanPair
        builder.removeSpan(span)
        builder.insert(start, "\u2022 ")
    }
    return builder
}

val currentTimestamp: String
    get() {
        val simpleDateFormat = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US)
        return simpleDateFormat.format(Date())
    }

val shellIsRoot: Boolean
    get() = Shell.getCachedShell()?.isRoot ?: Shell.getShell().isRoot

fun NavDestination.destinationToItem(): NavItem? = listOf(
    NavItem.Explore,
    NavItem.Latest,
    NavItem.Installed,
    NavItem.Prefs,
    NavItem.PersonalPrefs,
    NavItem.UpdatesPrefs,
    NavItem.ReposPrefs,
    NavItem.OtherPrefs
).find { this.route == it.destination }

fun Activity.setCustomTheme() {
    AppCompatDelegate.setDefaultNightMode(Preferences[Preferences.Key.Theme].nightMode)
    if (!isDynamicColorsTheme) setTheme(Preferences[Preferences.Key.Theme].resId)
}