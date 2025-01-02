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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.ui.compose.utils.addIf
import kotlinx.coroutines.launch

@Composable
fun CategoriesList(
    modifier: Modifier = Modifier,
    items: List<Pair<String, ImageVector>>,
    selectedKey: MutableState<String>,
    onClick: (String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val expanded by remember(selectedKey.value) { mutableStateOf(selectedKey.value.isNotEmpty()) }
    val scrollState = rememberLazyListState()

    val categories: LazyListScope.() -> Unit = {
        itemsIndexed(items) { index, item ->
            CategoryItem(
                icon = item.second,
                label = item.first,
                expanded = expanded,
                selected = item.first == selectedKey.value,
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

    if (expanded) {
        LazyRow(
            modifier = modifier.fillMaxWidth(),
            state = scrollState,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            content = categories,
        )
        HorizontalDivider()
    } else {
        LazyColumn(
            modifier = modifier.fillMaxHeight(),
            state = scrollState,
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = categories,
        )
    }
}

@Composable
fun CategoryItem(
    icon: ImageVector,
    label: String,
    expanded: Boolean,
    selected: Boolean,
    modifier: Modifier = Modifier
        .addIf(condition = !expanded) {
            fillMaxWidth()
        },
    colors: SelectableChipColors = FilterChipDefaults.filterChipColors(
        containerColor = Color.Transparent,
        selectedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        selectedLeadingIconColor = MaterialTheme.colorScheme.primary,
        selectedLabelColor = MaterialTheme.colorScheme.primary,
        iconColor = MaterialTheme.colorScheme.onSurface,
        labelColor = MaterialTheme.colorScheme.onSurface,
    ),
    onClick: () -> Unit,
) {
    FilterChip(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        border = null,
        colors = colors,
        onClick = onClick,
        selected = selected,
        label = {
            Text(
                modifier = Modifier
                    .addIf(!expanded) {
                        padding(vertical = 12.dp)
                    },
                text = label,
                maxLines = 1,
            )
        },
        leadingIcon = {
            AnimatedVisibility(visible = selected || !expanded) {
                Icon(
                    modifier = Modifier
                        .addIf(!expanded) {
                            padding(12.dp)
                        },
                    imageVector = icon,
                    contentDescription = label,
                )
            }
        }
    )
}
