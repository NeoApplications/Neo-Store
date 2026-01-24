package com.machiav3lli.fdroid.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.data.entity.ActionState
import com.machiav3lli.fdroid.data.entity.ColoringState
import com.machiav3lli.fdroid.data.entity.ComponentState

@Composable
fun ActionButton(
    modifier: Modifier = Modifier,
    text: String,
    coloring: ColoringState = ColoringState.Positive,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    FilledTonalButton(
        modifier = modifier,
        colors = ButtonDefaults.filledTonalButtonColors(
            contentColor = when (coloring) {
                ColoringState.Positive -> MaterialTheme.colorScheme.onPrimaryContainer
                ColoringState.Negative -> MaterialTheme.colorScheme.onTertiaryContainer
                ColoringState.Neutral  -> MaterialTheme.colorScheme.onSecondaryContainer
            },
            containerColor = when (coloring) {
                ColoringState.Positive -> MaterialTheme.colorScheme.primaryContainer
                ColoringState.Negative -> MaterialTheme.colorScheme.tertiaryContainer
                ColoringState.Neutral  -> MaterialTheme.colorScheme.secondaryContainer
            }
        ),
        enabled = enabled,
        onClick = onClick,
    ) {
        if (icon != null) Icon(imageVector = icon, contentDescription = text)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleSmall,
        )
    }
}

@Composable
fun OutlinedActionButton(
    modifier: Modifier = Modifier,
    text: String,
    coloring: ColoringState = ColoringState.Positive,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    OutlinedButton(
        modifier = modifier,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = when (coloring) {
                ColoringState.Positive -> MaterialTheme.colorScheme.primary
                ColoringState.Negative -> MaterialTheme.colorScheme.tertiary
                ColoringState.Neutral  -> MaterialTheme.colorScheme.secondary
            },
        ),
        border = BorderStroke(
            width = 1.dp,
            color = when (coloring) {
                ColoringState.Positive -> MaterialTheme.colorScheme.primary
                ColoringState.Negative -> MaterialTheme.colorScheme.tertiary
                ColoringState.Neutral  -> MaterialTheme.colorScheme.secondary
            },
        ),
        enabled = enabled,
        onClick = onClick,
    ) {
        if (icon != null) Icon(imageVector = icon, contentDescription = text)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleSmall,
        )
    }
}

@Composable
fun FlatActionButton(
    modifier: Modifier = Modifier,
    text: String,
    coloring: ColoringState = ColoringState.Positive,
    iconOnSide: Boolean = false,
    icon: ImageVector? = null,
    onClick: () -> Unit,
) {
    TextButton(
        modifier = modifier,
        colors = ButtonDefaults.textButtonColors(
            contentColor = when (coloring) {
                ColoringState.Positive -> MaterialTheme.colorScheme.primary
                ColoringState.Negative -> MaterialTheme.colorScheme.tertiary
                ColoringState.Neutral  -> MaterialTheme.colorScheme.secondary
            }
        ),
        onClick = onClick
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 4.dp),
            text = text,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleSmall
        )
        if (icon != null) {
            if (iconOnSide) Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = icon,
                contentDescription = text
            )
        }
    }
}

@Composable
fun MainActionButton(
    modifier: Modifier = Modifier,
    actionState: ActionState,
    onClick: () -> Unit,
) {
    val containerColor by animateColorAsState(
        targetValue = when (actionState) {
            is ActionState.CancelPending,
            is ActionState.CancelConnecting,
            is ActionState.CancelDownloading,
                                    -> MaterialTheme.colorScheme.tertiaryContainer

            is ActionState.NoAction -> MaterialTheme.colorScheme.inverseSurface
            else                    -> MaterialTheme.colorScheme.primaryContainer
        }, label = "containerColor"
    )
    val contentColor by animateColorAsState(
        targetValue = when (actionState) {
            is ActionState.CancelPending,
            is ActionState.CancelConnecting,
            is ActionState.CancelDownloading,
                                    -> MaterialTheme.colorScheme.onTertiaryContainer

            is ActionState.NoAction -> MaterialTheme.colorScheme.inverseOnSurface
            else                    -> MaterialTheme.colorScheme.onPrimaryContainer
        }, label = "contentColor"
    )

    ExtendedFloatingActionButton(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = containerColor,
        contentColor = contentColor,
        elevation = FloatingActionButtonDefaults.elevation(0.dp),
        onClick = onClick
    ) {
        AnimatedContent(
            targetState = actionState,
            transitionSpec = {
                when (targetState) {
                    is ActionState.CancelPending,
                    is ActionState.CancelConnecting,
                    is ActionState.CancelDownloading,
                         ->
                        ((slideInVertically { height -> height } + fadeIn()).togetherWith(
                            slideOutVertically { height -> -height } + fadeOut()))

                    else ->
                        ((slideInVertically { height -> -height } + fadeIn()).togetherWith(
                            slideOutVertically { height -> height } + fadeOut()))
                }
                    .using(SizeTransform(clip = false))
            },
            label = "actionState",
        ) {
            Row(
                Modifier.defaultMinSize(minHeight = ButtonDefaults.MinHeight),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = it.icon, contentDescription = stringResource(id = it.textId))
                Text(text = stringResource(id = it.textId))
            }
        }
    }
}

@Composable
fun SecondaryActionButton(
    modifier: Modifier = Modifier,
    packageState: ComponentState?,
    onClick: () -> Unit,
) {
    packageState?.let {
        SecondaryActionButton(
            modifier = modifier,
            icon = it.icon,
            description = stringResource(id = it.textId),
            onClick = onClick,
        )
    }
}

@Composable
fun SecondaryActionButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
) {
    OutlinedIconButton(
        modifier = modifier.size(56.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = IconButtonDefaults.outlinedIconButtonColors(
            contentColor = MaterialTheme.colorScheme.secondary,
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.secondary,
        ),
        onClick = onClick
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description
        )
    }
}
