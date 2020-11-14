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
package com.saggitt.omega.search

import android.content.Context
import android.view.ContextThemeWrapper
import com.android.launcher3.Utilities
import com.saggitt.omega.search.providers.*
import com.saggitt.omega.search.webproviders.*
import com.saggitt.omega.theme.ThemeManager
import com.saggitt.omega.theme.ThemeOverride
import com.saggitt.omega.util.SingletonHolder
import com.saggitt.omega.util.ensureOnMainThread
import com.saggitt.omega.util.useApplicationContext

class SearchProviderController(private val context: Context) {

    private val prefs by lazy { Utilities.getOmegaPrefs(context) }
    private var cache: SearchProvider? = null
    private var cached: String = ""

    private val themeOverride = ThemeOverride(ThemeOverride.Launcher(), ThemeListener())
    private var themeRes: Int = 0

    private val listeners = HashSet<OnProviderChangeListener>()

    val isGoogle get() = searchProvider is GoogleSearchProvider

    init {
        ThemeManager.getInstance(context).addOverride(themeOverride)
    }

    fun addOnProviderChangeListener(listener: OnProviderChangeListener) {
        listeners.add(listener)
    }

    fun removeOnProviderChangeListener(listener: OnProviderChangeListener) {
        listeners.remove(listener)
    }

    fun onSearchProviderChanged() {
        cache = null
        notifyProviderChanged()
    }

    private fun notifyProviderChanged() {
        HashSet(listeners).forEach(OnProviderChangeListener::onSearchProviderChanged)
    }

    val searchProvider: SearchProvider
        get() {
            val curr = prefs.searchProvider
            if (cache == null || cached != curr) {
                cache = null
                try {
                    val constructor = Class.forName(prefs.searchProvider).getConstructor(Context::class.java)
                    val themedContext = ContextThemeWrapper(context, themeRes)
                    val prov = constructor.newInstance(themedContext) as SearchProvider
                    if (prov.isAvailable) {
                        cache = prov
                    }
                } catch (ignored: Exception) {
                }
                if (cache == null) cache = GoogleSearchProvider(context)
                cached = cache!!::class.java.name
                notifyProviderChanged()
            }
            return cache!!
        }

    inner class ThemeListener : ThemeOverride.ThemeOverrideListener {

        override val isAlive = true

        override fun applyTheme(themeRes: Int) {
            this@SearchProviderController.themeRes = themeRes
        }

        override fun reloadTheme() {
            cache = null
            applyTheme(themeOverride.getTheme(context))
            onSearchProviderChanged()
        }
    }

    interface OnProviderChangeListener {

        fun onSearchProviderChanged()
    }

    companion object : SingletonHolder<SearchProviderController, Context>(ensureOnMainThread(useApplicationContext(::SearchProviderController))) {
        fun getSearchProviders(context: Context) = listOf(
                AppSearchSearchProvider(context),
                GoogleSearchProvider(context),
                SFinderSearchProvider(context),
                GoogleGoSearchProvider(context),
                FirefoxSearchProvider(context),
                DuckDuckGoSearchProvider(context),
                BingSearchProvider(context),
                BaiduSearchProvider(context),
                YandexSearchProvider(context),
                QwantSearchProvider(context),
                SearchLiteSearchProvider(context),
                CoolSearchSearchProvider(context),
                EdgeSearchProvider(context),

                /*Web Providers*/
                BaiduWebSearchProvider(context),
                BingWebSearchProvider(context),
                DDGWebSearchProvider(context),
                EcosiaWebSearchProvider(context),
                GoogleWebSearchProvider(context),
                QwantWebSearchProvider(context),
                StartpageWebSearchProvider(context),
                YahooWebSearchProvider(context),
                YandexWebSearchProvider(context)
        ).filter { it.isAvailable }
    }
}