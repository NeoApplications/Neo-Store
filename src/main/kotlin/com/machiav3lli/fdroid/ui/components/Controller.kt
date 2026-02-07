package com.machiav3lli.fdroid.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.machiav3lli.fdroid.R

@Composable
fun SwitchPreference(
    modifier: Modifier = Modifier,
    text: String,
    withContainer: Boolean = false,
    index: Int = 0,
    groupSize: Int = 1,
    initSelected: () -> Boolean,
    onCheckedChanged: (Boolean) -> Unit,
) {
    val (selected, select) = remember(initSelected()) { mutableStateOf(initSelected()) }
    val interactionSource = remember { MutableInteractionSource() }
    val base = index.toFloat() / groupSize
    val rank = (index + 1f) / groupSize

    ListItem(
        modifier = modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(
                    topStart = if (base == 0f) MaterialTheme.shapes.large.topStart
                    else MaterialTheme.shapes.extraSmall.topStart,
                    topEnd = if (base == 0f) MaterialTheme.shapes.large.topEnd
                    else MaterialTheme.shapes.extraSmall.topEnd,
                    bottomStart = if (rank == 1f) MaterialTheme.shapes.large.bottomStart
                    else MaterialTheme.shapes.extraSmall.bottomStart,
                    bottomEnd = if (rank == 1f) MaterialTheme.shapes.large.bottomEnd
                    else MaterialTheme.shapes.extraSmall.bottomEnd
                )
            )
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = {
                    select(!selected)
                    onCheckedChanged(!selected)
                }
            ),
        colors = ListItemDefaults.colors(
            containerColor = if (withContainer) MaterialTheme.colorScheme.surfaceContainer
            else Color.Transparent,
        ),
        headlineContent = {
            Text(
                text = text,
                maxLines = 2,
            )
        },
        trailingContent = {
            Switch(
                checked = selected,
                interactionSource = interactionSource,
                colors = SwitchDefaults.colors(uncheckedBorderColor = Color.Transparent),
                onCheckedChange = {
                    select(it)
                    onCheckedChanged(it)
                }
            )
        }
    )
}

@Composable
fun DeSelectAll(
    completeList: List<String>,
    selectedList: SnapshotStateList<String>,
) {
    if (completeList.size > 1) Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        AnimatedVisibility(
            visible = selectedList.isNotEmpty(),
            enter = fadeIn()
                    + slideInHorizontally { w -> -w }
                    + expandHorizontally(expandFrom = Alignment.Start, clip = false),
            exit = fadeOut()
                    + slideOutHorizontally { w -> -w }
                    + shrinkHorizontally(shrinkTowards = Alignment.Start, clip = false),
        ) {
            FilledTonalButton(onClick = { selectedList.clear() }) {
                Text(text = stringResource(id = R.string.select_all))
            }
        }
        AnimatedVisibility(
            visible = selectedList.size != completeList.size,
            enter = fadeIn()
                    + slideInHorizontally { w -> w }
                    + expandHorizontally(expandFrom = Alignment.End, clip = false),
            exit = fadeOut()
                    + slideOutHorizontally { w -> w }
                    + shrinkHorizontally(shrinkTowards = Alignment.End, clip = false),
        ) {
            FilledTonalButton(onClick = { selectedList.addAll(completeList - selectedList) }) {
                Text(text = stringResource(id = R.string.deselect_all))
            }
        }
    }
}
