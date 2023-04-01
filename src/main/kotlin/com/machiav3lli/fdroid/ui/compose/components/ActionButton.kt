package com.machiav3lli.fdroid.ui.compose.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.entity.ActionState
import com.machiav3lli.fdroid.entity.ComponentState

@Composable
fun ActionButton(
    modifier: Modifier = Modifier,
    text: String,
    positive: Boolean = true,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    ElevatedButton(
        modifier = modifier,
        colors = ButtonDefaults.elevatedButtonColors(
            contentColor = when {
                positive -> MaterialTheme.colorScheme.onPrimaryContainer
                else -> MaterialTheme.colorScheme.onTertiaryContainer
            },
            containerColor = when {
                positive -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.tertiaryContainer
            }
        ),
        enabled = enabled,
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

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainActionButton(
    modifier: Modifier = Modifier,
    actionState: ActionState,
    onClick: () -> Unit
) {

    ElevatedButton(
        modifier = modifier,
        onClick = {
            onClick()
        },
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = when (actionState) {
                is ActionState.Cancel -> MaterialTheme.colorScheme.tertiaryContainer
                is ActionState.NoAction -> MaterialTheme.colorScheme.inverseSurface
                else -> MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
            },
            contentColor = when (actionState) {
                is ActionState.Cancel -> MaterialTheme.colorScheme.onTertiaryContainer
                is ActionState.NoAction -> MaterialTheme.colorScheme.inverseOnSurface
                else -> MaterialTheme.colorScheme.primary
            },
        )
    ) {
        AnimatedContent(
            targetState = actionState,
            transitionSpec = {
                when (targetState) {
                    is ActionState.Cancel ->
                        (slideInVertically { height -> height } + fadeIn() with
                                slideOutVertically { height -> -height } + fadeOut())
                    else ->
                        (slideInVertically { height -> -height } + fadeIn() with
                                slideOutVertically { height -> height } + fadeOut())
                }
                    .using(SizeTransform(clip = false))
            }
        ) {
            Row(
                Modifier.defaultMinSize(minHeight = ButtonDefaults.MinHeight),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = it.icon, contentDescription = stringResource(id = it.textId))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(id = it.textId))
            }
        }
    }
}

@Composable
fun SecondaryActionButton(
    modifier: Modifier = Modifier,
    packageState: ComponentState?,
    onClick: () -> Unit
) {
    packageState?.let {
        ElevatedButton(
            modifier = modifier,
            colors = ButtonDefaults.elevatedButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                contentColor = MaterialTheme.colorScheme.primary
            ),
            onClick = { onClick() }
        ) {
            Row(
                Modifier.defaultMinSize(minHeight = ButtonDefaults.MinHeight),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = it.icon, contentDescription = stringResource(id = it.textId))
            }
        }
    }
}