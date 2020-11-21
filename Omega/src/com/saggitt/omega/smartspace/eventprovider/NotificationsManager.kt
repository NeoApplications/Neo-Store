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

/*
 *     This file is part of Lawnchair Launcher.
 *
 *     Lawnchair Launcher is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Lawnchair Launcher is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Lawnchair Launcher.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saggitt.omega.smartspace.eventprovider

import android.service.notification.StatusBarNotification
import com.android.launcher3.notification.NotificationListener
import com.saggitt.omega.util.runOnMainThread
import com.saggitt.omega.util.runOnUiWorkerThread

object NotificationsManager : NotificationListener.StatusBarNotificationsChangedListener {

    private val notificationsMap = mutableMapOf<String, StatusBarNotification>()
    private val listeners = mutableListOf<OnChangeListener>()

    var notifications = emptyList<StatusBarNotification>()
        private set

    init {
        NotificationListener.setStatusBarNotificationsChangedListener(this)
    }

    fun addListener(listener: OnChangeListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: OnChangeListener) {
        listeners.remove(listener)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        notificationsMap[sbn.key] = sbn
        onChange()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        notificationsMap.remove(sbn.key)
        onChange()
    }

    override fun onNotificationFullRefresh() {
        runOnUiWorkerThread {
            val tmpMap = NotificationListener.getInstanceIfConnected()
                    ?.activeNotifications?.associateBy { it.key }
            runOnMainThread {
                notificationsMap.clear()
                if (tmpMap != null) {
                    notificationsMap.putAll(tmpMap)
                }
                onChange()
            }
        }
    }

    private fun onChange() {
        val notifications = notificationsMap.values.toList()
        NotificationsManager.notifications = notifications
        listeners.forEach(OnChangeListener::onNotificationsChanged)
    }

    interface OnChangeListener {

        fun onNotificationsChanged()
    }
}
