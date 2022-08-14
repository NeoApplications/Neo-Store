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
import com.android.launcher3.SessionCommitReceiver.ADD_ICON_PREFERENCE_KEY
import com.android.launcher3.Utilities
import com.android.launcher3.Utilities.makeComponentKey
import com.android.launcher3.states.RotationHelper.ALLOW_ROTATION_PREFERENCE_KEY
import com.android.launcher3.util.ComponentKey
import com.android.launcher3.util.MainThreadInitializedObject
import com.android.launcher3.util.Themes
import com.saggitt.omega.*
import com.saggitt.omega.groups.AppGroupsManager
import com.saggitt.omega.groups.DrawerTabs
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
import com.saggitt.omega.util.Config
import com.saggitt.omega.util.Temperature
import com.saggitt.omega.util.feedProviders
import com.saggitt.omega.util.languageOptions
import kotlin.math.roundToInt

class OmegaPreferences(val context: Context) : BasePreferences(context) {

    private val onIconShapeChanged = {
        initializeIconShape()
        com.android.launcher3.graphics.IconShape.init(context)
        LauncherAppState.getInstance(context).reloadIcons()
    }

    fun initializeIconShape() {
        val shape = themeIconShape.onGetValue()
        CustomAdaptiveIconDrawable.sInitialized = true
        CustomAdaptiveIconDrawable.sMaskId = shape.getHashString()
        CustomAdaptiveIconDrawable.sMask = shape.getMaskPath()
    }

    // TODO add the rest of keys to @Constants
    // TODO sort and filter the prefs we need
    // TODO add iconId to the respective prefs
    // TODO bring string names in line with pref names
    // DESKTOP
    var desktopGridSizeDelegate = ResettableLazy {
        GridSize2D(
            this, PREFS_DESKTOP_ROWS_RAW, PREFS_DESKTOP_COLUMNS_RAW,
            LauncherAppState.getIDP(context), reloadIcons
        )
    }
    val desktopGridSize by desktopGridSizeDelegate
    val desktopColumns = IdpIntPref(
        key = PREFS_DESKTOP_COLUMNS,
        titleId = R.string.grid_size_width,
        selectDefaultValue = { numColumns },
        onChange = reloadGrid,
        minValue = 2f,
        maxValue = 16f,
        steps = 15
    )
    val desktopRows = IdpIntPref(
        key = PREFS_DESKTOP_ROWS,
        titleId = R.string.grid_size_height,
        selectDefaultValue = { numRows },
        onChange = reloadGrid,
        minValue = 2f,
        maxValue = 16f,
        steps = 15
    )
    val desktopAddIconsToHome = BooleanPref(
        key = ADD_ICON_PREFERENCE_KEY,
        titleId = R.string.auto_add_shortcuts_label,
        summaryId = R.string.auto_add_shortcuts_description,
        defaultValue = false,
        onChange = doNothing
    )
    val desktopAllowRotation = BooleanPref(
        key = ALLOW_ROTATION_PREFERENCE_KEY,
        titleId = R.string.allow_rotation_title,
        summaryId = R.string.allow_rotation_desc,
        defaultValue = false,
        onChange = doNothing
    )
    val desktopIconScale = FloatPref(
        key = PREFS_DESKTOP_ICON_SCALE,
        titleId = R.string.title__desktop_icon_size,
        defaultValue = 1f,
        maxValue = 2f,
        minValue = 0.5f,
        steps = 150,
        specialOutputs = { "${(it * 100).roundToInt()}%" },
        onChange = reloadGrid
    )
    val desktopUsePopupMenuView = BooleanPref(
        key = PREFS_DESKTOP_POPUP_MENU,
        titleId = R.string.title_desktop_icon_popup_menu,
        defaultValue = true,
        onChange = doNothing
    )
    var dashLineSize = FloatPref(
        key = PREFS_DASH_LINESIZE,
        titleId = R.string.dash_linesize,
        defaultValue = 6f,
        maxValue = 6f,
        minValue = 4f,
        steps = 1,
        specialOutputs = { it.roundToInt().toString() },
        onChange = doNothing
    )
    var dashProviders = StringListPref(
        prefKey = PREFS_DASH_PROVIDERS,
        titleId = R.string.edit_dash,
        summaryId = R.string.edit_dash_summary,
        default = listOf("17", "15", "4", "6", "8", "5"),
        onChange = doNothing
    )
    val desktopLock = BooleanPref(
        key = PREFS_DESKTOP_LOCK,
        titleId = R.string.title_desktop_lock_desktop,
        defaultValue = false,
        onChange = reloadAll
    )
    val desktopHideStatusBar = BooleanPref(
        key = PREFS_STATUSBAR_HIDE,
        titleId = R.string.title_desktop_hide_statusbar,
        defaultValue = false,
        onChange = doNothing
    )
    var desktopAllowEmptyScreens = BooleanPref(
        key = PREFS_EMPTY_SCREENS,
        titleId = R.string.title_desktop_keep_empty,
        defaultValue = false
    )
    val desktopHideAppLabels = BooleanPref(
        key = PREFS_DESKTOP_HIDE_LABEL,
        titleId = R.string.title__desktop_hide_icon_labels,
        defaultValue = false,
        onChange = reloadApps
    )
    val desktopTextScale = FloatPref(
        key = PREFS_DESKTOP_ICON_TEXT_SCALE,
        titleId = R.string.title_desktop_text_size,
        defaultValue = 1f,
        maxValue = 2f,
        minValue = 0.5f,
        steps = 150,
        specialOutputs = { "${(it * 100).roundToInt()}%" },
        onChange = reloadApps
    )
    val desktopAllowFullWidthWidgets = BooleanPref(
        key = PREFS_WIDGETS_FULL_WIDTH,
        titleId = R.string.title_desktop_full_width_widgets,
        summaryId = R.string.summary_full_width_widgets,
        defaultValue = false,
        onChange = restart
    )
    val desktopMultilineLabel = BooleanPref(
        key = PREFS_DESKTOP_ICON_LABEL_TWOLINES,
        titleId = R.string.title__multiline_labels,
        defaultValue = false,
        onChange = reloadApps
    )
    val desktopLabelRows get() = if (desktopMultilineLabel.onGetValue()) 2 else 1
    var desktopFolderRadius = DimensionPref(
        key = PREFS_FOLDER_RADIUS,
        titleId = R.string.folder_radius,
        defaultValue = -1f,
        maxValue = 24f,
        minValue = -1f,
        steps = 24,
        specialOutputs = {
            when {
                it < 0f -> context.getString(R.string.automatic_short)
                else -> "${it.roundToInt()}dp"
            }
        },
        onChange = recreate
    ) // TODO add
    val desktopCustomFolderBackground = BooleanPref(
        key = PREFS_FOLDER_BACKGROUND_CUSTOM,
        titleId = R.string.folder_custom_background,
        defaultValue = false,
        onChange = recreate
    ) // TODO add
    val desktopFolderBackground = IntPref( // TODO add
        key = PREFS_FOLDER_BACKGROUND,
        titleId = R.string.folder_background,
        defaultValue = Themes.getAttrColor(context, R.attr.folderFillColor),
        onChange = restart
    )
    val desktopFolderColumns = FloatPref(
        key = PREFS_FOLDER_COLUMNS,
        titleId = R.string.folder_columns,
        defaultValue = 4f,
        maxValue = 5f,
        minValue = 2f,
        steps = 2,
        specialOutputs = { it.roundToInt().toString() },
        onChange = reloadGrid
    ) // TODO add
    val desktopFolderRows = FloatPref(
        key = PREFS_FOLDER_ROWS,
        titleId = R.string.folder_rows,
        defaultValue = 4f,
        maxValue = 5f,
        minValue = 2f,
        steps = 2,
        specialOutputs = { it.roundToInt().toString() },
        onChange = reloadGrid
    ) // TODO add
    val desktopPopupEdit = BooleanPref(
        key = PREFS_DESKTOP_POPUP_EDIT,
        titleId = R.string.action_preferences,
        defaultValue = true,
        onChange = doNothing
    )
    val desktopPopupRemove = BooleanPref(
        key = PREFS_DESKTOP_POPUP_REMOVE,
        titleId = R.string.remove_drop_target_label,
        defaultValue = false,
        onChange = doNothing
    )


    // DOCK
    var dockHide = BooleanPref(
        key = PREFS_DOCK_HIDE,
        titleId = R.string.title__dock_hide,
        defaultValue = false,
        onChange = restart
    )
    val dockIconScale = FloatPref(
        key = PREFS_DOCK_ICON_SCALE,
        titleId = R.string.title__dock_icon_size,
        defaultValue = 1f,
        maxValue = 2f,
        minValue = 0.5f,
        steps = 150,
        specialOutputs = { "${(it * 100).roundToInt()}%" },
        onChange = recreate
    )
    var dockScale = FloatPref(
        key = PREFS_DOCK_SCALE,
        titleId = R.string.title__dock_scale,
        defaultValue = 1f,
        maxValue = 1.75f,
        minValue = 0.70f,
        steps = 100,
        specialOutputs = { "${(it * 100).roundToInt()}%" },
        onChange = restart
    )
    val dockBackground = BooleanPref(
        key = PREFS_DOCK_BACKGROUND,
        titleId = R.string.title_dock_fill,
        defaultValue = false,
        onChange = recreate
    )
    val dockBackgroundColor = ColorIntPref(
        key = PREFS_DOCK_BACKGROUND_COLOR,
        titleId = R.string.title_dock_background_color,
        defaultValue = 0x101010,
        onChange = recreate
    )
    var dockOpacity = AlphaPref(
        key = PREFS_DOCK_OPACITY,
        titleId = R.string.title_opacity,
        defaultValue = 0f,
        onChange = recreate
    )
    var dockSearchBar = BooleanPref(
        key = PREFS_DOCK_SEARCH,
        titleId = R.string.title__dock_search_bar,
        summaryId = R.string.summary_dock_search,
        defaultValue = false,
        onChange = restart
    )
    val dockGridSizeDelegate = ResettableLazy {
        GridSize(
            prefs = this,
            rowsKey = PREFS_DOCK_COLUMNS_RAW,
            targetObject = LauncherAppState.getIDP(context),
            onChangeListener = reloadIcons
        )
    }
    val dockGridSize by dockGridSizeDelegate
    val dockNumIcons = IdpIntPref(
        key = PREFS_DOCK_COLUMNS,
        titleId = R.string.num_hotseat_icons_pref_title,
        selectDefaultValue = { numHotseatIcons },
        onChange = reloadGrid,
        minValue = 2f,
        maxValue = 16f,
        steps = 15
    )


    // DRAWER
    val drawerSearch = BooleanPref(
        key = PREFS_DRAWER_SEARCH,
        titleId = R.string.title_all_apps_search,
        summaryId = R.string.summary_all_apps_search,
        defaultValue = true,
        onChange = recreate
    )
    var drawerSortMode = StringIntPref( // TODO replace usages with the new one
        key = PREFS_SORT,
        titleId = R.string.title__sort_mode,
        defaultValue = 0,
        onChange = recreate
    )
    var drawerSortModeNew = IntSelectionPref(
        key = PREFS_SORT_X,
        titleId = R.string.title__sort_mode,
        defaultValue = Config.SORT_AZ,
        entries = drawerSortOptions,
        onChange = recreate
    )
    var drawerHiddenAppSet = StringSetPref(
        key = PREFS_HIDDEN_SET,
        titleId = R.string.title_search_hidden_apps,
        summaryId = R.string.summary_all_apps_search,
        defaultValue = setOf(),
        onChange = reloadApps
    )
    var drawerHiddenApps by drawerHiddenAppSet
    var drawerHiddenPredictionAppSet = StringSetPref(
        key = PREFS_HIDDEN_PREDICTION_SET,
        titleId = -1,
        defaultValue = setOf(),
        onChange = doNothing
    )
    var drawerProtectedAppsSet = StringSetPref(
        key = PREFS_PROTECTED_SET,
        titleId = -1,
        defaultValue = setOf(),
        onChange = reloadApps
    )
    var drawerProtectedApps by drawerProtectedAppsSet
    var drawerEnableProtectedApps = BooleanPref(
        key = PREFS_PROTECTED_APPS,
        titleId = R.string.enable_protected_apps,
        defaultValue = false
    )
    var drawerIconScale = FloatPref(
        key = PREFS_DRAWER_ICON_SCALE,
        titleId = R.string.title__drawer_icon_size,
        defaultValue = 1f,
        maxValue = 2f,
        minValue = 0.5f,
        steps = 150,
        specialOutputs = { "${(it * 100).roundToInt()}%" },
        onChange = reloadApps
    )
    val drawerTextScale = FloatPref(
        key = PREFS_DRAWER_ICON_TEXT_SCALE,
        titleId = R.string.title_desktop_text_size,
        defaultValue = 1f,
        maxValue = 1.8f,
        minValue = 0.3f,
        steps = 150,
        specialOutputs = { "${(it * 100).roundToInt()}%" },
    )
    val drawerHideAppLabels = BooleanPref(
        key = PREFS_DRAWER_HIDE_LABEL,
        titleId = R.string.title__drawer_hide_icon_labels,
        defaultValue = false,
        onChange = reloadApps
    )
    val drawerMultilineLabel = BooleanPref(
        key = PREFS_DRAWER_ICON_LABEL_TWOLINES,
        titleId = R.string.title__multiline_labels,
        defaultValue = false,
        onChange = reloadApps
    )
    val drawerLabelRows get() = if (drawerMultilineLabel.onGetValue()) 2 else 1
    val drawerCellHeightMultiplier = FloatPref(
        key = PREFS_DRAWER_HEIGHT_MULTIPLIER,
        titleId = R.string.title_drawer_row_height,
        defaultValue = 1F,
        maxValue = 2f,
        minValue = 0.5f,
        steps = 150,
        specialOutputs = { "${(it * 100).roundToInt()}%" },
        onChange = restart
    )
    val drawerSeparateWorkApps = BooleanPref(
        key = PREFS_WORK_PROFILE_SEPARATED,
        titleId = R.string.title_separate_work_apps,
        defaultValue = false,
        onChange = recreate
    )
    val drawerAppGroupsManager by lazy { AppGroupsManager(this) }
    val drawerTabs get() = drawerAppGroupsManager.drawerTabs
    val drawerTabsModelCurrent
        get() = drawerAppGroupsManager.getEnabledModel() as? DrawerTabs
            ?: drawerAppGroupsManager.drawerTabs

    val drawerSaveScrollPosition = BooleanPref(
        key = PREFS_KEEP_SCROLL_STATE,
        titleId = R.string.title_all_apps_keep_scroll_state,
        defaultValue = false,
        onChange = doNothing
    )
    val drawerLayout = StringIntPref( // TODO replace usages with the new one
        key = PREFS_DRAWER_LAYOUT,
        titleId = R.string.title_drawer_layout,
        defaultValue = 0,
        onChange = recreate
    )
    val drawerLayoutNew = IntSelectionPref(
        key = PREFS_DRAWER_LAYOUT_X,
        titleId = R.string.title_drawer_layout,
        defaultValue = Config.DRAWER_VERTICAL,
        entries = drawerLayoutOptions,
        onChange = recreate
    )
    private val drawerGridSizeDelegate = ResettableLazy {
        GridSize(
            prefs = this,
            rowsKey = PREFS_DRAWER_COLUMNS_RAW,
            targetObject = LauncherAppState.getIDP(context),
            onChangeListener = reloadIcons
        )
    }
    val drawerGridSize by drawerGridSizeDelegate
    val drawerColumns = IdpIntPref(
        key = PREFS_DRAWER_COLUMNS,
        titleId = R.string.title__drawer_columns,
        selectDefaultValue = { numAllAppsColumns },
        onChange = reloadGrid,
        minValue = 2f,
        maxValue = 16f,
        steps = 15
    )
    val drawerPopupEdit = BooleanPref(
        key = PREFS_DRAWER_POPUP_EDIT,
        titleId = R.string.action_preferences,
        defaultValue = true,
        onChange = doNothing
    )
    val drawerPopupUninstall = BooleanPref(
        key = PREFS_DRAWER_POPUP_UNINSTALL,
        titleId = R.string.uninstall_drop_target_label,
        defaultValue = false,
        onChange = doNothing
    )
    val drawerBackground = BooleanPref(
        key = PREFS_DRAWER_CUSTOM_BACKGROUND,
        titleId = R.string.title_drawer_enable_background,
        defaultValue = false,
        onChange = doNothing
    )
    val drawerBackgroundColor = ColorIntPref(
        key = PREFS_DRAWER_BACKGROUND_COLOR,
        titleId = R.string.title_dock_background_color,
        defaultValue = 0x101010,
        onChange = recreate
    )
    val drawerOpacity = FloatPref(
        key = PREFS_DRAWER_OPACITY,
        titleId = R.string.title_opacity,
        defaultValue = 1f,
        maxValue = 1f,
        minValue = 0f,
        steps = 100,
        specialOutputs = { "${(it * 100).roundToInt()}%" },
        onChange = recreate
    )


    // PROFILE: LANGUAGE & THEME
    var language = StringSelectionPref(
        key = PREFS_LANGUAGE,
        titleId = R.string.title__advanced_language,
        defaultValue = "",
        entries = context.languageOptions(),
        onChange = recreate
    )
    var themePref = StringIntPref( // TODO replace usages with the new one
        PREFS_THEME,
        ThemeManager.getDefaultTheme()
    ) { ThemeManager.getInstance(context).updateTheme() }
    var themePrefNew = IntSelectionPref(
        key = PREFS_THEME_X,
        titleId = R.string.title__general_theme,
        defaultValue = if (OmegaApp.minSDK(31)) THEME_SYSTEM else THEME_WALLPAPER,
        entries = themeItems,
    ) { ThemeManager.getInstance(context).updateTheme() }
    val themeAccentColor = IntPref(
        key = PREFS_ACCENT,
        titleId = R.string.title__theme_accent_color,
        defaultValue = (0xffff1744).toInt(),
        onChange = doNothing
    )
    var themeBlurEnable = BooleanPref(
        key = PREFS_BLUR,
        titleId = R.string.title__theme_blur,
        summaryId = R.string.summary__theme_blur,
        defaultValue = false,
        onChange = updateBlur
    )
    var themeBlurRadius = IntPref(
        key = PREFS_BLUR_RADIUS,
        titleId = R.string.title__theme_blur_radius,
        defaultValue = 75,
        onChange = updateBlur
    )
    var themeCornerRadiusOverride = BooleanPref(
        key = PREFS_WINDOWCORNER,
        titleId = R.string.title_override_corner_radius,
        defaultValue = false,
        onChange = doNothing
    )
    var themeCornerRadius = FloatPref(
        key = PREFS_WINDOWCORNER_RADIUS,
        titleId = R.string.title_override_corner_radius_value,
        defaultValue = 8f,
        maxValue = 24f,
        minValue = -1f,
        steps = 24,
        specialOutputs = {
            when {
                it < 0f -> context.getString(R.string.automatic_short)
                else -> "${it.roundToInt()}dp"
            }
        },
        onChange = updateBlur
    )
    var themeIconPackGlobal = StringPref(
        key = PREFS_ICON_PACK,
        titleId = R.string.title_theme_icon_packs,
        defaultValue = "",
        onChange = reloadIcons
    )
    var themeIconShape = StringBasedPref(
        key = PREFS_ICON_SHAPE,
        titleId = R.string.title__theme_icon_shape,
        defaultValue = IconShape.Circle,
        onChange = onIconShapeChanged,
        fromString = {
            IconShape.fromString(it) ?: IconShapeManager.getSystemIconShape(context)
        },
        toString = IconShape::toString,
        dispose = { /* no dispose */ }
    )
    var themeIconColoredBackground = BooleanPref(
        key = PREFS_COLORED_BACKGROUND,
        titleId = R.string.title_colored_backgrounds,
        summaryId = R.string.summary_colored_backgrounds,
        defaultValue = false,
        onChange = doNothing
    )
    var themeIconWhiteOnlyTreatment = BooleanPref(
        key = PREFS_WHITE_TREATMENT,
        titleId = R.string.title_white_only_treatment,
        summaryId = R.string.summary_white_only_treatment,
        defaultValue = false,
        onChange = doNothing
    )
    var themeIconLegacyTreatment = BooleanPref(
        key = PREFS_LEGACY_TREATMENT,
        titleId = R.string.title_legacy_treatment,
        summaryId = R.string.summary_legacy_treatment,
        defaultValue = false,
        onChange = doNothing
    )
    var themeIconAdaptify = BooleanPref(
        key = PREFS_FORCE_ADAPTIVE,
        titleId = R.string.title_adaptify_pack,
        defaultValue = false,
        onChange = doNothing
    )
    var themeIconForceShapeless = BooleanPref(
        key = PREFS_FORCE_SHAPELESS,
        titleId = R.string.title_force_shapeless,
        summaryId = R.string.summary_force_shapeless,
        defaultValue = false,
        onChange = doNothing
    )


    // SEARCH & FEED
    var searchBarRadius = DimensionPref(
        key = PREFS_SEARCH_BAR_RADIUS,
        titleId = R.string.title__search_bar_radius,
        defaultValue = -1f,
        maxValue = 24f,
        minValue = -1f,
        steps = 24,
        specialOutputs = {
            when {
                it < 0f -> context.getString(R.string.automatic_short)
                else -> "${it.roundToInt()}dp"
            }
        },
        onChange = recreate
    )

    var searchProvider = StringSelectionPref(
        key = PREFS_SEARCH_PROVIDER,
        titleId = R.string.title_search_provider,
        defaultValue = "",
        entries = SearchProviderController.getSearchProvidersMap(context),
        onChange = { SearchProviderController.getInstance(context).onSearchProviderChanged() }
    )
    val searchHiddenApps = BooleanPref(
        key = PREFS_SEARCH_HIDDEN_APPS,
        titleId = R.string.title_search_hidden_apps,
        summaryId = R.string.summary_search_hidden_apps,
        defaultValue = false
    )
    val searchFuzzy = BooleanPref(
        key = PREFS_SEARCH_FUZZY,
        titleId = R.string.title_fuzzy_search,
        summaryId = R.string.summary_fuzzy_search,
        defaultValue = true
    )
    var searchGlobal = BooleanPref(
        key = PREFS_SEARCH_GLOBAL,
        titleId = R.string.title_all_apps_google_search,
        summaryId = R.string.summary_all_apps_google_search,
        defaultValue = true,
        onChange = doNothing
    )

    var searchContacts = BooleanPref(
        key = PREFS_SEARCH_CONTACTS,
        titleId = R.string.title_search_contacts,
        defaultValue = false,
        onChange = recreate
    )

    var feedProvider = StringSelectionPref(
        key = PREFS_FEED_PROVIDER,
        titleId = R.string.title_feed_provider,
        defaultValue = "",
        entries = context.feedProviders(),
        onChange = restart
    )

    // NOTIFICATION & GESTURES
    var gestureDoubleTap = StringPref(
        key = PREFS_GESTURE_DOUBLE_TAP,
        titleId = R.string.gesture_double_tap,
        defaultValue = "",
        onChange = restart
    )
    var gestureLongPress = StringPref(
        key = PREFS_GESTURE_LONG_PRESS,
        titleId = R.string.gesture_long_press,
        defaultValue = "",
        onChange = restart
    )
    var gestureHomePress = StringPref(
        key = PREFS_GESTURE_HOME,
        titleId = R.string.gesture_press_home,
        defaultValue = "",
        onChange = restart
    )
    var gestureBackPress = StringPref(
        key = PREFS_GESTURE_BACK,
        titleId = R.string.gesture_press_back,
        defaultValue = "",
        onChange = restart
    )
    var gestureSwipeDown = StringPref(
        key = PREFS_GESTURE_SWIPE_DOWN,
        titleId = R.string.title__gesture_swipe_down,
        defaultValue = "",
        onChange = restart
    )
    var gestureSwipeUp = StringPref(
        key = PREFS_GESTURE_SWIPE_UP,
        titleId = R.string.gesture_swipe_up,
        defaultValue = "",
        onChange = restart
    )
    var gestureDockSwipeUp = StringPref(
        key = PREFS_GESTURE_SWIPE_UP_DOCK,
        titleId = R.string.gesture_dock_swipe_up,
        defaultValue = "",
        onChange = restart
    )
    var gestureLaunchAssistant = StringPref(
        key = PREFS_GESTURE_ASSISTANT,
        titleId = R.string.gesture_launch_assistant,
        defaultValue = "",
        onChange = restart
    )


    // WIDGETS (SMARTSPACE) & NOTIFICATIONS
    var smartspaceUsePillQsb = BooleanPref(
        key = PREF_PILL_QSB,
        titleId = R.string.title_use_pill_qsb,
        defaultValue = false,
        onChange = recreate
    )
    val smartspaceEnable = BooleanPref(
        key = PREFS_SMARTSPACE_ENABLE,
        titleId = R.string.title_smartspace,
        defaultValue = false,
        onChange = recreate
    )
    val smartspaceTime = BooleanPref(
        key = PREFS_SMARTSPACE_TIME,
        titleId = R.string.title_smartspace_time,
        defaultValue = false,
        onChange = recreate
    )
    val smartspaceDate = BooleanPref(
        key = PREFS_SMARTSPACE_DATE,
        titleId = R.string.title_smartspace_date,
        defaultValue = true,
        onChange = recreate
    )
    val smartspaceTimeAbove = BooleanPref(
        key = PREFS_SMARTSPACE_TIME_ABOVE,
        titleId = R.string.title_smartspace_time_above,
        defaultValue = false,
        onChange = recreate
    )
    val smartspaceTime24H = BooleanPref(
        key = PREFS_TIME_24H,
        titleId = R.string.title_smartspace_time_24_h,
        defaultValue = false,
        onChange = recreate
    )
    val smartspaceWeatherUnit = StringBasedPref(
        key = PREFS_SMARTSPACE_WEATHER_UNITS,
        titleId = R.string.title_smartspace_weather_units,
        defaultValue = Temperature.Unit.Celsius,
        onChange = ::updateSmartspaceProvider,
        fromString = Temperature.Companion::unitFromString,
        toString = Temperature.Companion::unitToString,
        dispose = { }
    )
    var smartspaceWidgetId = IntPref(
        key = PREFS_SMARTSPACE_WIDGET_ID,
        titleId = -1,
        defaultValue = -1,
        onChange = doNothing
    )
    var smartspaceWeatherIconPack = StringPref(
        key = PREFS_SMARTSPACE_WEATHER_ICONS,
        titleId = -1,
        defaultValue = "",
        onChange = doNothing
    )
    var smartspaceWeatherProvider = StringPref(
        key = PREFS_SMARTSPACE_WEATHER_PROVIDER,
        titleId = R.string.title_smartspace_widget_provider,
        defaultValue = SmartSpaceDataWidget::class.java.name,
        onChange = ::updateSmartspaceProvider
    )
    var smartspaceEventProvider = StringPref(
        key = PREFS_SMARTSPACE_EVENT_PROVIDER,
        titleId = -1,
        defaultValue = SmartSpaceDataWidget::class.java.name,
        onChange = ::updateSmartspaceProvider
    )
    var smartspaceEventProviders = StringListPref(
        prefKey = PREFS_SMARTSPACE_EVENT_PROVIDERS,
        titleId = R.string.title_smartspace_event_providers,
        default = listOf(
            smartspaceEventProvider.onGetValue(),
            NotificationUnreadProvider::class.java.name,
            NowPlayingProvider::class.java.name,
            BatteryStatusProvider::class.java.name,
            PersonalityProvider::class.java.name
        ),
        onChange = ::updateSmartspaceProvider
    )
    val notificationCount = BooleanPref(
        key = PREFS_NOTIFICATION_COUNT,
        titleId = R.string.title__notification_count,
        defaultValue = false,
        onChange = recreate
    )
    val notificationCustomColor = BooleanPref(
        key = PREFS_NOTIFICATION_BACKGROUND_CUSTOM,
        titleId = R.string.notification_custom_color,
        defaultValue = false,
        onChange = recreate
    )
    val notificationBackground = ColorIntPref(
        key = PREFS_NOTIFICATION_BACKGROUND,
        titleId = R.string.title__notification_background,
        defaultValue = R.color.notification_background,
        onChange = recreate
    )
    val notificationCountFolder = BooleanPref(
        key = PREFS_NOTIFICATION_COUNT_FOLDER,
        titleId = R.string.title__folder_badge_count,
        defaultValue = true,
        onChange = recreate
    )


    // ADVANCED
    var restoreSuccess by BooleanPref(
        key = PREFS_RESTORE_SUCCESS,
        titleId = R.string.restore_success,
        defaultValue = false
    )
    val recentBackups = object : MutableListPref<Uri>(
        prefs = Utilities.getDevicePrefs(context),
        prefKey = PREFS_RECENT_BACKUP,
        titleId = -1,
    ) {
        override fun unflattenValue(value: String) = Uri.parse(value)
    }


    // DEVELOPER
    var developerOptionsEnabled = BooleanPref(
        key = PREFS_DEV_PREFS_SHOW,
        titleId = R.string.title__dev_show_Dev,
        defaultValue = false,
        onChange = recreate
    )
    var desktopModeEnabled by BooleanPref(
        key = PREFS_DESKTOP_MODE,
        titleId = R.string.pref_desktop_mode,
        summaryId = R.string.pref_desktop_mode_summary,
        defaultValue = true,
        onChange = recreate
    )
    private val lowPerformanceMode by BooleanPref(
        key = PREFS_LOW_PREFORMANCE,
        titleId = -1,
        defaultValue = false,
        onChange = restart
    ) // TODO Add
    val enablePhysics get() = !lowPerformanceMode
    val showDebugInfo = BooleanPref(
        key = PREFS_DEBUG_MODE,
        titleId = R.string.title__dev_show_debug_info,
        defaultValue = false,
        onChange = doNothing
    )


    // MISC
    val customAppName =
        object : MutableMapPref<ComponentKey, String>("pref_appNameMap", reloadAll) {
            override fun flattenKey(key: ComponentKey) = key.toString()
            override fun unflattenKey(key: String) = makeComponentKey(context, key)
            override fun flattenValue(value: String) = value
            override fun unflattenValue(value: String) = value
        }
    var firstRun by BooleanPref(
        key = PREFS_FIRST_RUN,
        titleId = -1,
        defaultValue = true
    )
    var torchState by BooleanPref(
        key = PREFS_TORCH,
        titleId = R.string.dash_torch,
        defaultValue = false,
        onChange = doNothing
    )

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
        private val INSTANCE = MainThreadInitializedObject(::OmegaPreferences)

        @JvmStatic
        fun getInstance(context: Context) = INSTANCE.get(context)!!

    }
}