/*
 * Neo Store: An open-source modern F-Droid client.
 * Copyright (C) 2023  Antonios Hazim
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
package com.machiav3lli.fdroid.ui.navigation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import com.machiav3lli.fdroid.content.Preferences
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SlidePager(
    modifier: Modifier = Modifier,
    pageItems: List<NavItem>,
    pagerState: PagerState,
    navController: NavHostController,
) {
    HorizontalPager(modifier = modifier, state = pagerState, beyondBoundsPageCount = 1) { page ->
        pageItems[page].ComposablePage(
            navController = navController,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PagerNavBar(pageItems: List<NavItem>, pagerState: PagerState) {
    val scope = rememberCoroutineScope()

    NavigationBar(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        pageItems.forEachIndexed { index, item ->
            val selected = pagerState.currentPage == index

            if (Preferences[Preferences.Key.AltNavBarItem])
                AltNavBarItem(
                    modifier = Modifier.weight(1f),
                    icon = item.icon,
                    labelId = item.title,
                    selected = selected,
                    onClick = {
                        scope.launch { pagerState.scrollToPage(index) }
                    }
                )
            else NavBarItem(
                modifier = Modifier.weight(if (selected) 2f else 1f),
                icon = item.icon,
                labelId = item.title,
                selected = selected,
                onClick = {
                    scope.launch { pagerState.scrollToPage(index) }
                }
            )
        }
    }
}
