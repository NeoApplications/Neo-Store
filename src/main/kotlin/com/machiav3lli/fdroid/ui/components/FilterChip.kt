package com.machiav3lli.fdroid.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CheckCircle
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Circle
import com.machiav3lli.fdroid.ui.compose.utils.addIf
import com.machiav3lli.fdroid.utils.extension.android.launchView

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
    icon: ImageVector? = null,
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
    val selectionCornerRadius by animateDpAsState(
        when {
            checked -> 4.dp
            else    -> 16.dp
        }
    )
    val icon by remember(checked) {
        mutableStateOf(
            icon
                ?: if (checked) Phosphor.CheckCircle
                else Phosphor.Circle
        )
    }

    FilterChip(
        modifier = modifier,
        colors = colors,
        shape = RoundedCornerShape(selectionCornerRadius),
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
    val selectionCornerRadius by animateDpAsState(
        when {
            checked -> 4.dp
            else    -> 28.dp
        }
    )

    FilterChip(
        modifier = modifier,
        colors = colors,
        shape = RoundedCornerShape(selectionCornerRadius),
        selected = checked,
        leadingIcon = {
            Icon(
                imageVector = when {
                    checked -> Phosphor.CheckCircle
                    else    -> Phosphor.Circle
                },
                contentDescription = null,
            )
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
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = MaterialTheme.shapes.extraLarge,
            )
            .padding(horizontal = 4.dp)
            .fillMaxWidth(),
    ) {
        SegmentedTabButton(
            text = stringResource(id = firstTextId),
            icon = firstIcon,
            selected = { firstSelected },
            onClick = {
                onCheckedChange(true)
                selectFirst(true)
            }
        )
        SegmentedTabButton(
            text = stringResource(id = secondTextId),
            icon = secondIcon,
            selected = { !firstSelected },
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
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
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