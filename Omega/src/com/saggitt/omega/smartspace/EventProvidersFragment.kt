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

package com.saggitt.omega.smartspace

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceDialogFragmentCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.saggitt.omega.preferences.custom.EventProvidersPreference
import com.saggitt.omega.util.applyAccent

class EventProvidersFragment : PreferenceDialogFragmentCompat() {

    private val providersPreference get() = preference as EventProvidersPreference
    private lateinit var adapter: EventProvidersAdapter

    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)
        adapter = EventProvidersAdapter(view.context)

        val recyclerView = view.findViewById<RecyclerView>(com.android.launcher3.R.id.list)
        recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter
        (recyclerView.itemAnimator as? DefaultItemAnimator)?.supportsChangeAnimations = false
        adapter.itemTouchHelper = ItemTouchHelper(adapter.TouchHelperCallback()).apply {
            attachToRecyclerView(recyclerView)
        }
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            providersPreference.setProviders(adapter.saveSpecs())
        }
    }

    override fun onStart() {
        super.onStart()
        (dialog as AlertDialog).applyAccent()
    }

    companion object {

        fun newInstance(key: String?) = EventProvidersFragment().apply {
            arguments = Bundle(1).apply {
                putString(ARG_KEY, key)
            }
        }
    }
}
