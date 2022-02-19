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
import android.content.Intent
import android.util.AttributeSet
import com.android.launcher3.DeviceProfile
import com.android.launcher3.LauncherAppState
import com.android.launcher3.R
import com.android.launcher3.allapps.AllAppsContainerView
import com.android.launcher3.allapps.SearchUiManager
import com.saggitt.omega.OmegaLauncher.Companion.getLauncher
import com.saggitt.omega.search.providers.AppsSearchProvider
import kotlin.math.roundToInt

class AllAppsQsbLayout(context: Context, attrs: AttributeSet? = null) :
    AbstractQsbLayout(context, attrs), SearchUiManager {

    private val mUseFallbackSearch = false
    var removeFallback = false

    private var mFallback: AllAppsQsbFallback? = null
    private lateinit var mAppsView: AllAppsContainerView

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val requestedWidth = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        val idp = LauncherAppState.getIDP(mContext)!!
        val dp: DeviceProfile = idp.getDeviceProfile(mContext)
        val cellWidth = DeviceProfile.calculateCellWidth(
            requestedWidth,
            dp.cellLayoutBorderSpacingPx,
            dp.numShownAllAppsColumns
        )
        val width = requestedWidth - (cellWidth - (dp.allAppsIconSizePx * 0.92f).roundToInt())
        setMeasuredDimension(width, height)

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child != null) {
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0)
            }
        }
    }

    override fun initializeSearch(allAppsContainerView: AllAppsContainerView) {
        mAppsView = allAppsContainerView
    }

    override fun resetSearch() {
        if (mUseFallbackSearch) {
            resetFallbackView()
        } else if (!removeFallback) {
            removeFallbackView()
        }
    }

    override fun startSearch() {
        post { startSearch("") }
    }

    override fun startSearch(str: String?) {
        val provider = SearchProviderController.getInstance(mContext).searchProvider
        if (shouldUseFallbackSearch(provider)) {
            startDrawerSearch(str)
        } else {
            provider.startSearch { intent: Intent? ->
                getLauncher(context).startActivity(intent)
            }
        }
    }

    private fun startDrawerSearch(query: String?) {
        ensureFallbackView();
        mFallback?.setText(query);
        mFallback?.showKeyboard();
    }

    private fun ensureFallbackView() {
        if (mFallback == null) {
            setOnClickListener(null)
            mFallback = getLauncher().layoutInflater
                .inflate(
                    R.layout.search_container_all_apps_fallback,
                    this,
                    false
                ) as AllAppsQsbFallback
            val allAppsContainerView: AllAppsContainerView = mAppsView
            mFallback!!.allAppsQsbLayout = this
            mFallback!!.initializeSearch(allAppsContainerView)
            addView(mFallback)
        }
    }

    private fun resetFallbackView() {
        if (mFallback != null) {
            mFallback!!.reset()
            mFallback!!.clearSearchResult()
        }
    }

    private fun removeFallbackView() {
        if (mFallback != null) {
            mFallback!!.clearSearchResult()
            removeView(mFallback)
            mFallback = null
        }
    }

    private fun shouldUseFallbackSearch(provider: SearchProvider) =
        !prefs.allAppsGlobalSearch
                || provider is AppsSearchProvider
                || provider is WebSearchProvider
}