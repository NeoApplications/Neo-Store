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

package com.saggitt.omega.font

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.launcher3.R
import com.android.launcher3.databinding.ActivitySettingsSearchBinding
import com.saggitt.omega.settings.SettingsBaseActivity

class FontSelectionActivity : SettingsBaseActivity(), SearchView.OnQueryTextListener {
    private val adapter by lazy { FontAdapter(this) }
    lateinit var binding: ActivitySettingsSearchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsSearchBinding.inflate(layoutInflater)

        decorLayout.hideToolbar = true
        setContentView(R.layout.activity_settings_search)

        val listResults = binding.listResults
        listResults.shouldTranslateSelf = false
        listResults.adapter = adapter
        listResults.layoutManager = LinearLayoutManager(this)
        listResults.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy != 0) {
                    hideKeyboard()
                }
            }
        })
        setSupportActionBar(binding.searchToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.searchView.queryHint = getString(R.string.title__general_search)
        binding.searchView.setOnQueryTextListener(this)
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        adapter.search(newText ?: "")
        return true
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return true
    }

    private fun hideKeyboard() {
        val view = currentFocus ?: return
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)

        binding.listResults.requestFocus()
    }

    companion object {
        const val EXTRA_KEY = "key"
        private const val TYPE_FAMILY = 0
        private const val TYPE_DIVIDER = 1
        private const val TYPE_ADD_BUTTON = 2
    }

    inner class FontAdapter(private val context: Context) :
        RecyclerView.Adapter<FontAdapter.Holder>() {

        private val items = ArrayList<Item>()
        private val filtered = ArrayList<Item>()
        private var searchQuery = ""

        init {

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            return when (viewType) {
                TYPE_FAMILY -> FamilyHolder(parent)
                TYPE_DIVIDER -> DividerHolder(parent)
                else -> throw IllegalArgumentException("Unknown viewType $viewType")
            }
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            holder.bind(filtered[position])
        }

        override fun getItemCount() = filtered.size

        fun search(query: String) {
            searchQuery = query.toLowerCase()
            filterItems()
        }

        private fun filterItems() {
            filtered.clear()
            if (!searchQuery.isEmpty()) {
                /*items.filterTo(filtered) {
                    it is FamilyCache && it.displayName.toLowerCase().contains(searchQuery)
                }*/
            } else {
                filtered.addAll(items)
            }
            notifyDataSetChanged()
        }

        abstract inner class Holder(parent: ViewGroup, layout: Int) :
            RecyclerView.ViewHolder(LayoutInflater.from(context).inflate(layout, parent, false)) {

            open var selected = false

            open fun bind(item: Item) {

            }
        }

        inner class DividerHolder(parent: ViewGroup) : Holder(parent, R.layout.list_divider)
        inner class FamilyHolder(parent: ViewGroup) : Holder(parent, R.layout.font_item) {

        }

        abstract inner class Item {

            abstract val viewType: Int
        }
    }
}