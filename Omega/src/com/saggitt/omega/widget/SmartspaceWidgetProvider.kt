/*
 * Copyright (c) 2020 Omega Launcher
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
 */

package com.saggitt.omega.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.android.launcher3.BuildConfig
import com.android.launcher3.R
import com.google.android.apps.nexuslauncher.smartspace.SmartspaceBroadcastReceiver

class SmartspaceWidgetProvider : AppWidgetProvider() {

    fun getlayout(): Int {
        return R.layout.smartspace_widget
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        /* init widget */
        onReceive(context, Intent(ACTION_WIDGET_INIT))

        /* ask a refresh from the service if there is one */
        context.sendBroadcast(Intent(ACTION_WIDGET_INIT).setPackage(BuildConfig.APPLICATION_ID))
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == null || !action.startsWith(ACTION_WIDGET_PREFIX)) {
            super.onReceive(context, intent)
            return
        }

        val views = RemoteViews(BuildConfig.APPLICATION_ID, getlayout())
        val partial = ACTION_WIDGET_INIT != action


        applyUpdate(context, views, partial)
    }

    private fun applyUpdate(context: Context, views: RemoteViews, partial: Boolean) {
        val widget = ComponentName(context, this.javaClass)
        val manager = AppWidgetManager.getInstance(context)
        if (partial)
            manager.partiallyUpdateAppWidget(manager.getAppWidgetIds(widget), views)
        else
            manager.updateAppWidget(widget, views)
    }


    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        context.sendBroadcast(Intent(ACTION_WIDGET_ENABLED, null, context.applicationContext, SmartspaceBroadcastReceiver::class.java))
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        context.sendBroadcast(Intent(ACTION_WIDGET_DISABLED, null, context.applicationContext, SmartspaceBroadcastReceiver::class.java))
    }

    companion object {
        const val TAG = "SmartspaceWidget"
        val ACTION_WIDGET_PREFIX = "widget."
        val ACTION_WIDGET_INIT = ACTION_WIDGET_PREFIX + "INIT"
        val ACTION_WIDGET_UPDATE = ACTION_WIDGET_PREFIX + "UPDATE"
        val ACTION_WIDGET_UPDATE_COVER = ACTION_WIDGET_PREFIX + "UPDATE_COVER"
        val ACTION_WIDGET_UPDATE_POSITION = ACTION_WIDGET_PREFIX + "UPDATE_POSITION"
        val ACTION_WIDGET_ENABLED = ACTION_WIDGET_PREFIX + "ENABLED"
        val ACTION_WIDGET_DISABLED = ACTION_WIDGET_PREFIX + "DISABLED"
    }
}