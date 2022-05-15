package com.saggitt.omega.compose.objects

import android.media.AudioManager
import android.view.KeyEvent
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.android.launcher3.R

class MusicControlItem(
    @DrawableRes val icon: Int,
    @StringRes val description: Int,
    val onClick: (AudioManager) -> Unit
) {
    // TODO fix descriptions
    companion object {
        val PLAY = MusicControlItem(
            R.drawable.ic_music_play,
            R.string.dash_media_player
        ) { musicManager ->
            musicManager.dispatchMediaKeyEvent(
                KeyEvent(
                    KeyEvent.ACTION_DOWN,
                    KeyEvent.KEYCODE_MEDIA_PLAY
                )
            )
            musicManager.dispatchMediaKeyEvent(
                KeyEvent(
                    KeyEvent.ACTION_UP,
                    KeyEvent.KEYCODE_MEDIA_PLAY
                )
            )
        }

        val PAUSE = MusicControlItem(
            R.drawable.ic_music_pause,
            R.string.dash_media_player
        ) { musicManager ->
            musicManager.dispatchMediaKeyEvent(
                KeyEvent(
                    KeyEvent.ACTION_DOWN,
                    KeyEvent.KEYCODE_MEDIA_PAUSE
                )
            )
            musicManager.dispatchMediaKeyEvent(
                KeyEvent(
                    KeyEvent.ACTION_UP,
                    KeyEvent.KEYCODE_MEDIA_PAUSE
                )
            )
        }

        val PREVIOUS = MusicControlItem(
            R.drawable.ic_music_previous,
            R.string.dash_media_player
        ) { musicManager ->
            musicManager.dispatchMediaKeyEvent(
                KeyEvent(
                    KeyEvent.ACTION_DOWN,
                    KeyEvent.KEYCODE_MEDIA_PREVIOUS
                )
            )
            musicManager.dispatchMediaKeyEvent(
                KeyEvent(
                    KeyEvent.ACTION_UP,
                    KeyEvent.KEYCODE_MEDIA_PREVIOUS
                )
            )
        }

        val NEXT = MusicControlItem(
            R.drawable.ic_music_next,
            R.string.dash_media_player
        ) { musicManager ->
            musicManager.dispatchMediaKeyEvent(
                KeyEvent(
                    KeyEvent.ACTION_DOWN,
                    KeyEvent.KEYCODE_MEDIA_NEXT
                )
            )
            musicManager.dispatchMediaKeyEvent(
                KeyEvent(
                    KeyEvent.ACTION_UP,
                    KeyEvent.KEYCODE_MEDIA_NEXT
                )
            )
        }
    }
}
