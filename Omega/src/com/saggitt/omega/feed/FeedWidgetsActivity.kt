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

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.DisplayMetrics
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.launcher3.Launcher
import com.android.launcher3.R
import com.saggitt.omega.feed.AddedWidgetsAdapter.OnActionClickListener
import com.saggitt.omega.settings.SettingsBaseActivity
import java.util.*

// TODO replace startActivityForResult()
class FeedWidgetsActivity : SettingsBaseActivity(), OnActionClickListener {
    private var mAppWidgetManager: AppWidgetManager? = null
    private var mAppWidgetHost: AppWidgetHost? = null
    private var mLauncher: Launcher? = null
    private var mAddedWidgetsAdapter: AddedWidgetsAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.widget_feed)
        mLauncher = Launcher.getLauncher(applicationContext)
        mLauncher?.let {
            mAppWidgetManager = AppWidgetManager.getInstance(it.applicationContext)
            mAppWidgetHost = it.appWidgetHost
        }
        val addedWidgets = findViewById<RecyclerView>(R.id.added_widgets_recycler_view)
        addedWidgets.layoutManager = LinearLayoutManager(applicationContext)
        addedWidgets.setHasFixedSize(false)
        addedWidgets.isNestedScrollingEnabled = false
        addedWidgets.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        mAddedWidgetsAdapter = AddedWidgetsAdapter(this, metrics.densityDpi)
        addedWidgets.adapter = mAddedWidgetsAdapter
        refreshRecyclerView()
        findViewById<View>(R.id.add_widget_button).setOnClickListener { selectWidget() }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun refreshRecyclerView() {
        val widgets: MutableList<Widget> = ArrayList()
        val widgetIds = mAppWidgetHost!!.appWidgetIds
        Arrays.sort(widgetIds)
        for (id in widgetIds) {
            val appWidgetInfo = mAppWidgetManager!!.getAppWidgetInfo(id)
            if (appWidgetInfo != null) {
                val widget = Widget()
                widget.id = id
                widget.info = appWidgetInfo
                widgets.add(widget)
            }
        }
        mAddedWidgetsAdapter!!.setAppWidgetProviderInfos(widgets)
    }

    fun selectWidget() {
        val appWidgetId = mAppWidgetHost!!.allocateAppWidgetId()
        val pickIntent = Intent(this, WidgetPicker::class.java)
        pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        addEmptyData(pickIntent)
        startActivityForResult(pickIntent, REQUEST_PICK_APPWIDGET)
    }

    fun addEmptyData(pickIntent: Intent) {
        val customInfo: ArrayList<Parcelable> = arrayListOf()
        pickIntent.putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_INFO, customInfo)
        val customExtras: ArrayList<Parcelable> = arrayListOf()
        pickIntent.putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_EXTRAS, customExtras)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_PICK_APPWIDGET) {
                configureWidget(data)
            } else if (requestCode == REQUEST_CREATE_APPWIDGET) {
                createWidget(data)
            }
        } else if (resultCode == RESULT_CANCELED && data != null) {
            val appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
            if (appWidgetId != -1) {
                removeWidget(appWidgetId)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun configureWidget(data: Intent?) {
        val extras = data!!.extras
        val appWidgetId = extras!!.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
        val appWidgetInfo = mAppWidgetManager!!.getAppWidgetInfo(appWidgetId)
        if (appWidgetInfo?.configure != null) {
            /*Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
            intent.setComponent(appWidgetInfo.configure);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            startActivityForResult(intent, REQUEST_CREATE_APPWIDGET);*/
            startAppWidgetConfigureActivitySafely(appWidgetId)
        } else {
            createWidget(data)
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    fun createWidget(data: Intent?) {
        val extras = data!!.extras
        val appWidgetId = extras!!.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
        val appWidgetInfo = mAppWidgetManager!!.getAppWidgetInfo(appWidgetId)
        val hostView = mAppWidgetHost!!.createView(
            applicationContext, appWidgetId,
            appWidgetInfo
        ) as RoundedWidgetView
        hostView.setAppWidget(appWidgetId, appWidgetInfo)
        //WidgetManager.getInstance().enqueueAddWidget(hostView);
        refreshRecyclerView()
    }

    fun startAppWidgetConfigureActivitySafely(appWidgetId: Int) {
        try {
            mAppWidgetHost!!.startAppWidgetConfigureActivityForResult(
                this, appWidgetId, 0,
                REQUEST_CREATE_APPWIDGET, null
            )
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show()
        }
    }

    override fun removeWidget(id: Int) {
        mAppWidgetHost!!.deleteAppWidgetId(id)
    }

    companion object {
        private const val REQUEST_PICK_APPWIDGET = 455
        private const val REQUEST_CREATE_APPWIDGET = 189
    }
}