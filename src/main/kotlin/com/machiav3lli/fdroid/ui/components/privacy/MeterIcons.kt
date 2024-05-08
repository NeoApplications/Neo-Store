package com.machiav3lli.fdroid.ui.components.privacy

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
import com.machiav3lli.fdroid.ui.components.Tooltip
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ArrowCircleLeft
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ArrowCircleRight
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CircleWavyQuestion
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CrosshairSimple
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ShieldStar
import com.machiav3lli.fdroid.ui.compose.theme.LightGreen
import com.machiav3lli.fdroid.ui.compose.theme.Orange
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MeterIconsBar(
    modifier: Modifier = Modifier,
    selectedTrackers: Int? = null,
    selectedPermissions: Int? = null,
    currentPage: Int,
    onClick: () -> Unit = {},
) {
    val showTrackers by remember {
        derivedStateOf { Preferences[Preferences.Key.ShowTrackers] }
    }
    val trackersRank by remember(selectedTrackers) {
        derivedStateOf { selectedTrackers?.coerceIn(0, 4) }
    }
    val permissionsRank by remember(selectedPermissions) {
        derivedStateOf { selectedPermissions?.coerceIn(0, 4) }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (showTrackers) {
                Icon(
                    imageVector = Phosphor.CrosshairSimple,
                    contentDescription = stringResource(id = R.string.trackers)
                )
                MeterIcon(
                    modifier = Modifier.weight(1f),
                    selected = trackersRank,
                    tooltips = trackersTooltips,
                )
            }
            Icon(
                imageVector = Phosphor.ShieldStar,
                contentDescription = stringResource(id = R.string.permissions)
            )
            MeterIcon(
                modifier = Modifier.weight(1f),
                selected = permissionsRank,
                tooltips = permissionsTooltips
            )
        }
        IconButton(onClick = onClick) {
            Icon(
                imageVector = if (currentPage == 0) Phosphor.ArrowCircleRight
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
    tooltips: ImmutableList<Int> = persistentListOf(0, 0, 0, 0, 0),
) {
    val colors = persistentListOf(Color.Red, Orange, Color.Yellow, LightGreen, Color.Green)
    val openPopup = remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.extraSmall)
            .clickable { openPopup.value = true },
    ) {
        colors.forEachIndexed { index, color ->
            val isSelected by remember(selected) { derivedStateOf { selected == index } }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(28.dp),
                color = if (isSelected) color else color.copy(alpha = 0.3f),
                shape = RoundedCornerShape(
                    topStart = if (index == 0) 8.dp else if (isSelected) 2.dp else 0.dp,
                    bottomStart = if (index == 0) 8.dp else if (isSelected) 2.dp else 0.dp,
                    topEnd = if (index == 4) 8.dp else if (isSelected) 2.dp else 0.dp,
                    bottomEnd = if (index == 4) 8.dp else if (isSelected) 2.dp else 0.dp,
                ),
                border = BorderStroke(
                    if (isSelected) 2.dp else 0.dp,
                    if (isSelected) MaterialTheme.colorScheme.onBackground
                    else Color.Transparent,
                ),
                content = {
                    if (index == 2 && selected == null) Icon(
                        imageVector = Phosphor.CircleWavyQuestion,
                        contentDescription = ""
                    )
                }
            )

            if (isSelected && openPopup.value) {
                Tooltip(stringResource(id = tooltips[index]), openPopup)
            } else if (index == 2 && selected == null && openPopup.value) {
                Tooltip(stringResource(id = R.string.no_trackers_data_available), openPopup)
            }
        }
    }
}

val trackersTooltips: ImmutableList<Int> = persistentListOf(
    R.string.trackers_note_0,
    R.string.trackers_note_1,
    R.string.trackers_note_2,
    R.string.trackers_note_3,
    R.string.trackers_note_4,
)

val permissionsTooltips: ImmutableList<Int> = persistentListOf(
    R.string.permissions_note_0,
    R.string.permissions_note_1,
    R.string.permissions_note_2,
    R.string.permissions_note_3,
    R.string.permissions_note_4,
)
