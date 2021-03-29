/*
 *  This file is part of Omega Launcher.
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

package com.saggitt.omega.search

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceDialogFragmentCompat
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.saggitt.omega.util.applyAccent
import com.saggitt.omega.util.applyColor
import com.saggitt.omega.util.isVisible
import com.saggitt.omega.util.omegaPrefs

class SelectSearchProviderFragment : PreferenceDialogFragmentCompat() {

    private val searchProviders by lazy { SearchProviderController.getSearchProviders(requireActivity()) }
    private lateinit var list: ListView

    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)
        list = view.findViewById(R.id.pack_list)
        list.adapter = SearchProviderAdapter(requireContext(),
                searchProviders,
                requireContext().omegaPrefs.searchProvider) {
            context?.omegaPrefs?.searchProvider = it
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        (dialog as AlertDialog).applyAccent()
    }

    override fun onDialogClosed(positiveResult: Boolean) {
    }

    inner class SearchProviderAdapter(context: Context,
                                      private val providers: List<SearchProvider>,
                                      private val selected: String,
                                      private val onSelect: (String) -> Unit) :
            ArrayAdapter<SearchProvider>(context, R.layout.list_item_icon, 0, providers) {

        private val color = Utilities.getOmegaPrefs(context).accentColor
        private val showDebug = context.omegaPrefs.showDebugInfo

        @SuppressLint("ViewHolder")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            return View.inflate(context, R.layout.list_item_icon, null).apply {
                val provider = providers[position]
                findViewById<ImageView>(R.id.icon).setImageDrawable(provider.getIcon())
                findViewById<TextView>(R.id.title).text = provider.name
                if (showDebug) {
                    findViewById<TextView>(R.id.summary).apply {
                        text = provider.packageName
                        isVisible = true
                    }
                }
                findViewById<RadioButton>(R.id.select).apply {
                    isChecked = provider.name == selected
                    setOnCheckedChangeListener { _, _ ->
                        onSelect(provider.name)
                    }
                    applyColor(color)
                }
                setOnClickListener {
                    onSelect(provider.name)
                }
            }
        }
    }

    companion object {
        fun newInstance() = SelectSearchProviderFragment().apply {
            arguments = Bundle(1).apply {
                putString(ARG_KEY, SearchProviderPreference.KEY)
            }
        }
    }
}