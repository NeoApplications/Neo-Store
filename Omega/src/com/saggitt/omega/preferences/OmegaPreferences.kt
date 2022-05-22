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

package com.saggitt.omega.preferences

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.android.launcher3.LauncherAppState
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.Utilities.makeComponentKey
import com.android.launcher3.util.ComponentKey
import com.android.launcher3.util.MainThreadInitializedObject
import com.android.launcher3.util.Themes
import com.saggitt.omega.*
import com.saggitt.omega.groups.AppGroupsManager
import com.saggitt.omega.groups.DrawerTabs
import com.saggitt.omega.iconpack.CustomIconEntry
import com.saggitt.omega.icons.CustomAdaptiveIconDrawable
import com.saggitt.omega.icons.IconShape
import com.saggitt.omega.icons.IconShapeManager
import com.saggitt.omega.preferences.custom.GridSize
import com.saggitt.omega.preferences.custom.GridSize2D
import com.saggitt.omega.search.SearchProviderController
import com.saggitt.omega.smartspace.SmartSpaceDataWidget
import com.saggitt.omega.smartspace.eventprovider.BatteryStatusProvider
import com.saggitt.omega.smartspace.eventprovider.NotificationUnreadProvider
import com.saggitt.omega.smartspace.eventprovider.NowPlayingProvider
import com.saggitt.omega.smartspace.eventprovider.PersonalityProvider
import com.saggitt.omega.theme.ThemeManager
import com.saggitt.omega.util.Temperature

class OmegaPreferences(val context: Context) : BasePreferences(context) {

    private val onIconShapeChanged = {
        initializeIconShape()
        com.android.launcher3.graphics.IconShape.init(context)
        idp.onPreferencesChanged(context)
    }

    fun initializeIconShape() {
        val shape = iconShape
        CustomAdaptiveIconDrawable.sInitialized = true
        CustomAdaptiveIconDrawable.sMaskId = shape.getHashString()
        CustomAdaptiveIconDrawable.sMask = shape.getMaskPath()
    }

    // DESKTOP
    private var desktopGridSizeDelegate = ResettableLazy {
        GridSize2D(
            this, PREFS_ROWS, PREFS_COLUMNS,
            LauncherAppState.getIDP(context), reloadIcons
        )
    }
    val desktopGridSize by desktopGridSizeDelegate
    val workspaceColumns = IdpIntPref("pref_${PREFS_COLUMNS}", { numColumns }, reloadGrid)
    val workspaceRows = IdpIntPref("pref_${PREFS_ROWS}", { numRows }, reloadGrid)

    val desktopIconScale by FloatPref(PREFS_DESKTOP_ICON_SCALE, 1f, reloadGrid)
    val usePopupMenuView by BooleanPref(PREFS_DESKTOP_POPUP_MENU, true, doNothing)
    var dashLineSize by FloatPref(PREFS_DASH_LINESIZE, 6f, doNothing)
    var dashProviders = StringListPref(
        PREFS_DASH_PROVIDERS,
        listOf("17", "15", "4", "6", "8", "5"), doNothing
    )
    val lockDesktop by BooleanPref(PREFS_DESKTOP_LOCK, false, reloadAll)
    val hideStatusBar by BooleanPref(PREFS_STATUSBAR_HIDE, false, doNothing)
    var allowEmptyScreens by BooleanPref(PREFS_EMPTY_SCREENS, false)
    val hideAppLabels by BooleanPref(PREFS_DESKTOP_HIDE_LABEL, false, reloadApps)
    val desktopTextScale by FloatPref(PREFS_DESKTOP_ICON_TEXT_SCALE, 1f, reloadApps)
    val allowFullWidthWidgets by BooleanPref(PREFS_WIDGETS_FULL_WIDTH, false, restart)
    private val homeMultilineLabel by BooleanPref(
        PREFS_DESKTOP_ICON_LABEL_TWOLINES,
        false,
        reloadApps
    )
    val homeLabelRows get() = if (homeMultilineLabel) 2 else 1
    var folderRadius by DimensionPref(PREFS_FOLDER_RADIUS, -1f, recreate) // TODO add
    val customFolderBackground by BooleanPref(
        PREFS_FOLDER_BACKGROUND_CUSTOM,
        false,
        recreate
    ) // TODO add
    val folderBackground by IntPref( // TODO add
        PREFS_FOLDER_BACKGROUND,
        Themes.getAttrColor(context, R.attr.folderFillColor),
        restart
    )
    val folderColumns by FloatPref(PREFS_FOLDER_COLUMNS, 4f, reloadGrid) // TODO add
    val folderRows by FloatPref(PREFS_FOLDER_ROWS, 4f, reloadGrid) // TODO add

    // DOCK
    var dockHide by BooleanPref(PREFS_DOCK_HIDE, false, restart)
    val dockIconScale by FloatPref(PREFS_DOCK_ICON_SCALE, 1f, recreate)
    var dockScale by FloatPref(PREFS_DOCK_SCALE, 1f, restart)
    val dockBackground by BooleanPref(PREFS_DOCK_BACKGROUND, false, recreate)
    val dockBackgroundColor by IntPref(PREFS_DOCK_BACKGROUND_COLOR, 0x101010, recreate)
    var dockOpacity by AlphaPref(PREFS_DOCK_OPACITY, -1, recreate)
    var dockSearchBar by BooleanPref("pref_dock_search", false, restart)
    private val dockGridSizeDelegate = ResettableLazy {
        GridSize(this, "numHotseatIcons", LauncherAppState.getIDP(context), reloadIcons)
    }
    val dockGridSize by dockGridSizeDelegate
    val numHotseatIcons = IdpIntPref("pref_numHotseatIcons", { numHotseatIcons }, reloadGrid)

    // DRAWER
    val allAppsSearch by BooleanPref("pref_all_apps_search", true, recreate)
    var allAppsGlobalSearch by BooleanPref("pref_all_apps_global_search", true, doNothing)
    var sortMode by StringIntPref(PREFS_SORT, 0, recreate)
    var hiddenAppSet by StringSetPref(PREFS_HIDDEN_SET, setOf(), reloadApps)
    var hiddenPredictionAppSet by StringSetPref(PREFS_HIDDEN_PREDICTION_SET, setOf(), doNothing)
    var protectedAppsSet by StringSetPref(PREFS_PROTECTED_SET, setOf(), reloadApps)
    var enableProtectedApps by BooleanPref(PREFS_PROTECTED_APPS, false)
    var allAppsIconScale by FloatPref(PREFS_DRAWER_ICON_SCALE, 1f, reloadApps)
    val allAppsTextScale by FloatPref(PREFS_DRAWER_ICON_TEXT_SCALE, 1f)
    val hideAllAppsAppLabels by BooleanPref(PREFS_DRAWER_HIDE_LABEL, false, reloadApps)
    private val drawerMultilineLabel by BooleanPref(
        PREFS_DRAWER_ICON_LABEL_TWOLINES,
        false,
        reloadApps
    )
    val drawerLabelRows get() = if (drawerMultilineLabel) 2 else 1
    val allAppsCellHeightMultiplier by FloatPref(PREFS_DRAWER_HEIGHT_MULTIPLIER, 1F, restart)
    val separateWorkApps by BooleanPref(PREFS_WORK_PROFILE_SEPARATED, false, recreate)
    val appGroupsManager by lazy { AppGroupsManager(this) }
    val drawerTabs get() = appGroupsManager.drawerTabs
    val currentTabsModel
        get() = appGroupsManager.getEnabledModel() as? DrawerTabs ?: appGroupsManager.drawerTabs

    val saveScrollPosition by BooleanPref(PREFS_KEEP_SCROLL_STATE, false, doNothing)

    val drawerLayout by StringIntPref("pref_drawer_layout", 0, recreate)
    private val drawerGridSizeDelegate = ResettableLazy {
        GridSize(this, "numAllAppsColumns", LauncherAppState.getIDP(context), reloadIcons)
    }
    val drawerGridSize by drawerGridSizeDelegate

    val numAllAppsColumns = IdpIntPref("pref_numAllAppsColumns", { numAllAppsColumns }, reloadGrid)

    val customAppName =
        object : MutableMapPref<ComponentKey, String>("pref_appNameMap", reloadAll) {
            override fun flattenKey(key: ComponentKey) = key.toString()
            override fun unflattenKey(key: String) = makeComponentKey(context, key)
            override fun flattenValue(value: String) = value
            override fun unflattenValue(value: String) = value
        }

    // THEME
    var launcherTheme by StringIntPref(
        PREFS_THEME,
        ThemeManager.getDefaultTheme()
    ) { ThemeManager.getInstance(context).updateTheme() }
    val accentColor by IntPref(PREFS_ACCENT, (0xffff1744).toInt(), doNothing)
    var enableBlur by BooleanPref(PREFS_BLUR, false, updateBlur)
    var blurRadius by IntPref(PREFS_BLUR_RADIUS, 75, updateBlur)
    var customWindowCorner by BooleanPref(PREFS_WINDOWCORNER, false, doNothing)
    var windowCornerRadius by FloatPref(PREFS_WINDOWCORNER_RADIUS, 8f, updateBlur)
    var iconPackPackage by StringPref(PREFS_ICON_PACK, "", reloadIcons)

    var iconShape by StringBasedPref(
        PREFS_ICON_SHAPE, IconShape.Circle, onIconShapeChanged,
        {
            IconShape.fromString(it) ?: IconShapeManager.getSystemIconShape(context)
        }, IconShape::toString
    ) { /* no dispose */ }
    var coloredBackground by BooleanPref(PREFS_COLORED_BACKGROUND, false, doNothing)
    var enableWhiteOnlyTreatment by BooleanPref(PREFS_WHITE_TREATMENT, false, doNothing)
    var enableLegacyTreatment by BooleanPref(PREFS_LEGACY_TREATMENT, false, doNothing)
    var adaptifyIconPacks by BooleanPref(PREFS_FORCE_ADAPTIVE, false, doNothing)
    var forceShapeless by BooleanPref(PREFS_FORCE_SHAPELESS, false, doNothing)

    // SEARCH & FOLDER
    var searchBarRadius by DimensionPref("pref_searchbar_radius", -1f, recreate)
    var showLensIcon by BooleanPref("show_lens_icon", true, recreate)
    var searchProvider by StringPref(PREFS_SEARCH_PROVIDER, "") {
        SearchProviderController.getInstance(context).onSearchProviderChanged()
    }
    val searchHiddenApps by BooleanPref("pref_search_hidden_apps", false)
    val fuzzySearch by BooleanPref("pref_fuzzy_search", true)

    // GESTURES & NOTIFICATION
    val notificationCount: Boolean by BooleanPref(PREFS_NOTIFICATION_COUNT, false, recreate)
    val notificationCustomColor: Boolean by BooleanPref(
        PREFS_NOTIFICATION_BACKGROUND_CUSTOM,
        false,
        recreate
    )
    val notificationBackground by IntPref(
        PREFS_NOTIFICATION_BACKGROUND,
        R.color.notification_background,
        recreate
    )
    val folderBadgeCount by BooleanPref(PREFS_NOTIFICATION_COUNT_FOLDER, true, recreate)

    /*
    * Preferences not used. Added to register the change and restart only
    */
    var doubleTapGesture by StringPref(PREFS_GESTURE_DOUBLE_TAP, "", restart)
    var longPressGesture by StringPref(PREFS_GESTURE_LONG_PRESS, "", restart)
    var homePressGesture by StringPref(PREFS_GESTURE_HOME, "", restart)
    var backPressGesture by StringPref(PREFS_GESTURE_BACK, "", restart)
    var swipeDownGesture by StringPref(PREFS_GESTURE_SWIPE_DOWN, "", restart)
    var swipeUpGesture by StringPref(PREFS_GESTURE_SWIPE_UP, "", restart)
    var dockSwipeUpGesture by StringPref(PREFS_GESTURE_SWIPE_UP_DOCK, "", restart)
    var launchAssistantGesture by StringPref(PREFS_GESTURE_ASSISTANT, "", restart)

    // ADVANCED
    var language by StringPref(PREFS_LANGUAGE, "", recreate)
    var firstRun by BooleanPref(PREFS_FIRST_RUN, true)
    var restoreSuccess by BooleanPref(PREFS_RESTORE_SUCCESS, false)
    val recentBackups = object : MutableListPref<Uri>(
        Utilities.getDevicePrefs(context), PREFS_RECENT_BACKUP
    ) {
        override fun unflattenValue(value: String) = Uri.parse(value)
    }

    // DEVELOPER PREFERENCES
    var developerOptionsEnabled by BooleanPref(PREFS_DEV_PREFS_SHOW, false, recreate)
    var desktopModeEnabled by BooleanPref(PREFS_DESKTOP_MODE, true, recreate)
    private val lowPerformanceMode by BooleanPref(PREFS_LOW_PREFORMANCE, false, restart) // TODO Add
    val enablePhysics get() = !lowPerformanceMode
    val showDebugInfo by BooleanPref(PREFS_DEBUG_MODE, false, doNothing)

    // FEED
    var feedProvider by StringPref(PREFS_FEED_PROVIDER, "", restart)
    val ignoreFeedWhitelist by BooleanPref(PREFS_FEED_PROVIDER_ALLOW_ALL, true, restart)

    // SMARTSPACE
    var usePillQsb by BooleanPref(PREF_PILL_QSB, false, recreate)
    val enableSmartspace by BooleanPref(PREFS_SMARTSPACE_ENABLE, false, recreate)
    val smartspaceTime by BooleanPref(PREFS_SMARTSPACE_TIME, false, recreate)
    val smartspaceDate by BooleanPref(PREFS_SMARTSPACE_DATE, true, recreate)
    val smartspaceTimeAbove by BooleanPref(PREFS_SMARTSPACE_TIME_ABOVE, false, recreate)
    val smartspaceTime24H by BooleanPref(PREFS_TIME_24H, false, recreate)
    val weatherUnit by StringBasedPref(
        "pref_weather_units", Temperature.Unit.Celsius, ::updateSmartspaceProvider,
        Temperature.Companion::unitFromString, Temperature.Companion::unitToString
    ) { }
    var smartspaceWidgetId by IntPref("smartspace_widget_id", -1, doNothing)
    var weatherIconPack by StringPref("pref_weatherIcons", "", doNothing)
    var weatherProvider by StringPref(
        "pref_smartspace_widget_provider",
        SmartSpaceDataWidget::class.java.name, ::updateSmartspaceProvider
    )
    var eventProvider by StringPref(
        "pref_smartspace_event_provider",
        SmartSpaceDataWidget::class.java.name, ::updateSmartspaceProvider
    )
    var eventProviders = StringListPref(
        "pref_smartspace_event_providers", listOf(
            eventProvider,
            NotificationUnreadProvider::class.java.name,
            NowPlayingProvider::class.java.name,
            BatteryStatusProvider::class.java.name,
            PersonalityProvider::class.java.name
        ),
        ::updateSmartspaceProvider
    )

    var torchState by BooleanPref(PREFS_TORCH, false, doNothing)

    // POPUP DIALOG PREFERENCES
    val desktopPopupEdit by BooleanPref(PREFS_DESKTOP_POPUP_EDIT, true, doNothing)
    val desktopPopupRemove by BooleanPref(PREFS_DESKTOP_POPUP_REMOVE, false, doNothing)
    val drawerPopupEdit by BooleanPref(PREFS_DRAWER_POPUP_EDIT, true, doNothing)
    val drawerPopupUninstall by BooleanPref(PREFS_DRAWER_POPUP_UNINSTALL, false, doNothing)

    // ITEMS CUSTOM PREFERENCES
    val customAppIcon = object : MutableMapPref<ComponentKey, CustomIconEntry>(
        PREFS_APP_ICON_MAP, reloadAll
    ) {
        override fun flattenKey(key: ComponentKey) = key.toString()
        override fun unflattenKey(key: String) = makeComponentKey(context, key)
        override fun flattenValue(value: CustomIconEntry) = value.toString()
        override fun unflattenValue(value: String) = CustomIconEntry.fromString(value)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        onChangeMap[key]?.invoke()
        onChangeListeners[key]?.toSet()?.forEach { it.onValueChanged(key, this, false) }
    }

    fun addOnPreferenceChangeListener(listener: OnPreferenceChangeListener, vararg keys: String) {
        keys.forEach { addOnPreferenceChangeListener(it, listener) }
    }

    fun addOnPreferenceChangeListener(key: String, listener: OnPreferenceChangeListener) {
        if (onChangeListeners[key] == null) {
            onChangeListeners[key] = HashSet()
        }
        onChangeListeners[key]?.add(listener)
        listener.onValueChanged(key, this, true)
    }

    fun removeOnPreferenceChangeListener(
        listener: OnPreferenceChangeListener,
        vararg keys: String
    ) {
        keys.forEach { removeOnPreferenceChangeListener(it, listener) }
    }

    fun removeOnPreferenceChangeListener(key: String, listener: OnPreferenceChangeListener) {
        onChangeListeners[key]?.remove(listener)
    }

    interface OnPreferenceChangeListener {
        fun onValueChanged(key: String, prefs: OmegaPreferences, force: Boolean)
    }

    interface MutableListPrefChangeListener {
        fun onListPrefChanged(key: String)
    }

    fun beginBlockingEdit() {
        blockingEditing = true
    }

    fun endBlockingEdit() {
        blockingEditing = false
    }

    inline fun blockingEdit(body: OmegaPreferences.() -> Unit) {
        beginBlockingEdit()
        body(this)
        endBlockingEdit()
    }

    companion object {

        @JvmField
        val INSTANCE = MainThreadInitializedObject(::OmegaPreferences)

        @JvmStatic
        fun getInstance(context: Context) = INSTANCE.get(context)!!

    }
}