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

package com.saggitt.omega.allapps

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.saggitt.omega.preferences.SelectableAppsAdapter
import com.saggitt.omega.settings.SettingsBaseActivity

class HiddenAppsActivity : SettingsBaseActivity(),
    SelectableAppsAdapter.Callback {
    private lateinit var adapter: SelectableAppsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hidden_apps)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val hiddenApps = Utilities.getOmegaPrefs(this)::hiddenAppSet
        val protectedApps = Utilities.getOmegaPrefs(this)::protectedAppsSet
        adapter = SelectableAppsAdapter.ofProperty(
            this,
            hiddenApps, protectedApps, this, OmegaAppFilter(this)
        )
        val recyclerView = findViewById<RecyclerView>(R.id.iconList)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter
    }

    override fun onSelectionsChanged(newSize: Int) {
        title = if (newSize > 0) {
            "$newSize${getString(R.string.hide_app_selected)}"
        } else {
            getString(R.string.title__drawer_hide_apps)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.menu_hide_apps, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_reset -> {
                adapter.clearSelection()
                true
            }
            R.id.action_reset_protected -> {
                adapter.clearProtectedApps()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}