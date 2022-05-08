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

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SwitchCompat
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.databinding.FragmentAppCategorizationBinding
import com.saggitt.omega.groups.AppGroupsManager
import com.saggitt.omega.groups.DrawerFoldersAdapter
import com.saggitt.omega.groups.DrawerTabsAdapter
import com.saggitt.omega.groups.FlowerpotTabsAdapter
import com.saggitt.omega.groups.ui.AppCategorizationOptions
import com.saggitt.omega.groups.ui.AppGroupsAdapter
import com.saggitt.omega.preferences.OmegaPreferences
import com.saggitt.omega.theme.OmegaAppTheme
import com.saggitt.omega.util.Config
import com.saggitt.omega.util.applyColor

class AppCategorizationFragment : Fragment(), OmegaPreferences.OnPreferenceChangeListener {
    lateinit var binding: FragmentAppCategorizationBinding

    private val mContext by lazy { activity as Context }
    private val prefs by lazy { Utilities.getOmegaPrefs(mContext) }
    private val manager by lazy { prefs.appGroupsManager }

    private var groupAdapter: AppGroupsAdapter<*, *>? = null
        set(value) {
            if (field != value) {
                field?.itemTouchHelper?.attachToRecyclerView(null)
                field?.saveChanges()
                field = value

                binding.recyclerView.adapter = value?.also {
                    it.loadAppGroups()
                    it.itemTouchHelper.attachToRecyclerView(binding.recyclerView)
                }
            }
        }
    private val drawerTabsAdapter by lazy { DrawerTabsAdapter(mContext) }
    private val flowerpotTabsAdapter by lazy { FlowerpotTabsAdapter(mContext) }
    private val drawerFoldersAdapter by lazy { DrawerFoldersAdapter(mContext) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAppCategorizationBinding.inflate(inflater, container, false)

        binding.categorizationType.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                OmegaAppTheme {
                    AppCategorizationOptions()
                }
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.layoutManager = LinearLayoutManager(mContext)
        setupEnableToggle(binding.enableToggle.root)
        setupStyleSection()
    }

    private fun setupEnableToggle(enableToggle: View) {
        val switch = enableToggle.findViewById<SwitchCompat>(R.id.switchWidget)
        switch.applyColor(prefs.accentColor)
        val syncSwitch = {
            switch.isChecked = manager.categorizationEnabled
            updateGroupAdapter()
        }

        enableToggle.setOnClickListener {
            manager.categorizationEnabled = !manager.categorizationEnabled
            syncSwitch()
        }

        syncSwitch()
    }

    private fun setupStyleSection() {
    }

    private fun updateGroupAdapter() {
        groupAdapter = when (manager.getEnabledType()) {
            AppGroupsManager.CategorizationType.Tabs -> drawerTabsAdapter
            AppGroupsManager.CategorizationType.Flowerpot -> flowerpotTabsAdapter
            AppGroupsManager.CategorizationType.Folders -> drawerFoldersAdapter
            else -> null
        }
    }

    override fun onResume() {
        super.onResume()

        groupAdapter?.loadAppGroups()
        prefs.addOnPreferenceChangeListener("pref_apps_categorization_type", this)
        requireActivity().title = requireActivity().getString(R.string.title_app_categorize)
    }

    override fun onPause() {
        super.onPause()

        groupAdapter?.saveChanges()
        prefs.removeOnPreferenceChangeListener("pref_apps_categorization_type", this)
    }

    override fun onValueChanged(key: String, prefs: OmegaPreferences, force: Boolean) {
        updateGroupAdapter()
    }
}