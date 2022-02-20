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
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import com.android.launcher3.DeviceProfile
import com.android.launcher3.Insettable
import com.android.launcher3.LauncherAppState
import com.android.launcher3.R
import com.android.launcher3.allapps.AllAppsContainerView
import com.android.launcher3.allapps.SearchUiManager
import com.android.launcher3.icons.IconNormalizer
import com.saggitt.omega.OmegaLauncher.Companion.getLauncher
import com.saggitt.omega.search.providers.AppsSearchProvider
import kotlin.math.roundToInt

class AllAppsQsbLayout(context: Context, attrs: AttributeSet? = null) :
    AbstractQsbLayout(context, attrs), SearchUiManager, Insettable {

    private val mUseFallbackSearch = false
    var removeFallback = false
    private val mVerticalOffset =
        resources.getDimensionPixelSize(R.dimen.all_apps_search_vertical_offset)

    private var mFallback: AllAppsQsbFallback? = null
    private lateinit var mAppsView: AllAppsContainerView

    init {
        visibility = (if (prefs.allAppsSearch) View.VISIBLE else View.GONE)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Update the width to match the grid padding
        val idp = LauncherAppState.getIDP(mContext)!!
        val dp: DeviceProfile = idp.getDeviceProfile(mContext)
        val myRequestedWidth = MeasureSpec.getSize(widthMeasureSpec)
        val rowWidth = (myRequestedWidth - mAppsView.activeRecyclerView.paddingLeft
                - mAppsView.activeRecyclerView.paddingRight)

        val cellWidth = DeviceProfile.calculateCellWidth(
            rowWidth, dp.cellLayoutBorderSpacingPx,
            dp.numShownHotseatIcons
        )
        val iconVisibleSize = (IconNormalizer.ICON_VISIBLE_AREA_FACTOR * dp.iconSizePx).roundToInt()
        val iconPadding = cellWidth - iconVisibleSize

        val myWidth = rowWidth - iconPadding + paddingLeft + paddingRight
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(myWidth, MeasureSpec.EXACTLY),
            heightMeasureSpec
        )
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        // Shift the widget horizontally so that its centered in the parent (b/63428078)
        val parent = parent as View
        val availableWidth = parent.width - parent.paddingLeft - parent.paddingRight
        val myWidth = right - left
        val expectedLeft = parent.paddingLeft + (availableWidth - myWidth) / 2
        val shift = expectedLeft - left
        translationX = shift.toFloat()

        var containerTopMargin = 0
        if (!prefs.allAppsSearch) {
            val mlp = layoutParams as MarginLayoutParams
            containerTopMargin = -(mlp.topMargin + mlp.height)
        }
        offsetTopAndBottom(mVerticalOffset - containerTopMargin)
    }

    override fun setInsets(insets: Rect?) {
        val mlp = layoutParams as MarginLayoutParams
        mlp.topMargin = insets!!.top
        requestLayout()
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
        ensureFallbackView()
        mFallback?.setText(query)
        mFallback?.showKeyboard()
    }

    private fun ensureFallbackView() {
        if (mFallback == null) {
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