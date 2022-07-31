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

import android.content.ComponentName
import android.content.Context
import android.content.pm.LauncherApps
import com.android.launcher3.AppFilter
import com.android.launcher3.LauncherAppState
import com.android.launcher3.Utilities
import com.android.launcher3.allapps.AllAppsGridAdapter.AdapterItem
import com.android.launcher3.allapps.search.DefaultAppSearchAlgorithm
import com.android.launcher3.model.AllAppsList
import com.android.launcher3.model.BaseModelUpdateTask
import com.android.launcher3.model.BgDataModel
import com.android.launcher3.model.data.AppInfo
import com.android.launcher3.pm.UserCache
import com.android.launcher3.search.SearchCallback
import com.android.launcher3.search.StringMatcherUtility
import com.saggitt.omega.allapps.OmegaAppFilter
import me.xdrop.fuzzywuzzy.FuzzySearch
import me.xdrop.fuzzywuzzy.algorithms.WeightedRatio
import java.util.*

class CustomAppSearchAlgorithm(val context: Context) : DefaultAppSearchAlgorithm(context) {

    private val mBaseFilter: AppFilter = OmegaAppFilter(context)
    private val prefs = Utilities.getOmegaPrefs(context)

    override fun doSearch(query: String, callback: SearchCallback<AdapterItem>?) {
        mAppState.model.enqueueModelUpdateTask(object : BaseModelUpdateTask() {
            override fun execute(app: LauncherAppState, dataModel: BgDataModel, apps: AllAppsList) {
                val result = getSearchResult(apps.data, query)
                val suggestions = getSuggestions(query)
                mResultHandler.post {
                    callback!!.onSearchResult(
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
        val mApps = getApps(context, apps, mBaseFilter)

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
        val mApps = getApps(context, apps, mBaseFilter)
        var i = 0

        while (i < total && resultCount < MAX_RESULTS_COUNT) {
            val info = mApps!![i]
            if (StringMatcherUtility.matches(queryTextLower, info!!.title.toString(), matcher)) {
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

    private fun getApps(
        context: Context,
        defaultApps: List<AppInfo?>?,
        filter: AppFilter
    ): List<AppInfo?>? {
        if (!Utilities.getOmegaPrefs(context).searchHiddenApps.onGetValue()) {
            return defaultApps
        }
        val apps: MutableList<AppInfo?> = ArrayList()
        val iconCache = LauncherAppState.getInstance(context).iconCache
        for (user in UserCache.INSTANCE[context].userProfiles) {
            val duplicatePreventionCache: MutableList<ComponentName> = ArrayList()
            for (info in context.getSystemService(
                LauncherApps::class.java
            ).getActivityList(null, user)) {
                if (!filter.shouldShowApp(info.componentName, user)) {
                    continue
                }
                if (!duplicatePreventionCache.contains(info.componentName)) {
                    duplicatePreventionCache.add(info.componentName)
                    val appInfo = AppInfo(context, info, user)
                    iconCache.getTitleAndIcon(appInfo, false)
                    apps.add(appInfo)
                }
            }
        }
        return apps
    }
}