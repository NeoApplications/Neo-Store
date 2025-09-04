package com.machiav3lli.fdroid.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.R

@Composable
fun SwitchPreference(
    modifier: Modifier = Modifier,
    text: String,
    initSelected: () -> Boolean,
    onCheckedChanged: (Boolean) -> Unit,
) {
    Row(
        modifier = modifier
            .padding(horizontal = 6.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val (selected, select) = remember(initSelected()) { mutableStateOf(initSelected()) }
        Text(
            modifier = Modifier.weight(1f, true),
            text = text,
        )
        Switch(
            checked = selected,
            colors = SwitchDefaults.colors(uncheckedBorderColor = Color.Transparent),
            onCheckedChange = {
                select(it)
                onCheckedChanged(it)
            }
        )
    }
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
