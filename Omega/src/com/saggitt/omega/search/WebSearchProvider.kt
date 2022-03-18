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

package com.saggitt.omega.search

import android.content.Context
import android.content.Intent
import android.util.Log
import com.android.launcher3.LauncherAppState
import com.android.launcher3.LauncherState
import com.android.launcher3.anim.AnimatorListeners
import com.saggitt.omega.util.openURLinBrowser
import com.saggitt.omega.util.toArrayList
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray

abstract class WebSearchProvider(context: Context) : SearchProvider(context) {

    override val supportsVoiceSearch = false
    override val supportsAssistant = false
    override val supportsFeed = false

    /**
     * Suggestions API URL. %s will be replaced with the search query.
     */
    protected abstract val suggestionsUrl: String?

    override fun startSearch(callback: (intent: Intent) -> Unit) {
        val launcher = LauncherAppState.getInstanceNoCreate().launcher
        launcher.stateManager.goToState(
            LauncherState.ALL_APPS,
            true,
            AnimatorListeners.forEndCallback(Runnable { launcher.appsView.searchUiManager.startSearch() })
        )
    }

    open fun getSuggestions(query: String): List<String> {
        val client = OkHttpClient()
        if (suggestionsUrl == null) return emptyList()
        if (query.isEmpty()) return emptyList()
        val request = Request.Builder()
            .url(suggestionsUrl!!.format(query))
            .build()
        try {
            val response = client.newCall(request).execute()
            val result = JSONArray(response.body?.string())
                .getJSONArray(1)
                .toArrayList<String>()
                .take(MAX_SUGGESTIONS)
            response.close()

            Log.e("WebSearchProvider", "Websearch Query: $result")
            return result
        } catch (ex: Exception) {
            Log.e("WebSearchProvider", ex.message ?: "", ex)
        }

        return emptyList()
    }

    open fun openResults(query: String) {
        openURLinBrowser(context, getResultUrl(query))
    }

    protected open fun getResultUrl(query: String) = packageName.format(query)

    companion object {
        const val MAX_SUGGESTIONS = 5
    }
}