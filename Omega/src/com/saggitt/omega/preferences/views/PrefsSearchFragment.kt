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

import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import com.android.launcher3.R
import com.saggitt.omega.PREFS_SEARCH_ASSISTANT
import com.saggitt.omega.PREFS_SEARCH_SHOW_ASSISTANT
import com.saggitt.omega.search.SearchProvider
import com.saggitt.omega.search.SearchProviderController
import com.saggitt.omega.search.WebSearchProvider

class PrefsSearchFragment :
    BasePreferenceFragment(R.xml.preferences_search, R.string.title__general_search),
    SearchProviderController.OnProviderChangeListener {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        SearchProviderController.getInstance(requireContext()).addOnProviderChangeListener(this)
        reloadPreferences()
    }

    override fun onDestroy() {
        super.onDestroy()
        SearchProviderController.getInstance(requireContext()).removeOnProviderChangeListener(this)
    }

    override fun onSearchProviderChanged() {
        reloadPreferences()
    }

    private fun reloadPreferences() {
        findPreference<Preference>(PREFS_SEARCH_SHOW_ASSISTANT)?.apply {
            val provider: SearchProvider =
                SearchProviderController.getInstance(requireContext()).searchProvider
            isEnabled = provider !is WebSearchProvider
        }

        findPreference<Preference>(PREFS_SEARCH_ASSISTANT)?.apply {
            val provider: SearchProvider =
                SearchProviderController.getInstance(requireContext()).searchProvider
            isEnabled = provider !is WebSearchProvider
        }
    }
}