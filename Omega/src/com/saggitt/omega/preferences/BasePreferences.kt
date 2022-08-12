/*
 * This file is part of Omega Launcher
 * Copyright (c) 2022   Omega Launcher Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saggitt.omega.preferences

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.StringRes
import com.android.launcher3.InvariantDeviceProfile
import com.android.launcher3.LauncherFiles
import com.saggitt.omega.util.dpToPx
import com.saggitt.omega.util.pxToDp
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import kotlin.math.roundToInt
import kotlin.reflect.KProperty

abstract class BasePreferences(context: Context) :
    SharedPreferences.OnSharedPreferenceChangeListener {
    private val mContext = context
    val sharedPrefs = createPreferences()
    val onChangeMap: MutableMap<String, () -> Unit> = HashMap()
    val onChangeListeners: MutableMap<String, MutableSet<OmegaPreferences.OnPreferenceChangeListener>> =
        HashMap()
    private var onChangeCallback: OmegaPreferencesChangeCallback? = null

    val doNothing = { }

    val restart = {
        restart()
    }
    val reloadApps = { reloadApps() }
    val reloadAll = { reloadAll() }
    val updateBlur = { updateBlur() }
    val recreate = { recreate() }

    val idp: InvariantDeviceProfile get() = InvariantDeviceProfile.INSTANCE.get(mContext)
    val reloadIcons = { idp.onPreferencesChanged(context) }
    val reloadGrid: () -> Unit = { idp.onPreferencesChanged(context) }

    private fun createPreferences(): SharedPreferences {
        val dir = mContext.cacheDir.parent
        val oldFile = File(dir, "shared_prefs/" + LauncherFiles.OLD_SHARED_PREFERENCES_KEY + ".xml")
        val newFile = File(dir, "shared_prefs/" + LauncherFiles.SHARED_PREFERENCES_KEY + ".xml")

        //Migrate old preferences to new file name
        val oldNeoFile = File(dir, "shared_prefs/com.saggitt.omega.neo_preferences.xml")
        val oldDebugFile = File(dir, "shared_prefs/com.saggitt.omega.debug_preferences.xml")

        if (oldFile.exists() && !newFile.exists()) {
            oldFile.renameTo(newFile)
            oldFile.delete()
        } else if (oldNeoFile.exists() && !newFile.exists()) {
            oldNeoFile.renameTo(newFile)
            oldNeoFile.delete()
        } else if (oldDebugFile.exists() && !newFile.exists()) {
            oldDebugFile.renameTo(newFile)
            oldDebugFile.delete()
        }

        return mContext.applicationContext
            .getSharedPreferences(LauncherFiles.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)
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

    fun restart() {
        onChangeCallback?.restart()
    }

    private fun updateBlur() {
        onChangeCallback?.updateBlur()
    }

    fun updateSmartspaceProvider() {
        onChangeCallback?.updateSmartspaceProvider()
    }

    inline fun withChangeCallback(
        crossinline callback: (OmegaPreferencesChangeCallback) -> Unit
    ): () -> Unit {
        return { getOnChangeCallback()?.let { callback(it) } }
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

    abstract inner class PrefDelegate<T : Any>( // TODO Add iconId
        val key: String,
        @StringRes val titleId: Int,
        @StringRes val summaryId: Int = -1,
        val defaultValue: T,
        val onChange: () -> Unit
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
        @StringRes titleId: Int,
        @StringRes summaryId: Int = -1,
        defaultValue: Boolean = false,
        onChange: () -> Unit = doNothing
    ) : PrefDelegate<Boolean>(key, titleId, summaryId, defaultValue, onChange) {
        override fun onGetValue(): Boolean = sharedPrefs.getBoolean(getKey(), defaultValue)

        override fun onSetValue(value: Boolean) {
            edit { putBoolean(getKey(), value) }
        }
    }

    open inner class FloatPref(
        key: String,
        @StringRes titleId: Int,
        @StringRes summaryId: Int = -1,
        defaultValue: Float = 0f,
        val minValue: Float,
        val maxValue: Float,
        val steps: Int,
        val specialOutputs: ((Float) -> String) = Float::toString,
        onChange: () -> Unit = doNothing
    ) : PrefDelegate<Float>(key, titleId, summaryId, defaultValue, onChange) {
        override fun onGetValue(): Float = sharedPrefs.getFloat(getKey(), defaultValue)

        override fun onSetValue(value: Float) {
            edit { putFloat(getKey(), value) }
        }
    }

    open inner class ColorIntPref(
        key: String,
        @StringRes titleId: Int,
        @StringRes summaryId: Int = -1,
        defaultValue: Int = 0,
        onChange: () -> Unit = doNothing
    ) : IntPref(key, titleId, summaryId, defaultValue, onChange)

    open inner class DimensionPref(
        key: String,
        @StringRes titleId: Int,
        @StringRes summaryId: Int = -1,
        defaultValue: Float = 0f,
        minValue: Float,
        maxValue: Float,
        steps: Int,
        specialOutputs: ((Float) -> String) = Float::toString,
        onChange: () -> Unit = doNothing
    ) : FloatPref(
        key,
        titleId,
        summaryId,
        defaultValue,
        minValue,
        maxValue,
        steps,
        specialOutputs,
        onChange
    ) {

        override fun onGetValue(): Float = dpToPx(sharedPrefs.getFloat(getKey(), defaultValue))

        override fun onSetValue(value: Float) {
            edit { putFloat(getKey(), pxToDp(value)) }
        }
    }

    open inner class IntPref( // TODO migrate to ?
        key: String,
        @StringRes titleId: Int,
        @StringRes summaryId: Int = -1,
        defaultValue: Int = 0,
        onChange: () -> Unit = doNothing
    ) :
        PrefDelegate<Int>(key, titleId, summaryId, defaultValue, onChange) {
        override fun onGetValue(): Int = sharedPrefs.getInt(getKey(), defaultValue)

        override fun onSetValue(value: Int) {
            edit { putInt(getKey(), value) }
        }
    }

    inner class IdpIntPref(
        key: String,
        @StringRes titleId: Int,
        @StringRes summaryId: Int = -1,
        private val selectDefaultValue: InvariantDeviceProfile.GridOption.() -> Int,
        val specialOutputs: ((Int) -> String) = Int::toString,
        val minValue: Float,
        val maxValue: Float,
        val steps: Int,
        onChange: () -> Unit = doNothing,
    ) : IntPref(key, titleId, summaryId, -1, onChange) {

        override fun onGetValue(): Int {
            return sharedPrefs.getInt(getKey(), defaultValue)
        }

        override fun onSetValue(value: Int) {
            edit { putInt(getKey(), value) }
        }

        fun defaultValue(defaultGrid: InvariantDeviceProfile.GridOption): Int {
            return selectDefaultValue(defaultGrid)
        }

        fun get(defaultGrid: InvariantDeviceProfile.GridOption): Int {
            val value = super.onGetValue()
            return if (value == -1 || value == -0) {
                selectDefaultValue(defaultGrid)
            } else {
                value
            }
        }

        fun set(newValue: Int, defaultGrid: InvariantDeviceProfile.GridOption) {
            if (newValue == selectDefaultValue(defaultGrid)) {
                super.onSetValue(-1)
            } else {
                super.onSetValue(newValue)
            }
        }
    }

    open inner class AlphaPref(
        key: String,
        @StringRes titleId: Int,
        @StringRes summaryId: Int = -1,
        defaultValue: Float = 0f,
        steps: Int = 100,
        specialOutputs: ((Float) -> String) = { "${(it * steps).roundToInt()}%" },
        onChange: () -> Unit = doNothing
    ) : FloatPref(
        key,
        titleId,
        summaryId,
        defaultValue,
        minValue = 0f,
        maxValue = 1f,
        steps,
        specialOutputs,
        onChange
    ) {
        override fun onGetValue(): Float = sharedPrefs.getFloat(getKey(), defaultValue)

        override fun onSetValue(value: Float) {
            edit { putFloat(getKey(), value) }
        }
    }

    inline fun <reified T : Enum<T>> EnumPref( // TODO remove when done migration
        key: String,
        @StringRes titleId: Int,
        @StringRes summaryId: Int = -1,
        defaultValue: T,
        noinline onChange: () -> Unit = doNothing
    ): PrefDelegate<T> {
        return IntBasedPref(key, titleId, summaryId, defaultValue, onChange, { value ->
            enumValues<T>().firstOrNull { item -> item.ordinal == value } ?: defaultValue
        }, { it.ordinal }, { })
    }

    open inner class EnumSelectionPref<T : Enum<T>>(
        key: String,
        @StringRes titleId: Int,
        @StringRes summaryId: Int = -1,
        defaultValue: T,
        entries: Map<Int, Int>,
        onChange: () -> Unit = doNothing
    ) : IntSelectionPref(key, titleId, summaryId, defaultValue.ordinal, entries, onChange) {
        override fun onGetValue(): Int = sharedPrefs.getInt(getKey(), defaultValue)

        override fun onSetValue(value: Int) {
            edit { putInt(getKey(), value) }
        }
    }

    open inner class IntSelectionPref(
        key: String,
        @StringRes titleId: Int,
        @StringRes summaryId: Int = -1,
        defaultValue: Int = 0,
        val entries: Map<Int, Int>,
        onChange: () -> Unit = doNothing
    ) :
        PrefDelegate<Int>(key, titleId, summaryId, defaultValue, onChange) {
        override fun onGetValue(): Int = sharedPrefs.getInt(getKey(), defaultValue)

        override fun onSetValue(value: Int) {
            edit { putInt(getKey(), value) }
        }
    }

    open inner class StringSelectionPref(
        key: String,
        @StringRes titleId: Int,
        @StringRes summaryId: Int = -1,
        defaultValue: String = "",
        val entries: Map<String, Int>,
        onChange: () -> Unit = doNothing
    ) : PrefDelegate<String>(key, titleId, summaryId, defaultValue, onChange) {

        override fun onGetValue(): String = sharedPrefs.getString(getKey(), defaultValue)!!
        override fun onSetValue(value: String) {
            edit { putString(getKey(), value) }
        }
    }

    open inner class StringMultiSelectionPref(
        key: String,
        @StringRes titleId: Int,
        @StringRes summaryId: Int = -1,
        defaultValue: Set<String>,
        val entries: Map<String, Int>,
        onChange: () -> Unit = doNothing
    ) :
        PrefDelegate<Set<String>>(key, titleId, summaryId, defaultValue, onChange) {
        override fun onGetValue(): Set<String> = sharedPrefs.getStringSet(getKey(), defaultValue)!!

        override fun onSetValue(value: Set<String>) {
            edit { putStringSet(getKey(), value) }
        }
    }

    open inner class IntBasedPref<T : Any>(
        key: String,
        @StringRes titleId: Int,
        @StringRes summaryId: Int = -1,
        defaultValue: T, onChange: () -> Unit = doNothing,
        private val fromInt: (Int) -> T,
        private val toInt: (T) -> Int,
        private val dispose: (T) -> Unit
    ) : PrefDelegate<T>(key, titleId, summaryId, defaultValue, onChange) {
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

    open inner class StringPref( // TODO migrate to @StringSelectionPref
        key: String,
        @StringRes titleId: Int,
        @StringRes summaryId: Int = -1,
        defaultValue: String = "",
        onChange: () -> Unit = doNothing
    ) : PrefDelegate<String>(key, titleId, summaryId, defaultValue, onChange) {
        override fun onGetValue(): String = sharedPrefs.getString(getKey(), defaultValue)!!

        override fun onSetValue(value: String) {
            edit { putString(getKey(), value) }
        }
    }

    inner class StringBasedPref<T : Any>( // TODO migrate to @StringSelectionPref
        key: String, defaultValue: T,
        @StringRes titleId: Int,
        @StringRes summaryId: Int = -1,
        onChange: () -> Unit = doNothing,
        private val fromString: (String) -> T,
        private val toString: (T) -> String,
        private val dispose: (T) -> Unit
    ) :
        PrefDelegate<T>(key, titleId, summaryId, defaultValue, onChange) {
        override fun onGetValue(): T = sharedPrefs.getString(getKey(), null)?.run(fromString)
            ?: defaultValue

        override fun onSetValue(value: T) {
            edit { putString(getKey(), toString(value)) }
        }

        override fun disposeOldValue(oldValue: T) {
            dispose(oldValue)
        }
    }

    open inner class StringIntPref( // TODO remove when done migration
        key: String,
        @StringRes titleId: Int,
        @StringRes summaryId: Int = -1,
        defaultValue: Int = 0,
        onChange: () -> Unit = doNothing
    ) : PrefDelegate<Int>(key, titleId, summaryId, defaultValue, onChange) {
        override fun onGetValue(): Int = try {
            sharedPrefs.getString(getKey(), "$defaultValue")!!.toInt()
        } catch (e: Exception) {
            sharedPrefs.getInt(getKey(), defaultValue)
        }

        override fun onSetValue(value: Int) {
            edit { putString(getKey(), "$value") }
        }
    }

    open inner class StringSetPref( // TODO migrate to @StringMultiSelectionPref
        key: String,
        @StringRes titleId: Int,
        @StringRes summaryId: Int = -1,
        defaultValue: Set<String>,
        onChange: () -> Unit = doNothing
    ) :
        PrefDelegate<Set<String>>(key, titleId, summaryId, defaultValue, onChange) {
        override fun onGetValue(): Set<String> = sharedPrefs.getStringSet(getKey(), defaultValue)!!

        override fun onSetValue(value: Set<String>) {
            edit { putStringSet(getKey(), value) }
        }
    }

    open inner class StringListPref( // TODO migrate to @StringMultiSelectionPref
        prefKey: String,
        @StringRes titleId: Int,
        @StringRes summaryId: Int = -1,
        default: List<String> = emptyList(),
        onChange: () -> Unit = doNothing
    ) : MutableListPref<String>(prefKey, titleId, summaryId, onChange, default) {
        override fun unflattenValue(value: String) = value
        override fun flattenValue(value: String) = value
    }

    abstract inner class MutableListPref<T>( // TODO re-evaluate if needed
        private val prefs: SharedPreferences,
        @StringRes titleId: Int,
        @StringRes summaryId: Int = -1,
        private val prefKey: String,
        onChange: () -> Unit = doNothing,
        default: List<T> = emptyList()
    ) : BasePreferences.PrefDelegate<List<T>>(prefKey, titleId, summaryId, default, onChange) {
        constructor(
            prefKey: String,
            @StringRes titleId: Int,
            @StringRes summaryId: Int = -1,
            onChange: () -> Unit = doNothing,
            default: List<T> = emptyList()
        ) : this(sharedPrefs, titleId, summaryId, prefKey, onChange, default)

        private val valueList = arrayListOf<T>()
        private val listeners = mutableSetOf<OmegaPreferences.MutableListPrefChangeListener>()

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

        fun addListener(listener: OmegaPreferences.MutableListPrefChangeListener) {
            listeners.add(listener)
        }

        fun removeListener(listener: OmegaPreferences.MutableListPrefChangeListener) {
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
}