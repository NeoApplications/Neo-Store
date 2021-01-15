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

import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.saggitt.omega.views.RecyclerViewFragment


class DashFragment : RecyclerViewFragment() {
    private val adapter by lazy { DashEditAdapter(requireContext()) }

    override fun onRecyclerViewCreated(recyclerView: RecyclerView) {
        recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        recyclerView.adapter = adapter
        (recyclerView.itemAnimator as? DefaultItemAnimator)?.supportsChangeAnimations = false
        adapter.itemTouchHelper = ItemTouchHelper(adapter.TouchHelperCallback()).apply {
            attachToRecyclerView(recyclerView)
        }
    }

    /*override fun onPause() {
        super.onPause()
        Utilities.getOmegaPrefs(context).dashItems = adapter.getDashItems()
    }*/
}