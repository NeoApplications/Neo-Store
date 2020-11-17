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

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.saggitt.omega.allapps.OmegaAppFilter
import com.saggitt.omega.views.RecyclerViewFragment

class HiddenAppsFragment : RecyclerViewFragment(), SelectableAppsAdapter.Callback {

    private lateinit var adapter: SelectableAppsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onRecyclerViewCreated(recyclerView: RecyclerView) {
        val context = recyclerView.context
        adapter = SelectableAppsAdapter.ofProperty(context,
                Utilities.getOmegaPrefs(context)::hiddenAppSet, this, OmegaAppFilter(context))
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter
    }

    override fun onSelectionsChanged(newSize: Int) {
        if (newSize > 0) {
            activity?.title = "$newSize${getString(R.string.hide_app_selected)}"
        } else {
            activity?.title = getString(R.string.title__drawer_hide_apps)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        return inflater.inflate(R.menu.menu_hide_apps, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_reset -> {
                adapter.clearSelection()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}