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

package com.saggitt.omega.settings

import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter

class AboutPagerAdapter(private val mLists: Array<View>?, private val mTitles: Array<String>) : PagerAdapter() {
    override fun getCount(): Int {
        return mLists?.size ?: 0
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        return mLists!![position]
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return if (position < 0 || position >= mTitles.size)
            ""
        else
            mTitles[position]
    }
}