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

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckedTextView
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceDialogFragmentCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.saggitt.omega.util.applyAccent
import com.saggitt.omega.util.omegaPrefs

class SelectSearchProviderFragment : PreferenceDialogFragmentCompat() {

    private val key by lazy { requireArguments().getString("key") }
    private val value by lazy { requireArguments().getString("value") }

    private var selectedProvider: SearchProvider? = null

    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)

        val recyclerView = view.findViewById<RecyclerView>(R.id.list)
        recyclerView.adapter = ProviderListAdapter(activity as Context)
        recyclerView.layoutManager = LinearLayoutManager(activity)
    }

    private fun saveChanges() {
        Utilities.getOmegaPrefs(activity).sharedPrefs.edit().putString(key, selectedProvider.toString()).apply()
        dismiss()
    }

    override fun onPrepareDialogBuilder(builder: AlertDialog.Builder) {
        super.onPrepareDialogBuilder(builder)

        builder.setPositiveButton(null, null)
    }

    override fun onStart() {
        super.onStart()
        (dialog as AlertDialog).applyAccent()
    }

    override fun onDialogClosed(positiveResult: Boolean) {
    }

    inner class ProviderListAdapter(private val context: Context) : RecyclerView.Adapter<ProviderListAdapter.Holder>() {

        val Providers = SearchProviderController.getSearchProviders(context)
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            return Holder(LayoutInflater.from(context).inflate(R.layout.list_item_icon, parent, false))
        }

        override fun getItemCount() = Providers.size

        override fun onBindViewHolder(holder: Holder, position: Int) {
            holder.text.text = Providers[position].name
            holder.text.isChecked = Providers[position]::class.java.name == value
            holder.providerIcon.setImageDrawable((Providers[position].getIcon()))
        }

        inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
            val providerIcon = itemView.findViewById<ImageView>(R.id.provider_icon)
            val text = itemView.findViewById<CheckedTextView>(android.R.id.text1)!!.apply {
                setOnClickListener(this@Holder)
                val tintList = ColorStateList.valueOf(context.omegaPrefs.accentColor)
                compoundDrawableTintList = tintList
                backgroundTintList = tintList
            }

            override fun onClick(v: View) {
                selectedProvider = Providers[adapterPosition]
                saveChanges()
            }
        }
    }

    companion object {
        fun newInstance(preference: SearchProviderPreference) = SelectSearchProviderFragment().apply {
            arguments = Bundle(2).apply {
                putString("key", preference.key)
                putString("value", preference.value)
            }
        }
    }
}