/*
 * This file is part of Neo Launcher
 * Copyright (c) 2022   Neo Launcher Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saggitt.omega.compose.screens

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.TabRow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LeadingIconTab
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.shortcuts.ShortcutKey
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import com.saggitt.omega.compose.components.ExpandableListItem
import com.saggitt.omega.compose.components.ListItemWithIcon
import com.saggitt.omega.compose.components.ViewWithActionBar
import com.saggitt.omega.compose.components.preferences.PreferenceGroup
import com.saggitt.omega.compose.navigation.Routes
import com.saggitt.omega.compose.navigation.preferenceGraph
import com.saggitt.omega.compose.screens.preferences.EditDashPage
import com.saggitt.omega.compose.screens.preferences.GesturesPrefsPage
import com.saggitt.omega.data.AppItemWithShortcuts
import com.saggitt.omega.gestures.GestureController
import com.saggitt.omega.gestures.handlers.StartAppGestureHandler
import com.saggitt.omega.preferences.OmegaPreferences
import com.saggitt.omega.util.Config
import com.saggitt.omega.util.appsList
import kotlinx.coroutines.launch
import org.json.JSONObject

fun NavGraphBuilder.gesturePageGraph(route: String) {
    preferenceGraph(route, { GesturesPrefsPage() }) { subRoute ->
        gesturePrefGraph(route = subRoute(Routes.GESTURE_SELECTOR))
        preferenceGraph(route = subRoute(Routes.EDIT_DASH), { EditDashPage() })
    }
}

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.gesturePrefGraph(route: String) {
    preferenceGraph(route, { }) { subRoute ->
        composable(
            route = subRoute("{titleId}/{key}/{default}"),
            arguments = listOf(
                navArgument("titleId") { type = NavType.IntType },
                navArgument("key") { type = NavType.StringType },
                navArgument("default") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val args = backStackEntry.arguments!!
            val title = args.getInt("titleId")
            val key = args.getString("key") ?: ""
            val default = args.getString("default") ?: ""
            GestureSelector(titleId = title, key = key, default = default)
        }
    }
}

@Composable
fun GestureSelector(titleId: Int, key: String, default: String) {
    ViewWithActionBar(
        title = stringResource(titleId),
    ) {
        MainGesturesScreen(key, default)
    }
}

@OptIn(ExperimentalPagerApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainGesturesScreen(key: String, default: String) {
    val pagerState = rememberPagerState()
    val prefs = Utilities.getOmegaPrefs(LocalContext.current)
    val selectedOption = remember {
        mutableStateOf(
            prefs.sharedPrefs.getString(key, default)
        )
    }

    val tabs = listOf(
        TabItem(R.drawable.ic_assistant, R.string.tab_launcher) {
            LauncherScreen(
                prefs,
                selectedOption,
                key
            )
        },
        TabItem(R.drawable.ic_apps, R.string.apps_label) { AppsScreen(prefs, selectedOption, key) },
        TabItem(R.drawable.ic_edit_dash, R.string.tab_shortcuts) {
            ShortcutsScreen(
                prefs,
                selectedOption,
                key
            )
        }
    )

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text(text = stringResource(R.string.app_name), fontSize = 18.sp) }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Tabs(tabs = tabs, pagerState = pagerState)
            TabsContent(tabs = tabs, pagerState = pagerState)
        }
    }
}

@Composable
fun LauncherScreen(prefs: OmegaPreferences, selectedOption: MutableState<String?>, key: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
    ) {
        val context = LocalContext.current
        val launcherItems = GestureController.getGestureHandlers(context, true, true)
        val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
        val colors = RadioButtonDefaults.colors(
            selectedColor = MaterialTheme.colorScheme.onPrimary,
            unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
        )

        PreferenceGroup {
            LazyColumn {
                itemsIndexed(launcherItems) { _, item ->
                    ListItemWithIcon(
                        title = item.displayName,
                        modifier = Modifier
                            .background(
                                color = if (item.toString() == selectedOption.value)
                                    MaterialTheme.colorScheme.primary.copy(0.65f)
                                else Color.Transparent
                            )
                            .clickable {
                                selectedOption.value = item.toString()
                                prefs.sharedPrefs
                                    .edit()
                                    .putString(key, selectedOption.value)
                                    .apply()
                                backDispatcher?.onBackPressed()
                            },
                        summary = "",
                        startIcon = {
                            Image(
                                painter = rememberDrawablePainter(drawable = item.icon),
                                contentDescription = null,
                                modifier = Modifier.size(36.dp)
                            )
                        },
                        endCheckbox = {
                            RadioButton(
                                selected = (item.toString() == selectedOption.value),
                                onClick = {
                                    selectedOption.value = item.toString()
                                    prefs.sharedPrefs.edit()
                                        .putString(key, selectedOption.value)
                                        .apply()
                                    backDispatcher?.onBackPressed()
                                },
                                colors = colors
                            )
                        },
                        verticalPadding = 4.dp
                    )
                }
            }
        }
    }
}

@Composable
fun AppsScreen(prefs: OmegaPreferences, selectedHandler: MutableState<String?>, key: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
    ) {
        val context = LocalContext.current
        val apps =
            appsList().value //Config(context).getAppsList(filter = null).sortedBy { it.label.toString() }
        val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
        val colors = RadioButtonDefaults.colors(
            selectedColor = MaterialTheme.colorScheme.onPrimary,
            unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
        )

        val selectedOption = remember {
            mutableStateOf(selectedHandler.value)
        }
        PreferenceGroup {
            LazyColumn {
                itemsIndexed(apps) { _, item ->
                    val config = JSONObject("{}")
                    config.apply {
                        put("appName", item.label)
                        put("packageName", item.packageName)
                        put("target", item.key)
                        put("type", "app")
                    }

                    val appGestureHandler = StartAppGestureHandler(context, config)
                    appGestureHandler.apply {
                        appName = item.label
                    }
                    ListItemWithIcon(
                        title = item.label,
                        modifier = Modifier
                            .background(
                                color = if (appGestureHandler.toString() == selectedOption.value)
                                    MaterialTheme.colorScheme.primary.copy(0.65f)
                                else Color.Transparent
                            )
                            .clickable {
                                selectedOption.value = appGestureHandler.toString()
                                prefs.sharedPrefs
                                    .edit()
                                    .putString(key, selectedOption.value)
                                    .apply()
                                backDispatcher?.onBackPressed()
                            },
                        summary = "",
                        startIcon = {
                            Image(
                                painter = BitmapPainter(item.icon.asImageBitmap()),
                                contentDescription = null,
                                modifier = Modifier.size(36.dp)
                            )
                        },
                        endCheckbox = {
                            RadioButton(
                                selected = (appGestureHandler.toString() == selectedOption.value),
                                onClick = {
                                    selectedOption.value = appGestureHandler.toString()
                                    prefs.sharedPrefs.edit()
                                        .putString(key, selectedOption.value)
                                        .apply()
                                    backDispatcher?.onBackPressed()
                                },
                                colors = colors
                            )
                        },
                        verticalPadding = 4.dp
                    )
                }
            }
        }
    }
}

@Composable
fun ShortcutsScreen(prefs: OmegaPreferences, selectedHandler: MutableState<String?>, key: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
    ) {
        val context = LocalContext.current
        val apps = Config(context).getAppsList(filter = null)
            .sortedBy { it.label.toString() }
            .map { AppItemWithShortcuts(context, it) }
        val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

        val colors = RadioButtonDefaults.colors(
            selectedColor = MaterialTheme.colorScheme.onPrimary,
            unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
        val selectedOption = remember {
            mutableStateOf(selectedHandler.value)
        }

        PreferenceGroup {
            LazyColumn {
                itemsIndexed(apps) { _, app ->
                    if (app.hasShortcuts) {
                        ExpandableListItem(
                            title = app.info.label.toString(),
                            icon = app.info.getIcon(LocalContext.current.resources.displayMetrics.densityDpi)
                        ) {
                            app.shortcuts.forEach {

                                val config = JSONObject("{}")
                                config.apply {
                                    put("appName", it.label.toString())
                                    put("packageName", it.info.`package`)
                                    put("intent", ShortcutKey.makeIntent(it.info).toUri(0))
                                    put("user", 0)
                                    put("id", it.info.id)
                                    put("type", "shortcut")
                                }
                                val appGestureHandler = StartAppGestureHandler(context, config)
                                appGestureHandler.apply {
                                    appName = it.label.toString()
                                }
                                ListItemWithIcon(
                                    title = it.label.toString(),
                                    modifier = Modifier
                                        .clip(MaterialTheme.shapes.medium)
                                        .background(
                                            color = if (appGestureHandler.toString() == selectedOption.value)
                                                MaterialTheme.colorScheme.primary.copy(0.65f)
                                            else Color.Transparent
                                        )
                                        .clickable {
                                            selectedOption.value = appGestureHandler.toString()
                                            prefs.sharedPrefs
                                                .edit()
                                                .putString(key, selectedOption.value)
                                                .apply()
                                            backDispatcher?.onBackPressed()
                                        },
                                    summary = "",
                                    startIcon = {
                                        Image(
                                            painter = rememberDrawablePainter(
                                                drawable = it.iconDrawable
                                            ),
                                            contentDescription = null,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    },
                                    verticalPadding = 4.dp,
                                    horizontalPadding = 0.dp,
                                    endCheckbox = {
                                        RadioButton(
                                            selected = (appGestureHandler.toString() == selectedOption.value),
                                            onClick = {
                                                selectedOption.value = appGestureHandler.toString()
                                                prefs.sharedPrefs.edit()
                                                    .putString(key, selectedOption.value)
                                                    .apply()
                                                backDispatcher?.onBackPressed()
                                            },
                                            colors = colors
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun Tabs(tabs: List<TabItem>, pagerState: PagerState) {
    val scope = rememberCoroutineScope()
    TabRow(
        selectedTabIndex = pagerState.currentPage,
        backgroundColor = MaterialTheme.colorScheme.background,
        contentColor = Color.White,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
            )
        }
    ) {
        tabs.forEachIndexed { index, tab ->
            LeadingIconTab(
                icon = { Icon(painter = painterResource(id = tab.icon), contentDescription = "") },
                text = { Text(text = stringResource(id = tab.title)) }, // TODO Needs better layout for longer words
                selected = pagerState.currentPage == index,
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun TabsContent(tabs: List<TabItem>, pagerState: PagerState) {
    HorizontalPager(state = pagerState, count = tabs.size) { page ->
        tabs[page].screen()
    }
}

typealias ComposableFun = @Composable () -> Unit

class TabItem(var icon: Int, var title: Int, var screen: ComposableFun)