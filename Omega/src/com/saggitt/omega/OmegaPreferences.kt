/*
 *  Copyright (c) 2020 Omega Launcher
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.saggitt.omega

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Looper
import android.text.TextUtils
import com.android.launcher3.LauncherAppState
import com.android.launcher3.LauncherFiles
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.Utilities.makeComponentKey
import com.android.launcher3.allapps.search.DefaultAppSearchAlgorithm
import com.android.launcher3.util.ComponentKey
import com.android.launcher3.util.Executors
import com.android.launcher3.util.Executors.MAIN_EXECUTOR
import com.saggitt.omega.allapps.PredictionsFloatingHeader
import com.saggitt.omega.groups.AppGroupsManager
import com.saggitt.omega.groups.DrawerTabs
import com.saggitt.omega.iconpack.IconPackManager
import com.saggitt.omega.preferences.GridSize
import com.saggitt.omega.preferences.GridSize2D
import com.saggitt.omega.search.SearchProviderController
import com.saggitt.omega.theme.ThemeManager
import com.saggitt.omega.util.Config
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import kotlin.math.roundToInt
import kotlin.reflect.KProperty

class OmegaPreferences(val context: Context) : SharedPreferences.OnSharedPreferenceChangeListener {
    val mContext = context;

    val doNothing = { }
    val restart = { restart() }
    val reloadApps = { reloadApps() }
    val reloadAll = { reloadAll() }
    private val refreshGrid = { refreshGrid() }
    val updateBlur = { updateBlur() }
    val reloadIcons = { reloadIcons() }
    private val reloadIconPacks = { IconPackManager.getInstance(context).packList.reloadPacks() }
    val recreate = { recreate() }
    val omegaConfig = Config(context)

    private val onChangeMap: MutableMap<String, () -> Unit> = HashMap()
    val onChangeListeners: MutableMap<String, MutableSet<OnPreferenceChangeListener>> = HashMap()
    private var onChangeCallback: OmegaPreferencesChangeCallback? = null
    val sharedPrefs = migratePrefs()

    /* --APP DRAWER-- */
    var sortMode by StringIntPref("pref_key__sort_mode", 0, recreate)
    val showPredictions by BooleanPref("pref_show_predictions", false, doNothing)
    val showAllAppsLabel by BooleanPref("pref_showAllAppsLabel", false) {
        val header = onChangeCallback?.launcher?.appsView?.floatingHeaderView as? PredictionsFloatingHeader
        header?.updateShowAllAppsLabel()
    }
    var hiddenAppSet by StringSetPref("hidden-app-set", Collections.emptySet(), reloadApps)
    var hiddenPredictionAppSet by StringSetPref("pref_hidden_prediction_set", Collections.emptySet(), doNothing)
    var allAppsIconScale by FloatPref("allAppsIconSize", 1f, reloadApps)
    val drawerLabelColor by IntPref("pref_drawer_label_color", R.color.qsb_drawer_text_color_normal, reloadApps)
    val drawerTextScale by FloatPref("pref_allAppsIconTextScale", 1f, recreate)
    val drawerPaddingScale by FloatPref("pref_allAppsPaddingScale", 1.0f, recreate)
    private val drawerMultilineLabel by BooleanPref("pref_iconLabelsInTwoLines", false, recreate)
    val drawerLabelRows get() = if (drawerMultilineLabel) 2 else 1
    val hideAllAppsAppLabels by BooleanPref("pref_hideAllAppsAppLabels", false, recreate)
    val currentTabsModel
        get() = appGroupsManager.getEnabledModel() as? DrawerTabs ?: appGroupsManager.drawerTabs
    val drawerTabs get() = appGroupsManager.drawerTabs
    val appGroupsManager by lazy { AppGroupsManager(this) }
    val separateWorkApps by BooleanPref("pref_separateWorkApps", true, recreate)
    val drawerBackgroundColor by IntPref("pref_drawer_background_color", R.color.white, recreate)
    val customBackground by BooleanPref("pref_enable_custom_background", false, doNothing)
    val allAppsOpacity by AlphaPref("pref_allAppsOpacitySB", -1, recreate)

    /* --DESKTOP-- */
    var autoAddInstalled by BooleanPref("pref_add_icon_to_home", true, doNothing)
    var dashEnable by BooleanPref("pref_key__dash_enable", true, recreate)
    val desktopTextScale by FloatPref("pref_iconTextScale", 1f, reloadAll)
    val desktopIconScale by FloatPref("pref_iconSize", 1f, recreate)
    private var gridSizeDelegate = ResettableLazy {
        GridSize2D(this, "numRows", "numColumns",
                LauncherAppState.getIDP(context), refreshGrid)
    }
    val gridSize by gridSizeDelegate
    val allowFullWidthWidgets by BooleanPref("pref_fullWidthWidgets", false, restart)
    private val homeMultilineLabel by BooleanPref("pref_homeIconLabelsInTwoLines", false, recreate)
    val homeLabelRows get() = if (homeMultilineLabel) 2 else 1
    val usePopupMenuView by BooleanPref("pref_desktopUsePopupMenuView", true, doNothing)
    val hideAppLabels by BooleanPref("pref_hideAppLabels", false, recreate)
    val lockDesktop by BooleanPref("pref_lockDesktop", false, reloadAll)
    val hideStatusBar by BooleanPref("pref_hideStatusBar", false, restart)

    /* --DOCK-- */
    var dockHide by BooleanPref("pref_hideHotseat", false, restart)
    val dockIconScale by FloatPref("pref_hotseatIconSize", 1f, recreate)
    var dockSearchBarPref by BooleanPref("pref_dock_search", true, restart)
    inline val dockSearchBar get() = !dockHide && dockSearchBarPref
    var dockScale by FloatPref("pref_dockScale", -1f, recreate)
    val dockBackground by BooleanPref("pref_dockBackground", false, recreate)
    inline val dockGradientStyle get() = !dockBackground
    var dockOpacity by AlphaPref("pref_hotseatCustomOpacity", -1, recreate)
    val dockBackgroundColor by IntPref("pref_dock_background_color", R.color.transparentish, recreate)
    private val dockGridSizeDelegate = ResettableLazy { GridSize(this, "numHotseatIcons", LauncherAppState.getIDP(context), restart) }
    val dockGridSize by dockGridSizeDelegate
    var dockRadius by FloatPref("pref_dockRadius", 16f, recreate)
    var dockShadow by BooleanPref("pref_dockShadow", false, recreate)
    var dockShowArrow by BooleanPref("pref_hotseatShowArrow", true, recreate)
    val dockShowPageIndicator by BooleanPref("pref_hotseatShowPageIndicator",
            true, { onChangeCallback?.updatePageIndicator() })
    val hideDockLabels by BooleanPref("pref_hideDockLabels", true, restart)
    private val dockMultilineLabel by BooleanPref("pref_dockIconLabelsInTwoLines", false, recreate)
    val dockLabelRows get() = if (dockMultilineLabel) 2 else 1

    /* --THEME-- */
    var launcherTheme by StringIntPref("pref_launcherTheme", 1) { ThemeManager.getInstance(context).updateTheme() }
    val accentColor by IntPref("pref_key__accent_color", R.color.colorAccent, recreate)
    var iconShape by StringPref("pref_iconShape", "", doNothing)
    val iconPackMasking by BooleanPref("pref_iconPackMasking", true, reloadIcons)
    private var iconPack by StringPref("pref_icon_pack", "", reloadIconPacks)
    val iconPacks = object : MutableListPref<String>("pref_iconPacks", reloadIconPacks,
            if (!TextUtils.isEmpty(iconPack)) listOf(iconPack) else omegaConfig.defaultIconPacks.asList()) {
        override fun unflattenValue(value: String) = value
    }
    var colorizedLegacyTreatment by BooleanPref("pref_colorizeGeneratedBackgrounds", false, doNothing)
    var enableWhiteOnlyTreatment by BooleanPref("pref_enableWhiteOnlyTreatment", false, doNothing)
    var enableLegacyTreatment by BooleanPref("pref_enableLegacyTreatment", false, doNothing)
    var adaptifyIconPacks by BooleanPref("pref_generateAdaptiveForIconPack", false, doNothing)
    var forceShapeless by BooleanPref("pref_forceShapeless", false, doNothing)

    /* --NOTIFICATION-- */
    val notificationCount: Boolean by BooleanPref("pref_notification_count", true, restart)
    val notificationBackground by IntPref("pref_notification_background", R.color.notification_background, recreate)

    /* --ADVANCED-- */
    var settingsSearch by BooleanPref("pref_settings_search", true, recreate)

    /* --BLUR--*/
    var enableBlur by BooleanPref("pref_enableBlur", omegaConfig.defaultEnableBlur(), updateBlur)
    val blurRadius by FloatPref("pref_blurRadius", omegaConfig.defaultBlurStrength, updateBlur)

    /* --SEARCH-- */
    var searchProvider by StringPref("pref_globalSearchProvider", omegaConfig.defaultSearchProvider) {
        SearchProviderController.getInstance(context).onSearchProviderChanged()
    }
    val searchHiddenApps by BooleanPref(DefaultAppSearchAlgorithm.SEARCH_HIDDEN_APPS, false)

    val recentBackups = object : MutableListPref<Uri>(
            Utilities.getDevicePrefs(context), "pref_recentBackups") {
        override fun unflattenValue(value: String) = Uri.parse(value)
    }

    /* --DEV-- */
    var developerOptionsEnabled by BooleanPref("pref_showDevOptions", false, recreate)
    val showDebugInfo by BooleanPref("pref_showDebugInfo", false, doNothing)
    val lowPerformanceMode by BooleanPref("pref_lowPerformanceMode", false, recreate)
    val enablePhysics get() = !lowPerformanceMode
    val debugOkHttp by BooleanPref("pref_debugOkhttp", onChange = restart)

    var restoreSuccess by BooleanPref("pref_restoreSuccess", false)

    val customAppName = object : MutableMapPref<ComponentKey, String>("pref_appNameMap", reloadAll) {
        override fun flattenKey(key: ComponentKey) = key.toString()
        override fun unflattenKey(key: String) = makeComponentKey(context, key)
        override fun flattenValue(value: String) = value
        override fun unflattenValue(value: String) = value
    }

    val customAppIcon = object : MutableMapPref<ComponentKey, IconPackManager.CustomIconEntry>("pref_appIconMap", reloadAll) {
        override fun flattenKey(key: ComponentKey) = key.toString()
        override fun unflattenKey(key: String) = makeComponentKey(context, key)
        override fun flattenValue(value: IconPackManager.CustomIconEntry) = value.toString()
        override fun unflattenValue(value: String) = IconPackManager.CustomIconEntry.fromString(value)
    }


    private fun migratePrefs(): SharedPreferences {
        val dir = mContext.cacheDir.parent
        val oldFile = File(dir, "shared_prefs/" + LauncherFiles.OLD_SHARED_PREFERENCES_KEY + ".xml")
        val newFile = File(dir, "shared_prefs/" + LauncherFiles.SHARED_PREFERENCES_KEY + ".xml")
        if (oldFile.exists() && !newFile.exists()) {
            oldFile.renameTo(newFile)
            oldFile.delete()
        }
        return mContext.applicationContext
                .getSharedPreferences(LauncherFiles.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)
                .apply {
                    migrateConfig(this)
                }
    }

    fun migrateConfig(prefs: SharedPreferences) {
        val version = prefs.getInt(VERSION_KEY, CURRENT_VERSION)
        if (version != CURRENT_VERSION) {
            with(prefs.edit()) {
                // Migration codes here

                if (version == 100) {
                    initialConfig(this, prefs)
                }

                putInt(VERSION_KEY, CURRENT_VERSION)
                commit()
            }
        }
    }

    private fun initialConfig(editor: SharedPreferences.Editor, prefs: SharedPreferences) = with(editor) {
        // Set flags
        putBoolean("pref_legacyUpgrade", true)
        putBoolean("pref_restoreSuccess", false)

        // misc
        putBoolean("pref_add_icon_to_home", prefs.getBoolean("pref_autoAddShortcuts", true))
        putString("pref_iconShape", "")
        putInt("pref_notification_background", R.color.notification_background)

    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        onChangeMap[key]?.invoke()
        onChangeListeners[key]?.forEach {
            if (key != null) {
                it.onValueChanged(key, this, false)
            }
        }
    }

    fun registerCallback(callback: OmegaPreferencesChangeCallback) {
        sharedPrefs.registerOnSharedPreferenceChangeListener(this)
        onChangeCallback = callback
    }

    fun unregisterCallback() {
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(this)
        onChangeCallback = null
    }

    fun getOnChangeCallback() = onChangeCallback

    fun recreate() {
        onChangeCallback?.recreate()
    }

    fun reloadApps() {
        onChangeCallback?.reloadApps()
    }

    private fun reloadAll() {
        onChangeCallback?.reloadAll()
    }

    fun reloadDrawer() {
        onChangeCallback?.reloadDrawer()
    }

    fun restart() {
        onChangeCallback?.restart()
    }

    fun refreshGrid() {
        onChangeCallback?.refreshGrid()
    }

    private fun updateBlur() {
        onChangeCallback?.updateBlur()
    }

    fun reloadIcons() {
        LauncherAppState.getInstance(context).reloadIconCache()
        MAIN_EXECUTOR.post { onChangeCallback?.recreate() }
    }

    fun updateSortApps() {
        onChangeCallback?.forceReloadApps()
    }

    inline fun withChangeCallback(
            crossinline callback: (OmegaPreferencesChangeCallback) -> Unit): () -> Unit {
        return { getOnChangeCallback()?.let { callback(it) } }
    }

    fun addOnPreferenceChangeListener(listener: OnPreferenceChangeListener, vararg keys: String) {
        keys.forEach { addOnPreferenceChangeListener(it, listener) }
    }

    fun addOnPreferenceChangeListener(key: String, listener: OnPreferenceChangeListener) {
        if (onChangeListeners[key] == null) {
            onChangeListeners[key] = HashSet()
        }
        onChangeListeners[key]?.add(listener)
        listener.onValueChanged(key, this, true)
    }

    fun removeOnPreferenceChangeListener(listener: OnPreferenceChangeListener, vararg keys: String) {
        keys.forEach { removeOnPreferenceChangeListener(it, listener) }
    }

    fun removeOnPreferenceChangeListener(key: String, listener: OnPreferenceChangeListener) {
        onChangeListeners[key]?.remove(listener)
    }

    // ----------------
    // Helper functions and class
    // ----------------

    fun commitOrApply(editor: SharedPreferences.Editor, commit: Boolean) {
        if (commit) {
            editor.commit()
        } else {
            editor.apply()
        }
    }

    var blockingEditing = false
    var bulkEditing = false
    var editor: SharedPreferences.Editor? = null

    fun beginBlockingEdit() {
        blockingEditing = true
    }

    fun endBlockingEdit() {
        blockingEditing = false
    }

    @SuppressLint("CommitPrefEdits")
    fun beginBulkEdit() {
        bulkEditing = true
        editor = sharedPrefs.edit()
    }

    fun endBulkEdit() {
        bulkEditing = false
        commitOrApply(editor!!, blockingEditing)
        editor = null
    }

    inline fun blockingEdit(body: OmegaPreferences.() -> Unit) {
        beginBlockingEdit()
        body(this)
        endBlockingEdit()
    }

    inline fun bulkEdit(body: OmegaPreferences.() -> Unit) {
        beginBulkEdit()
        body(this)
        endBulkEdit()
    }

    //PREFERENCE CLASSES
    abstract inner class MutableListPref<T>(private val prefs: SharedPreferences, private val prefKey: String,
                                            onChange: () -> Unit = doNothing,
                                            default: List<T> = emptyList()) : PrefDelegate<List<T>>(prefKey, default, onChange) {

        constructor(prefKey: String, onChange: () -> Unit = doNothing,
                    default: List<T> = emptyList()) : this(sharedPrefs, prefKey, onChange, default)

        private val valueList = ArrayList<T>()
        private val listeners: MutableSet<MutableListPrefChangeListener> =
                Collections.newSetFromMap(WeakHashMap())

        init {
            var arr: JSONArray
            try {
                arr = JSONArray(prefs.getString(prefKey, getJsonString(default)))
            } catch (e: ClassCastException) {
                e.printStackTrace()
                arr = JSONArray()
            }
            (0 until arr.length()).mapTo(valueList) { unflattenValue(arr.getString(it)) }
            if (onChange != doNothing) {
                onChangeMap[prefKey] = onChange
            }
        }

        fun toList() = ArrayList<T>(valueList)

        open fun flattenValue(value: T) = value.toString()
        open fun customAdder(value: T): Unit = error("not implemented in base class")

        abstract fun unflattenValue(value: String): T

        operator fun get(position: Int): T {
            return valueList[position]
        }

        operator fun set(position: Int, value: T) {
            valueList[position] = value
            saveChanges()
        }

        fun getAll(): List<T> = valueList

        fun setAll(value: List<T>) {
            valueList.clear()
            valueList.addAll(value)
            saveChanges()
        }

        fun add(value: T) {
            valueList.add(value)
            saveChanges()
        }

        fun add(position: Int, value: T) {
            valueList.add(position, value)
            saveChanges()
        }

        fun remove(value: T) {
            valueList.remove(value)
            saveChanges()
        }

        fun removeAt(position: Int) {
            valueList.removeAt(position)
            saveChanges()
        }

        fun contains(value: T): Boolean {
            return valueList.contains(value)
        }

        fun replaceWith(newList: List<T>) {
            valueList.clear()
            valueList.addAll(newList)
            saveChanges()
        }

        fun getList() = valueList

        fun addListener(listener: MutableListPrefChangeListener) {
            listeners.add(listener)
        }

        fun removeListener(listener: MutableListPrefChangeListener) {
            listeners.remove(listener)
        }

        private fun saveChanges() {
            @SuppressLint("CommitPrefEdits") val editor = prefs.edit()
            editor.putString(prefKey, getJsonString(valueList))
            if (!bulkEditing) commitOrApply(editor, blockingEditing)
            listeners.forEach { it.onListPrefChanged(prefKey) }
        }

        private fun getJsonString(list: List<T>): String {
            val arr = JSONArray()
            list.forEach { arr.put(flattenValue(it)) }
            return arr.toString()
        }

        override fun onGetValue(): List<T> {
            return getAll()
        }

        override fun onSetValue(value: List<T>) {
            setAll(value)
        }
    }

    interface MutableListPrefChangeListener {
        fun onListPrefChanged(key: String)
    }

    abstract inner class MutableMapPref<K, V>(private val prefKey: String, onChange: () -> Unit = doNothing) {
        private val valueMap = HashMap<K, V>()

        init {
            val obj = JSONObject(sharedPrefs.getString(prefKey, "{}"))
            obj.keys().forEach {
                valueMap[unflattenKey(it)] = unflattenValue(obj.getString(it))
            }
            if (onChange !== doNothing) {
                onChangeMap[prefKey] = onChange
            }
        }

        fun toMap() = HashMap<K, V>(valueMap)

        open fun flattenKey(key: K) = key.toString()
        abstract fun unflattenKey(key: String): K

        open fun flattenValue(value: V) = value.toString()
        abstract fun unflattenValue(value: String): V

        operator fun set(key: K, value: V?) {
            if (value != null) {
                valueMap[key] = value
            } else {
                valueMap.remove(key)
            }
            saveChanges()
        }

        private fun saveChanges() {
            val obj = JSONObject()
            valueMap.entries.forEach { obj.put(flattenKey(it.key), flattenValue(it.value)) }
            @SuppressLint("CommitPrefEdits") val editor =
                    if (bulkEditing) editor!! else sharedPrefs.edit()
            editor.putString(prefKey, obj.toString())
            if (!bulkEditing) commitOrApply(editor, blockingEditing)
        }

        operator fun get(key: K): V? {
            return valueMap[key]
        }

        fun clear() {
            valueMap.clear()
            saveChanges()
        }
    }

    inner class ResettableLazy<out T : Any>(private val create: () -> T) {

        private var initialized = false
        private var currentValue: T? = null

        operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
            if (!initialized) {
                currentValue = create()
                initialized = true
            }
            return currentValue!!
        }

        fun resetValue() {
            initialized = false
            currentValue = null
        }
    }

    abstract inner class PrefDelegate<T : Any>(val key: String, val defaultValue: T, private val onChange: () -> Unit) {

        private var cached = false
        private lateinit var value: T

        init {
            onChangeMap[key] = { onValueChanged() }
        }

        operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
            if (!cached) {
                value = onGetValue()
                cached = true
            }
            return value
        }

        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            cached = false
            onSetValue(value)
        }

        abstract fun onGetValue(): T

        abstract fun onSetValue(value: T)

        protected inline fun edit(body: SharedPreferences.Editor.() -> Unit) {
            @SuppressLint("CommitPrefEdits")
            val editor = if (bulkEditing) editor!! else sharedPrefs.edit()
            body(editor)
            if (!bulkEditing)
                commitOrApply(editor, blockingEditing)
        }

        internal fun getKey() = key

        private fun onValueChanged() {
            discardCachedValue()
            cached = false
            onChange.invoke()
        }

        private fun discardCachedValue() {
            if (cached) {
                cached = false
                value.let(::disposeOldValue)
            }
        }

        open fun disposeOldValue(oldValue: T) {
        }
    }

    open inner class IntPref(key: String, defaultValue: Int = 0, onChange: () -> Unit = doNothing) : PrefDelegate<Int>(key, defaultValue, onChange) {
        override fun onGetValue(): Int = sharedPrefs.getInt(getKey(), defaultValue)

        override fun onSetValue(value: Int) {
            edit { putInt(getKey(), value) }
        }
    }

    open inner class AlphaPref(key: String, defaultValue: Int = 0, onChange: () -> Unit = doNothing) :
            PrefDelegate<Int>(key, defaultValue, onChange) {
        override fun onGetValue(): Int = (sharedPrefs.getFloat(getKey(), defaultValue.toFloat() / 255) * 255).roundToInt()

        override fun onSetValue(value: Int) {
            edit { putFloat(getKey(), value.toFloat() / 255) }
        }
    }

    open inner class IntSetPref(key: String, defaultValue: Set<Int>, onChange: () -> Unit = doNothing) :
            PrefDelegate<Set<Int>>(key, defaultValue, onChange) {

        private val defaultStringSet = super.defaultValue.mapTo(mutableSetOf()) { "$it" }

        override fun onGetValue(): Set<Int> = sharedPrefs.getStringSet(getKey(), defaultStringSet)!!
                .mapTo(mutableSetOf()) { Integer.parseInt(it) }

        override fun onSetValue(value: Set<Int>) {
            edit {
                putStringSet(getKey(), value.mapTo(mutableSetOf()) { "$it" })
            }
        }
    }

    inline fun <reified T : Enum<T>> EnumPref(key: String, defaultValue: T,
                                              noinline onChange: () -> Unit = doNothing): PrefDelegate<T> {
        return IntBasedPref(key, defaultValue, onChange, { value ->
            enumValues<T>().firstOrNull { item -> item.ordinal == value } ?: defaultValue
        }, { it.ordinal }, { })
    }

    open inner class IntBasedPref<T : Any>(key: String, defaultValue: T, onChange: () -> Unit = doNothing,
                                           private val fromInt: (Int) -> T,
                                           private val toInt: (T) -> Int,
                                           private val dispose: (T) -> Unit) : PrefDelegate<T>(key, defaultValue, onChange) {
        override fun onGetValue(): T {
            return if (sharedPrefs.contains(key)) {
                fromInt(sharedPrefs.getInt(getKey(), toInt(defaultValue)))
            } else defaultValue
        }

        override fun onSetValue(value: T) {
            edit { putInt(getKey(), toInt(value)) }
        }

        override fun disposeOldValue(oldValue: T) {
            dispose(oldValue)
        }
    }

    open inner class BooleanPref(key: String, defaultValue: Boolean = false, onChange: () -> Unit = doNothing) : PrefDelegate<Boolean>(key, defaultValue, onChange) {
        override fun onGetValue(): Boolean = sharedPrefs.getBoolean(getKey(), defaultValue)

        override fun onSetValue(value: Boolean) {
            edit { putBoolean(getKey(), value) }
        }
    }

    open inner class FloatPref(key: String, defaultValue: Float = 0f, onChange: () -> Unit = doNothing) : PrefDelegate<Float>(key, defaultValue, onChange) {
        override fun onGetValue(): Float = sharedPrefs.getFloat(getKey(), defaultValue)

        override fun onSetValue(value: Float) {
            edit { putFloat(getKey(), value) }
        }
    }

    open inner class StringBasedPref<T : Any>(key: String, defaultValue: T, onChange: () -> Unit = doNothing,
                                              private val fromString: (String) -> T,
                                              private val toString: (T) -> String,
                                              private val dispose: (T) -> Unit) :
            PrefDelegate<T>(key, defaultValue, onChange) {
        override fun onGetValue(): T = sharedPrefs.getString(getKey(), null)?.run(fromString)
                ?: defaultValue

        override fun onSetValue(value: T) {
            edit { putString(getKey(), toString(value)) }
        }

        override fun disposeOldValue(oldValue: T) {
            dispose(oldValue)
        }
    }

    open inner class StringPref(key: String, defaultValue: String = "", onChange: () -> Unit = doNothing) : PrefDelegate<String>(key, defaultValue, onChange) {
        override fun onGetValue(): String = sharedPrefs.getString(getKey(), defaultValue)!!

        override fun onSetValue(value: String) {
            edit { putString(getKey(), value) }
        }
    }

    open inner class StringSetPref(key: String, defaultValue: Set<String>, onChange: () -> Unit = doNothing) :
            PrefDelegate<Set<String>>(key, defaultValue, onChange) {
        override fun onGetValue(): Set<String> = sharedPrefs.getStringSet(getKey(), defaultValue)!!

        override fun onSetValue(value: Set<String>) {
            edit { putStringSet(getKey(), value) }
        }
    }

    open inner class StringIntPref(key: String, defaultValue: Int = 0, onChange: () -> Unit = doNothing) : PrefDelegate<Int>(key, defaultValue, onChange) {
        override fun onGetValue(): Int = try {
            sharedPrefs.getString(getKey(), "$defaultValue")!!.toInt()
        } catch (e: Exception) {
            sharedPrefs.getInt(getKey(), defaultValue)
        }

        override fun onSetValue(value: Int) {
            edit { putString(getKey(), "$value") }
        }
    }

    interface OnPreferenceChangeListener {
        fun onValueChanged(key: String, prefs: OmegaPreferences, force: Boolean)
    }

    companion object {
        private var INSTANCE: OmegaPreferences? = null
        const val CURRENT_VERSION = 200
        const val VERSION_KEY = "config_version"

        fun getInstance(context: Context): OmegaPreferences {
            if (INSTANCE == null) {
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    INSTANCE = OmegaPreferences(context.applicationContext)
                } else {
                    try {
                        return Executors.MAIN_EXECUTOR.submit(Callable { getInstance(context) }).get()
                    } catch (e: InterruptedException) {
                        throw RuntimeException(e)
                    } catch (e: ExecutionException) {
                        throw RuntimeException(e)
                    }

                }
            }
            return INSTANCE!!
        }

        fun getInstanceNoCreate(): OmegaPreferences {
            return INSTANCE!!
        }

        fun destroyInstance() {
            INSTANCE?.apply {
                onChangeListeners.clear()
                onChangeCallback = null
            }
        }
    }
}