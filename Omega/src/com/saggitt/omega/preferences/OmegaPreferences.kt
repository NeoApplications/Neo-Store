/*
 *  This file is part of Omega Launcher
 *  Copyright (c) 2021   Saul Henriquez
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saggitt.omega.preferences

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Looper
import com.android.launcher3.InvariantDeviceProfile
import com.android.launcher3.LauncherFiles
import com.android.launcher3.R
import com.android.launcher3.Utilities.makeComponentKey
import com.android.launcher3.util.ComponentKey
import com.android.launcher3.util.Executors.MAIN_EXECUTOR
import com.android.launcher3.util.Themes
import com.saggitt.omega.icons.CustomAdaptiveIconDrawable
import com.saggitt.omega.icons.IconShape
import com.saggitt.omega.icons.IconShapeManager
import com.saggitt.omega.theme.ThemeManager
import com.saggitt.omega.util.dpToPx
import com.saggitt.omega.util.pxToDp
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import kotlin.reflect.KProperty
import com.android.launcher3.graphics.IconShape as L3IconShape

class OmegaPreferences(private val context: Context) :
    SharedPreferences.OnSharedPreferenceChangeListener {
    val mContext = context

    val doNothing = { }
    val recreate = { recreate() }
    val restart = { restart() }
    val reloadApps = { reloadApps() }
    val reloadAll = { reloadAll() }
    private val updateBlur = { updateBlur() }
    private val idp get() = InvariantDeviceProfile.INSTANCE.get(mContext)
    private val reloadIcons = { idp.onPreferencesChanged(context) }
    private val onIconShapeChanged = {
        initializeIconShape()
        L3IconShape.init(context)
        idp.onPreferencesChanged(context)
    }

    fun initializeIconShape() {
        val shape = iconShape
        CustomAdaptiveIconDrawable.sInitialized = true
        CustomAdaptiveIconDrawable.sMaskId = shape.getHashString()
        CustomAdaptiveIconDrawable.sMask = shape.getMaskPath()
    }

    private val onChangeMap: MutableMap<String, () -> Unit> = HashMap()
    val onChangeListeners: MutableMap<String, MutableSet<OnPreferenceChangeListener>> = HashMap()
    private var onChangeCallback: OmegaPreferencesChangeCallback? = null
    val sharedPrefs = createPreferences()

    //HOME SCREEN PREFERENCES
    val usePopupMenuView by BooleanPref("pref_desktopUsePopupMenuView", true, doNothing)
    var dashProviders = StringListPref(
        "pref_dash_providers",
        listOf(
            context.getString(R.string.dash_wifi),
            context.getString(R.string.dash_mobile_network_title),
            context.getString(R.string.dash_device_settings_title),
            context.getString(R.string.launch_assistant),
            context.getString(R.string.dash_volume_title),
            context.getString(R.string.edit_dash)
        ), doNothing
    )

    val lockDesktop by BooleanPref("pref_lockDesktop", false, reloadAll)
    val hideStatusBar by BooleanPref("pref_hideStatusBar", false, restart)

    var torchState = false

    //DRAWER
    var sortMode by StringIntPref("pref_key__sort_mode", 0, reloadApps)
    var hiddenAppSet by StringSetPref("hidden-app-set", setOf(), reloadApps)

    var protectedAppsSet by StringSetPref("protected-app-set", setOf(), reloadApps)
    var enableProtectedApps by BooleanPref("pref_protected_apps", false)

    /*POPUP DIALOG PREFERENCES */
    val desktopPopupEdit by BooleanPref("desktop_popup_edit", true, doNothing)
    val desktopPopupRemove by BooleanPref("desktop_popup_remove", false, doNothing)
    val drawerPopupEdit by BooleanPref("drawer_popup_edit", true, doNothing)
    val drawerPopupUninstall by BooleanPref("drawer_popup_uninstall", false, doNothing)

    //THEME
    var launcherTheme by StringIntPref(
        "pref_launcherTheme",
        ThemeManager.getDefaultTheme()
    ) { ThemeManager.getInstance(context).updateTheme() }
    val accentColor by IntPref("pref_key__accent_color", R.color.colorAccent, recreate)
    var enableBlur by BooleanPref("pref_enableBlur", false, updateBlur)
    val blurRadius by FloatPref("pref_blurRadius", 75f, updateBlur)
    var overrideWindowCornerRadius by BooleanPref("pref_override_corner_radius", false, doNothing)
    val windowCornerRadius by FloatPref("pref_global_corner_radius", 8f, updateBlur)
    val iconPackPackage = StringPref("pref_iconPackPackage", "", reloadIcons)

    var iconShape by StringBasedPref(
        "pref_iconShape", IconShape.Circle, onIconShapeChanged,
        {
            IconShape.fromString(it) ?: IconShapeManager.getSystemIconShape(context)
        }, IconShape::toString
    ) { /* no dispose */ }


    //FOLDER
    var folderRadius by DimensionPref("pref_folder_radius", -1f, recreate)
    val customFolderBackground by BooleanPref("pref_custom_folder_background", false, recreate)
    val folderBackground by IntPref(
        "pref_folder_background",
        Themes.getAttrColor(context, R.attr.folderFillColor),
        recreate
    )

    //SMARTSPACE
    val smartspaceTime24H by BooleanPref("pref_smartspace_time_24_h", true, recreate)

    //ADVANCED
    var settingsSearch by BooleanPref("pref_settings_search", true, recreate)
    var firstRun by BooleanPref("pref_first_run", true)

    //DEVELOPER PREFERENCES
    var developerOptionsEnabled by BooleanPref("pref_showDevOptions", false, recreate)
    var desktopModeEnabled by BooleanPref("pref_desktop_mode", true, recreate)
    private val lowPerformanceMode by BooleanPref("pref_lowPerformanceMode", false, recreate)
    val enablePhysics get() = !lowPerformanceMode
    val showDebugInfo by BooleanPref("pref_showDebugInfo", true, doNothing)

    val customAppName =
        object : MutableMapPref<ComponentKey, String>("pref_appNameMap", reloadAll) {
            override fun flattenKey(key: ComponentKey) = key.toString()
            override fun unflattenKey(key: String) = makeComponentKey(context, key)
            override fun flattenValue(value: String) = value
            override fun unflattenValue(value: String) = value
        }

    private fun createPreferences(): SharedPreferences {
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

    /*Load Initial preferences*/
    private fun migrateConfig(prefs: SharedPreferences) {
        val version = prefs.getInt(VERSION_KEY, CURRENT_VERSION)
        if (version != CURRENT_VERSION) {
            with(prefs.edit()) {

                // Default accent color
                putInt("pref_key__accent_color", 0XE80142)
                initializeIconShape()
                putInt(VERSION_KEY, CURRENT_VERSION)
                commit()
            }
        }
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

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        onChangeMap[key]?.invoke()
        onChangeListeners[key]?.forEach {
            if (key != null) {
                it.onValueChanged(key, this, false)
            }
        }
    }

    fun removeOnPreferenceChangeListener(
        listener: OnPreferenceChangeListener,
        vararg keys: String
    ) {
        keys.forEach { removeOnPreferenceChangeListener(it, listener) }
    }

    fun removeOnPreferenceChangeListener(key: String, listener: OnPreferenceChangeListener) {
        onChangeListeners[key]?.remove(listener)
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

    fun updateSortApps() {
        onChangeCallback?.reloadApps()
    }

    inline fun withChangeCallback(
        crossinline callback: (OmegaPreferencesChangeCallback) -> Unit
    ): () -> Unit {
        return { getOnChangeCallback()?.let { callback(it) } }
    }

    fun recreate() {
        onChangeCallback?.recreate()
    }

    fun reloadApps() {
        onChangeCallback?.reloadApps()
    }

    private fun reloadAll() {
        onChangeCallback?.reloadAll()
    }

    fun restart() {
        onChangeCallback?.restart()
    }

    private fun updateBlur() {
        onChangeCallback?.updateBlur()
    }

    /*Base Preferences*/
    abstract inner class PrefDelegate<T : Any>(
        val key: String,
        val defaultValue: T,
        private val onChange: () -> Unit
    ) {

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

    open inner class BooleanPref(
        key: String,
        defaultValue: Boolean = false,
        onChange: () -> Unit = doNothing
    ) : PrefDelegate<Boolean>(key, defaultValue, onChange) {
        override fun onGetValue(): Boolean = sharedPrefs.getBoolean(getKey(), defaultValue)

        override fun onSetValue(value: Boolean) {
            edit { putBoolean(getKey(), value) }
        }
    }

    open inner class DimensionPref(
        key: String,
        defaultValue: Float = 0f,
        onChange: () -> Unit = doNothing
    ) :
        PrefDelegate<Float>(key, defaultValue, onChange) {

        override fun onGetValue(): Float = dpToPx(sharedPrefs.getFloat(getKey(), defaultValue))

        override fun onSetValue(value: Float) {
            edit { putFloat(getKey(), pxToDp(value)) }
        }
    }

    open inner class FloatPref(
        key: String,
        defaultValue: Float = 0f,
        onChange: () -> Unit = doNothing
    ) : PrefDelegate<Float>(key, defaultValue, onChange) {
        override fun onGetValue(): Float = sharedPrefs.getFloat(getKey(), defaultValue)

        override fun onSetValue(value: Float) {
            edit { putFloat(getKey(), value) }
        }
    }

    open inner class IntPref(key: String, defaultValue: Int = 0, onChange: () -> Unit = doNothing) :
        PrefDelegate<Int>(key, defaultValue, onChange) {
        override fun onGetValue(): Int = sharedPrefs.getInt(getKey(), defaultValue)

        override fun onSetValue(value: Int) {
            edit { putInt(getKey(), value) }
        }
    }

    open inner class StringPref(
        key: String,
        defaultValue: String = "",
        onChange: () -> Unit = doNothing
    ) : PrefDelegate<String>(key, defaultValue, onChange) {
        override fun onGetValue(): String = sharedPrefs.getString(getKey(), defaultValue)!!

        override fun onSetValue(value: String) {
            edit { putString(getKey(), value) }
        }
    }

    open inner class StringBasedPref<T : Any>(
        key: String, defaultValue: T, onChange: () -> Unit = doNothing,
        private val fromString: (String) -> T,
        private val toString: (T) -> String,
        private val dispose: (T) -> Unit
    ) :
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

    open inner class StringIntPref(
        key: String,
        defaultValue: Int = 0,
        onChange: () -> Unit = doNothing
    ) : PrefDelegate<Int>(key, defaultValue, onChange) {
        override fun onGetValue(): Int = try {
            sharedPrefs.getString(getKey(), "$defaultValue")!!.toInt()
        } catch (e: Exception) {
            sharedPrefs.getInt(getKey(), defaultValue)
        }

        override fun onSetValue(value: Int) {
            edit { putString(getKey(), "$value") }
        }
    }

    open inner class StringSetPref(
        key: String,
        defaultValue: Set<String>,
        onChange: () -> Unit = doNothing
    ) :
        PrefDelegate<Set<String>>(key, defaultValue, onChange) {
        override fun onGetValue(): Set<String> = sharedPrefs.getStringSet(getKey(), defaultValue)!!

        override fun onSetValue(value: Set<String>) {
            edit { putStringSet(getKey(), value) }
        }
    }

    inner class StringListPref(
        prefKey: String,
        default: List<String> = emptyList(),
        onChange: () -> Unit = doNothing
    ) : MutableListPref<String>(prefKey, onChange, default) {

        override fun unflattenValue(value: String) = value
        override fun flattenValue(value: String) = value
    }

    abstract inner class MutableListPref<T>(
        private val prefs: SharedPreferences, private val prefKey: String,
        onChange: () -> Unit = doNothing,
        default: List<T> = emptyList()
    ) : PrefDelegate<List<T>>(prefKey, default, onChange) {

        constructor(
            prefKey: String, onChange: () -> Unit = doNothing,
            default: List<T> = emptyList()
        ) : this(sharedPrefs, prefKey, onChange, default)

        private val valueList = arrayListOf<T>()
        private val listeners = mutableSetOf<MutableListPrefChangeListener>()

        init {
            val arr: JSONArray = try {
                JSONArray(prefs.getString(prefKey, getJsonString(default)))
            } catch (e: ClassCastException) {
                e.printStackTrace()
                JSONArray()
            }
            (0 until arr.length()).mapTo(valueList) { unflattenValue(arr.getString(it)) }
            if (onChange != doNothing) {
                onChangeMap[prefKey] = onChange
            }
        }

        fun toList() = ArrayList<T>(valueList)

        open fun flattenValue(value: T) = value.toString()

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

    abstract inner class MutableMapPref<K, V>(
        private val prefKey: String,
        onChange: () -> Unit = doNothing
    ) {
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

    interface OnPreferenceChangeListener {
        fun onValueChanged(key: String, prefs: OmegaPreferences, force: Boolean)
    }

    /*
        Helper functions and class
    */

    var blockingEditing = false
    var bulkEditing = false
    var editor: SharedPreferences.Editor? = null
    fun commitOrApply(editor: SharedPreferences.Editor, commit: Boolean) {
        if (commit) {
            editor.commit()
        } else {
            editor.apply()
        }
    }

    fun beginBlockingEdit() {
        blockingEditing = true
    }

    fun endBlockingEdit() {
        blockingEditing = false
    }

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

    companion object {

        @SuppressLint("StaticFieldLeak")
        private var INSTANCE: OmegaPreferences? = null
        const val CURRENT_VERSION = 300
        const val VERSION_KEY = "config_version"
        fun getInstance(context: Context): OmegaPreferences {
            if (INSTANCE == null) {
                return if (Looper.myLooper() == Looper.getMainLooper()) {
                    OmegaPreferences(context)
                } else {
                    try {
                        MAIN_EXECUTOR.submit(Callable { getInstance(context) }).get()
                    } catch (e: InterruptedException) {
                        throw RuntimeException(e)
                    } catch (e: ExecutionException) {
                        throw RuntimeException(e)
                    }

                }
            }
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