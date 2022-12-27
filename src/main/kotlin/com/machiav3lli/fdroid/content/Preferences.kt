package com.machiav3lli.fdroid.content

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.appcompat.app.AppCompatDelegate
import com.machiav3lli.fdroid.FILTER_CATEGORY_ALL
import com.machiav3lli.fdroid.PREFS_LANGUAGE
import com.machiav3lli.fdroid.PREFS_LANGUAGE_DEFAULT
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.entity.InstallerType
import com.machiav3lli.fdroid.entity.Order
import com.machiav3lli.fdroid.ui.navigation.NavItem
import com.machiav3lli.fdroid.utility.extension.android.Android
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.net.Proxy

object Preferences : OnSharedPreferenceChangeListener {
    private lateinit var preferences: SharedPreferences

    private val mutableSubject = MutableSharedFlow<Key<*>>()
    val subject = mutableSubject.asSharedFlow()

    private val keys = sequenceOf(
        Key.Language,
        Key.AutoSync,
        Key.AutoSyncInterval,
        Key.ReleasesCacheRetention,
        Key.ImagesCacheRetention,
        Key.InstallAfterSync,
        Key.IncompatibleVersions,
        Key.ShowScreenshots,
        Key.UpdatedApps,
        Key.NewApps,
        Key.ProxyHost,
        Key.ProxyPort,
        Key.ProxyType,
        Key.Installer,
        Key.RootSessionInstaller,
        Key.SortOrderExplore,
        Key.SortOrderLatest,
        Key.SortOrderInstalled,
        Key.SortOrderAscendingExplore,
        Key.SortOrderAscendingLatest,
        Key.SortOrderAscendingInstalled,
        Key.ReposFilterExplore,
        Key.ReposFilterLatest,
        Key.ReposFilterInstalled,
        Key.CategoriesFilterExplore,
        Key.CategoriesFilterLatest,
        Key.CategoriesFilterInstalled,
        Key.Theme,
        Key.DefaultTab,
        Key.UpdateNotify,
        Key.UpdateUnstable,
        Key.IgnoreIgnoreBatteryOptimization
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
                mutableSubject.emit(it)
            }
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
    }

    interface Enumeration<T> {
        val values: List<T>
        val valueString: String
    }

    sealed class Key<T>(val name: String, val default: Value<T>) {
        object Null : Key<Int>("", Value.IntValue(0))

        object Language : Key<String>(PREFS_LANGUAGE, Value.StringValue(PREFS_LANGUAGE_DEFAULT))
        object AutoSync : Key<Preferences.AutoSync>(
            "auto_sync",
            Value.EnumerationValue(Preferences.AutoSync.Wifi)
        )

        object ReleasesCacheRetention : Key<Int>("releases_cache_retention", Value.IntValue(1))

        object ImagesCacheRetention : Key<Int>("images_cache_retention", Value.IntValue(14))

        object AutoSyncInterval : Key<Int>("auto_sync_interval", Value.IntValue(60))

        object InstallAfterSync :
            Key<Boolean>("auto_sync_install", Value.BooleanValue(Android.sdk(31)))

        object IncompatibleVersions :
            Key<Boolean>("incompatible_versions", Value.BooleanValue(false))

        object ShowScreenshots :
            Key<Boolean>("show_screenshots", Value.BooleanValue(true))

        object UpdatedApps : Key<Int>("updated_apps", Value.IntValue(100))
        object NewApps : Key<Int>("new_apps", Value.IntValue(30))

        object ProxyHost : Key<String>("proxy_host", Value.StringValue("localhost"))
        object ProxyPort : Key<Int>("proxy_port", Value.IntValue(9050))
        object ProxyType : Key<Preferences.ProxyType>(
            "proxy_type",
            Value.EnumerationValue(Preferences.ProxyType.Direct)
        )

        object Installer : Key<Preferences.Installer>(
            "installer_type",
            Value.EnumerationValue(Preferences.Installer.Default)
        )

        object RootSessionInstaller :
            Key<Boolean>("root_session_installer", Value.BooleanValue(false))

        object SortOrderExplore : Key<SortOrder>(
            "sort_order_explore",
            Value.EnumerationValue(SortOrder.Update)
        )

        object SortOrderLatest : Key<SortOrder>(
            "sort_order_latest",
            Value.EnumerationValue(SortOrder.Update)
        )

        object SortOrderInstalled : Key<SortOrder>(
            "sort_order_installed",
            Value.EnumerationValue(SortOrder.Name)
        )

        object SortOrderAscendingExplore :
            Key<Boolean>("sort_order_ascending_explore", Value.BooleanValue(false))

        object SortOrderAscendingLatest :
            Key<Boolean>("sort_order_ascending_latest", Value.BooleanValue(false))

        object SortOrderAscendingInstalled :
            Key<Boolean>("sort_order_ascending_installed", Value.BooleanValue(true))

        object ReposFilterExplore : Key<Set<String>>(
            "repos_filter_explore",
            Value.StringSetValue(emptySet())
        )

        object ReposFilterLatest : Key<Set<String>>(
            "repos_filter_latest",
            Value.StringSetValue(emptySet())
        )

        object ReposFilterInstalled : Key<Set<String>>(
            "repos_filter_installed",
            Value.StringSetValue(emptySet())
        )

        object CategoriesFilterExplore : Key<String>(
            "category_filter_explore",
            Value.StringValue(FILTER_CATEGORY_ALL)
        )

        object CategoriesFilterLatest : Key<String>(
            "category_filter_latest",
            Value.StringValue(FILTER_CATEGORY_ALL)
        )

        object CategoriesFilterInstalled : Key<String>(
            "category_filter_installed",
            Value.StringValue(FILTER_CATEGORY_ALL)
        )

        object Theme : Key<Preferences.Theme>(
            "theme", Value.EnumerationValue(
                when {
                    Android.sdk(31) -> Preferences.Theme.Dynamic
                    Android.sdk(29) -> Preferences.Theme.SystemBlack
                    else -> Preferences.Theme.Light
                }
            )
        )

        object DefaultTab : Key<Preferences.DefaultTab>(
            "default_tab", Value.EnumerationValue(
                Preferences.DefaultTab.Explore
            )
        )

        object UpdateNotify : Key<Boolean>("update_notify", Value.BooleanValue(true))
        object UpdateUnstable : Key<Boolean>("update_unstable", Value.BooleanValue(false))

        object IgnoreIgnoreBatteryOptimization :
            Key<Boolean>("ignore_ignore_battery_optimization", Value.BooleanValue(false))
    }

    sealed class AutoSync(override val valueString: String) : Enumeration<AutoSync> {
        override val values: List<AutoSync>
            get() = listOf(Never, Wifi, WifiBattery, Always)

        object Never : AutoSync("never")
        object Wifi : AutoSync("wifi")
        object WifiBattery : AutoSync("wifi-battery")
        object Always : AutoSync("always")
    }

    sealed class ProxyType(override val valueString: String, val proxyType: Proxy.Type) :
        Enumeration<ProxyType> {
        override val values: List<ProxyType>
            get() = listOf(Direct, Http, Socks)

        object Direct : ProxyType("direct", Proxy.Type.DIRECT)
        object Http : ProxyType("http", Proxy.Type.HTTP)
        object Socks : ProxyType("socks", Proxy.Type.SOCKS)
    }

    sealed class SortOrder(override val valueString: String, val order: Order) :
        Enumeration<SortOrder> {
        override val values: List<SortOrder>
            get() = listOf(Name, Added, Update)

        object Name : SortOrder("name", Order.NAME)
        object Added : SortOrder("added", Order.DATE_ADDED)
        object Update : SortOrder("update", Order.LAST_UPDATE)
    }

    sealed class Installer(override val valueString: String, val installer: InstallerType) :
        Enumeration<Installer> {
        override val values: List<Installer>
            get() = listOf(Default, Root, Legacy)

        object Default : Installer("session", InstallerType.DEFAULT)
        object Root : Installer("root", InstallerType.ROOT)
        object Legacy : Installer("legacy", InstallerType.LEGACY)
    }

    sealed class Theme(override val valueString: String) : Enumeration<Theme> {
        override val values: List<Theme>
            get() = if (Android.sdk(31)) listOf(Dynamic, System, SystemBlack, Light, Dark, Black)
            else if (Android.sdk(29)) listOf(System, SystemBlack, Light, Dark, Black)
            else listOf(Light, Dark, Black)

        abstract val resId: Int
        abstract val nightMode: Int

        object System : Theme("system") {
            override val resId: Int
                get() = R.style.Theme_Main
            override val nightMode: Int
                get() = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }

        object SystemBlack : Theme("system-amoled") {
            override val resId: Int
                get() = R.style.Theme_Main_Amoled
            override val nightMode: Int
                get() = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }

        object Dynamic : Theme("dynamic-system") {
            override val resId: Int
                get() = -1
            override val nightMode: Int
                get() = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }

        object Light : Theme("light") {
            override val resId: Int
                get() = R.style.Theme_Main
            override val nightMode: Int
                get() = AppCompatDelegate.MODE_NIGHT_NO
        }

        object Dark : Theme("dark") {
            override val resId: Int
                get() = R.style.Theme_Main
            override val nightMode: Int
                get() = AppCompatDelegate.MODE_NIGHT_YES
        }

        object Black : Theme("amoled") {
            override val resId: Int
                get() = R.style.Theme_Main_Amoled
            override val nightMode: Int
                get() = AppCompatDelegate.MODE_NIGHT_YES
        }
    }

    sealed class DefaultTab(override val valueString: String) : Enumeration<DefaultTab> {
        override val values: List<DefaultTab>
            get() = listOf(Explore, Latest, Installed)

        object Explore : DefaultTab(NavItem.Explore.destination)
        object Latest : DefaultTab(NavItem.Latest.destination)
        object Installed : DefaultTab(NavItem.Installed.destination)
    }

    operator fun <T> get(key: Key<T>): T {
        return key.default.get(preferences, key.name, key.default)
    }

    operator fun <T> set(key: Key<T>, value: T) {
        key.default.set(preferences, key.name, value)
    }
}
