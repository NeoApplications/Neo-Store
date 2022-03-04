/*
 *  This file is part of Omega Launcher
 *  Copyright (c) 2021   Omega Launcher Team
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

package com.saggitt.omega.allapps

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.LauncherActivityInfo
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.launcher3.AppFilter
import com.android.launcher3.LauncherAppState
import com.android.launcher3.R
import com.android.launcher3.model.data.AppInfo
import com.android.launcher3.util.ComponentKey
import com.android.launcher3.util.Executors.MAIN_EXECUTOR
import com.android.launcher3.util.Executors.MODEL_EXECUTOR
import com.saggitt.omega.util.Config
import java.util.*
import java.util.Comparator.comparing

open class AppsAdapter(
    private val context: Context,
    private val callback: Callback? = null,
    private val filter: AppFilter? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var isLoaded = false
    val apps = ArrayList<App>()

    open val comparator = defaultComparator

    fun postLoadApps() {
        MODEL_EXECUTOR.handler.postAtFrontOfQueue(::loadAppsList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return if (getItemViewType(0) == TYPE_ITEM) {
            AppHolder(layoutInflater.inflate(R.layout.app_item, parent, false))
        } else {
            LoadingHolder(layoutInflater.inflate(R.layout.adapter_loading, parent, false))
        }
    }

    override fun getItemCount() = if (isLoaded) apps.size else 1

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is AppHolder) {
            holder.bind(position)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (isLoaded) TYPE_ITEM else TYPE_LOADING
    }

    protected open fun loadAppsList() {
        val config = Config(context)

        apps.addAll(config.getAppsList(filter)
            .map { App(context, it) }
            .sortedWith(comparator))
        MAIN_EXECUTOR.handler.postAtFrontOfQueue(::onAppsListLoaded)
    }

    @SuppressLint("NotifyDataSetChanged")
    protected open fun onAppsListLoaded() {
        isLoaded = true
        notifyDataSetChanged()
    }

    open fun onBindApp(app: App, holder: AppHolder, position: Int) {
    }

    open fun onClickApp(position: Int, holder: AppHolder) {
        callback?.onAppSelected(apps[position])
    }

    class App(context: Context, val info: LauncherActivityInfo) {

        val iconDrawable: Drawable
        val key = ComponentKey(info.componentName, info.user)

        init {
            val appInfo = AppInfo(context, info, info.user)
            LauncherAppState.getInstance(context).iconCache.getTitleAndIcon(appInfo, false)
            iconDrawable = BitmapDrawable(context.resources, appInfo.bitmap.icon)
        }
    }

    inner class AppHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        private val label: TextView = itemView.findViewById(R.id.label)
        private val icon: ImageView = itemView.findViewById(R.id.icon)

        val checkBox: CheckBox = itemView.findViewById(R.id.check)
        val mHiddenView: ImageView = itemView.findViewById(R.id.item_hidden_app_switch)
        val mProtectedView: ImageView = itemView.findViewById(R.id.item_protected_app_switch)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(position: Int) {
            val app = apps[position]
            label.text = app.info.label
            icon.setImageDrawable(app.iconDrawable)

            onBindApp(app, this, position)
        }

        override fun onClick(v: View) {
            onClickApp(bindingAdapterPosition, this)
        }
    }

    inner class LoadingHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    interface Callback {

        fun onAppSelected(app: App)
    }

    companion object {
        val defaultComparator =
            comparing<App, String> { it.info.label.toString().lowercase(Locale.getDefault()) }!!

        const val TYPE_LOADING = 0
        const val TYPE_ITEM = 1
    }
}
