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

package com.saggitt.omega.dash

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import com.android.launcher3.Launcher
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.saggitt.omega.views.CenterFloatingView

/*
* Esta clase despliega la vista circular desde la parte inferior de la pantalla.
*/
class DashBottomSheet(context: Context) : RelativeLayout(context) {
    private var adapter: DashItemAdapter? = null
    private var mInflater: LayoutInflater? = null
    private val prefs = Utilities.getOmegaPrefs(context)
    private val allItems = ArrayList(DashEditAdapter.getDashProviders(context))

    init {
        View.inflate(context, R.layout.dash_view, this)
        val dashProviderPrefs = prefs.dashProviders.getAll()
        val items: ArrayList<DashProvider> = ArrayList()
        dashProviderPrefs.forEach {
            val item = checkAndAddProvider(it)
            if (item != null) {
                items.add(item)
            }
        }

        val circularListView = findViewById<View>(R.id.my_circular_list) as DashListView
        mInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        adapter = DashItemAdapter(mInflater, items, context)
        circularListView.setAdapter(adapter)
        circularListView.setRadius(150f)
    }

    private fun checkAndAddProvider(s: String): DashProvider? {
        val iterator = allItems.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            if (item.name == s) {
                iterator.remove()
                return item
            }
        }
        return null
    }

    companion object {
        fun show(launcher: Launcher, animate: Boolean) {
            val sheet = CenterFloatingView.inflate(launcher)
            val view = DashBottomSheet(launcher)
            sheet.show(view, animate)
        }
    }
}