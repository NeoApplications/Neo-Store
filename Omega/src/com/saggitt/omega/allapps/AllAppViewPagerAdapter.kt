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
import com.android.launcher3.LauncherAppState
import com.android.launcher3.allapps.AlphabeticalAppsList
import com.android.launcher3.pageindicators.PageIndicatorDots

class AllAppViewPagerAdapter(val context: Context) {
    private var mPageCount = 1
    private var mShowAnimation = false
    private var mNumColums = 0
    private var mNumRows = 0

    var mIndicator: PageIndicatorDots? = null
    var appsItem: AlphabeticalAppsList? = null

    fun setData(alphabeticalAppsList: AlphabeticalAppsList) {
        setData(alphabeticalAppsList, true)
    }

    fun setData(alphabeticalAppsList: AlphabeticalAppsList, showAnimation: Boolean) {
        appsItem = alphabeticalAppsList
        mShowAnimation = showAnimation
        mNumColums = LauncherAppState.getIDP(context).numAllAppsColumns
        mNumRows = 6 //TODO Agregar al IDP la opcion de Filas

        if (mNumRows != 0) {

            val totalApps = alphabeticalAppsList.mApps.size
            val appsPerPage = mNumColums * mNumRows
            mPageCount = (totalApps / appsPerPage) + (if (totalApps % appsPerPage > 0) 1 else 0)
        }
    }

    fun setIndicator(circleIndicator: PageIndicatorDots) {
        mIndicator = circleIndicator
    }

    fun getCount(): Int {
        return mPageCount
    }
}