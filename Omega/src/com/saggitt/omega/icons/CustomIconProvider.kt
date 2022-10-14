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

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_DATE_CHANGED
import android.content.Intent.ACTION_TIMEZONE_CHANGED
import android.content.Intent.ACTION_TIME_CHANGED
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.pm.LauncherActivityInfo
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Process
import android.os.UserHandle
import android.os.UserManager
import android.util.ArrayMap
import android.util.Log
import androidx.core.content.getSystemService
import com.android.launcher3.BuildConfig
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.icons.IconProvider
import com.android.launcher3.icons.ThemedIconDrawable
import com.android.launcher3.util.ComponentKey
import com.android.launcher3.util.SafeCloseable
import com.saggitt.omega.LAWNICONS_PACKAGE_NAME
import com.saggitt.omega.data.IconOverrideRepository
import com.saggitt.omega.iconpack.IconEntry
import com.saggitt.omega.iconpack.IconPack
import com.saggitt.omega.iconpack.IconPackProvider
import com.saggitt.omega.iconpack.IconType
import com.saggitt.omega.util.MultiSafeCloseable
import org.xmlpull.v1.XmlPullParser
import java.util.function.Supplier

class CustomIconProvider @JvmOverloads constructor(
    private val context: Context,
    supportsIconTheme: Boolean = false
) :
    IconProvider(context, supportsIconTheme) {
    private val prefs = Utilities.getOmegaPrefs(context)
    private val iconPackPref = prefs.themeIconPackGlobal.onGetValue()
    private val mContext = context
    private val iconPackProvider = IconPackProvider.INSTANCE.get(context)
    private val overrideRepo = IconOverrideRepository.INSTANCE.get(context)
    private val iconPack get() = iconPackProvider.getIconPackOrSystem(prefs.themeIconPackGlobal.onGetValue())
    private var lawniconsVersion = 0L

    private var _themeMap: Map<ComponentName, ThemedIconDrawable.ThemeData>? = null
    private val themeMap: Map<ComponentName, ThemedIconDrawable.ThemeData>
        get() {
            if (_themeMap == null) {
                _themeMap = createThemedIconMap()
            }
            return _themeMap!!
        }
    private val supportsIconTheme get() = themeMap != DISABLED_MAP

    init {
        setIconThemeSupported(supportsIconTheme)
    }

    override fun setIconThemeSupported(isSupported: Boolean) {
        _themeMap = if (isSupported) null else DISABLED_MAP
    }

    private fun resolveIconEntry(componentName: ComponentName, user: UserHandle): IconEntry? {
        val componentKey = ComponentKey(componentName, user)
        // first look for user-overridden icon
        val overrideItem = overrideRepo.overridesMap[componentKey]
        if (overrideItem != null) {
            return overrideItem.toIconEntry()
        }

        val iconPack = this.iconPack ?: return null
        // then look for dynamic calendar
        val calendarEntry = iconPack.getCalendar(componentName)
        if (calendarEntry != null) {
            return calendarEntry
        }
        // finally, look for normal icon
        return iconPack.getIcon(componentName)
    }

    override fun getIconWithOverrides(
        packageName: String,
        component: String,
        user: UserHandle,
        iconDpi: Int,
        fallback: Supplier<Drawable>
    ): Drawable {
        val componentName = ComponentName(packageName, component)
        val iconEntry = resolveIconEntry(componentName, user)
        var resolvedEntry = iconEntry
        var iconType = ICON_TYPE_DEFAULT
        var themeData: ThemedIconDrawable.ThemeData? = null
        if (iconEntry != null) {
            val clock = iconPackProvider.getClockMetadata(iconEntry)
            when {
                iconEntry.type == IconType.Calendar -> {
                    resolvedEntry = iconEntry.resolveDynamicCalendar(getDay())
                    themeData = getThemeData(mCalendar.packageName, "")
                    iconType = ICON_TYPE_CALENDAR
                }
                !supportsIconTheme -> {
                    // theming is disabled, don't populate theme data
                }
                clock != null -> {
                    // the icon supports dynamic clock, use dynamic themed clock
                    themeData = getThemeData(mClock.packageName, "")
                    iconType = ICON_TYPE_CLOCK
                }
                packageName == mClock.packageName -> {
                    // is clock app but icon might not be adaptive, fallback to static themed clock
                    themeData = ThemedIconDrawable.ThemeData(
                        context.resources,
                        BuildConfig.APPLICATION_ID,
                        R.drawable.themed_icon_static_clock
                    )
                }
                packageName == mCalendar.packageName -> {
                    // calendar app, apply the dynamic calendar icon
                    themeData = getThemeData(mCalendar.packageName, "")
                    iconType = ICON_TYPE_CALENDAR
                }
                else -> {
                    // regular icon
                    themeData = getThemeData(componentName)
                }
            }
        }
        val icon = resolvedEntry?.let { iconPackProvider.getDrawable(it, iconDpi, user) }
        val td = themeData
        if (icon != null) {
            return if (td != null) td.wrapDrawable(icon, iconType) else icon
        }
        return super.getIconWithOverrides(packageName, component, user, iconDpi, fallback)
    }

    override fun isThemeEnabled(): Boolean {
        return _themeMap != DISABLED_MAP
    }

    override fun getThemeData(componentName: ComponentName): ThemedIconDrawable.ThemeData? {
        return themeMap[componentName] ?: themeMap[ComponentName(componentName.packageName, "")]
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

    override fun getSystemStateForPackage(systemState: String, packageName: String): String {
        return super.getSystemStateForPackage(systemState, packageName)
    }

    override fun getSystemIconState(): String {
        return super.getSystemIconState() + ",pack:${iconPackPref},lawnicons:${lawniconsVersion}"
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
        private val iconPackPref = prefs.themeIconPackGlobal

        private val subscription = Runnable {
            val newState = systemIconState
            if (iconState != newState) {
                iconState = newState
                callback.onSystemIconStateChanged(iconState)
                recreateCalendarAndClockChangeReceiver()
            }
        }

        init {
            recreateCalendarAndClockChangeReceiver()
        }

        private fun recreateCalendarAndClockChangeReceiver() {
            val iconPack =
                IconPackProvider.INSTANCE.get(context)
                    .getIconPackOrSystem(iconPackPref.onGetValue())
            calendarAndClockChangeReceiver = if (iconPack != null) {
                CalendarAndClockChangeReceiver(context, handler, iconPack, callback)
            } else {
                null
            }
        }

        override fun close() {
            calendarAndClockChangeReceiver = null
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

    private fun createThemedIconMap(): MutableMap<ComponentName, ThemedIconDrawable.ThemeData> {
        val map = ArrayMap<ComponentName, ThemedIconDrawable.ThemeData>()

        fun updateMapFromResources(resources: Resources, packageName: String) {
            try {
                val xmlId = resources.getIdentifier(THEMED_ICON_MAP_FILE, "xml", packageName)
                if (xmlId != 0) {
                    val parser = resources.getXml(xmlId)
                    val depth = parser.depth
                    var type: Int
                    while (
                        (parser.next()
                            .also { type = it } != XmlPullParser.END_TAG || parser.depth > depth) &&
                        type != XmlPullParser.END_DOCUMENT
                    ) {
                        if (type != XmlPullParser.START_TAG) continue
                        if (TAG_ICON == parser.name) {
                            val pkg = parser.getAttributeValue(null, ATTR_PACKAGE)
                            val cmp = parser.getAttributeValue(null, ATTR_COMPONENT) ?: ""
                            val iconId = parser.getAttributeResourceValue(null, ATTR_DRAWABLE, 0)
                            if (iconId != 0 && pkg.isNotEmpty()) {
                                map[ComponentName(pkg, cmp)] =
                                    ThemedIconDrawable.ThemeData(resources, packageName, iconId)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("CustomIconProvider", "Unable to parse icon map.", e)
            }
        }

        if (prefs.themeIconPackGlobal.onGetValue() == LAWNICONS_PACKAGE_NAME) {
            updateMapFromResources(
                resources = context.packageManager.getResourcesForApplication(LAWNICONS_PACKAGE_NAME),
                packageName = LAWNICONS_PACKAGE_NAME
            )
        } else {
            updateMapFromResources(
                resources = context.resources,
                packageName = context.packageName
            )
        }

        return map
    }

    companion object {
        val DISABLED_MAP = emptyMap<ComponentName, ThemedIconDrawable.ThemeData>()
    }
}