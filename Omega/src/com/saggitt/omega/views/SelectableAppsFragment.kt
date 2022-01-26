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

package com.saggitt.omega.views

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.os.Process
import android.os.ResultReceiver
import android.os.UserHandle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.launcher3.AppFilter
import com.android.launcher3.R
import com.android.launcher3.R.string
import com.android.launcher3.util.ComponentKey
import com.saggitt.omega.allapps.OmegaAppFilter
import com.saggitt.omega.groups.DrawerTabs
import com.saggitt.omega.preferences.views.PreferencesActivity.Companion.KEY_CALLBACK
import com.saggitt.omega.preferences.views.PreferencesActivity.Companion.KEY_FILTER_IS_WORK
import com.saggitt.omega.preferences.views.PreferencesActivity.Companion.KEY_SELECTION

class SelectableAppsFragment : Fragment(), SelectableAppsAdapter.Callback {

    val layoutId = R.layout.activity_hidden_apps
    private var selection: Set<String> = emptySet()
    private val mContext by lazy { activity as Context }
    private var changed = false

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
        val arguments = requireArguments()
        val profile = arguments.getParcelable<DrawerTabs.Profile>(KEY_FILTER_IS_WORK)!!
        selection = HashSet(arguments.getStringArrayList(KEY_SELECTION))

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(mContext)
        recyclerView.adapter = SelectableAppsAdapter.ofProperty(
                requireContext(),
                ::selection,
                this,
                createAppFilter(mContext, DrawerTabs.getWorkFilter(profile))
        )
    }

    override fun onResume() {
        super.onResume()
        updateTitle(selection.size)
    }

    override fun onDestroy() {
        super.onDestroy()
        val receiver = requireArguments().getParcelable<ResultReceiver>(KEY_CALLBACK)!!
        if (changed) {
            receiver.send(RESULT_OK, Bundle(1).apply {
                putStringArrayList(KEY_SELECTION, ArrayList(selection))
            })
        } else {
            receiver.send(RESULT_CANCELED, null)
        }
    }

    override fun onSelectionsChanged(newSize: Int) {
        changed = true
        updateTitle(newSize)
    }

    private fun updateTitle(size: Int) {
        requireActivity().title = getString(string.selected_count, size)
    }

    private fun createAppFilter(context: Context, predicate: (ComponentKey) -> Boolean): AppFilter {
        return object : AppFilter() {

            val base = OmegaAppFilter(context)

            override fun shouldShowApp(app: ComponentName, user: UserHandle?): Boolean {
                if (!base.shouldShowApp(app, user)) {
                    return false
                }
                return predicate(ComponentKey(app, user ?: Process.myUserHandle()))
            }
        }
    }
}