package com.saggitt.omega.compose.components

import android.media.AudioManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
            CardButton(
                modifier = Modifier.weight(1f),
                icon = painterResource(id = it.icon),
                description = stringResource(id = it.description),
                ratio = ratio / 2.8f,
                onClick = {
                    it.onClick(audioManager)
                    play(it != MusicControlItem.PAUSE)
                })
        }
    }
}