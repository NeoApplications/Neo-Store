/*
 * This file is part of Neo Launcher
 * Copyright (c) 2022   Neo Launcher Team
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

package com.saggitt.omega.search

import android.content.Context
import com.android.launcher3.LauncherAppState
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.allapps.AllAppsGridAdapter.AdapterItem
import com.android.launcher3.allapps.search.DefaultAppSearchAlgorithm
import com.android.launcher3.model.AllAppsList
import com.android.launcher3.model.BaseModelUpdateTask
import com.android.launcher3.model.BgDataModel
import com.android.launcher3.model.data.AppInfo
import com.android.launcher3.search.SearchCallback
import com.android.launcher3.search.StringMatcherUtility
import com.saggitt.omega.OmegaLauncher
import com.saggitt.omega.data.PeopleRepository
import me.xdrop.fuzzywuzzy.FuzzySearch
import me.xdrop.fuzzywuzzy.algorithms.WeightedRatio
import java.util.*

class CustomAppSearchAlgorithm(val context: Context) : DefaultAppSearchAlgorithm(context) {

    private val prefs = Utilities.getOmegaPrefs(context)

    override fun doSearch(query: String, callback: SearchCallback<AdapterItem>?) {
        mAppState.model.enqueueModelUpdateTask(object : BaseModelUpdateTask() {
            override fun execute(app: LauncherAppState, dataModel: BgDataModel, apps: AllAppsList) {
                val result = getSearchResult(apps.data, query)
                var suggestions = emptyList<String?>()

                if (prefs.searchContacts.onGetValue()) {
                    val repository = PeopleRepository.INSTANCE.get(app.context)
                    val contacts = repository.findPeople(query)
                    val total = result.size
                    var position = total + 1
                    if (contacts.isNotEmpty()) {
                        result.add(AdapterItem.asAllAppsDivider(position))
                        position++
                        result.add(
                            AdapterItem.asSectionHeader(
                                position,
                                context.getString(R.string.section_contacts)
                            )
                        )
                        position++
                        contacts.forEach {
                            result.add(AdapterItem.asContact(position, it))
                            position++
                        }
                    }
                }

                mResultHandler.post {
                    callback?.onSearchResult(
                        query,
                        result,
                        suggestions
                    )
                }

                if (callback!!.showWebResult()) {
                    suggestions = getSuggestions(query)
                    callback.setShowWebResult(false)
                }
                mResultHandler.post {
                    callback.onSearchResult(
                        query,
                        result,
                        suggestions
                    )
                }
            }
        })
    }

    private fun getSearchResult(apps: MutableList<AppInfo>, query: String): ArrayList<AdapterItem> {
        return if (prefs.searchFuzzy.onGetValue()) {
            getFuzzySearchResult(apps, query)
        } else {
            getTitleMatchResult(apps, query)
        }
    }

    private fun getFuzzySearchResult(apps: List<AppInfo>, query: String): ArrayList<AdapterItem> {
        val result = ArrayList<AdapterItem>()
        val mApps = if (prefs.searchHiddenApps.onGetValue()) {
            OmegaLauncher.getLauncher(context).allApps
        } else {
            apps
        }

        val matcher = FuzzySearch.extractSorted(
            query.lowercase(Locale.getDefault()), mApps,
            { it!!.title.toString() }, WeightedRatio(), 65
        )
        var resultCount = 0
        val total = matcher.size
        var i = 0
        while (i < total && resultCount < MAX_RESULTS_COUNT) {
            val info = matcher!![i]
            val appItem = AdapterItem.asApp(resultCount, "", info.referent, resultCount)
            result.add(appItem)
            resultCount++
            i++
        }

        return result
    }

    override fun getTitleMatchResult(
        apps: MutableList<AppInfo>,
        query: String?
    ): ArrayList<AdapterItem> {

        // Do an intersection of the words in the query and each title, and filter out all the
        // apps that don't match all of the words in the query.
        val queryTextLower = query!!.lowercase(Locale.getDefault())
        val result = ArrayList<AdapterItem>()

        val matcher = StringMatcherUtility.StringMatcher.getInstance()

        var resultCount = 0
        val total = apps.size
        val mApps = if (prefs.searchHiddenApps.onGetValue()) {
            OmegaLauncher.getLauncher(context).allApps
        } else {
            apps
        }
        var i = 0

        while (i < total && resultCount < MAX_RESULTS_COUNT) {
            val info = mApps[i]
            if (StringMatcherUtility.matches(queryTextLower, info.title.toString(), matcher)) {
                val appItem = AdapterItem.asApp(resultCount, "", info, resultCount)
                result.add(appItem)
                resultCount++
            }

            i++
        }
        return result
    }

    private fun getSuggestions(query: String): List<String?> {
        if (!Utilities.getOmegaPrefs(context).searchGlobal.onGetValue()) {
            return emptyList<String>()
        }
        val provider = SearchProviderController
            .getInstance(context).searchProvider
        return if (provider is WebSearchProvider) {
            provider.getSuggestions(query)
        } else emptyList<String>()
    }
}