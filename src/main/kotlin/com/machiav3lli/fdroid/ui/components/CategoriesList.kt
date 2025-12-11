/*
 * Neo Store: An open-source modern F-Droid client.
 * Copyright (C) 2022  Antonios Hazim
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
package com.machiav3lli.fdroid.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.ui.compose.utils.addIf
import kotlinx.coroutines.launch

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun CategoriesList(
    modifier: Modifier = Modifier,
    items: List<Triple<String, String, ImageVector>>,
    selectedKey: MutableState<String>,
    onClick: (String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val expanded by remember(selectedKey.value) { mutableStateOf(selectedKey.value.isNotEmpty()) }
    val scrollState = rememberLazyListState()

    SharedTransitionLayout {
        AnimatedContent(
            targetState = expanded,
            modifier = modifier.fillMaxWidth(),
            label = "categories_list"
        ) { isExpanded ->
            val categories: LazyListScope.() -> Unit = {
                itemsIndexed(items, key = { _, item -> item.first }) { index, item ->
                    CategoryItem(
                        icon = item.third,
                        label = item.second,
                        isExpanded = isExpanded,
                        isSelected = item.first == selectedKey.value,
                        avs = this@AnimatedContent,
                        sts = this@SharedTransitionLayout,
                        onClick = {
                            selectedKey.value = item.first
                            onClick(item.first)
                            scrollState.layoutInfo.visibleItemsInfo.none { it.index == index }.let {
                                scope.launch {
                                    scrollState.animateScrollToItem((index - 1).coerceAtLeast(0))
                                }
                            }
                        }
                    )
                }
            }

            if (isExpanded) {
                Column {
                    LazyRow(
                        modifier = modifier.fillMaxWidth(),
                        state = scrollState,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        content = categories,
                    )
                    HorizontalDivider()
                }
            } else {
                LazyColumn(
                    modifier = modifier
                        .fillMaxSize()
                        .clipToBounds(),
                    state = scrollState,
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    content = categories,
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun CategoryItem(
    icon: ImageVector,
    label: String,
    isExpanded: Boolean,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    colors: SelectableChipColors = FilterChipDefaults.filterChipColors(
        containerColor = Color.Transparent,
        selectedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        selectedLeadingIconColor = MaterialTheme.colorScheme.primary,
        selectedLabelColor = MaterialTheme.colorScheme.primary,
        iconColor = MaterialTheme.colorScheme.onSurface,
        labelColor = MaterialTheme.colorScheme.onSurface,
    ),
    avs: AnimatedVisibilityScope,
    sts: SharedTransitionScope,
    onClick: () -> Unit,
) {
    with(sts) {
        FilterChip(
            selected = isSelected,
            modifier = modifier
                .sharedBounds(
                    sharedContentState = rememberSharedContentState(key = "category/$label"),
                    animatedVisibilityScope = avs,
                    resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds(
                        ContentScale.Fit,
                        Alignment.CenterStart
                    ),
                    boundsTransform = { _, _ -> tween() }
                )
                .addIf(condition = !isExpanded) {
                    fillMaxWidth()
                },
            shape = MaterialTheme.shapes.medium,
            border = null,
            colors = colors,
            onClick = onClick,
            leadingIcon = {
                AnimatedVisibility(
                    visible = isSelected || !isExpanded,
                    enter = scaleIn(),
                    exit = scaleOut(),
                ) {
                    Icon(
                        modifier = Modifier
                            .sharedElement(
                                sharedContentState = rememberSharedContentState(key = "category_icon/$label"),
                                animatedVisibilityScope = avs,
                            )
                            .addIf(condition = !isExpanded) {
                                padding(12.dp)
                            },
                        imageVector = icon,
                        contentDescription = label,
                    )
                }
            },
            label = {
                Text(
                    modifier = Modifier
                        .sharedElement(
                            sharedContentState = rememberSharedContentState(key = "category_label/$label"),
                            animatedVisibilityScope = avs
                        )
                        .addIf(condition = !isExpanded) {
                            padding(vertical = 12.dp)
                        },
                    text = label,
                    maxLines = 1,
                )
            }
        )
    }
}
