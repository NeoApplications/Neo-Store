package com.machiav3lli.fdroid.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ArrowsClockwise

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RoundButton(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    description: String = "",
    onLongClick: (() -> Unit) = {},
    onClick: (() -> Unit),
) {
    Surface(
        modifier = modifier
            .clip(CircleShape)
            .combinedClickable(role = Role.Button, onClick = onClick, onLongClick = onLongClick)
            .padding(8.dp),
        shape = CircleShape,
        color = Color.Transparent,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            modifier = Modifier.size(size)
        )
    }
}

@Composable
fun FilledRoundButton(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = MaterialTheme.colorScheme.primary,
    onTint: Color = MaterialTheme.colorScheme.onPrimary,
    description: String = "",
    onClick: () -> Unit,
) {
    FilledTonalIconButton(
        modifier = modifier,
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = tint,
            contentColor = onTint,
        ),
        onClick = onClick,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            modifier = Modifier.size(size)
        )
    }
}

@Composable
fun SyncButton(
    modifier: Modifier = Modifier,
    isSyncing: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
) {
    val angle by animateFloatAsState(
        if (isSyncing) {
            val infiniteTransition = rememberInfiniteTransition(label = "infiniteTransition")

            val animationProgress by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 20f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 50000,
                        easing = LinearEasing
                    )
                ), label = "animationProgress"
            )
            val angle = 360f * animationProgress
            angle
        } else 0f, label = "iconAngle"
    )

    RoundButton(
        modifier = modifier
            .graphicsLayer { rotationZ = angle },
        icon = Phosphor.ArrowsClockwise,
        description = stringResource(id = R.string.sync_repositories),
        onLongClick = onLongClick,
        onClick = onClick
    )
}