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

package com.saggitt.omega.groups.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Switch
import androidx.annotation.Keep
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.launcher3.R
import com.android.launcher3.databinding.FragmentAppCategorizationBinding
import com.saggitt.omega.OmegaPreferences
import com.saggitt.omega.groups.AppGroupsManager
import com.saggitt.omega.groups.DrawerFoldersAdapter
import com.saggitt.omega.groups.DrawerTabsAdapter
import com.saggitt.omega.groups.FlowerpotTabsAdapter
import com.saggitt.omega.util.*

@Keep
class AppCategorizationFragment : Fragment(), OmegaPreferences.OnPreferenceChangeListener {
    lateinit var binding: FragmentAppCategorizationBinding

    private val ourContext by lazy { activity as Context }
    private val prefs by lazy { ourContext.omegaPrefs }
    private val manager by lazy { prefs.appGroupsManager }

    private val accent by lazy { ourContext.getColorAccent() }
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
    private val drawerTabsAdapter by lazy { DrawerTabsAdapter(ourContext) }
    private val flowerpotTabsAdapter by lazy { FlowerpotTabsAdapter(ourContext) }
    private val drawerFoldersAdapter by lazy { DrawerFoldersAdapter(ourContext) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAppCategorizationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.layoutManager = LinearLayoutManager(ourContext)
        setupEnableToggle(binding.enableToggle.root)
        setupStyleSection()
    }

    override fun onResume() {
        super.onResume()

        groupAdapter?.loadAppGroups()
        prefs.addOnPreferenceChangeListener("pref_appsCategorizationType", this)
    }

    override fun onPause() {
        super.onPause()

        groupAdapter?.saveChanges()
        prefs.removeOnPreferenceChangeListener("pref_appsCategorizationType", this)
    }

    override fun onValueChanged(key: String, prefs: OmegaPreferences, force: Boolean) {
        updateGroupAdapter()
    }

    private fun updateGroupAdapter() {
        groupAdapter = when (manager.getEnabledType()) {
            AppGroupsManager.CategorizationType.Tabs -> drawerTabsAdapter
            AppGroupsManager.CategorizationType.Flowerpot -> flowerpotTabsAdapter
            AppGroupsManager.CategorizationType.Folders -> drawerFoldersAdapter
            else -> null
        }
    }

    private fun setupEnableToggle(enableToggle: View) {
        enableToggle.findViewById<ImageView>(android.R.id.icon).tintDrawable(accent)

        val switch = enableToggle.findViewById<Switch>(R.id.switchWidget)
        switch.applyColor(accent)
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
        val title = binding.styleHeader.root.findViewById<AppCompatTextView>(android.R.id.title)
        title.setText(R.string.pref_appcategorization_style_text)
        title.setTextColor(ourContext.createDisabledColor(accent))

        binding.folderTypeItem.root.setup(
            AppGroupsManager.CategorizationType.Folders,
            R.string.app_categorization_folders,
            R.string.pref_appcategorization_folders_summary
        )

        binding.tabTypeItem.root.setup(
            AppGroupsManager.CategorizationType.Tabs,
            R.string.app_categorization_tabs,
            R.string.pref_appcategorization_tabs_summary
        )
    }
}
