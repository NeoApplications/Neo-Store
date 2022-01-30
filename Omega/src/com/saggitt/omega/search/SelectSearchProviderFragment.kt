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
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.preference.PreferenceDialogFragmentCompat
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.saggitt.omega.preferences.custom.SearchProviderPreference
import com.saggitt.omega.util.applyAccent
import com.saggitt.omega.util.applyColor
import com.saggitt.omega.util.isVisible
import com.saggitt.omega.util.omegaPrefs

class SelectSearchProviderFragment : PreferenceDialogFragmentCompat() {
    private val searchProviders by lazy {
        SearchProviderController.getSearchProviders(
                requireActivity()
        )
    }
    private lateinit var list: ListView


    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)
        list = view.findViewById(R.id.pack_list)
        list.adapter = SearchProviderAdapter(
                requireContext(),
                searchProviders,
                requireContext().omegaPrefs.searchProvider
        ) {
            context?.omegaPrefs?.searchProvider = it.toString()
            dismiss()
        }
    }

    inner class SearchProviderAdapter(
            context: Context,
            private val providers: List<SearchProvider>,
            private val currentProvider: String,
            private val onSelect: (SearchProvider) -> Unit
    ) :
            ArrayAdapter<SearchProvider>(context, R.layout.list_item_icon, 0, providers) {

        private val color = Utilities.getOmegaPrefs(context).accentColor
        private val showDebug = context.omegaPrefs.showDebugInfo

        @SuppressLint("ViewHolder")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            return View.inflate(context, R.layout.list_item_icon, null).apply {
                val provider = providers[position]
                findViewById<AppCompatImageView>(R.id.icon).setImageDrawable(provider.icon)
                findViewById<AppCompatTextView>(R.id.title).text = provider.name
                if (showDebug) {
                    findViewById<AppCompatTextView>(R.id.summary).apply {
                        text = provider.packageName
                        isVisible = true
                    }
                }
                findViewById<AppCompatRadioButton>(R.id.select).apply {
                    isChecked = providers[position]::class.java.name == currentProvider
                    setOnCheckedChangeListener { _, _ ->
                        onSelect(provider)
                    }
                    applyColor(color)
                }
                setOnClickListener {
                    onSelect(provider)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        (dialog as AlertDialog).applyAccent()
    }

    override fun onDialogClosed(positiveResult: Boolean) {
    }

    companion object {
        fun newInstance(preference: SearchProviderPreference) =
                SelectSearchProviderFragment().apply {
                    arguments = Bundle(1).apply {
                        putString("key", preference.key)
                    }
                }
    }
}