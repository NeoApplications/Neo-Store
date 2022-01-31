/*
 *  This file is part of Omega Launcher
 *  Copyright (c) 2021   Saul Henriquez
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saggitt.omega.dash

import android.content.Context
import android.media.AudioManager
import android.view.KeyEvent
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.launcher3.Launcher
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.saggitt.omega.util.tintDrawable
import com.saggitt.omega.views.BaseBottomSheet

class DashBottomSheet(context: Context) : RelativeLayout(context) {
    private var controlFastAdapter: FastAdapter<DashControlItem>? = null
    private var dashActionFastAdapter: FastAdapter<DashActionItem>? = null
    private val controlItemAdapter = ItemAdapter<DashControlItem>()
    private val dashItemAdapter = ItemAdapter<DashActionItem>()
    private val prefs = Utilities.getOmegaPrefs(context)
    private val allActionItems = DashEditAdapter.getDashActionProviders(context)
    private val allControlItems = DashEditAdapter.getDashControlProviders(context)
    private val musicManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    init {
        View.inflate(context, R.layout.dash_view, this)
        val activeDashProviders = prefs.dashProviders.getAll()

        controlFastAdapter = FastAdapter.with(controlItemAdapter)
        controlFastAdapter?.setHasStableIds(true)
        dashActionFastAdapter = FastAdapter.with(dashItemAdapter)
        dashActionFastAdapter?.setHasStableIds(true)

        findViewById<RecyclerView>(R.id.dash_control_recycler).apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = controlFastAdapter
        }
        findViewById<RecyclerView>(R.id.dash_action_recycler).apply {
            // TODO add option to select between 4/6
            layoutManager = GridLayoutManager(context, 6)
            adapter = dashActionFastAdapter
        }
        val controlItems = activeDashProviders
            .mapNotNull { itemId ->
                allControlItems.find { it.itemId.toString() == itemId }?.let {
                    DashControlItem(context, it)
                }
            }

        controlItemAdapter.set(controlItems)
        val actionMediaPlayer = resources.getString(R.string.dash_media_player)

        val dashItems = activeDashProviders
            .mapNotNull { itemId ->
                allActionItems.find {
                    it.itemId.toString() == itemId && it.itemId != 2
                }?.let {
                    DashActionItem(context, it)
                }
            }

        dashItemAdapter.set(dashItems)

        val musicPlay = findViewById<AppCompatImageView>(R.id.musicPlay).apply {
            setImageResource(if (musicManager.isMusicActive) R.drawable.ic_music_pause else R.drawable.ic_music_play)
            setOnClickListener {
                if (musicManager.isMusicActive) {
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
                    setImageResource(R.drawable.ic_music_play)
                } else {
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
                    setImageResource(R.drawable.ic_music_pause)
                }
                tintDrawable(prefs.accentColor)
            }
            tintDrawable(prefs.accentColor)
        }
        findViewById<AppCompatImageView>(R.id.musicPrev).apply {
            setOnClickListener {
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
                musicPlay.setImageResource(R.drawable.ic_music_pause)
                musicPlay.tintDrawable(prefs.accentColor)
            }
            tintDrawable(prefs.accentColor)
        }
        findViewById<AppCompatImageView>(R.id.musicNext).apply {
            setOnClickListener {
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
                musicPlay.setImageResource(R.drawable.ic_music_pause)
                musicPlay.tintDrawable(prefs.accentColor)
            }
            tintDrawable(prefs.accentColor)
        }

        val musicTab = findViewById<ConstraintLayout>(R.id.musicTab)

        if (activeDashProviders.contains("2")) {
            musicTab.visibility = View.VISIBLE
        } else {
            musicTab.visibility = View.GONE
        }
    }

    companion object {
        fun show(launcher: Launcher, animate: Boolean) {
            val sheet = BaseBottomSheet.inflate(launcher)
            val view = DashBottomSheet(launcher)
            sheet.show(view, animate)
        }
    }
}