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

package com.saggitt.omega.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.launcher3.AppFilter
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.util.ComponentKey
import com.saggitt.omega.allapps.AppsAdapter
import com.saggitt.omega.util.comparing
import com.saggitt.omega.util.then
import kotlin.reflect.KMutableProperty0

abstract class SelectableAppsAdapter(context: Context,
                                     private val callback: Callback? = null,
                                     filter: AppFilter? = null
) : AppsAdapter(context, null, filter) {
    private val selections = HashSet<ComponentKey>()
    private val accentTintList = ColorStateList.valueOf(Utilities.getOmegaPrefs(context).accentColor)

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

    override val comparator = comparing<App, Int> { if (isSelected(it.key)) 0 else 1 }
            .then { it.info.label.toString().lowercase() }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return if (getItemViewType(0) == TYPE_ITEM) {
            AppHolder(layoutInflater.inflate(R.layout.app_item, parent, false))
        } else {
            LoadingHolder(layoutInflater.inflate(R.layout.adapter_loading, parent, false))
        }
    }

    override fun onBindApp(app: App, holder: AppHolder, position: Int) {
        super.onBindApp(app, holder, position)
        holder.checkBox.apply {
            visibility = View.VISIBLE
            isChecked = isSelected(app.key)
            buttonTintList = accentTintList
        }
        holder.mHiddenView.apply {
            visibility = View.GONE
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

    @SuppressLint("NotifyDataSetChanged")
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

    interface Callback {
        fun onSelectionsChanged(newSize: Int)
    }

    companion object {

        fun ofProperty(
                context: Context,
                property: KMutableProperty0<Set<String>>,
                callback: Callback? = null,
                filter: AppFilter? = null
        ) = object : SelectableAppsAdapter(context, callback, filter) {
            override fun getInitialSelections() =
                    HashSet(property.get().map { Utilities.makeComponentKey(context, it) })

            override fun setSelections(selections: Set<ComponentKey>) {
                property.set(HashSet(selections.map { it.toString() }))
            }
        }
    }
}