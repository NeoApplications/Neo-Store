package com.machiav3lli.fdroid.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Check
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CheckCircle
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Circle
import com.machiav3lli.fdroid.ui.compose.utils.addIf
import com.machiav3lli.fdroid.utils.extension.android.launchView

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
            SelectionState.Unselected -> 4.dp
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
        selected = text.contains("→"),
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
    onCheckedChange: (Boolean) -> Unit,
) {
    val (firstSelected, selectFirst) = remember { mutableStateOf(firstSelected) }

    SingleChoiceSegmentedButtonRow(
        modifier = Modifier
            .fillMaxWidth(),
        space = 24.dp,
    ) {
        SegmentedTabButton(
            text = stringResource(id = firstTextId),
            icon = firstIcon,
            selected = { firstSelected },
            index = 0,
            count = 2,
            onClick = {
                onCheckedChange(true)
                selectFirst(true)
            }
        )
        SegmentedTabButton(
            text = stringResource(id = secondTextId),
            icon = secondIcon,
            selected = { !firstSelected },
            index = 1,
            count = 2,
            onClick = {
                onCheckedChange(false)
                selectFirst(false)
            }
        )
    }
}

@Composable
fun LinkChip(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    url: String,
) {
    val context = LocalContext.current

    AssistChip(
        modifier = modifier,
        border = null,
        shape = MaterialTheme.shapes.medium,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        onClick = {
            context.launchView(url)
        }
    )
}