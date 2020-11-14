/*
 *  Copyright (c) 2020 Omega Launcher
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.saggitt.omega.search.webproviders

import android.content.Context
import android.content.Intent
import android.util.Log
import com.android.launcher3.LauncherAppState
import com.android.launcher3.LauncherState
import com.android.launcher3.Utilities
import com.saggitt.omega.search.SearchProvider
import com.saggitt.omega.util.OkHttpClientBuilder
import com.saggitt.omega.util.toArrayList
import okhttp3.Request
import org.json.JSONArray

abstract class WebSearchProvider(context: Context) : SearchProvider(context) {
    protected val client = OkHttpClientBuilder().build(context)

    override val supportsVoiceSearch = false
    override val supportsAssistant = false
    override val supportsFeed = false

    /**
     * Web URL to the search results page. %s will be replaced with the search query.
     */
    protected abstract val searchUrl: String

    /**
     * Suggestions API URL. %s will be replaced with the search query.
     */
    protected abstract val suggestionsUrl: String?

    override fun startSearch(callback: (intent: Intent) -> Unit) {
        val launcher = LauncherAppState.getInstanceNoCreate().launcher
        launcher.stateManager.goToState(LauncherState.ALL_APPS, true) {
            launcher.appsView.searchUiManager.startSearch()
        }
    }

    open fun getSuggestions(query: String): List<String> {
        if (suggestionsUrl == null) return emptyList()
        try {
            val response = client.newCall(Request.Builder().url(suggestionsUrl!!.format(query)).build()).execute()
            val result = JSONArray(response.body?.string())
                    .getJSONArray(1)
                    .toArrayList<String>()
                    .take(MAX_SUGGESTIONS)
            response.close();
            return result;
        } catch (ex: Exception) {
            Log.e("WebSearchProvider", ex.message ?: "", ex)
        }

        return emptyList()
    }

    open fun openResults(query: String) {
        Utilities.openURLinBrowser(context, getResultUrl(query))
    }

    protected open fun getResultUrl(query: String) = searchUrl.format(query)

    companion object {
        const val MAX_SUGGESTIONS = 5
    }
}