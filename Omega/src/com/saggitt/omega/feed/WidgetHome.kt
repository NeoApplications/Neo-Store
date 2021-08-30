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

package com.saggitt.omega.feed

import android.app.Activity.RESULT_OK
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.launcher3.LauncherAppState
import com.android.launcher3.R
import com.saggitt.omega.BlankActivity.Companion.startActivityForResult
import com.saggitt.omega.OmegaLauncher
import com.saggitt.omega.theme.white200
import java.util.*

@Composable
fun WidgetHomeContent(context: Context) {
    var mAppWidgetManager: AppWidgetManager? = null
    var mAppWidgetHost: AppWidgetHost? = null
    val mLauncher = (LauncherAppState.getInstance(context).launcher as? OmegaLauncher)
    val padding = 8.dp
    mLauncher?.let {
        mAppWidgetManager = AppWidgetManager.getInstance(it.applicationContext)
        mAppWidgetHost = it.appWidgetHost
    }

    Column(
        Modifier.padding(padding)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = { selectWidget(context, mAppWidgetHost, mAppWidgetManager) }) {
                Text(stringResource(R.string.feed_add_widget), color = white200)
            }
        }

        Spacer(Modifier.size(8.dp))

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {

            /*items(
                items = widgets,
                itemContent = {
                    WidgetListItem(widget = it)
                }
            )*/
        }
    }


}

@Preview
@Composable
fun addWidgetButton() {
    Button(
        onClick = { }) {
        Text(stringResource(R.string.feed_add_widget), color = white200)
    }
}

fun selectWidget(
    context: Context,
    mAppWidgetHost: AppWidgetHost?,
    mAppWidgetManager: AppWidgetManager?
) {
    val appWidgetId: Int = mAppWidgetHost!!.allocateAppWidgetId()
    val pickIntent = Intent(context, WidgetPicker::class.java)
    pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
    addEmptyData(pickIntent)
    startActivityForResult(context, pickIntent, 455, 0) { resultCode, _ ->
        if (resultCode == RESULT_OK) {

        }
    }
}

fun addEmptyData(pickIntent: Intent) {
    val customInfo: ArrayList<Parcelable> = arrayListOf()
    pickIntent.putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_INFO, customInfo)
    val customExtras: ArrayList<Parcelable> = arrayListOf()
    pickIntent.putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_EXTRAS, customExtras)
}

fun loadWidgets(context: Context): ArrayList<Widget> {

    var mAppWidgetManager: AppWidgetManager? = null
    var mAppWidgetHost: AppWidgetHost? = null
    val mLauncher = LauncherAppState.getInstance(context).launcher as? OmegaLauncher
    val widgets: ArrayList<Widget> = ArrayList()
    mLauncher?.let {
        mAppWidgetManager = AppWidgetManager.getInstance(it.applicationContext)
        mAppWidgetHost = it.appWidgetHost
    }
    val widgetIds: IntArray = mAppWidgetHost!!.appWidgetIds
    Arrays.sort(widgetIds)
    for (id in widgetIds) {
        val appWidgetInfo: AppWidgetProviderInfo = mAppWidgetManager!!.getAppWidgetInfo(id)
        val widget = Widget(1, appWidgetInfo)
        widget.id = id
        widget.info = appWidgetInfo
        widgets.add(widget)
    }
    return widgets;
}