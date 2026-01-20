package com.machiav3lli.fdroid.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.SingleChoiceSegmentedButtonRowScope
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

// TODO replace usage with respective Chip
@Composable
fun TabButton(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    colors: SelectableChipColors = FilterChipDefaults.filterChipColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        labelColor = MaterialTheme.colorScheme.onSurface,
        iconColor = MaterialTheme.colorScheme.onSurface,
        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ),
    onClick: () -> Unit,
) {
    FilterChip(
        modifier = modifier,
        colors = colors,
        shape = MaterialTheme.shapes.extraLarge,
        selected = selected,
        border = null,
        leadingIcon = if (icon != null) @Composable {
            {
                Icon(
                    modifier = Modifier.size(16.dp),
                    imageVector = icon,
                    contentDescription = text,
                )
            }
        } else null,
        onClick = onClick,
        label = {
            Text(text = text)
        }
    )
}

@Composable
fun SingleChoiceSegmentedButtonRowScope.SegmentedTabButton(
    text: String,
    icon: ImageVector,
    selected: () -> Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    SegmentedButton(
        modifier = modifier,
        selected = selected(),
        onClick = onClick,
        border = BorderStroke(0.dp, Color.Transparent),
        colors = SegmentedButtonDefaults.colors(
            activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
            activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            inactiveContainerColor = Color.Transparent,
            inactiveContentColor = MaterialTheme.colorScheme.onBackground,
        ),
        shape = MaterialTheme.shapes.extraLarge,
        icon = {
            Icon(imageVector = icon, contentDescription = text)
        }
    ) {
        Text(text = text)
    }
}

@Composable
fun itemShape(index: Int, count: Int, selected: () -> Boolean): Shape {
    if (count == 1 || selected()) return MaterialTheme.shapes.extraLarge

    return when (index) {
        0 -> MaterialTheme.shapes.extraLarge.copy(
            topEnd = CornerSize(4.dp),
            bottomEnd = CornerSize(4.dp)
        )

        count - 1 -> MaterialTheme.shapes.extraLarge.copy(
            topStart = CornerSize(4.dp),
            bottomStart = CornerSize(4.dp)
        )

        else -> MaterialTheme.shapes.extraSmall
    }
}

@Composable
fun TabIndicator(tabPosition: TabPosition) {
    Box(
        Modifier
            .tabIndicatorOffset(tabPosition)
            .fillMaxWidth()
            .height(8.dp)
            .padding(vertical = 2.dp, horizontal = 4.dp)
            .background(
                color = MaterialTheme.colorScheme.primary,
                shape = MaterialTheme.shapes.large
            )
    )
}
