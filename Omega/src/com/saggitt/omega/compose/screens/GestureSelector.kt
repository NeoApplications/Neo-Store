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

@file:OptIn(ExperimentalPagerApi::class)

package com.saggitt.omega.compose.screens

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.android.launcher3.R
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.saggitt.omega.compose.components.ListItemWithIcon
import com.saggitt.omega.compose.components.ViewWithActionBar
import com.saggitt.omega.compose.preferences.preferenceGraph
import com.saggitt.omega.gestures.actions.BlankGestureAction
import com.saggitt.omega.gestures.actions.GestureAction
import kotlinx.coroutines.launch

@Composable
fun GestureSelector(title: String) {
    ViewWithActionBar(
            title = title
    ) {
        val tabs = listOf(TabItem.Launcher, TabItem.Apps, TabItem.Shortcuts)
        val pagerState = rememberPagerState()
        Column(
                modifier = Modifier
                        .padding(top = 56.dp, start = 8.dp, end = 8.dp)
                        .verticalScroll(rememberScrollState())
                        .background(MaterialTheme.colorScheme.background),

                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
        ) {
            Tabs(tabs = tabs, pagerState = pagerState)
            TabsContent(tabs = tabs, pagerState = pagerState)
        }
    }
}

@Preview
@Composable
fun GestureSelectorPreview() {
    GestureSelector(title = "Swipe UP")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LauncherScreen() {

    val context = LocalContext.current
    val launcherItems = GestureAction.getLauncherActions(context, true)
    val colors = RadioButtonDefaults.colors(
            selectedColor = MaterialTheme.colorScheme.primary,
            unselectedColor = Color.Gray
    )

    val (selectedOption, onOptionSelected) = remember {
        mutableStateOf(BlankGestureAction::class.java.name)
    }

    Column {
        launcherItems.forEachIndexed { _, item ->
            ListItemWithIcon(
                    title = item.displayName,
                    modifier = Modifier.clickable {
                        onOptionSelected(item.displayName)
                    },
                    summary = "",
                    startIcon = {
                        Image(
                                painter = rememberDrawablePainter(drawable = item.icon),
                                contentDescription = null,
                                modifier = Modifier
                                        .clip(CircleShape)
                                        .size(36.dp)
                                        .background(
                                                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05F)
                                        )
                        )
                    },
                    endCheckbox = {
                        RadioButton(
                                selected = (item.displayName == selectedOption),
                                onClick = {
                                    onOptionSelected(item.displayName)
                                },
                                colors = colors
                        )
                    },
                    verticalPadding = 4.dp
            )
        }
    }
}

@Composable
fun AppsScreen() {

    Text(text = "Hola Apps")
}


@Composable
fun ShortcutScreen() {
    Text(text = "Hola Shortcuts")
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun Tabs(tabs: List<TabItem>, pagerState: PagerState) {
    val scope = rememberCoroutineScope()
    var selectedIndex by remember { mutableStateOf(0) }
    TabRow(
            selectedTabIndex = pagerState.currentPage,
            backgroundColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                    .padding(vertical = 4.dp, horizontal = 8.dp)
                    .clip(RoundedCornerShape(50)),
            indicator = {
                Box {}
            }
    ) {
        tabs.forEachIndexed { index, tab ->
            val selected = selectedIndex == index
            Tab(
                    modifier = if (selected) Modifier
                            .padding(1.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                    Color.White
                            )
                    else Modifier
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.primary),
                    selected = selected,
                    onClick = {
                        scope.launch {
                            selectedIndex = index
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = { Text(text = stringResource(id = tab.title)) }
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

sealed class TabItem(var title: Int, var screen: ComposableFun) {
    object Launcher : TabItem(R.string.tab_launcher, { LauncherScreen() })
    object Apps : TabItem(R.string.apps_label, { AppsScreen() })
    object Shortcuts : TabItem(R.string.tab_shortcuts, { ShortcutScreen() })
}

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.gestureGraph(route: String) {
    preferenceGraph(route, { GestureSelector("") }) { subRoute ->
        composable(
                route = subRoute("{title}"),
                arguments = listOf(
                        navArgument("title") { type = NavType.StringType }
                )
        ) { backStackEntry ->
            val args = backStackEntry.arguments!!
            val title = args.getString("title")!!
            GestureSelector(title = title)
        }
    }
}