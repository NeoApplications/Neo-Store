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

package com.saggitt.omega.dash

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import com.android.launcher3.Launcher
import com.android.launcher3.R
import com.saggitt.omega.views.CenterFloatingView

/*
* Esta clase despliega la vista circular desde la parte inferior de la pantalla.
*/
class DashBottomSheet(context: Context) : RelativeLayout(context) {
    private var adapter: DashItemAdapter? = null
    private var mInflater: LayoutInflater? = null

    init {
        View.inflate(context, R.layout.dash_view, this)
        val items = DashItems(context)
        val itemTitles = items.getItemList()
        val circularListView = findViewById<View>(R.id.my_circular_list) as DashListView
        mInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        adapter = DashItemAdapter(mInflater, itemTitles, context)
        circularListView.setAdapter(adapter)
        circularListView.setRadius(150f)
    }

    companion object {
        fun show(launcher: Launcher, animate: Boolean) {
            val sheet = CenterFloatingView.inflate(launcher)
            val view = DashBottomSheet(launcher)
            sheet.show(view, animate)
        }
    }
}