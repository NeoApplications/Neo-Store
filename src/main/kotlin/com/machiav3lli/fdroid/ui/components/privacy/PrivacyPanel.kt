package com.machiav3lli.fdroid.ui.components.privacy

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.ui.components.ActionButton
import com.machiav3lli.fdroid.ui.components.ExpandableBlock
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CaretDown
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.SlidersHorizontal

@Composable
fun PrivacyCard(
    modifier: Modifier = Modifier,
    heading: String? = null,
    actionText: String = "",
    actionIcon: ImageVector = Phosphor.SlidersHorizontal,
    preExpanded: Boolean = false,
    onAction: () -> Unit = { },
    content: @Composable ColumnScope.() -> Unit,
) {
    ExpandableBlock(
        modifier = modifier.padding(horizontal = 8.dp),
        heading = heading,
        positive = true,
        preExpanded = preExpanded
    ) {
        content()
        if (actionText.isNotEmpty()) Row(
            modifier = Modifier.padding(
                top = 8.dp,
                start = 8.dp,
                end = 8.dp,
            ),
        ) {
            ActionButton(
                modifier = Modifier.weight(1f),
                text = actionText,
                icon = actionIcon,
                positive = true,
                onClick = onAction
            )
        }
    }
}

@Composable
fun PrivacyItem(
    modifier: Modifier = Modifier,
    title: String? = null,
    icon: ImageVector,
) {
    Surface(
        modifier = Modifier.animateContentSize(),
        shape = MaterialTheme.shapes.large,
        color = Color.Transparent
    ) {
        Column(modifier = modifier) {
            PrivacyItemHeader(title, icon, false)
        }
    }
}

@Composable
fun PrivacyItemBlock(
    modifier: Modifier = Modifier,
    heading: String? = null,
    icon: ImageVector? = null,
    preExpanded: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(preExpanded) }
    val surfaceColor by animateColorAsState(
        targetValue = if (expanded) MaterialTheme.colorScheme.surfaceColorAtElevation(128.dp)
        else Color.Transparent,
        label = "surfaceColor"
    )
    val expandedPadding by animateDpAsState(
        targetValue = if (expanded) 2.dp else 0.dp,
        label = "expandedPadding"
    )

    Surface(
        modifier = Modifier
            .animateContentSize()
            .padding(vertical = expandedPadding),
        shape = MaterialTheme.shapes.large,
        onClick = { expanded = !expanded },
        color = surfaceColor
    ) {
        Column(modifier = modifier) {
            PrivacyItemHeader(heading, icon)
            AnimatedVisibility(visible = expanded) {
                Column(
                    Modifier.padding(
                        bottom = 16.dp,
                        start = 8.dp,
                        end = 8.dp,
                    )
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
fun PrivacyItemHeader(
    heading: String? = null,
    icon: ImageVector? = null,
    withIcon: Boolean = true,
) {
    var spacerHeight = 0
    if (heading == null) spacerHeight += 8
    Spacer(modifier = Modifier.requiredHeight(spacerHeight.dp))
    if (heading != null) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let {
                Icon(
                    imageVector = icon,
                    contentDescription = heading
                )
            }
            Text(
                modifier = Modifier.weight(1f),
                text = heading,
                style = MaterialTheme.typography.titleSmall,
            )
            if (withIcon) Icon(
                modifier = Modifier.size(24.dp),
                imageVector = Phosphor.CaretDown,
                contentDescription = heading
            )
        }
    }
}