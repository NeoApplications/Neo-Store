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
import com.saggitt.omega.iconpack.IconPackInfo
import com.saggitt.omega.iconpack.IconPackProvider
import com.saggitt.omega.preferences.custom.IconPackListPreference
import com.saggitt.omega.util.applyAccent
import com.saggitt.omega.util.applyColor
import com.saggitt.omega.util.isVisible
import com.saggitt.omega.util.omegaPrefs

class IconPackListFragment : PreferenceDialogFragmentCompat() {

    private lateinit var list: ListView

    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)
        list = view.findViewById(R.id.pack_list)

        val ipm = IconPackProvider(requireContext())
        val packList = ipm.getIconPackList()

        list.adapter = IconPackAdapter(
                requireContext(),
                packList,
                requireContext().omegaPrefs.iconPackPackage
        ) {
            context?.omegaPrefs?.iconPackPackage = it.packageName
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        (dialog as AlertDialog).applyAccent()
    }

    override fun onDialogClosed(positiveResult: Boolean) {}

    inner class IconPackAdapter(
            context: Context,
            private val iconPacks: List<IconPackInfo>,
            private val currentPack: String,
            private val onSelect: (IconPackInfo) -> Unit
    ) : ArrayAdapter<IconPackInfo>(context, R.layout.list_item_icon, 0, iconPacks) {

        private val color = context.omegaPrefs.accentColor
        private val showDebug = context.omegaPrefs.showDebugInfo

        @SuppressLint("ViewHolder")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            return View.inflate(context, R.layout.list_item_icon, null).apply {
                val pack = iconPacks[position]
                findViewById<AppCompatImageView>(R.id.icon).setImageDrawable(pack.icon)
                findViewById<AppCompatTextView>(R.id.title).text = pack.name
                if (showDebug) {
                    findViewById<AppCompatTextView>(R.id.summary).apply {
                        text = pack.packageName
                        isVisible = true
                    }
                }

                findViewById<AppCompatRadioButton>(R.id.select).apply {
                    isChecked = iconPacks[position].packageName == currentPack
                    setOnCheckedChangeListener { _, _ ->
                        onSelect(pack)
                    }
                    applyColor(color)
                }
                setOnClickListener {
                    onSelect(pack)
                }
            }
        }
    }

    companion object {
        fun newInstance(preference: IconPackListPreference) =
                IconPackListFragment().apply {
                    arguments = Bundle(1).apply {
                        putString("key", preference.key)
                    }
                }
    }

}