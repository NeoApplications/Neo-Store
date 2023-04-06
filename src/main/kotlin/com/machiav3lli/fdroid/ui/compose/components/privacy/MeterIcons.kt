package com.machiav3lli.fdroid.ui.compose.components.privacy

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.ui.compose.components.Tooltip
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ArrowCircleLeft
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ArrowCircleRight
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CircleWavyQuestion
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CrosshairSimple
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ShieldStar
import com.machiav3lli.fdroid.ui.compose.theme.LightGreen
import com.machiav3lli.fdroid.ui.compose.theme.Orange

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MeterIconsBar(
    modifier: Modifier = Modifier,
    selectedTrackers: Int? = null,
    selectedPermissions: Int? = null,
    pagerState: PagerState,
    onClick: () -> Unit = {},
) {
    Row(
        modifier = modifier.padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .background(
                    MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp),
                    MaterialTheme.shapes.medium,
                )
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (Preferences[Preferences.Key.ShowTrackers]) {
                Icon(
                    imageVector = Phosphor.CrosshairSimple,
                    contentDescription = stringResource(id = R.string.trackers)
                )
                MeterIcon(
                    modifier = Modifier.weight(1f),
                    selected = selectedTrackers?.coerceIn(0, 4),
                    tooltips = (0..4).map {
                        stringResource(
                            getTrackersTooltip(it)
                        )
                    }
                )
            }
            Icon(
                imageVector = Phosphor.ShieldStar,
                contentDescription = stringResource(id = R.string.permissions)
            )
            MeterIcon(
                modifier = Modifier.weight(1f),
                selected = selectedPermissions?.coerceIn(0, 4),
                tooltips = (0..4).map { stringResource(getPermissionsTooltip(it)) }
            )
        }
        IconButton(onClick = onClick) {
            Icon(
                imageVector = if (pagerState.currentPage == 0) Phosphor.ArrowCircleRight
                else Phosphor.ArrowCircleLeft,
                tint = MaterialTheme.colorScheme.primary,
                contentDescription = stringResource(id = R.string.privacy_panel)
            )
        }
    }
}

@Composable
fun MeterIcon(
    modifier: Modifier = Modifier,
    selected: Int? = 0,
    tooltips: List<String> = listOf("", "", "", "", ""),
) {
    val colors = listOf(Color.Red, Orange, Color.Yellow, LightGreen, Color.Green)
    val openPopup = remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.extraSmall)
            .clickable { openPopup.value = true },
    ) {
        colors.forEachIndexed { index, color ->
            val isSelected = selected == index
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(24.dp),
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

            if (isSelected && openPopup.value) {
                Tooltip(tooltips[index], openPopup)
            } else if (index == 2 && selected == null && openPopup.value) {
                Tooltip(stringResource(id = R.string.no_trackers_data_available), openPopup)
            }
        }
    }
}

fun getTrackersTooltip(note: Int) = when (note) {
    1    -> R.string.trackers_note_1
    2    -> R.string.trackers_note_2
    3    -> R.string.trackers_note_3
    4    -> R.string.trackers_note_4
    else -> R.string.trackers_note_0
}

fun getPermissionsTooltip(note: Int) = when (note) {
    1    -> R.string.permissions_note_1
    2    -> R.string.permissions_note_2
    3    -> R.string.permissions_note_3
    4    -> R.string.permissions_note_4
    else -> R.string.permissions_note_0
}
