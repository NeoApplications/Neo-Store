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

package com.saggitt.omega.allapps

import android.content.Context
import android.util.AttributeSet
import android.util.LayoutDirection
import android.view.LayoutInflater
import com.android.launcher3.R
import com.android.launcher3.allapps.AllAppsPagedView
import com.android.launcher3.allapps.AlphabeticalAppsList
import com.android.launcher3.pageindicators.PageIndicatorDots

abstract class HorizontalAllAppsView(context: Context, attrs: AttributeSet? = null) :
    AllAppsPagedView(context, attrs) {

    lateinit var mPagerAdapter: AllAppViewPagerAdapter
    private var mViewPager: AllAppsPagedView
    private var mViewPagerLayout: HorizontalViewPagerLayout
    var mIndicator: PageIndicatorDots

    init {
        LayoutInflater.from(context).inflate(R.layout.horizontal_all_apps_view, this)
        mViewPagerLayout = findViewById(R.id.all_apps_pager)
        mViewPager = mViewPagerLayout.getViewPager()
        mViewPager.overScrollMode = 0
        mIndicator = findViewById(R.id.all_apps_indicator)
    }

    fun getViewPager(): AllAppsPagedView {
        return mViewPager
    }

    fun isRtl(): Boolean {
        return layoutDirection == LayoutDirection.RTL
    }

    fun setData(
        alphabeticalAppsList: AlphabeticalAppsList, allAppViewPagerAdapter: AllAppViewPagerAdapter?
    ) {
        mPagerAdapter = allAppViewPagerAdapter!!
        mPagerAdapter.setIndicator(mIndicator)
        mPagerAdapter.setData(alphabeticalAppsList)
        mIndicator.setMarkersCount(mPagerAdapter.getCount())
    }

    fun swipeToPage(page: Int) {
        mViewPager.snapToPage(page)
    }

}