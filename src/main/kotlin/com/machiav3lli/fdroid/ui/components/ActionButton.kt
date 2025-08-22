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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.machiav3lli.fdroid.data.entity.ComponentState

@Composable
fun ActionButton(
    modifier: Modifier = Modifier,
    text: String,
    // TODO add neutral using ENUM
    positive: Boolean = true,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    ElevatedButton(
        modifier = modifier,
        colors = ButtonDefaults.elevatedButtonColors(
            contentColor = when {
                positive -> MaterialTheme.colorScheme.onPrimary
                else     -> MaterialTheme.colorScheme.onTertiary
            },
            containerColor = when {
                positive -> MaterialTheme.colorScheme.primary
                else     -> MaterialTheme.colorScheme.tertiary
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
    // TODO add neutral using ENUM
    positive: Boolean = true,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    OutlinedButton(
        modifier = modifier,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = when {
                positive -> MaterialTheme.colorScheme.onPrimaryContainer
                else     -> MaterialTheme.colorScheme.onTertiaryContainer
            },
        ),
        border = BorderStroke(
            width = 1.dp,
            color = when {
                positive -> MaterialTheme.colorScheme.onPrimaryContainer
                else     -> MaterialTheme.colorScheme.onTertiaryContainer
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
    positive: Boolean = true,
    iconOnSide: Boolean = false,
    icon: ImageVector? = null,
    onClick: () -> Unit,
) {
    TextButton(
        modifier = modifier,
        colors = ButtonDefaults.textButtonColors(
            contentColor = if (positive) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.tertiary
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
                                    -> MaterialTheme.colorScheme.tertiary

            is ActionState.NoAction -> MaterialTheme.colorScheme.inverseSurface
            else                    -> MaterialTheme.colorScheme.primary
        }, label = "containerColor"
    )
    val contentColor by animateColorAsState(
        targetValue = when (actionState) {
            is ActionState.CancelPending,
            is ActionState.CancelConnecting,
            is ActionState.CancelDownloading,
                                    -> MaterialTheme.colorScheme.onTertiary

            is ActionState.NoAction -> MaterialTheme.colorScheme.inverseOnSurface
            else                    -> MaterialTheme.colorScheme.onPrimary
        }, label = "contentColor"
    )

    FloatingActionButton(
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
    FloatingActionButton(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = MaterialTheme.colorScheme.secondary,
        contentColor = MaterialTheme.colorScheme.onSecondary,
        elevation = FloatingActionButtonDefaults.elevation(0.dp),
        onClick = onClick
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description
        )
    }
}
