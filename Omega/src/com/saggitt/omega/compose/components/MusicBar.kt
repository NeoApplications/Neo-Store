package com.saggitt.omega.compose.components

import android.media.AudioManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.saggitt.omega.compose.objects.MusicControlItem

// Make the each button a ControlDashProvider??
@Composable
fun MusicBar(
    ratio: Float,
    audioManager: AudioManager
) {
    val (playing, play) = remember {
        mutableStateOf(audioManager.isMusicActive)
    }

    Row(
        modifier = Modifier
            .background(Color.Transparent, MaterialTheme.shapes.medium),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        listOf(
            MusicControlItem.PREVIOUS,
            if (playing) MusicControlItem.PAUSE else MusicControlItem.PLAY,
            MusicControlItem.NEXT
        ).forEach {
            FilledTonalIconButton(
                modifier = Modifier
                    .padding(4.dp)
                    .weight(1f)
                    .aspectRatio(ratio / 2.8f),
                shape = MaterialTheme.shapes.medium,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                onClick = {
                    it.onClick(audioManager)
                    play(it != MusicControlItem.PAUSE)
                }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        modifier = Modifier
                            .fillMaxHeight(0.5f)
                            .aspectRatio(1f),
                        painter = painterResource(id = it.icon),
                        contentDescription = stringResource(id = it.description)
                    )
                }
            }
        }
    }
}