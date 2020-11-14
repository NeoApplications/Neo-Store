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

package com.saggitt.omega.util

import android.content.Context
import androidx.preference.ListPreference

class EntriesBuilder(private val context: Context) {

    private val entries = ArrayList<String>()
    private val entryValues = ArrayList<String>()

    fun addEntry(entry: Int, value: String) {
        addEntry(context.getString(entry), value)
    }

    fun addEntry(entry: String, value: Int) {
        addEntry(entry, "$value")
    }

    fun addEntry(entry: Int, value: Int) {
        addEntry(context.getString(entry), "$value")
    }

    fun addEntry(entry: String, value: String) {
        entries.add(entry)
        entryValues.add(value)
    }

    fun build(): Pair<Array<String>, Array<String>> {
        return Pair(entries.toTypedArray(), entryValues.toTypedArray())
    }

    fun build(listPreference: ListPreference) {
        listPreference.entries = entries.toTypedArray()
        listPreference.entryValues = entryValues.toTypedArray()
    }
}

inline fun ListPreference.buildEntries(edit: EntriesBuilder.() -> Unit) {
    EntriesBuilder(context).apply(edit).build(this)
}
