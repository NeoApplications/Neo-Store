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
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.shortcuts.ShortcutKey
import com.android.launcher3.util.ComponentKey
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
import com.saggitt.omega.compose.screens.preferences.GesturesPrefsPage
import com.saggitt.omega.data.AppItemWithShortcuts
import com.saggitt.omega.gestures.actions.BlankGestureAction
import com.saggitt.omega.gestures.actions.GestureAction
import com.saggitt.omega.gestures.handlers.StartAppGestureHandler
import com.saggitt.omega.util.Config
import kotlinx.coroutines.launch
import org.json.JSONObject

fun NavGraphBuilder.gesturePageGraph(route: String) {
    preferenceGraph(route, { GesturesPrefsPage() }) { subRoute ->
        gesturePrefGraph(route = subRoute(Routes.GESTURE_SELECTOR))
    }
}

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.gesturePrefGraph(route: String) {
    preferenceGraph(route, { }) { subRoute ->
        composable(
            route = subRoute("{titleId}/{key}"),
            arguments = listOf(
                navArgument("titleId") { type = NavType.IntType },
                navArgument("key") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val args = backStackEntry.arguments!!
            val title = args.getInt("titleId")
            val key = args.getString("key") ?: ""
            GestureSelector(titleId = title, key = key)
        }
    }
}

@Composable
fun GestureSelector(titleId: Int, key: String) {
    ViewWithActionBar(
        title = stringResource(titleId),
    ) {
        MainGesturesScreen(key)
    }
}

@OptIn(ExperimentalPagerApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainGesturesScreen(key: String) {
    val tabs = listOf(TabItem.Launcher, TabItem.Apps, TabItem.Shortcuts)
    val pagerState = rememberPagerState()
    val prefs = Utilities.getOmegaPrefs(LocalContext.current)
    val selectedOption = remember {
        mutableStateOf(prefs.sharedPrefs.getString(key, "")) // TODO pass it to pages
    }

    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    Scaffold(
        topBar = { TopBar() },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                onClick = {
                    prefs.sharedPrefs.edit()
                        .putString(key, selectedOption.value)
                        .apply()
                    backDispatcher?.onBackPressed()
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = stringResource(id = R.string.tab_bottom_sheet_save)
                )
                Text(text = stringResource(id = R.string.tab_bottom_sheet_save))
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Tabs(tabs = tabs, pagerState = pagerState)
            TabsContent(tabs = tabs, pagerState = pagerState)
        }
    }
}

@Composable
fun TopBar() {
    TopAppBar( // TODO migrate to M3
        title = { Text(text = stringResource(R.string.app_name), fontSize = 18.sp) },
        backgroundColor = colorResource(id = R.color.colorPrimary),
        contentColor = Color.White
    )
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
@Preview(showBackground = true)
@Composable
fun TabsPreview() {
    val tabs = listOf(
        TabItem.Launcher,
        TabItem.Apps,
        TabItem.Shortcuts
    )
    val pagerState = rememberPagerState()
    Tabs(tabs = tabs, pagerState = pagerState)
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun TabsContent(tabs: List<TabItem>, pagerState: PagerState) {
    HorizontalPager(state = pagerState, count = tabs.size) { page ->
        tabs[page].screen()
    }
}

@Composable
fun LauncherScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
    ) {
        val context = LocalContext.current
        val launcherItems = GestureAction.getLauncherActions(context, true)
        val colors = RadioButtonDefaults.colors(
            selectedColor = MaterialTheme.colorScheme.onPrimary,
            unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
        )

        val (selectedOption, onOptionSelected) = remember {
            mutableStateOf(BlankGestureAction::class.java.name)
        }

        PreferenceGroup {
            LazyColumn {
                itemsIndexed(launcherItems) { _, item ->
                    ListItemWithIcon(
                        title = item.displayName,
                        modifier = Modifier
                            .background(
                                color = if (item.javaClass.name == selectedOption)
                                    MaterialTheme.colorScheme.primary.copy(0.65f)
                                else Color.Transparent
                            )
                            .clickable {
                                onOptionSelected(item.javaClass.name)
                            },
                        summary = "",
                        startIcon = {
                            Image(
                                painter = painterResource(id = item.icon),
                                contentDescription = null,
                                modifier = Modifier.size(36.dp)
                            )
                        },
                        endCheckbox = {
                            RadioButton(
                                selected = (item.javaClass.name == selectedOption),
                                onClick = {
                                    onOptionSelected(item.javaClass.name)
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
fun AppsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
    ) {
        val context = LocalContext.current
        val prefs = Utilities.getOmegaPrefs(context)
        val apps = Config(context).getAppsList(filter = null).sortedBy { it.label.toString() }
        val colors = RadioButtonDefaults.colors(
            selectedColor = MaterialTheme.colorScheme.onPrimary,
            unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
        val (selectedOption, onOptionSelected) = remember {
            mutableStateOf("")
        }
        PreferenceGroup {
            LazyColumn {
                itemsIndexed(apps) { _, item ->
                    val config = JSONObject("{}")
                    config.apply {
                        put("appName", item.label.toString())
                        put("packageName", item.componentName.packageName)
                        put("target", ComponentKey(item.componentName, item.user))
                        put("type", "app")
                    }

                    val appGestureHandler = StartAppGestureHandler(context, config)
                    appGestureHandler.apply {
                        appName = item.label.toString()
                    }
                    ListItemWithIcon(
                        title = item.label.toString(),
                        modifier = Modifier
                            .background(
                                color = if (item.componentName.packageName == selectedOption)
                                    MaterialTheme.colorScheme.primary.copy(0.65f)
                                else Color.Transparent
                            )
                            .clickable {
                                onOptionSelected(item.componentName.packageName)

                                prefs.sharedPrefs
                                    .edit()
                                    .putString(
                                        "gesture_action",
                                        appGestureHandler.toString()
                                    )
                                    .apply()
                            },
                        summary = "",
                        startIcon = {
                            Image(
                                painter = rememberDrawablePainter(
                                    drawable = item.getIcon(LocalContext.current.resources.displayMetrics.densityDpi)
                                ),
                                contentDescription = null,
                                modifier = Modifier.size(36.dp)
                            )
                        },
                        endCheckbox = {
                            RadioButton(
                                selected = (item.componentName.packageName == selectedOption),
                                onClick = {
                                    onOptionSelected(item.componentName.packageName)
                                    prefs.sharedPrefs.edit().putString(
                                        "gesture_action",
                                        appGestureHandler.toString()
                                    ).apply()
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
fun ShortcutsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
    ) {
        val context = LocalContext.current
        val prefs = Utilities.getOmegaPrefs(context)
        val apps = Config(context).getAppsList(filter = null)
            .sortedBy { it.label.toString() }
            .map { AppItemWithShortcuts(context, it) }

        val colors = RadioButtonDefaults.colors(
            selectedColor = MaterialTheme.colorScheme.onPrimary,
            unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
        )

        val (selectedOption, onOptionSelected) = remember {
            mutableStateOf("")
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
                                            color = if (appGestureHandler.toString() == selectedOption)
                                                MaterialTheme.colorScheme.primary.copy(0.65f)
                                            else Color.Transparent
                                        )
                                        .clickable {
                                            onOptionSelected(appGestureHandler.toString())
                                            prefs.sharedPrefs
                                                .edit()
                                                .putString(
                                                    "gesture_action",
                                                    appGestureHandler.toString()
                                                )
                                                .apply()
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
                                            selected = (appGestureHandler.toString() == selectedOption),
                                            onClick = {
                                                onOptionSelected(appGestureHandler.toString())
                                                prefs.sharedPrefs.edit().putString(
                                                    "gesture_action",
                                                    appGestureHandler.toString()
                                                ).apply()
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

typealias ComposableFun = @Composable () -> Unit

sealed class TabItem(var icon: Int, var title: Int, var screen: ComposableFun) {
    object Launcher :
        TabItem(R.drawable.ic_assistant, R.string.tab_launcher, { LauncherScreen() })

    object Apps :
        TabItem(R.drawable.ic_apps, R.string.apps_label, { AppsScreen() })

    object Shortcuts :
        TabItem(R.drawable.ic_edit_dash, R.string.tab_shortcuts, { ShortcutsScreen() })
}
