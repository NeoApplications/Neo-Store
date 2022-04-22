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

package com.saggitt.omega.search

import android.content.Context
import android.graphics.Rect
import android.text.Selection
import android.text.SpannableStringBuilder
import android.text.method.TextKeyListener
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.ViewGroup.MarginLayoutParams
import com.android.launcher3.BaseDraggingActivity
import com.android.launcher3.ExtendedEditText
import com.android.launcher3.Insettable
import com.android.launcher3.allapps.AllAppsContainerView
import com.android.launcher3.allapps.AllAppsGridAdapter.AdapterItem
import com.android.launcher3.allapps.AllAppsStore
import com.android.launcher3.allapps.AlphabeticalAppsList
import com.android.launcher3.allapps.SearchUiManager
import com.android.launcher3.allapps.search.AllAppsSearchBarController
import com.android.launcher3.search.SearchCallback

class AllAppsQsbFallback(context: Context, attrs: AttributeSet? = null) :
    ExtendedEditText(context, attrs),
    SearchUiManager, SearchCallback<AdapterItem>,
    AllAppsStore.OnUpdateListener, Insettable {

    private val mLauncher: BaseDraggingActivity = BaseDraggingActivity.fromContext(context)
    private val mSearchBarController: AllAppsSearchBarController = AllAppsSearchBarController()
    private val mSearchQueryBuilder: SpannableStringBuilder = SpannableStringBuilder()
    private val searchProvider: SearchProvider =
        SearchProviderController.getInstance(getContext()).searchProvider

    var mApps: AlphabeticalAppsList? = null
    private var mAppsView: AllAppsContainerView? = null
    var allAppsQsbLayout: AllAppsQsbLayout? = null

    init {
        Selection.setSelection(mSearchQueryBuilder, 0)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mAppsView!!.appsStore.addUpdateListener(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mAppsView!!.appsStore.removeUpdateListener(this)
    }

    override fun initializeSearch(appsView: AllAppsContainerView) {
        mApps = appsView.apps
        mAppsView = appsView
        mSearchBarController.initialize(
            CustomAppSearchAlgorithm(mLauncher),
            this, mLauncher, this
        )
    }

    override fun onAppsUpdated() {
        mSearchBarController.refreshSearchResult()
    }

    override fun resetSearch() {
        mSearchBarController.reset()
    }

    override fun preDispatchKeyEvent(event: KeyEvent) {
        // Determine if the key event was actual text, if so, focus the search bar and then dispatch
        // the key normally so that it can process this key event
        if (!mSearchBarController.isSearchFieldFocused &&
            event.action == KeyEvent.ACTION_DOWN
        ) {
            val unicodeChar = event.unicodeChar
            val isKeyNotWhitespace = unicodeChar > 0 &&
                    !Character.isWhitespace(unicodeChar) && !Character.isSpaceChar(unicodeChar)
            if (isKeyNotWhitespace) {
                val gotKey = TextKeyListener.getInstance().onKeyDown(
                    this, mSearchQueryBuilder,
                    event.keyCode, event
                )
                if (gotKey && mSearchQueryBuilder.isNotEmpty()) {
                    mSearchBarController.focusSearchField()
                }
            }
        }
    }

    override fun startSearch() {
    }

    override fun onSearchResult(
        query: String?,
        items: java.util.ArrayList<AdapterItem>?,
        suggestions: MutableList<String>?
    ) {
        if (items != null) {
            mApps!!.setSearchResults(items)
        }
        if (suggestions != null) {
            mApps!!.setSearchSuggestions(suggestions)
        }
        if (mApps != null || suggestions != null) {
            notifyResultChanged()
            mAppsView!!.setLastSearchQuery(query)
        }
    }

    override fun onAppendSearchResult(query: String?, items: ArrayList<AdapterItem?>?) {
        if (items != null) {
            mApps!!.appendSearchResults(items)
            notifyResultChanged()
        }
    }

    override fun clearSearchResult() {
        if (mApps!!.setSearchResults(null) || mApps!!.setSearchSuggestions(null)) {
            notifyResultChanged()
        }

        // Clear the search query
        mSearchQueryBuilder.clear()
        mSearchQueryBuilder.clearSpans()
        Selection.setSelection(mSearchQueryBuilder, 0)
        allAppsQsbLayout!!.mDoNotRemoveFallback = true
        mAppsView!!.onClearSearchResult()
        allAppsQsbLayout!!.mDoNotRemoveFallback = false
    }

    override fun onSubmitSearch(query: String?): Boolean {
        return if (searchProvider is WebSearchProvider) {
            searchProvider.openResults(query!!)
            true
        } else {
            false
        }
    }

    private fun notifyResultChanged() {
        mAppsView!!.onSearchResultsChanged()
    }

    override fun setInsets(insets: Rect) {
        val mlp = layoutParams as MarginLayoutParams
        mlp.topMargin = insets.top
        requestLayout()
    }

    override fun getEditText(): ExtendedEditText {
        return this
    }
}