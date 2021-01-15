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