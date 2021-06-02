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
import com.saggitt.omega.views.CenterFloatingView

class DashBottomSheet(context: Context) : RelativeLayout(context) {
    private var controlFastAdapter: FastAdapter<DashControlItem>? = null
    private var dashActionFastAdapter: FastAdapter<DashActionItem>? = null
    private val controlItemAdapter = ItemAdapter<DashControlItem>()
    private val dashItemAdapter = ItemAdapter<DashActionItem>()
    private val prefs = Utilities.getOmegaPrefs(context)
    private val allActionItems = DashEditAdapter.getDashActionProviders(context)
    private val allControlItems = DashEditAdapter.getDashControlProviders(context)

    init {
        View.inflate(context, R.layout.dash_view, this)
        val activeDashProviders = prefs.dashProviders.getAll()

        controlFastAdapter = FastAdapter.with(controlItemAdapter)
        controlFastAdapter?.setHasStableIds(true)
        dashActionFastAdapter = FastAdapter.with(dashItemAdapter)
        dashActionFastAdapter?.setHasStableIds(true)

        val controlRecycler = findViewById<View>(R.id.dash_control_recycler) as RecyclerView
        controlRecycler.layoutManager = GridLayoutManager(context, 2)
        controlRecycler.adapter = controlFastAdapter
        val dashRecycler = findViewById<View>(R.id.dash_recycler) as RecyclerView
        dashRecycler.layoutManager = GridLayoutManager(context, 4)
        dashRecycler.adapter = dashActionFastAdapter

        val controlItems = activeDashProviders
            .mapNotNull { name ->
                allControlItems.find { it.name == name }?.let {
                    DashControlItem(context, it)
                }
            }
        controlItemAdapter.set(controlItems)
        val dashItems = activeDashProviders
            .mapNotNull { name ->
                allActionItems.find { it.name == name }?.let {
                    DashActionItem(context, it)
                }
            }
        dashItemAdapter.set(dashItems)
    }

    companion object {
        fun show(launcher: Launcher, animate: Boolean) {
            val sheet = CenterFloatingView.inflate(launcher)
            val view = DashBottomSheet(launcher)
            sheet.show(view, animate)
        }
    }
}