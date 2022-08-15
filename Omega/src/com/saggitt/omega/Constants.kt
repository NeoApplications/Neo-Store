package com.saggitt.omega

import com.android.launcher3.R
import com.saggitt.omega.theme.ThemeManager
import com.saggitt.omega.util.Config

// DESKTOP PREFS
const val PREFS_DESKTOP_ROWS = "pref_numRows"
const val PREFS_DESKTOP_ROWS_RAW = "numRows"
const val PREFS_DESKTOP_COLUMNS = "pref_numColumns"
const val PREFS_DESKTOP_COLUMNS_RAW = "numColumns"
const val PREFS_DESKTOP_ICON_SCALE = "pref_home_icon_scale"
const val PREFS_DESKTOP_HIDE_LABEL = "pref_hide_app_label"
const val PREFS_DESKTOP_ICON_LABEL_TWOLINES = "pref_icon_labels_two_lines"
const val PREFS_DESKTOP_ICON_TEXT_SCALE = "pref_icon_text_scale"
const val PREFS_DESKTOP_POPUP_MENU = "pref_desktopUsePopupMenuView"
const val PREFS_DESKTOP_LOCK = "pref_lock_desktop"
const val PREFS_WIDGETS_FULL_WIDTH = "pref_full_width_widgets"
const val PREFS_EMPTY_SCREENS = "pref_keepEmptyScreens"
const val PREFS_FOLDER_RADIUS = "pref_folder_radius"
const val PREFS_FOLDER_COLUMNS = "pref_folder_columns"
const val PREFS_FOLDER_ROWS = "pref_folder_rows"
const val PREFS_FOLDER_BACKGROUND = "pref_folder_background"
const val PREFS_FOLDER_BACKGROUND_CUSTOM = "pref_custom_folder_background"
const val PREFS_STATUSBAR_HIDE = "pref_hideStatusBar"
const val PREFS_DASH_LINESIZE = "pref_dash_linesize"
const val PREFS_DASH_PROVIDERS = "pref_dash_providers"

// DOCK PREFS
const val PREFS_DOCK_HIDE = "pref_hideHotseat"
const val PREFS_DOCK_ICON_SCALE = "pref_hotseat_icon_scale"
const val PREFS_DOCK_SCALE = "pref_dockScale"
const val PREFS_DOCK_BACKGROUND = "pref_dockBackground"
const val PREFS_DOCK_BACKGROUND_COLOR = "pref_dock_background_color"
const val PREFS_DOCK_OPACITY = "pref_dockOpacity"
const val PREFS_DOCK_SEARCH = "pref_dock_search"
const val PREFS_DOCK_COLUMNS = "pref_numHotseatIcons"
const val PREFS_DOCK_COLUMNS_RAW = "numHotseatIcons"

// DRAWER PREFS
const val PREFS_SORT = "pref_sortMode"
const val PREFS_SORT_X = "pref_sortMode_x"
const val PREFS_PROTECTED_APPS = "pref_protected_apps"
const val PREFS_TRUST_APPS = "pref_trust_apps"
const val PREFS_HIDDEN_SET = "hidden_app_set"
const val PREFS_HIDDEN_PREDICTION_SET = "pref_hidden_prediction_set"
const val PREFS_PROTECTED_SET = "protected_app_set"
const val PREFS_DRAWER_ICON_SCALE = "pref_allapps_icon_scale"
const val PREFS_DRAWER_SEARCH = "pref_all_apps_search"
const val PREFS_DRAWER_ICON_TEXT_SCALE = "pref_allapps_icon_text_scale"
const val PREFS_DRAWER_HIDE_LABEL = "pref_hide_allapps_app_label"
const val PREFS_DRAWER_ICON_LABEL_TWOLINES = "pref_apps_icon_labels_two_lines"
const val PREFS_DRAWER_HEIGHT_MULTIPLIER = "pref_allAppsCellHeightMultiplier"
const val PREFS_WORK_PROFILE_SEPARATED = "pref_separate_work_apps"
const val PREFS_KEEP_SCROLL_STATE = "pref_keep_scroll_state"
const val PREFS_DRAWER_COLUMNS = "pref_numAllAppsColumns"
const val PREFS_DRAWER_COLUMNS_RAW = "numAllAppsColumns"
const val PREFS_DRAWER_LAYOUT = "pref_drawer_layout"
const val PREFS_DRAWER_LAYOUT_X = "pref_drawer_layout_x"
const val PREFS_DRAWER_OPACITY = "pref_drawer_opacity"
const val PREFS_DRAWER_CUSTOM_BACKGROUND = "pref_drawer_background"
const val PREFS_DRAWER_BACKGROUND_COLOR = "pref_drawer_background_color"

// THEME PREFS
const val PREFS_THEME = "pref_launcherTheme"
const val PREFS_THEME_X = "pref_launcherTheme_x"
const val PREFS_ACCENT = "pref_accent_color"
const val PREFS_BLUR = "pref_enableBlur"
const val PREFS_BLUR_RADIUS = "pref_blurRadius"
const val PREFS_BLUR_RADIUS_X = "pref_blurRadius_x"
const val PREFS_WINDOWCORNER = "pref_customWindowCorner"
const val PREFS_WINDOWCORNER_RADIUS = "pref_customWindowCornerRadius"
const val PREFS_ICON_PACK = "pref_icon_pack_package"
const val PREFS_ICON_SHAPE = "pref_iconShape"
const val PREFS_COLORED_BACKGROUND = "pref_colored_background"
const val PREFS_WHITE_TREATMENT = "pref_white_only_treatment"
const val PREFS_LEGACY_TREATMENT = "pref_legacy_treatment"
const val PREFS_FORCE_ADAPTIVE = "pref_adaptive_icon_pack"
const val PREFS_FORCE_SHAPELESS = "pref_force_shape_less"

const val THEME_LIGHT = 0
const val THEME_DARK = ThemeManager.THEME_DARK
const val THEME_BLACK = ThemeManager.THEME_DARK or ThemeManager.THEME_USE_BLACK
const val THEME_WALLPAPER = ThemeManager.THEME_FOLLOW_WALLPAPER
const val THEME_WALLPAPER_BLACK =
    ThemeManager.THEME_FOLLOW_WALLPAPER or ThemeManager.THEME_USE_BLACK
const val THEME_SYSTEM = ThemeManager.THEME_FOLLOW_NIGHT_MODE
const val THEME_SYSTEM_BLACK = ThemeManager.THEME_FOLLOW_NIGHT_MODE or ThemeManager.THEME_USE_BLACK

val themeItems = mutableMapOf(
    THEME_LIGHT to R.string.theme_light,
    THEME_DARK to R.string.theme_dark,
    THEME_BLACK to R.string.theme_black,
    THEME_SYSTEM to R.string.theme_auto_night_mode,
    THEME_SYSTEM_BLACK to R.string.theme_auto_night_mode_black,
    THEME_WALLPAPER to R.string.theme_dark_theme_mode_follow_wallpaper,
    THEME_WALLPAPER_BLACK to R.string.theme_dark_theme_mode_follow_wallpaper_black,
)

val drawerLayoutOptions = mutableMapOf(
    Config.DRAWER_VERTICAL to R.string.title_drawer_vertical,
    Config.DRAWER_PAGED to R.string.title_drawer_paged,
)

val drawerSortOptions = mutableMapOf(
    Config.SORT_AZ to R.string.title__sort_alphabetical_az,
    Config.SORT_ZA to R.string.title__sort_alphabetical_za,
    Config.SORT_MOST_USED to R.string.title__sort_most_used,
    Config.SORT_BY_COLOR to R.string.title__sort_by_color,
)

// SEARCH PREFS
const val PREFS_SEARCH_PROVIDER = "pref_global_search_provider"
const val PREFS_SEARCH_BAR_RADIUS = "pref_searchbar_radius"
const val PREFS_SEARCH_HIDDEN_APPS = "pref_search_hidden_apps"
const val PREFS_SEARCH_FUZZY = "pref_fuzzy_search"
const val PREFS_SEARCH_GLOBAL = "pref_all_apps_global_search"
const val PREFS_SEARCH_SHOW_ASSISTANT = "opa_enabled"
const val PREFS_SEARCH_ASSISTANT = "opa_assistant"
const val PREFS_SEARCH_CONTACTS = "search_contacts"

// GESTURES PREFS
const val PREFS_NOTIFICATION_COUNT = "pref_notification_count"
const val PREFS_NOTIFICATION_BACKGROUND_CUSTOM = "pref_custom_background"
const val PREFS_NOTIFICATION_BACKGROUND = "pref_notification_background"
const val PREFS_NOTIFICATION_COUNT_FOLDER = "pref_folder_badge_count"
const val NOTIFICATION_BADGING = "notification_badging"
const val NOTIFICATION_DOTS_PREFERENCE_KEY = "pref_icon_badging"
const val PREFS_GESTURE_DOUBLE_TAP = "pref_gesture_double_tap"
const val PREFS_GESTURE_LONG_PRESS = "pref_gesture_long_press"
const val PREFS_GESTURE_HOME = "pref_gesture_press_home"
const val PREFS_GESTURE_BACK = "pref_gesture_press_back"
const val PREFS_GESTURE_SWIPE_DOWN = "pref_gesture_swipe_down"
const val PREFS_GESTURE_SWIPE_UP = "pref_gesture_swipe_up"
const val PREFS_GESTURE_SWIPE_UP_DOCK = "pref_gesture_dock_swipe_up"
const val PREFS_GESTURE_ASSISTANT = "pref_gesture_launch_assistant"

//SMARTSPACE PREFS
const val PREFS_SMARTSPACE_SHOW = "pref_show_smartspace"
const val PREFS_SMARTSPACE_ENABLE = "enable_smartspace"
const val PREFS_SMARTSPACE_DATE = "pref_smartspace_date"
const val PREFS_SMARTSPACE_TIME = "pref_smartspace_time"
const val PREFS_SMARTSPACE_WIDGET_ID = "smartspace_widget_id"
const val PREFS_SMARTSPACE_TIME_ABOVE = "pref_smartspace_time_above"
const val PREFS_SMARTSPACE_WEATHER_UNITS = "pref_weather_units"
const val PREFS_SMARTSPACE_WEATHER_ICONS = "pref_weatherIcons"
const val PREFS_SMARTSPACE_WEATHER_PROVIDER = "pref_smartspace_widget_provider"
const val PREFS_SMARTSPACE_EVENT_PROVIDER = "pref_smartspace_event_provider"
const val PREFS_SMARTSPACE_EVENT_PROVIDERS = "pref_smartspace_event_providers"
const val PREF_PILL_QSB = "pref_use_pill_qsb"
const val PREFS_TIME_24H = "pref_smartspace_time_24_h"

// ADVANCED PREFS
const val PREFS_LANGUAGE = "pref_language"
const val PREFS_LANGUAGE_DEFAULT_NAME = "System"
const val PREFS_LANGUAGE_DEFAULT_CODE = "en"
const val PREFS_FIRST_RUN = "pref_first_run"
const val PREFS_RESTORE_SUCCESS = "pref_restore_success"
const val PREFS_RECENT_BACKUP = "pref_recent_backups"

// DEVELOPER PREFS
const val PREFS_DEV_PREFS_SHOW = "pref_showDevOptions"
const val PREFS_DESKTOP_MODE = "pref_desktop_mode"
const val PREFS_DESKTOP_MODE_SETTINGS = "pref_desktop_mode_settings"
const val PREFS_LOW_PREFORMANCE = "pref_lowPerformanceMode"
const val PREFS_DEBUG_MODE = "pref_showDebugInfo"
const val PREFS_KILL = "kill"

// EXTRA PREFS
const val PREFS_FEED_PROVIDER = "pref_feed_provider"
const val PREFS_TORCH = "pref_torch"
const val PREFS_DESKTOP_POPUP_EDIT = "desktop_popup_edit"
const val PREFS_DESKTOP_POPUP_REMOVE = "desktop_popup_remove"
const val PREFS_DRAWER_POPUP_EDIT = "drawer_popup_edit"
const val PREFS_DRAWER_POPUP_UNINSTALL = "drawer_popup_uninstall"
const val PREFS_FOLDER_COVER_MODE = "pref_cover_mode"
const val PREFS_FOLDER_SWIPE_UP = "pref_swipe_up_gesture"
