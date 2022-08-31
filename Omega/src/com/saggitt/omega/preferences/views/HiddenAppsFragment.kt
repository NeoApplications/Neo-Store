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

package com.saggitt.omega.preferences.views

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.saggitt.omega.allapps.OmegaAppFilter
import com.saggitt.omega.allapps.ProtectedAppsAdapter

class HiddenAppsFragment : Fragment(), ProtectedAppsAdapter.Callback {
    private lateinit var adapter: ProtectedAppsAdapter
    val layoutId = R.layout.activity_hidden_apps

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return LayoutInflater.from(container!!.context).inflate(layoutId, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onRecyclerViewCreated(view.findViewById(R.id.iconList))
    }

    private fun onRecyclerViewCreated(recyclerView: RecyclerView) {
        val hiddenApps = Utilities.getOmegaPrefs(requireContext())::drawerHiddenApps
        val protectedApps = Utilities.getOmegaPrefs(requireContext())::drawerProtectedApps
        adapter = ProtectedAppsAdapter.ofProperty(
            requireContext(),
            hiddenApps, protectedApps, this, OmegaAppFilter(requireContext())
        )

        (recyclerView.itemAnimator as? DefaultItemAnimator)?.supportsChangeAnimations = false
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        recyclerView.adapter = adapter
    }

    @SuppressLint("StringFormatInvalid")
    override fun onSelectionsChanged(newSize: Int) {
        requireActivity().title = if (newSize > 0) {
            getString(R.string.hide_app_selected, newSize)
        } else {
            getString(R.string.title__drawer_hide_apps)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_hide_apps, menu)
        return super.onCreateOptionsMenu(menu, inflater)
    }

    @Deprecated("Deprecated in Java")
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