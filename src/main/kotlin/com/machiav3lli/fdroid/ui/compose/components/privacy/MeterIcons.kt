package com.machiav3lli.fdroid.ui.compose.components.privacy

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CircleWavyQuestion
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CrosshairSimple
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Nut
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ShieldStar
import com.machiav3lli.fdroid.ui.compose.theme.LightGreen
import com.machiav3lli.fdroid.ui.compose.theme.Orange

@Composable
fun MeterIconsBar(
    modifier: Modifier = Modifier,
    selectedTrackers: Int? = null,
    selectedPermissions: Int? = null,
    onClick: () -> Unit = {},
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .background(
                    MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp),
                    MaterialTheme.shapes.medium,
                )
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Phosphor.CrosshairSimple,
                contentDescription = stringResource(id = R.string.trackers)
            )
            MeterIcon(modifier = Modifier.weight(1f), selected = selectedTrackers)
            Icon(
                imageVector = Phosphor.ShieldStar,
                contentDescription = stringResource(id = R.string.permissions)
            )
            MeterIcon(modifier = Modifier.weight(1f), selected = selectedPermissions)
        }
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Phosphor.Nut,
                contentDescription = ""
            )
        }
    }
}

@Composable
fun MeterIcon(
    modifier: Modifier = Modifier,
    selected: Int? = 0,
) {
    val colors = listOf(Color.Red, Orange, Color.Yellow, LightGreen, Color.Green)

    Row(
        modifier = modifier.clip(MaterialTheme.shapes.extraSmall),
    ) {
        colors.forEachIndexed { index, color ->
            val isSelected = selected == index
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(32.dp),
                color = if (isSelected) color else color.copy(alpha = 0.3f),
                shape = RoundedCornerShape(
                    topStart = if (index == 0) 8.dp else if (isSelected) 2.dp else 0.dp,
                    bottomStart = if (index == 0) 8.dp else if (isSelected) 2.dp else 0.dp,
                    topEnd = if (index == 4) 8.dp else if (isSelected) 2.dp else 0.dp,
                    bottomEnd = if (index == 4) 8.dp else if (isSelected) 2.dp else 0.dp,
                ),
                border = BorderStroke(
                    if (isSelected) 2.dp else 0.dp,
                    if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                ),
                content = {
                    if (index == 2 && selected == null) Icon(
                        imageVector = Phosphor.CircleWavyQuestion,
                        contentDescription = ""
                    )
                }
            )
        }
    }
}