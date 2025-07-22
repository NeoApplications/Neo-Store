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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.data.content.Preferences
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch

@Composable
fun SlidePager(
    modifier: Modifier = Modifier,
    pageItems: ImmutableList<NavItem>,
    pagerState: PagerState,
    preComposePages: Int = 3,
) {
    HorizontalPager(
        modifier = modifier,
        state = pagerState,
        beyondViewportPageCount = preComposePages
    ) { page ->
        pageItems[page].content()
    }
}

@Composable
fun PagerNavBar(pageItems: List<NavItem>, pagerState: PagerState) {
    val scope = rememberCoroutineScope()

    NavigationBar(
        modifier = Modifier.padding(horizontal = 8.dp),
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        pageItems.forEachIndexed { index, item ->
            val selected by derivedStateOf { pagerState.currentPage == index }

            if (Preferences[Preferences.Key.AltNavBarItem])
                AltNavBarItem(
                    icon = item.icon,
                    labelId = item.title,
                    selected = selected,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        scope.launch { pagerState.animateScrollToPage(index) }
                    }
                )
            else NavBarItem(
                icon = item.icon,
                labelId = item.title,
                selected = selected,
                modifier = Modifier.weight(if (selected) 2f else 1f),
                onClick = {
                    scope.launch { pagerState.animateScrollToPage(index) }
                }
            )
        }
    }
}

@Composable
fun RowScope.AltNavBarItem(
    icon: ImageVector,
    labelId: Int,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.surfaceContainer
        else Color.Transparent,
        label = "backgroundColor",
    )
    val iconSize by animateDpAsState(
        targetValue = if (selected) 32.dp else 24.dp,
        label = "iconSize",
    )
    val iconColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onBackground,
        label = "iconColor",
    )

    Row(
        modifier = modifier
            .clickable { onClick() }
            .weight(1f),
        horizontalArrangement = Arrangement.Center,
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = stringResource(id = labelId),
                modifier = Modifier
                    .background(backgroundColor, CircleShape)
                    .padding(8.dp)
                    .size(iconSize),
                tint = iconColor,
            )
            AnimatedVisibility(
                visible = !selected,
                enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
                exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top),
            ) {
                Text(
                    text = stringResource(id = labelId),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = iconColor,
                )
            }
        }
    }
}

@Composable
fun RowScope.NavBarItem(
    icon: ImageVector,
    labelId: Int,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val background by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.surfaceContainer
        else Color.Transparent, label = "backgroundColor"
    )
    val iconColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onBackground,
        label = "iconColor",
    )

    Row(
        modifier = modifier
            .padding(vertical = 8.dp)
            .clip(MaterialTheme.shapes.extraLarge)
            .clickable { onClick() }
            .background(
                background,
                MaterialTheme.shapes.extraLarge
            )
            .padding(8.dp)
            .weight(1f),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = stringResource(id = labelId),
            modifier = Modifier.size(24.dp),
            tint = iconColor,
        )
        AnimatedVisibility(
            visible = selected,
            enter = fadeIn() + expandHorizontally(expandFrom = Alignment.Start),
            exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.Start),
        ) {
            Text(
                text = stringResource(id = labelId),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = iconColor,
            )
        }
    }
}
