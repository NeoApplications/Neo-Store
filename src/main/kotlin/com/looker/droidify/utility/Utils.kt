package com.looker.droidify.utility

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
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager
import com.looker.droidify.BuildConfig
import com.looker.droidify.PREFS_LANGUAGE_DEFAULT
import com.looker.droidify.R
import com.looker.droidify.content.Preferences
import com.looker.droidify.database.entity.Installed
import com.looker.droidify.database.entity.Product
import com.looker.droidify.database.entity.Release
import com.looker.droidify.database.entity.Repository
import com.looker.droidify.entity.LinkType
import com.looker.droidify.entity.PermissionsType
import com.looker.droidify.service.Connection
import com.looker.droidify.service.DownloadService
import com.looker.droidify.ui.dialog.LaunchDialog
import com.looker.droidify.utility.extension.android.Android
import com.looker.droidify.utility.extension.android.singleSignature
import com.looker.droidify.utility.extension.android.versionCodeCompat
import com.looker.droidify.utility.extension.resources.getDrawableCompat
import com.looker.droidify.utility.extension.text.hex
import com.looker.droidify.utility.extension.text.nullIfEmpty
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.flow.MutableStateFlow
import java.security.MessageDigest
import java.security.cert.Certificate
import java.security.cert.CertificateEncodingException
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

    fun getDefaultApplicationIcon(context: Context): Drawable =
        context.getDrawableCompat(R.drawable.ic_placeholder)

    fun getToolbarIcon(context: Context, resId: Int): Drawable {
        return context.getDrawableCompat(resId).mutate()
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

    val rootInstallerEnabled: Boolean
        get() = Preferences[Preferences.Key.RootPermission] &&
                (Shell.getCachedShell()?.isRoot ?: Shell.getShell().isRoot)

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
        localeCode.isEmpty() -> if (Android.sdk(24)) {
            resources.configuration.locales[0]
        } else {
            resources.configuration.locale
        }
        localeCode.contains("-r") -> Locale(
            localeCode.substring(0, 2),
            localeCode.substring(4)
        )
        localeCode.contains("_") -> Locale(
            localeCode.substring(0, 2),
            localeCode.substring(3)
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

}

fun <T> findSuggestedProduct(
    products: List<T>,
    installed: Installed?,
    extract: (T) -> Product,
): T? {
    return products.maxWithOrNull(compareBy({
        extract(it).compatible &&
                (installed == null || installed.signature in extract(it).signatures)
    }, { extract(it).versionCode }))
}

val isDarkTheme: Boolean
    get() = when (Preferences[Preferences.Key.Theme]) {
        is Preferences.Theme.Light -> false
        is Preferences.Theme.Dark -> true
        is Preferences.Theme.Amoled -> true
        else -> false
    }

val isBlackTheme: Boolean
    get() = when (Preferences[Preferences.Key.Theme]) {
        is Preferences.Theme.Amoled -> true
        is Preferences.Theme.AmoledSystem -> true
        else -> false
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
                iconResId = R.drawable.ic_person,
                title = author.name,
                link = author.web.nullIfEmpty()?.let(Uri::parse)
            )
        )
    }
    author.email.nullIfEmpty()?.let {
        links.add(
            LinkType(
                R.drawable.ic_email,
                context.getString(R.string.author_email),
                Uri.parse("mailto:$it")
            )
        )
    }
    links.addAll(licenses.map {
        LinkType(
            R.drawable.ic_copyright,
            it,
            Uri.parse("https://spdx.org/licenses/$it.html")
        )
    })
    tracker.nullIfEmpty()
        ?.let {
            links.add(
                LinkType(
                    R.drawable.ic_bug_report,
                    context.getString(R.string.bug_tracker),
                    Uri.parse(it)
                )
            )
        }
    changelog.nullIfEmpty()?.let {
        links.add(
            LinkType(
                R.drawable.ic_history,
                context.getString(R.string.changelog),
                Uri.parse(it)
            )
        )
    }
    web.nullIfEmpty()
        ?.let {
            links.add(
                LinkType(
                    R.drawable.ic_public,
                    context.getString(R.string.project_website),
                    Uri.parse(it)
                )
            )
        }
    return links
}

fun Release.generatePermissionGroups(context: Context): List<PermissionsType> {
    val permissionGroups = mutableListOf<PermissionsType>()
    val packageManager = context.packageManager
    val permissions = permissions
        .asSequence().mapNotNull {
            try {
                packageManager.getPermissionInfo(it, 0)
            } catch (e: Exception) {
                null
            }
        }
        .groupBy(PackageItemResolver::getPermissionGroup)
        .asSequence().map { (group, permissionInfo) ->
            val permissionGroupInfo = try {
                group?.let { packageManager.getPermissionGroupInfo(it, 0) }
            } catch (e: Exception) {
                null
            }
            Pair(permissionGroupInfo, permissionInfo)
        }
        .groupBy({ it.first }, { it.second })
    if (permissions.isNotEmpty()) {
        permissionGroups.addAll(permissions.asSequence().filter { it.key != null }
            .map { PermissionsType(it.key, it.value.flatten()) })
        permissions.asSequence().find { it.key == null }
            ?.let { permissionGroups.add(PermissionsType(null, it.value.flatten())) }
    }
    return permissionGroups
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
