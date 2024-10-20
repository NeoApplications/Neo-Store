package com.machiav3lli.fdroid.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Check
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CheckCircle
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Circle
import com.machiav3lli.fdroid.ui.compose.utils.addIf

private enum class SelectionState { Unselected, Selected }

private class CategoryChipTransition(
    cornerRadius: State<Dp>,
) {
    val cornerRadius by cornerRadius
}

@Composable
private fun categoryChipTransition(selected: Boolean): CategoryChipTransition {
    val transition = updateTransition(
        targetState = if (selected) SelectionState.Selected else SelectionState.Unselected,
        label = "chip_transition"
    )
    val corerRadius = transition.animateDp(label = "chip_corner") { state ->
        when (state) {
            SelectionState.Unselected -> 8.dp
            SelectionState.Selected   -> 16.dp
        }
    }
    return remember(transition) {
        CategoryChipTransition(corerRadius)
    }
}

@Composable
fun InfoChip(
    modifier: Modifier = Modifier,
    text: String,
    colors: SelectableChipColors = FilterChipDefaults.filterChipColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        labelColor = MaterialTheme.colorScheme.onSurface,
        iconColor = MaterialTheme.colorScheme.onSurface,
        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ),
    onSelected: () -> Unit = {},
) {
    FilterChip(
        modifier = modifier,
        colors = colors,
        shape = MaterialTheme.shapes.small,
        border = null,
        selected = false,
        onClick = { onSelected() },
        label = {
            Text(text = text)
        }
    )
}

@Composable
fun SelectChip(
    modifier: Modifier = Modifier,
    text: String,
    checked: Boolean = false,
    colors: SelectableChipColors = FilterChipDefaults.filterChipColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        labelColor = MaterialTheme.colorScheme.onSurface,
        iconColor = MaterialTheme.colorScheme.onSurface,
        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ),
    alwaysShowIcon: Boolean = true,
    onSelected: () -> Unit = {},
) {
    val categoryChipTransitionState = categoryChipTransition(selected = checked)
    val icon by remember(checked) {
        mutableStateOf(
            if (checked) Phosphor.CheckCircle
            else Phosphor.Circle
        )
    }

    FilterChip(
        modifier = modifier,
        colors = colors,
        shape = RoundedCornerShape(categoryChipTransitionState.cornerRadius),
        border = null,
        selected = checked,
        leadingIcon = {
            if (alwaysShowIcon) Icon(
                imageVector = icon,
                contentDescription = null,
            )
            else AnimatedVisibility(
                visible = checked,
                enter = scaleIn(),
                exit = scaleOut(),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                )
            }
        },
        onClick = { onSelected() },
        label = {
            Text(text = text)
        }
    )
}

@Composable
fun CheckChip(
    modifier: Modifier = Modifier,
    text: String,
    icon: ImageVector = Phosphor.Check,
    checked: Boolean = false,
    colors: SelectableChipColors = FilterChipDefaults.filterChipColors(
        labelColor = MaterialTheme.colorScheme.onBackground,
        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
        iconColor = MaterialTheme.colorScheme.onBackground,
        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
        containerColor = Color.Transparent,
        selectedContainerColor = MaterialTheme.colorScheme.primary
    ),
    fullWidth: Boolean,
    onSelected: () -> Unit = {},
) {
    val categoryChipTransitionState = categoryChipTransition(selected = checked)

    FilterChip(
        modifier = modifier,
        colors = colors,
        shape = RoundedCornerShape(categoryChipTransitionState.cornerRadius),
        selected = checked,
        leadingIcon = {
            AnimatedVisibility(
                visible = checked,
                enter = scaleIn(),
                exit = scaleOut(),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                )
            }
        },
        onClick = { onSelected() },
        label = {
            Text(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .addIf(fullWidth) {
                        fillMaxWidth()
                    },
                text = text,
                textAlign = TextAlign.Center,
            )
        }
    )
}

@Composable
fun ChipsSwitch(
    firstTextId: Int,
    firstIcon: ImageVector,
    secondTextId: Int,
    secondIcon: ImageVector,
    firstSelected: Boolean = true,
    colors: SelectableChipColors = FilterChipDefaults.filterChipColors(
        containerColor = Color.Transparent,
        labelColor = MaterialTheme.colorScheme.onSurface,
        iconColor = MaterialTheme.colorScheme.onSurface,
        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ),
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.surfaceContainerLowest,
                MaterialTheme.shapes.medium
            )
            .padding(horizontal = 6.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val (firstSelected, selectFirst) = remember { mutableStateOf(firstSelected) }

        FilterChip(
            modifier = Modifier.weight(1f),
            shape = MaterialTheme.shapes.small,
            border = null,
            selected = firstSelected,
            colors = colors,
            onClick = {
                onCheckedChange(true)
                selectFirst(true)
            },
            leadingIcon = {
                Icon(
                    imageVector = firstIcon,
                    contentDescription = stringResource(id = firstTextId)
                )
            },
            label = {
                Text(
                    text = stringResource(id = firstTextId),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 8.dp),
                )
            }
        )
        FilterChip(
            modifier = Modifier.weight(1f),
            shape = MaterialTheme.shapes.small,
            border = null,
            selected = !firstSelected,
            colors = colors,
            onClick = {
                onCheckedChange(false)
                selectFirst(false)
            },
            label = {
                Text(
                    text = stringResource(id = secondTextId),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 8.dp),
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = secondIcon,
                    contentDescription = stringResource(id = secondTextId)
                )
            }
        )
    }
}
