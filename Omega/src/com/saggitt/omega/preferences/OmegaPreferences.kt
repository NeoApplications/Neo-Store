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
import android.util.Log
import com.android.launcher3.InvariantDeviceProfile
import com.android.launcher3.LauncherFiles
import com.android.launcher3.R
import com.android.launcher3.util.MainThreadInitializedObject
import com.android.launcher3.util.Themes
import com.saggitt.omega.PREFS_ACCENT
import com.saggitt.omega.PREFS_SORT
import com.saggitt.omega.groups.AppGroupsManager
import com.saggitt.omega.icons.CustomAdaptiveIconDrawable
import com.saggitt.omega.icons.IconShape
import com.saggitt.omega.icons.IconShapeManager
import com.saggitt.omega.search.SearchProviderController
import com.saggitt.omega.theme.ThemeManager
import com.saggitt.omega.util.dpToPx
import com.saggitt.omega.util.pxToDp
import org.json.JSONArray
import java.io.File
import kotlin.math.roundToInt
import kotlin.reflect.KProperty
import com.android.launcher3.graphics.IconShape as L3IconShape

class OmegaPreferences(val context: Context) : SharedPreferences.OnSharedPreferenceChangeListener {

    private val onChangeMap: MutableMap<String, () -> Unit> = HashMap()
    val onChangeListeners: MutableMap<String, MutableSet<OnPreferenceChangeListener>> = HashMap()
    private var onChangeCallback: OmegaPreferencesChangeCallback? = null
    val sharedPrefs = createPreferences()

    val doNothing = { }
    val restart = { restart() }
    val reloadApps = { reloadApps() }
    val reloadAll = { reloadAll() }
    private val updateBlur = { updateBlur() }
    val recreate = { recreate() }

    private val idp get() = InvariantDeviceProfile.INSTANCE.get(context)
    val reloadIcons = { idp.onPreferencesChanged(context) }
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

    private fun createPreferences(): SharedPreferences {
        val dir = context.cacheDir.parent
        val oldFile = File(dir, "shared_prefs/" + LauncherFiles.OLD_SHARED_PREFERENCES_KEY + ".xml")
        val newFile = File(dir, "shared_prefs/" + LauncherFiles.SHARED_PREFERENCES_KEY + ".xml")
        if (oldFile.exists() && !newFile.exists()) {
            oldFile.renameTo(newFile)
            oldFile.delete()
        }
        return context.applicationContext
                .getSharedPreferences(LauncherFiles.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)
    }

    // HOME SCREEN
    val desktopIconScale by FloatPref("pref_home_icon_scale", 1f, reloadIcons)
    val usePopupMenuView by BooleanPref("pref_desktopUsePopupMenuView", true, doNothing)
    var dashProviders = StringListPref(
            "pref_dash_providers",
            listOf("17", "15", "4", "6", "8", "5"), doNothing
    )

    val lockDesktop by BooleanPref("pref_lockDesktop", false, reloadAll)
    val hideStatusBar by BooleanPref("pref_hideStatusBar", false, doNothing)
    val enableMinus by BooleanPref("pref_enable_minus_one", false, restart)
    var allowEmptyScreens by BooleanPref("pref_keepEmptyScreens", false)
    val hideAppLabels by BooleanPref("pref_hide_app_label", false, reloadApps)
    val desktopTextScale by FloatPref("pref_icon_text_scale", 1f, reloadApps)
    val allowFullWidthWidgets by BooleanPref("pref_full_width_widgets", false, restart)
    private val homeMultilineLabel by BooleanPref("pref_icon_labels_two_lines", false, reloadApps)
    val homeLabelRows get() = if (homeMultilineLabel) 2 else 1

    var torchState by BooleanPref("pref_torch", false, doNothing)

    // DOCK
    var dockHide by BooleanPref("pref_hideHotseat", false, restart)
    val dockIconScale by FloatPref("pref_hotseat_icon_scale", 1f, restart)
    var dockScale by FloatPref("pref_dockScale", 1f, restart)
    val dockBackground by BooleanPref("pref_dockBackground", false, restart)
    val dockBackgroundColor by IntPref("pref_dock_background_color", 0x101010, restart)
    var dockOpacity by AlphaPref("pref_dockOpacity", -1, restart)

    // DRAWER
    var sortMode by StringIntPref(PREFS_SORT, 0, restart)
    var hiddenAppSet by StringSetPref("hidden_app_set", setOf(), reloadApps)
    var hiddenPredictionAppSet by StringSetPref(
            "pref_hidden_prediction_set",
            setOf(),
            doNothing
    )
    var protectedAppsSet by StringSetPref("protected_app_set", setOf(), reloadApps)
    var enableProtectedApps by BooleanPref("pref_protected_apps", false)
    var allAppsIconScale by FloatPref("pref_allapps_icon_scale", 1f, reloadApps)
    val allAppsTextScale by FloatPref("pref_allapps_icon_text_scale", 1f)
    val hideAllAppsAppLabels by BooleanPref("pref_hide_allapps_app_label", false, reloadApps)
    private val drawerMultilineLabel by BooleanPref(
            "pref_apps_icon_labels_two_lines",
            false,
            reloadApps
    )
    val drawerLabelRows get() = if (drawerMultilineLabel) 2 else 1
    val allAppsCellHeightMultiplier by FloatPref("pref_allAppsCellHeightMultiplier", 1F, restart)
    val separateWorkApps by BooleanPref("pref_separateWorkApps", false, recreate)
    val appGroupsManager by lazy { AppGroupsManager(this) }
    val drawerTabs get() = appGroupsManager.drawerTabs

    // POPUP DIALOG PREFERENCES
    val desktopPopupEdit by BooleanPref("desktop_popup_edit", true, doNothing)
    val desktopPopupRemove by BooleanPref("desktop_popup_remove", false, doNothing)
    val drawerPopupEdit by BooleanPref("drawer_popup_edit", true, doNothing)
    val drawerPopupUninstall by BooleanPref("drawer_popup_uninstall", false, doNothing)

    //THEME
    var launcherTheme by StringIntPref(
            "pref_launcherTheme",
            ThemeManager.getDefaultTheme()
    ) { ThemeManager.getInstance(context).updateTheme() }
    val accentColor by IntPref(PREFS_ACCENT, (0xffff1744).toInt(), doNothing)
    var enableBlur by BooleanPref("pref_enableBlur", false, updateBlur)
    var blurRadius by IntPref("pref_blurRadius", 75, updateBlur)
    var customWindowCorner by BooleanPref("pref_customWindowCorner", false, doNothing)
    var windowCornerRadius by FloatPref("pref_customWindowCornerRadius", 8f, updateBlur)
    var iconPackPackage by StringPref("pref_icon_pack_package", "", reloadIcons)

    var iconShape by StringBasedPref(
            "pref_iconShape", IconShape.Circle, onIconShapeChanged,
            {
                IconShape.fromString(it) ?: IconShapeManager.getSystemIconShape(context)
            }, IconShape::toString
    ) { /* no dispose */ }

    var coloredBackground by BooleanPref("pref_colored_background", false, doNothing)

    var enableWhiteOnlyTreatment by BooleanPref("pref_white_only_treatment", false, doNothing)
    var enableLegacyTreatment by BooleanPref("pref_legacy_treatment", false, doNothing)
    var adaptifyIconPacks by BooleanPref("pref_adaptive_icon_pack", false, doNothing)
    var forceShapeless by BooleanPref("pref_force_shape_less", false, doNothing)

    //FOLDER
    var folderRadius by DimensionPref("pref_folder_radius", -1f, restart)
    val customFolderBackground by BooleanPref("pref_custom_folder_background", false, restart)
    val folderBackground by IntPref(
            "pref_folder_background",
            Themes.getAttrColor(context, R.attr.folderFillColor),
            restart
    )

    //SMARTSPACE
    val smartspaceTime24H by BooleanPref("pref_smartspace_time_24_h", true, restart)

    //NOTIFICATION
    val notificationCount: Boolean by BooleanPref("pref_notification_count", false, restart)
    val notificationCustomColor: Boolean by BooleanPref("pref_custom_background", false, restart)
    val notificationBackground by IntPref(
            "pref_notification_background", R.color.notification_background, restart
    )

    /*
    * Preferences not used. Added to register the change and restart only
    */
    var doubleTapGesture by StringPref("pref_gesture_double_tap", "", restart)
    var longPressGesture by StringPref("pref_gesture_long_press", "", restart)
    var homePressGesture by StringPref("pref_gesture_press_home", "", restart)
    var backPressGesture by StringPref("pref_gesture_press_back", "", restart)
    var swipeDownGesture by StringPref("pref_gesture_swipe_down", "", restart)
    var swipeUpGesture by StringPref("pref_gesture_swipe_up", "", restart)
    var dockSwipeUpGesture by StringPref("pref_gesture_dock_swipe_up", "", restart)
    var launchAssistantGesture by StringPref("pref_gesture_launch_assistant", "", restart)

    //ADVANCED
    var language by StringPref("pref_language", "", restart)
    var settingsSearch by BooleanPref("pref_settings_search", true, restart)
    var firstRun by BooleanPref("pref_first_run", true)

    //SEARCH
    var searchProvider by StringPref(
            "pref_globalSearchProvider",
            ""
    ) {
        SearchProviderController.getInstance(context).onSearchProviderChanged()
    }

    // FEED
    var feedProvider by StringPref("pref_feedProvider", "", restart)
    val ignoreFeedWhitelist by BooleanPref("pref_feedProviderAllowAll", true, restart)

    //DEVELOPER PREFERENCES
    var developerOptionsEnabled by BooleanPref("pref_showDevOptions", false, restart)
    var desktopModeEnabled by BooleanPref("pref_desktop_mode", true, restart)
    private val lowPerformanceMode by BooleanPref("pref_lowPerformanceMode", false, restart)
    val enablePhysics get() = !lowPerformanceMode
    val showDebugInfo by BooleanPref("pref_showDebugInfo", true, doNothing)

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        Log.d("OmegaPreferences", "Cambiando preferencia " + key)
        onChangeMap[key]?.invoke()
        onChangeListeners[key]?.toSet()?.forEach { it.onValueChanged(key, this, false) }
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

    private fun updateBlur() {
        onChangeCallback?.updateBlur()
    }

    inline fun withChangeCallback(
            crossinline callback: (OmegaPreferencesChangeCallback) -> Unit
    ): () -> Unit {
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

    fun removeOnPreferenceChangeListener(
            listener: OnPreferenceChangeListener,
            vararg keys: String
    ) {
        keys.forEach { removeOnPreferenceChangeListener(it, listener) }
    }

    fun removeOnPreferenceChangeListener(key: String, listener: OnPreferenceChangeListener) {
        onChangeListeners[key]?.remove(listener)
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

    open inner class IntPref(key: String, defaultValue: Int = 0, onChange: () -> Unit = doNothing) :
            PrefDelegate<Int>(key, defaultValue, onChange) {
        override fun onGetValue(): Int = sharedPrefs.getInt(getKey(), defaultValue)

        override fun onSetValue(value: Int) {
            edit { putInt(getKey(), value) }
        }
    }

    open inner class AlphaPref(
            key: String,
            defaultValue: Int = 0,
            onChange: () -> Unit = doNothing
    ) :
            PrefDelegate<Int>(key, defaultValue, onChange) {
        override fun onGetValue(): Int =
                (sharedPrefs.getFloat(getKey(), defaultValue.toFloat() / 255) * 255).roundToInt()

        override fun onSetValue(value: Int) {
            edit { putFloat(getKey(), value.toFloat() / 255) }
        }
    }

    inline fun <reified T : Enum<T>> EnumPref(
            key: String, defaultValue: T,
            noinline onChange: () -> Unit = doNothing
    ): PrefDelegate<T> {
        return IntBasedPref(key, defaultValue, onChange, { value ->
            enumValues<T>().firstOrNull { item -> item.ordinal == value } ?: defaultValue
        }, { it.ordinal }, { })
    }

    open inner class IntBasedPref<T : Any>(
            key: String, defaultValue: T, onChange: () -> Unit = doNothing,
            private val fromInt: (Int) -> T,
            private val toInt: (T) -> Int,
            private val dispose: (T) -> Unit
    ) : PrefDelegate<T>(key, defaultValue, onChange) {
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

    companion object {

        @JvmField
        val INSTANCE = MainThreadInitializedObject(::OmegaPreferences)

        @JvmStatic
        fun getInstance(context: Context) = INSTANCE.get(context)!!

    }
}