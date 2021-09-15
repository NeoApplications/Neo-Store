/*
 *     This file is part of Lawnchair Launcher.
 *
 *     Lawnchair Launcher is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Lawnchair Launcher is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Lawnchair Launcher.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saggitt.omega.preferences

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import com.android.launcher3.AppFilter
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.Utilities.makeComponentKey
import com.android.launcher3.util.ComponentKey
import com.saggitt.omega.util.comparing
import com.saggitt.omega.util.then
import kotlin.reflect.KMutableProperty0

// TODO migrate to FastAdapater
abstract class ProtectedAppsAdapter(
    context: Context,
    private val callback: Callback? = null,
    filter: AppFilter? = null
) : AppsAdapter(context, null, filter) {

    private val hiddenApps = HashSet<ComponentKey>()
    private val protectedApps = HashSet<ComponentKey>()
    private val accentTintList =
        ColorStateList.valueOf(Utilities.getOmegaPrefs(context).accentColor)

    override val comparator = comparing<App, Int> { if (isSelected(it.key)) 0 else 1 }
        .then { it.info.label.toString().lowercase() }

    init {
        postLoadApps()
        callback?.onSelectionsChanged(0)
    }

    override fun onAppsListLoaded() {
        val tmp = HashSet(hiddenApps)
        hiddenApps.clear()
        apps.forEach {
            if (it.key in tmp) {
                hiddenApps.add(it.key)
            }
        }
        super.onAppsListLoaded()
        callback?.onSelectionsChanged(hiddenApps.size)
    }

    override fun onBindApp(app: App, holder: AppHolder, position: Int) {
        super.onBindApp(app, holder, position)
        holder.mHiddenView.apply {
            visibility = View.VISIBLE
            setImageResource(if (isSelected(app.key)) R.drawable.ic_hidden_locked else R.drawable.ic_hidden_unlocked)
            drawable.setTintList(accentTintList)
            setOnClickListener {
                toggleSelection(position)
                setImageResource(if (isSelected(app.key)) R.drawable.ic_hidden_locked else R.drawable.ic_hidden_unlocked)
            }
        }

        holder.mProtectedView.apply {
            visibility = View.VISIBLE
            var icon = R.drawable.ic_protected_unlocked
            if (isProtected(app.key)) {
                icon = R.drawable.ic_protected_locked
                setImageResource(icon)
                drawable.setTintList(ColorStateList.valueOf(Color.RED))
            } else {
                setImageResource(icon)
                drawable.setTintList(accentTintList)
            }

            setOnClickListener {
                toggleAppProtected(position)
                setImageResource(if (isProtected(app.key)) R.drawable.ic_protected_locked else R.drawable.ic_protected_unlocked)
            }
        }
    }

    override fun onClickApp(position: Int, holder: AppHolder) {
        super.onClickApp(position, holder)
        toggleSelection(position)
        holder.mHiddenView.setImageResource(if (isSelected(apps[position].key)) R.drawable.ic_hidden_locked else R.drawable.ic_hidden_unlocked)
        holder.mProtectedView.setImageResource(if (isSelected(apps[position].key)) R.drawable.ic_protected_locked else R.drawable.ic_protected_unlocked)
    }

    private fun isSelected(component: ComponentKey) = hiddenApps.contains(component)
    private fun isProtected(component: ComponentKey) = protectedApps.contains(component)

    private fun toggleAppProtected(position: Int) {
        val app = apps[position]
        val componentKey = app.key
        if (protectedApps.contains(componentKey)) {
            protectedApps.remove(componentKey)
        } else {
            protectedApps.add(componentKey)
        }
        setProtectedApps(protectedApps)
    }

    private fun toggleSelection(position: Int) {
        val app = apps[position]
        val componentKey = app.key
        if (hiddenApps.contains(componentKey)) {
            hiddenApps.remove(componentKey)
        } else {
            hiddenApps.add(componentKey)
        }
        setSelections(hiddenApps)
        callback?.onSelectionsChanged(hiddenApps.size)
    }

    fun clearSelection() {
        hiddenApps.clear()
        setSelections(hiddenApps)
        callback?.onSelectionsChanged(0)
        notifyDataSetChanged()
    }

    fun clearProtectedApps() {
        protectedApps.clear()
        setSelections(protectedApps)
        callback?.onSelectionsChanged(0)
        notifyDataSetChanged()
    }

    override fun loadAppsList() {
        hiddenApps.addAll(getInitialSelections())
        protectedApps.addAll(getInitialProtected())
        super.loadAppsList()
    }

    abstract fun getInitialSelections(): Set<ComponentKey>
    abstract fun getInitialProtected(): Set<ComponentKey>

    abstract fun setSelections(selections: Set<ComponentKey>)
    abstract fun setProtectedApps(selections: Set<ComponentKey>)

    interface Callback {
        fun onSelectionsChanged(newSize: Int)
    }

    companion object {

        fun ofProperty(
            context: Context,
            property: KMutableProperty0<Set<String>>,
            protectedApps: KMutableProperty0<Set<String>>,
            callback: Callback? = null, filter: AppFilter? = null
        ) = object : ProtectedAppsAdapter(context, callback, filter) {

            override fun getInitialSelections() =
                HashSet(property.get().map { makeComponentKey(context, it) })

            override fun setSelections(selections: Set<ComponentKey>) {
                property.set(HashSet(selections.map { it.toString() }))
            }

            override fun getInitialProtected() =
                HashSet(protectedApps.get().map { makeComponentKey(context, it) })

            override fun setProtectedApps(selections: Set<ComponentKey>) {
                protectedApps.set(HashSet(selections.map { it.toString() }))
            }
        }
    }
}