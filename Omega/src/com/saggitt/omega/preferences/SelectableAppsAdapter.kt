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

package com.saggitt.omega.preferences

import android.content.Context
import android.content.res.ColorStateList
import android.view.View
import com.android.launcher3.AppFilter
import com.android.launcher3.Utilities
import com.android.launcher3.Utilities.makeComponentKey
import com.android.launcher3.util.ComponentKey
import com.saggitt.omega.util.comparing
import com.saggitt.omega.util.then
import kotlin.reflect.KMutableProperty0

abstract class SelectableAppsAdapter(context: Context, private val callback: Callback? = null, filter: AppFilter? = null)
    : AppsAdapter(context, null, filter) {

    private val selections = HashSet<ComponentKey>()
    private val accentTintList = ColorStateList.valueOf(Utilities.getOmegaPrefs(context).accentColor)

    override val comparator = comparing<App, Int> { if (isSelected(it.key)) 0 else 1 }
            .then { it.info.label.toString().toLowerCase() }

    init {
        postLoadApps()
        callback?.onSelectionsChanged(0)
    }

    override fun onAppsListLoaded() {
        val tmp = HashSet(selections)
        selections.clear()
        apps.forEach {
            if (it.key in tmp) {
                selections.add(it.key)
            }
        }
        super.onAppsListLoaded()
        callback?.onSelectionsChanged(selections.size)
    }

    override fun onBindApp(app: App, holder: AppHolder) {
        super.onBindApp(app, holder)
        holder.checkBox.apply {
            visibility = View.VISIBLE
            isChecked = isSelected(app.key)
            buttonTintList = accentTintList
        }
    }

    override fun onClickApp(position: Int, holder: AppHolder) {
        super.onClickApp(position, holder)
        toggleSelection(position)
        holder.checkBox.isChecked = isSelected(apps[position].key)
    }

    private fun isSelected(component: ComponentKey) = selections.contains(component)

    private fun toggleSelection(position: Int) {
        val app = apps[position]
        val componentKey = app.key
        if (selections.contains(componentKey)) {
            selections.remove(componentKey)
        } else {
            selections.add(componentKey)
        }
        setSelections(selections)
        callback?.onSelectionsChanged(selections.size)
    }

    fun clearSelection() {
        selections.clear()
        setSelections(selections)
        callback?.onSelectionsChanged(0)
        notifyDataSetChanged()
    }

    override fun loadAppsList() {
        selections.addAll(getInitialSelections())
        super.loadAppsList()
    }

    abstract fun getInitialSelections(): Set<ComponentKey>

    abstract fun setSelections(selections: Set<ComponentKey>)

    interface

    Callback {

        fun onSelectionsChanged(newSize: Int)
    }

    companion object {

        fun ofProperty(context: Context, property: KMutableProperty0<Set<String>>,
                       callback: Callback? = null, filter: AppFilter? = null) = object : SelectableAppsAdapter(context, callback, filter) {

            override fun getInitialSelections() = HashSet(property.get().map { makeComponentKey(context, it) })

            override fun setSelections(selections: Set<ComponentKey>) {
                property.set(HashSet(selections.map { it.toString() }))
            }
        }
    }
}