/*
 *  This file is part of Omega Launcher
 *  Copyright (c) 2021   Omega Launcher Team
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

package com.saggitt.omega.icons

import android.content.*
import android.content.Intent.*
import android.content.pm.ActivityInfo
import android.content.pm.LauncherActivityInfo
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Process
import android.os.UserHandle
import android.os.UserManager
import androidx.core.content.getSystemService
import com.android.launcher3.Utilities
import com.android.launcher3.icons.IconProvider
import com.android.launcher3.util.SafeCloseable
import com.saggitt.omega.iconpack.IconEntry
import com.saggitt.omega.iconpack.IconPack
import com.saggitt.omega.iconpack.IconPackProvider
import com.saggitt.omega.util.MultiSafeCloseable
import java.util.function.Supplier

class CustomIconProvider @JvmOverloads constructor(
    context: Context,
    supportsIconTheme: Boolean = false
) :
    IconProvider(context, supportsIconTheme) {
    private val prefs = Utilities.getOmegaPrefs(context)
    private val mContext = context
    private val iconPackProvider = IconPackProvider.INSTANCE.get(context)
    private val iconPack get() = iconPackProvider.getIconPack(prefs.iconPackPackage.toString())

    override fun getIconWithOverrides(
        packageName: String,
        component: String,
        user: UserHandle,
        iconDpi: Int,
        fallback: Supplier<Drawable>
    ): Drawable {
        val iconPack = this.iconPack
        val componentName = ComponentName(packageName, component)
        var iconEntry: IconEntry? = null
        if (iconPack != null) {
            if (iconEntry == null) {
                iconEntry = iconPack.getCalendar(componentName)?.getIconEntry(getDay())
            }
            if (iconEntry == null) {
                iconEntry = iconPack.getIcon(componentName)
            }
        }
        val icon = iconEntry?.getDrawable(iconDpi, user)
        return icon ?: super.getIconWithOverrides(packageName, component, user, iconDpi, fallback)
    }

    override fun getIcon(info: ActivityInfo?): Drawable {
        return CustomAdaptiveIconDrawable.wrapNonNull(super.getIcon(info))
    }

    override fun getIcon(info: ActivityInfo?, iconDpi: Int): Drawable {
        return CustomAdaptiveIconDrawable.wrapNonNull(super.getIcon(info, iconDpi))
    }

    override fun getIcon(info: LauncherActivityInfo?, iconDpi: Int): Drawable {
        return CustomAdaptiveIconDrawable.wrapNonNull(super.getIcon(info, iconDpi))
    }

    override fun registerIconChangeListener(
        callback: IconChangeListener,
        handler: Handler
    ): SafeCloseable {
        return MultiSafeCloseable().apply {
            add(super.registerIconChangeListener(callback, handler))
            add(IconPackChangeReceiver(mContext, handler, callback))
        }
    }

    private inner class IconPackChangeReceiver(
        private val context: Context,
        private val handler: Handler,
        private val callback: IconChangeListener
    ) : SafeCloseable {

        private var calendarAndClockChangeReceiver: CalendarAndClockChangeReceiver? = null
            set(value) {
                field?.close()
                field = value
            }
        private var iconState = systemIconState
        private val iconPackPref = prefs.iconPackPackage
        /*private val subscription = iconPackPref.subscribeChanges {
            val newState = systemIconState
            if (iconState != newState) {
                iconState = newState
                callback.onSystemIconStateChanged(iconState)
                recreateCalendarAndClockChangeReceiver()
            }
        }*/

        init {
            recreateCalendarAndClockChangeReceiver()
        }

        private fun recreateCalendarAndClockChangeReceiver() {
            val iconPack =
                IconPackProvider.INSTANCE.get(context).getIconPack(iconPackPref.onGetValue())
            calendarAndClockChangeReceiver = if (iconPack != null) {
                CalendarAndClockChangeReceiver(context, handler, iconPack, callback)
            } else {
                null
            }
        }

        override fun close() {
            calendarAndClockChangeReceiver = null
            //subscription.close()
        }
    }

    private class CalendarAndClockChangeReceiver(
        private val context: Context, handler: Handler,
        private val iconPack: IconPack,
        private val callback: IconChangeListener
    ) : BroadcastReceiver(), SafeCloseable {

        init {
            val filter = IntentFilter(ACTION_TIMEZONE_CHANGED)
            filter.addAction(ACTION_TIME_CHANGED)
            filter.addAction(ACTION_DATE_CHANGED)
            context.registerReceiver(this, filter, null, handler)
        }

        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_TIMEZONE_CHANGED -> {
                    iconPack.getClocks().forEach { componentName ->
                        callback.onAppIconChanged(componentName.packageName, Process.myUserHandle())
                    }
                }
                ACTION_DATE_CHANGED, ACTION_TIME_CHANGED -> {
                    context.getSystemService<UserManager>()?.userProfiles?.forEach { user ->
                        iconPack.getCalendars().forEach { componentName ->
                            callback.onAppIconChanged(componentName.packageName, user)
                        }
                    }
                }
            }
        }

        override fun close() {
            context.unregisterReceiver(this)
        }
    }
}