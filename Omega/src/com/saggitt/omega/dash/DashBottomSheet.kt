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

package com.saggitt.omega.dash

import android.content.Context
import android.view.View
import android.widget.RelativeLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.launcher3.Launcher
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil.set
import com.saggitt.omega.views.CenterFloatingView

class DashBottomSheet(context: Context) : RelativeLayout(context) {
    private val dashItemAdapter = ItemAdapter<DashItem>()
    private var dashFastAdapter: FastAdapter<DashItem>? = null
    private val prefs = Utilities.getOmegaPrefs(context)
    private val allItems = ArrayList(DashEditAdapter.getDashProviders(context))

    init {
        View.inflate(context, R.layout.dash_view, this)
        val dashProviderPrefs = prefs.dashProviders.getAll()
        val items: ArrayList<DashProvider> = ArrayList()
        dashProviderPrefs.forEach {
            val item = checkAndAddProvider(it)
            if (item != null) {
                items.add(item)
            }
        }

        val circularListView = findViewById<View>(R.id.dash_recycler) as RecyclerView
        dashFastAdapter = FastAdapter.with(dashItemAdapter)
        dashFastAdapter?.setHasStableIds(true)
        circularListView.adapter = dashFastAdapter
        circularListView.layoutManager = GridLayoutManager(context,4)
        val dashItems = items.map { DashItem(context, it) }
        set(dashItemAdapter,dashItems)
    }

    private fun checkAndAddProvider(s: String): DashProvider? {
        val iterator = allItems.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            if (item.name == s) {
                iterator.remove()
                return item
            }
        }
        return null
    }

    companion object {
        fun show(launcher: Launcher, animate: Boolean) {
            val sheet = CenterFloatingView.inflate(launcher)
            val view = DashBottomSheet(launcher)
            sheet.show(view, animate)
        }
    }
}