package com.machiav3lli.fdroid.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRowScope
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun TabButton(
    modifier: Modifier = Modifier,
    text: String,
    icon: ImageVector? = null,
    onClick: () -> Unit,
) {
    TextButton(
        modifier = modifier,
        shape = RectangleShape,
        colors = ButtonDefaults.textButtonColors(
            containerColor = Color.Transparent,
        ),
        onClick = onClick
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) Icon(imageVector = icon, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = text)
        }
    }
}

@Composable
fun SingleChoiceSegmentedButtonRowScope.SegmentedTabButton(
    text: String,
    icon: ImageVector,
    index: Int,
    count: Int,
    selected: () -> Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    SegmentedButton(
        modifier = modifier,
        selected = selected(),
        onClick = onClick,
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.surfaceContainerHighest),
        colors = SegmentedButtonDefaults.colors(
            activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
            activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            inactiveContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            inactiveContentColor = MaterialTheme.colorScheme.onSurface,
        ),
        shape = itemShape(index, count, selected),
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
