package com.machiav3lli.fdroid.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.ui.components.privacy.PrivacyItemHeader
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CaretDownUp
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CaretUpDown

@Composable
fun ExpandableBlock(
    modifier: Modifier = Modifier,
    heading: String? = null,
    preExpanded: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(preExpanded) }
    val surfaceColor by animateColorAsState(
        targetValue = if (expanded && heading != null) MaterialTheme.colorScheme.surfaceContainer
        else MaterialTheme.colorScheme.surfaceContainerLowest,
        label = "surfaceColor"
    )

    Surface(
        modifier = Modifier.animateContentSize(),
        shape = MaterialTheme.shapes.extraLarge,
        onClick = { expanded = !expanded },
        color = surfaceColor
    ) {
        Column {
            ExpandableBlockHeader(heading, expanded)
            AnimatedVisibility(visible = expanded) {
                Column(modifier.padding(bottom = 8.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
fun ExpandableItemsBlock(
    modifier: Modifier = Modifier,
    heading: String? = null,
    icon: ImageVector? = null,
    preExpanded: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(preExpanded) }
    val surfaceColor by animateColorAsState(
        targetValue = if (expanded) MaterialTheme.colorScheme.surfaceContainerHigh
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
            PrivacyItemHeader(heading, icon, expanded)
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
fun ExpandableBlockHeader(
    heading: String? = null,
    expanded: Boolean = false,
    positive: Boolean = true,
) {
    if (heading != null) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CompositionLocalProvider(
                LocalContentColor provides if (positive) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = heading,
                    style = MaterialTheme.typography.titleSmall,
                )
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = if (expanded) Phosphor.CaretDownUp
                    else Phosphor.CaretUpDown,
                    contentDescription = heading
                )
            }
        }
    }
}