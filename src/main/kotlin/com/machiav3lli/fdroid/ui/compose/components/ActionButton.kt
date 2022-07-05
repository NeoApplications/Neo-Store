package com.machiav3lli.fdroid.ui.compose.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.entity.ActionState
import com.machiav3lli.fdroid.entity.ComponentState

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
                is ActionState.Cancel -> MaterialTheme.colorScheme.secondaryContainer
                is ActionState.NoAction -> MaterialTheme.colorScheme.inverseSurface
                else -> MaterialTheme.colorScheme.surface
            },
            contentColor = when (actionState) {
                is ActionState.Cancel -> MaterialTheme.colorScheme.secondary
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
                Modifier
                    .defaultMinSize(minHeight = ButtonDefaults.MinHeight)
                    .padding(ButtonDefaults.ContentPadding),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = it.icon, contentDescription = null)
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
            onClick = { onClick() }
        ) {
            Row(
                Modifier
                    .defaultMinSize(minHeight = ButtonDefaults.MinHeight)
                    .padding(
                        vertical = ButtonDefaults.ContentPadding.calculateTopPadding(),
                        horizontal = 0.dp
                    ),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = it.icon, contentDescription = null)
            }
        }
    }
}