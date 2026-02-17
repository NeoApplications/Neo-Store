package com.machiav3lli.fdroid.data.content

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.work.NetworkType
import com.machiav3lli.fdroid.FILTER_CATEGORY_ALL
import com.machiav3lli.fdroid.NeoApp
import com.machiav3lli.fdroid.PREFS_LANGUAGE
import com.machiav3lli.fdroid.PREFS_LANGUAGE_DEFAULT
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.data.entity.AndroidVersion
import com.machiav3lli.fdroid.data.entity.InstallerType
import com.machiav3lli.fdroid.data.entity.Order
import com.machiav3lli.fdroid.utils.amInstalled
import com.machiav3lli.fdroid.utils.extension.android.Android
import com.machiav3lli.fdroid.utils.getHasSystemInstallPermission
import com.machiav3lli.fdroid.utils.hasShizukuOrSui
import com.machiav3lli.fdroid.utils.isBiometricLockAvailable
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.net.Proxy

data object Preferences : OnSharedPreferenceChangeListener {
    private lateinit var preferences: SharedPreferences
    private val subject = MutableSharedFlow<Key<*>>()

    private val keys = sequenceOf(
        // Personalization
        Key.Language,
        Key.Theme,
        Key.DefaultTab,
        Key.KidsMode,
        Key.DownloadShowDialog,
        Key.ActionLockDialog,
        Key.UpdatedApps,
        Key.NewApps,
        // Layout
        Key.AltBlockLayout,
        Key.AltNavBarItem,
        Key.AltNewApps,
        Key.HideNewApps,
        Key.BottomSearchBar,
        Key.DisableListDetail,
        Key.ShowScreenshots,
        Key.ShowTrackers,
        Key.AndroidInsteadOfSDK,
        // Cache
        Key.EnableDownloadDirectory,
        Key.DownloadDirectory,
        Key.ReleasesCacheRetention,
        Key.ImagesCacheRetention,
        // Sync
        Key.AutoSync,
        Key.AutoSyncInterval,
        Key.InstallAfterSync,
        Key.IndexV2,
        // Updates
        Key.DownloadManager,
        Key.UpdateNotify,
        Key.UpdateUnstable,
        Key.IncompatibleVersions,
        Key.DisableDownloadVersionCheck,
        Key.DisableSignatureCheck,
        Key.DisablePermissionsCheck,
        Key.RBProvider,
        Key.DLStatsProvider,
        // Installation
        Key.KeepInstallNotification,
        Key.Installer,
        Key.RootSessionInstaller,
        Key.RootAllowDowngrades,
        Key.RootAllowInstallingOldApps,
        // Internet
        Key.DisableCertificateValidation,
        Key.MaxIdleConnections,
        Key.MaxParallelDownloads,
        Key.ProxyType,
        Key.ProxyUrl,
        Key.ProxyHost,
        Key.ProxyPort,
        // Sort & Filter
        Key.SortOrderExplore,
        Key.SortOrderLatest,
        Key.SortOrderInstalled,
        Key.SortOrderSearch,
        Key.SortOrderAscendingExplore,
        Key.SortOrderAscendingLatest,
        Key.SortOrderAscendingInstalled,
        Key.SortOrderAscendingSearch,
        Key.ReposFilterExplore,
        Key.ReposFilterLatest,
        Key.ReposFilterInstalled,
        Key.ReposFilterSearch,
        Key.CategoriesFilterExplore,
        Key.CategoriesFilterLatest,
        Key.CategoriesFilterInstalled,
        Key.CategoriesFilterSearch,
        Key.AntifeaturesFilterExplore,
        Key.AntifeaturesFilterLatest,
        Key.AntifeaturesFilterInstalled,
        Key.AntifeaturesFilterSearch,
        Key.LicensesFilterExplore,
        Key.LicensesFilterLatest,
        Key.LicensesFilterInstalled,
        Key.LicensesFilterSearch,
        Key.MinSDKExplore,
        Key.MinSDKLatest,
        Key.MinSDKInstalled,
        Key.MinSDKSearch,
        Key.TargetSDKExplore,
        Key.TargetSDKLatest,
        Key.TargetSDKInstalled,
        Key.TargetSDKSearch,
        // invisible values
        Key.InitialSync,
        Key.IgnoreDisableBatteryOptimization,
        Key.IgnoreShowNotifications,
        Key.IgnoreKeepAndroidOpenNotice,
        Key.TrackersLastModified,
        Key.RBLogsLastModified,
    ).map { Pair(it.name, it) }.toMap()

    fun init(context: Context) {
        preferences =
            context.getSharedPreferences(
                "${context.packageName}_preferences",
                Context.MODE_PRIVATE
            )
        preferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        CoroutineScope(Dispatchers.Default).launch {
            keys[key]?.let {
                subject.emit(it)
            }
        }
    }

    suspend fun addPreferencesChangeListener(listener: suspend (Key<*>) -> Unit) {
        subject.collect {
            listener(it)
        }
    }

    sealed class Value<T> {
        abstract val value: T

        internal abstract fun get(
            preferences: SharedPreferences,
            key: String,
            defaultValue: Value<T>,
        ): T

        internal abstract fun set(preferences: SharedPreferences, key: String, value: T)

        class BooleanValue(override val value: Boolean) : Value<Boolean>() {
            override fun get(
                preferences: SharedPreferences,
                key: String,
                defaultValue: Value<Boolean>,
            ): Boolean {
                return preferences.getBoolean(key, defaultValue.value)
            }

            override fun set(preferences: SharedPreferences, key: String, value: Boolean) {
                preferences.edit().putBoolean(key, value).apply()
            }
        }

        class IntValue(override val value: Int) : Value<Int>() {
            override fun get(
                preferences: SharedPreferences,
                key: String,
                defaultValue: Value<Int>,
            ): Int {
                return preferences.getInt(key, defaultValue.value)
            }

            override fun set(preferences: SharedPreferences, key: String, value: Int) {
                preferences.edit().putInt(key, value).apply()
            }
        }

        class LongValue(override val value: Long) : Value<Long>() {
            override fun get(
                preferences: SharedPreferences,
                key: String,
                defaultValue: Value<Long>,
            ): Long {
                return preferences.getLong(key, defaultValue.value)
            }

            override fun set(preferences: SharedPreferences, key: String, value: Long) {
                preferences.edit().putLong(key, value).apply()
            }
        }

        class StringSetValue(override val value: Set<String>) : Value<Set<String>>() {
            override fun get(
                preferences: SharedPreferences,
                key: String,
                defaultValue: Value<Set<String>>,
            ): Set<String> {
                return preferences.getStringSet(key, defaultValue.value) ?: emptySet()
            }

            override fun set(preferences: SharedPreferences, key: String, value: Set<String>) {
                preferences.edit().putStringSet(key, value).apply()
            }
        }

        class StringValue(override val value: String) : Value<String>() {
            override fun get(
                preferences: SharedPreferences,
                key: String,
                defaultValue: Value<String>,
            ): String {
                return preferences.getString(key, defaultValue.value) ?: defaultValue.value
            }

            override fun set(preferences: SharedPreferences, key: String, value: String) {
                preferences.edit().putString(key, value).apply()
            }
        }

        class EnumerationValue<T : Enumeration<T>>(override val value: T) : Value<T>() {
            override fun get(
                preferences: SharedPreferences,
                key: String,
                defaultValue: Value<T>,
            ): T {
                val value = preferences.getString(key, defaultValue.value.valueString)
                return defaultValue.value.values.find { it.valueString == value }
                    ?: defaultValue.value
            }

            override fun set(preferences: SharedPreferences, key: String, value: T) {
                preferences.edit().putString(key, value.valueString).apply()
            }
        }

        class EnumValue<T>(override val value: T, private val enumClass: Class<T>) : Value<T>()
                where T : Enum<T>, T : EnumEnumeration {
            override fun get(
                preferences: SharedPreferences,
                key: String,
                defaultValue: Value<T>,
            ): T {
                val value = preferences.getInt(key, defaultValue.value.ordinal)
                return enumFromOrdinal(enumClass, value)
            }

            override fun set(preferences: SharedPreferences, key: String, value: T) {
                preferences.edit().putInt(key, value.ordinal).apply()
            }

            private fun enumFromOrdinal(enumClass: Class<T>, ordinal: Int): T {
                return enumClass.enumConstants?.getOrNull(ordinal)
                    ?: enumClass.enumConstants?.first()
                    ?: throw NoSuchElementException("Enum ${enumClass.simpleName} is empty.")
            }
        }
    }

    interface Enumeration<T> {
        val values: List<T>
        val valueString: String
    }

    interface EnumEnumeration {
        val valueString: String
    }

    sealed class Key<T>(val name: String, val default: Value<T>) {
        data object Null : Key<Int>("", Value.IntValue(0))

        data object Language :
            Key<String>(PREFS_LANGUAGE, Value.StringValue(PREFS_LANGUAGE_DEFAULT))

        data object AutoSync : Key<Preferences.AutoSync>(
            "auto_sync",
            Value.EnumerationValue(Preferences.AutoSync.Wifi)
        )

        data object EnableDownloadDirectory :
            Key<Boolean>("download_directory_enable", Value.BooleanValue(false))

        data object IndexV2 : Key<Boolean>("index_v2", Value.BooleanValue(true))

        data object DownloadManager :
            Key<Boolean>("system_download_manager", Value.BooleanValue(false))

        data object DownloadDirectory :
            Key<String>("download_directory_value", Value.StringValue(""))

        data object DownloadShowDialog :
            Key<Boolean>("download_show_dialog", Value.BooleanValue(false))

        data object ActionLockDialog :
            Key<ActionLock>("action_lock", Value.EnumerationValue(ActionLock.None))

        data object ReleasesCacheRetention : Key<Int>("releases_cache_retention", Value.IntValue(1))

        data object ImagesCacheRetention : Key<Int>("images_cache_retention", Value.IntValue(14))

        data object AutoSyncInterval : Key<Int>("auto_sync_interval_hours", Value.IntValue(8))

        data object KeepInstallNotification :
            Key<Boolean>("keep_install_notification", Value.BooleanValue(false))

        data object InstallAfterSync :
            Key<Boolean>(
                "auto_sync_install",
                Value.BooleanValue(Android.sdk(Build.VERSION_CODES.S))
            )

        data object IncompatibleVersions :
            Key<Boolean>("incompatible_versions", Value.BooleanValue(false))

        data object DisableDownloadVersionCheck :
            Key<Boolean>("disable_download_version_check", Value.BooleanValue(false))

        data object DisableSignatureCheck :
            Key<Boolean>("disable_signature_check", Value.BooleanValue(false))

        data object DisablePermissionsCheck :
            Key<Boolean>("disable_permissions_check", Value.BooleanValue(false))

        data object RBProvider : Key<Preferences.RBProvider>(
            "rb_provider", Value.EnumerationValue(
                Preferences.RBProvider.IzzyOnDroid
            )
        )

        data object DLStatsProvider : Key<Preferences.DLStatsProvider>(
            "dlstats_provider", Value.EnumerationValue(
                Preferences.DLStatsProvider.IzzyOnDroid
            )
        )

        data object ShowScreenshots :
            Key<Boolean>("show_screenshots", Value.BooleanValue(true))

        data object ShowTrackers : Key<Boolean>("show_trackers", Value.BooleanValue(true))

        data object AltNavBarItem : Key<Boolean>("alt_navbar_item", Value.BooleanValue(false))
        data object AltNewApps : Key<Boolean>("alt_new_apps_layout", Value.BooleanValue(false))
        data object HideNewApps : Key<Boolean>("hide_new_apps", Value.BooleanValue(false))
        data object AltBlockLayout : Key<Boolean>("alt_block_layout", Value.BooleanValue(false))
        data object AndroidInsteadOfSDK :
            Key<Boolean>("android_instead_of_sdk", Value.BooleanValue(true))

        data object BottomSearchBar : Key<Boolean>("bottom_search_bar", Value.BooleanValue(false))
        data object DisableListDetail :
            Key<Boolean>("disable_list_detail", Value.BooleanValue(false))

        data object UpdatedApps : Key<Int>("updated_apps", Value.IntValue(150))
        data object NewApps : Key<Int>("new_apps", Value.IntValue(30))

        data object MaxIdleConnections : Key<Int>("max_num_idle_connections", Value.IntValue(10))
        data object MaxParallelDownloads : Key<Int>("max_num_parallel_downloads", Value.IntValue(5))

        data object DisableCertificateValidation :
            Key<Boolean>("disable_certificate_validation", Value.BooleanValue(false))

        data object ProxyUrl : Key<String>("proxy_url", Value.StringValue(""))
        data object ProxyHost : Key<String>("proxy_host", Value.StringValue("localhost"))
        data object ProxyPort : Key<Int>("proxy_port", Value.IntValue(9050))
        data object ProxyType : Key<Preferences.ProxyType>(
            "proxy_type",
            Value.EnumerationValue(Preferences.ProxyType.Direct)
        )

        data object Installer : Key<Preferences.Installer>(
            "installer_type",
            Value.EnumerationValue(Preferences.Installer.Default)
        )

        data object RootSessionInstaller :
            Key<Boolean>(
                "root_session_installer",
                Value.BooleanValue(Android.sdk(Build.VERSION_CODES.TIRAMISU))
            )

        data object RootAllowDowngrades :
            Key<Boolean>(
                "root_allow_downgrades",
                Value.BooleanValue(false)
            )

        data object RootAllowInstallingOldApps :
            Key<Boolean>(
                "root_allow_low_target_sdk",
                Value.BooleanValue(false)
            )

        data object SortOrderExplore : Key<SortOrder>(
            "sort_order_explore",
            Value.EnumerationValue(SortOrder.Update)
        )

        data object SortOrderLatest : Key<SortOrder>(
            "sort_order_latest_fix",
            Value.EnumerationValue(SortOrder.Update)
        )

        data object SortOrderInstalled : Key<SortOrder>(
            "sort_order_installed",
            Value.EnumerationValue(SortOrder.Update)
        )

        data object SortOrderSearch : Key<SortOrder>(
            "sort_order_search",
            Value.EnumerationValue(SortOrder.Update)
        )

        data object SortOrderAscendingExplore :
            Key<Boolean>("sort_order_ascending_explore", Value.BooleanValue(false))

        data object SortOrderAscendingLatest :
            Key<Boolean>("sort_order_ascending_latest", Value.BooleanValue(false))

        data object SortOrderAscendingInstalled :
            Key<Boolean>("sort_order_ascending_installed", Value.BooleanValue(false))

        data object SortOrderAscendingSearch :
            Key<Boolean>("sort_order_ascending_search", Value.BooleanValue(false))

        data object ReposFilterExplore : Key<Set<String>>(
            "repos_filter_explore",
            Value.StringSetValue(emptySet())
        )

        data object ReposFilterLatest : Key<Set<String>>(
            "repos_filter_latest",
            Value.StringSetValue(emptySet())
        )

        data object ReposFilterInstalled : Key<Set<String>>(
            "repos_filter_installed",
            Value.StringSetValue(emptySet())
        )

        data object ReposFilterSearch : Key<Set<String>>(
            "repos_filter_search",
            Value.StringSetValue(emptySet())
        )

        data object CategoriesFilterExplore : Key<String>(
            "category_filter_explore_fix",
            Value.StringValue("")
        )

        data object CategoriesFilterLatest : Key<String>(
            "category_filter_latest",
            Value.StringValue(FILTER_CATEGORY_ALL)
        )

        data object CategoriesFilterInstalled : Key<String>(
            "category_filter_installed",
            Value.StringValue(FILTER_CATEGORY_ALL)
        )

        data object CategoriesFilterSearch : Key<String>(
            "category_filter_search",
            Value.StringValue(FILTER_CATEGORY_ALL)
        )

        data object AntifeaturesFilterExplore : Key<Set<String>>(
            "antifeatures_filter_explore",
            Value.StringSetValue(emptySet())
        )

        data object AntifeaturesFilterLatest : Key<Set<String>>(
            "antifeatures_filter_latest",
            Value.StringSetValue(emptySet())
        )

        data object AntifeaturesFilterInstalled : Key<Set<String>>(
            "antifeatures_filter_installed",
            Value.StringSetValue(emptySet())
        )

        data object AntifeaturesFilterSearch : Key<Set<String>>(
            "antifeatures_filter_search",
            Value.StringSetValue(emptySet())
        )

        data object LicensesFilterExplore : Key<Set<String>>(
            "licenses_filter_explore",
            Value.StringSetValue(emptySet())
        )

        data object LicensesFilterLatest : Key<Set<String>>(
            "licenses_filter_latest",
            Value.StringSetValue(emptySet())
        )

        data object LicensesFilterInstalled : Key<Set<String>>(
            "licenses_filter_installed",
            Value.StringSetValue(emptySet())
        )

        data object LicensesFilterSearch : Key<Set<String>>(
            "licenses_filter_search",
            Value.StringSetValue(emptySet())
        )

        data object TargetSDKExplore : Key<AndroidVersion>(
            "targetsdk_filter_explore",
            Value.EnumValue(AndroidVersion.Unknown, AndroidVersion::class.java)
        )

        data object TargetSDKLatest : Key<AndroidVersion>(
            "targetsdk_filter_latest",
            Value.EnumValue(AndroidVersion.Unknown, AndroidVersion::class.java)
        )

        data object TargetSDKInstalled : Key<AndroidVersion>(
            "targetsdk_filter_installed",
            Value.EnumValue(AndroidVersion.Unknown, AndroidVersion::class.java)
        )

        data object TargetSDKSearch : Key<AndroidVersion>(
            "targetsdk_filter_search",
            Value.EnumValue(AndroidVersion.Unknown, AndroidVersion::class.java)
        )

        data object MinSDKExplore : Key<AndroidVersion>(
            "minsdk_filter_explore",
            Value.EnumValue(AndroidVersion.Unknown, AndroidVersion::class.java)
        )

        data object MinSDKLatest : Key<AndroidVersion>(
            "minsdk_filter_latest",
            Value.EnumValue(AndroidVersion.Unknown, AndroidVersion::class.java)
        )

        data object MinSDKInstalled : Key<AndroidVersion>(
            "minsdk_filter_installed",
            Value.EnumValue(AndroidVersion.Unknown, AndroidVersion::class.java)
        )

        data object MinSDKSearch : Key<AndroidVersion>(
            "minsdk_filter_search",
            Value.EnumValue(AndroidVersion.Unknown, AndroidVersion::class.java)
        )

        data object Theme : Key<Preferences.Theme>(
            "theme", Value.EnumerationValue(
                when {
                    Android.sdk(Build.VERSION_CODES.S) -> Preferences.Theme.Dynamic
                    Android.sdk(Build.VERSION_CODES.Q) -> Preferences.Theme.SystemBlack
                    else                               -> Preferences.Theme.Light
                }
            )
        )

        data object DefaultTab : Key<Preferences.DefaultTab>(
            "default_tab_int", Value.EnumerationValue(
                Preferences.DefaultTab.Latest
            )
        )

        data object UpdateNotify : Key<Boolean>("update_notify", Value.BooleanValue(true))
        data object UpdateUnstable : Key<Boolean>("update_unstable", Value.BooleanValue(false))
        data object KidsMode : Key<Boolean>("kids_mode", Value.BooleanValue(false))

        data object InitialSync : Key<Boolean>("initial_sync", Value.BooleanValue(false))

        data object IgnoreDisableBatteryOptimization :
            Key<Boolean>("ignore_disable_battery_optimization", Value.BooleanValue(false))

        data object IgnoreShowNotifications :
            Key<Boolean>("ignore_show_notifications", Value.BooleanValue(false))

        data object IgnoreKeepAndroidOpenNotice :
            Key<Boolean>("ignore_keep_android_open_notice", Value.BooleanValue(false))

        data object LastManualSyncTime :
            Key<Long>("last_manual_sync_time", Value.LongValue(0L))

        data object RBLogsLastModified :
            Key<String>("last_modified_rblogs", Value.StringValue(""))

        data object TrackersLastModified :
            Key<String>("last_modified_trackers", Value.StringValue(""))

        data object DownloadStatsLastModified :
            Key<String>("last_modified_downloadstats", Value.StringValue(""))
    }

    sealed class AutoSync(override val valueString: String) : Enumeration<AutoSync> {
        override val values: List<AutoSync>
            get() = listOf(Never, Wifi, WifiBattery, Battery, Always)

        data object Never : AutoSync("never")
        data object Wifi : AutoSync("wifi")
        data object WifiBattery : AutoSync("wifi-battery")
        data object Battery : AutoSync("battery")
        data object Always : AutoSync("always")

        fun requireBattery() = this is Battery || this is WifiBattery
        fun connectionType() = when (this) {
            Wifi,
            WifiBattery,
                 -> NetworkType.UNMETERED

            else -> NetworkType.CONNECTED
        }
    }

    sealed class ProxyType(override val valueString: String, val proxyType: Proxy.Type) :
        Enumeration<ProxyType> {
        override val values: List<ProxyType>
            get() = listOf(Direct, Http, Socks)

        data object Direct : ProxyType("direct", Proxy.Type.DIRECT)
        data object Http : ProxyType("http", Proxy.Type.HTTP)
        data object Socks : ProxyType("socks", Proxy.Type.SOCKS)
    }

    sealed class SortOrder(override val valueString: String, val order: Order) :
        Enumeration<SortOrder> {
        override val values: List<SortOrder>
            get() = listOf(Name, Added, Update)

        data object Name : SortOrder("name", Order.NAME)
        data object Added : SortOrder("added", Order.DATE_ADDED)
        data object Update : SortOrder("update", Order.LAST_UPDATE)
    }

    sealed class Installer(override val valueString: String, val installer: InstallerType) :
        Enumeration<Installer> {
        override val values: List<Installer>
            get() = buildList {
                addAll(listOf(Default, Root, Legacy))
                if (NeoApp.context.amInstalled)
                    add(AM)
                if (NeoApp.context.getHasSystemInstallPermission())
                    add(System)
                if (NeoApp.context.hasShizukuOrSui)
                    add(Shizuku)
            }

        data object Default : Installer("session", InstallerType.DEFAULT)
        data object Root : Installer("root", InstallerType.ROOT)
        data object AM : Installer("app_manager", InstallerType.AM)
        data object Legacy : Installer("legacy", InstallerType.LEGACY)
        data object System : Installer("system", InstallerType.SYSTEM)
        data object Shizuku : Installer("shizuku", InstallerType.SHIZUKU)
    }

    sealed class ActionLock(override val valueString: String, val order: Order) :
        Enumeration<ActionLock> {
        override val values: List<ActionLock>
            get() = buildList {
                addAll(listOf(None, Device))
                if (NeoApp.context.isBiometricLockAvailable())
                    add(Biometric)
            }

        data object None : ActionLock("none", Order.NAME)
        data object Device : ActionLock("device", Order.DATE_ADDED)
        data object Biometric : ActionLock("biometric", Order.LAST_UPDATE)
    }

    sealed class Theme(override val valueString: String) : Enumeration<Theme> {
        override val values: List<Theme>
            get() = buildList {
                addAll(
                    listOf(
                        Light,
                        Dark,
                        Black,
                        LightMediumContrast,
                        DarkMediumContrast,
                        BlackMediumContrast,
                        LightHighContrast,
                        DarkHighContrast,
                        BlackHighContrast,
                    )
                )
                if (Android.sdk(Build.VERSION_CODES.S)) addAll(
                    listOf(
                        Dynamic,
                        DynamicLight,
                        DynamicDark,
                        DynamicBlack
                    )
                )
                if (Android.sdk(Build.VERSION_CODES.Q)) addAll(listOf(System, SystemBlack))
            }

        abstract val resId: Int
        abstract val nightMode: Int

        data object System : Theme("system") {
            override val resId: Int
                get() = R.style.Theme_Main
            override val nightMode: Int
                get() = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }

        data object SystemBlack : Theme("system-amoled") {
            override val resId: Int
                get() = R.style.Theme_Main_Amoled
            override val nightMode: Int
                get() = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }

        data object Dynamic : Theme("dynamic-system") {
            override val resId: Int
                get() = -1
            override val nightMode: Int
                get() = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }

        data object DynamicLight : Theme("dynamic-light") {
            override val resId: Int
                get() = -1
            override val nightMode: Int
                get() = AppCompatDelegate.MODE_NIGHT_NO
        }

        data object DynamicDark : Theme("dynamic-dark") {
            override val resId: Int
                get() = -1
            override val nightMode: Int
                get() = AppCompatDelegate.MODE_NIGHT_YES
        }

        data object DynamicBlack : Theme("dynamic-black") {
            override val resId: Int
                get() = -1
            override val nightMode: Int
                get() = AppCompatDelegate.MODE_NIGHT_YES
        }

        data object Light : Theme("light") {
            override val resId: Int
                get() = R.style.Theme_Main
            override val nightMode: Int
                get() = AppCompatDelegate.MODE_NIGHT_NO
        }

        data object LightMediumContrast : Theme("light_medium_contrast") {
            override val resId: Int
                get() = R.style.Theme_Main
            override val nightMode: Int
                get() = AppCompatDelegate.MODE_NIGHT_NO
        }

        data object LightHighContrast : Theme("light_high_contrast") {
            override val resId: Int
                get() = R.style.Theme_Main
            override val nightMode: Int
                get() = AppCompatDelegate.MODE_NIGHT_NO
        }

        data object Dark : Theme("dark") {
            override val resId: Int
                get() = R.style.Theme_Main
            override val nightMode: Int
                get() = AppCompatDelegate.MODE_NIGHT_YES
        }

        data object DarkMediumContrast : Theme("dark_medium_contrast") {
            override val resId: Int
                get() = R.style.Theme_Main
            override val nightMode: Int
                get() = AppCompatDelegate.MODE_NIGHT_YES
        }

        data object DarkHighContrast : Theme("dark_high_contrast") {
            override val resId: Int
                get() = R.style.Theme_Main
            override val nightMode: Int
                get() = AppCompatDelegate.MODE_NIGHT_YES
        }

        data object Black : Theme("amoled") {
            override val resId: Int
                get() = R.style.Theme_Main_Amoled
            override val nightMode: Int
                get() = AppCompatDelegate.MODE_NIGHT_YES
        }

        data object BlackMediumContrast : Theme("black_medium_contrast") {
            override val resId: Int
                get() = R.style.Theme_Main_Amoled
            override val nightMode: Int
                get() = AppCompatDelegate.MODE_NIGHT_YES
        }

        data object BlackHighContrast : Theme("black_high_contrast") {
            override val resId: Int
                get() = R.style.Theme_Main_Amoled
            override val nightMode: Int
                get() = AppCompatDelegate.MODE_NIGHT_YES
        }
    }

    sealed class DefaultTab(override val valueString: String) : Enumeration<DefaultTab> {
        override val values: List<DefaultTab>
            get() = listOf(Latest, Explore, Installed)

        val index get() = valueString.toInt()

        data object Latest : DefaultTab("0")
        data object Explore : DefaultTab("1")
        data object Installed : DefaultTab("2")
    }

    sealed class RBProvider(override val valueString: String) : Enumeration<RBProvider> {
        override val values: List<RBProvider>
            get() = persistentListOf(
                None,
                IzzyOnDroid,
                BG443,
            )

        abstract val url: String

        data object None : RBProvider("none") {
            override val url: String
                get() = ""
        }

        data object IzzyOnDroid : RBProvider("iod") {
            override val url: String
                get() = "https://apt.izzysoft.de/fdroid/rbtlogs/izzy.json"
        }

        data object BG443 : RBProvider("bg443") {
            override val url: String
                get() = "https://apt.izzysoft.de/fdroid/rbtlogs/bg443.json"
        }

        data object OBFUSK : RBProvider("obfusk") {
            override val url: String
                get() = "https://apt.izzysoft.de/fdroid/rbtlogs/obfusk.json"
        }
    }

    sealed class DLStatsProvider(override val valueString: String) : Enumeration<DLStatsProvider> {
        override val values: List<DLStatsProvider>
            get() = persistentListOf(
                None,
                IzzyOnDroid,
            )

        abstract val url: String

        data object None : DLStatsProvider("none") {
            override val url: String
                get() = ""
        }

        data object IzzyOnDroid : DLStatsProvider("iod") {
            override val url: String
                get() = "https://dlstats.izzyondroid.org/iod-stats-collector"
        }
    }

    operator fun <T> get(key: Key<T>): T {
        return key.default.get(preferences, key.name, key.default)
    }

    operator fun <T> set(key: Key<T>, value: T) {
        key.default.set(preferences, key.name, value)
    }
}
