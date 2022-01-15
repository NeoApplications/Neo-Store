/*
 *  This file is part of Omega Launcher
 *  Copyright (c) 2021   Omega Launcher Team
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

package com.saggitt.omega.preferences.views

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.XmlRes
import androidx.fragment.app.DialogFragment
import com.android.launcher3.R
import com.saggitt.omega.preferences.custom.CustomDialogPreference
import com.saggitt.omega.util.getThemeAttr

class PreferenceDialogFragment : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_preference_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val content = requireArguments().getInt(KEY_CONTENT)
        val fragment = DialogSettingsFragment.newInstance("", content)
        childFragmentManager.beginTransaction()
                .replace(R.id.fragment_content, fragment)
                .commit()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireActivity(), requireActivity().getThemeAttr(R.attr.alertDialogTheme))
    }

    companion object {

        private const val KEY_THEME = "theme"
        private const val KEY_CONTENT = "content"

        fun newInstance(preference: CustomDialogPreference) = PreferenceDialogFragment().apply {
            arguments = Bundle(2).apply {
                putInt(KEY_THEME, preference.context.getThemeAttr(R.attr.alertDialogTheme))
                putInt(KEY_CONTENT, preference.content)
            }
        }
    }
}

class DialogSettingsFragment(val content: Int, val title: String) :
    BasePreferenceFragment(content) {

    companion object {
        fun newInstance(title: String?, @XmlRes content: Int): DialogSettingsFragment {
            val fragment = DialogSettingsFragment(content, title ?: "")
            val b = Bundle(2)
            b.putString("title", title)
            b.putInt("content_res_id", content)
            fragment.arguments = b
            return fragment
        }
    }
}
