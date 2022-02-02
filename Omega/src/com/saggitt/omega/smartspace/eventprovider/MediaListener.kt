/*
 *     This file is part of Lawnchair Launcher.
 *
 *     Lawnchair Launcher is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Lawnchair Launcher is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Lawnchair Launcher.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.saggitt.omega.smartspace.eventprovider

import android.app.Notification
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.service.notification.StatusBarNotification
import android.util.Log
import android.view.KeyEvent
import com.saggitt.omega.util.makeBasicHandler
import java.util.*

/**
 * Paused mode is not supported on Marshmallow because the MediaSession is missing
 * notifications. Without this information, it is impossible to hide on stop.
 */
class MediaListener internal constructor(
    private val mContext: Context,
    private val mOnChange: Runnable
) : MediaController.Callback(), NotificationsManager.OnChangeListener {
    private val mNotificationsManager: NotificationsManager = NotificationsManager
    private val mHandler = makeBasicHandler(true)
    var tracking: MediaNotificationController? = null
        private set
    private var mControllers: List<MediaNotificationController> = emptyList()
    fun onResume() {
        updateTracking()
        mNotificationsManager.addListener(this)
    }

    fun onPause() {
        updateTracking()
        mNotificationsManager.removeListener(this)
    }

    private fun updateControllers(controllers: List<MediaNotificationController>) {
        for (mnc in mControllers) {
            mnc.controller.unregisterCallback(this)
        }
        for (mnc in controllers) {
            mnc.controller.registerCallback(this)
        }
        mControllers = controllers
    }

    private fun updateTracking() {
        updateControllers(controllers)
        if (tracking != null) {
            tracking!!.reloadInfo()
        }

        // If the current controller is not playing, stop tracking it.
        if (tracking != null
            && (!mControllers.contains(tracking) || !tracking!!.isPlaying)
        ) {
            tracking = null
        }
        for (mnc in mControllers) {
            // Either we are not tracking a controller and this one is valid,
            // or this one is playing while the one we track is not.
            if (tracking == null && mnc.isPlaying
                || tracking != null && mnc.isPlaying && !tracking!!.isPlaying
            ) {
                tracking = mnc
            }
        }
        mHandler.removeCallbacks(mOnChange)
        mHandler.post(mOnChange)
    }

    private fun pressButton(keyCode: Int) {
        if (tracking != null) {
            tracking!!.pressButton(keyCode)
        }
    }

    fun toggle(finalClick: Boolean) {
        if (!finalClick) {
            Log.d(TAG, "Toggle")
            pressButton(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
        }
    }

    fun next(finalClick: Boolean) {
        if (finalClick) {
            Log.d(TAG, "Next")
            pressButton(KeyEvent.KEYCODE_MEDIA_NEXT)
            pressButton(KeyEvent.KEYCODE_MEDIA_PLAY)
        }
    }

    private val controllers: List<MediaNotificationController>
        get() {
            val controllers: MutableList<MediaNotificationController> = ArrayList()
            for (notif in mNotificationsManager.notifications) {
                val extras = notif.notification.extras
                val notifToken =
                    extras.getParcelable<MediaSession.Token>(Notification.EXTRA_MEDIA_SESSION)
                if (notifToken != null) {
                    val controller = MediaController(mContext, notifToken)
                    controllers.add(MediaNotificationController(controller, notif))
                }
            }
            return controllers
        }

    /**
     * Events that refresh the current handler.
     */
    override fun onPlaybackStateChanged(state: PlaybackState?) {
        super.onPlaybackStateChanged(state)
        updateTracking()
    }

    override fun onMetadataChanged(metadata: MediaMetadata?) {
        super.onMetadataChanged(metadata)
        updateTracking()
    }

    override fun onNotificationsChanged() {
        updateTracking()
    }

    inner class MediaInfo {
        var title: CharSequence? = null
        var artist: CharSequence? = null
        var album: CharSequence? = null
    }

    inner class MediaNotificationController constructor(
        val controller: MediaController,
        sbn: StatusBarNotification
    ) {
        val sbn: StatusBarNotification?
        var info: MediaInfo? = null
            private set

        val hasTitle: Boolean
            get() = info != null && info!!.title != null

        val isPlaying: Boolean
            get() =
                hasTitle && controller.playbackState != null && controller.playbackState!!.state == PlaybackState.STATE_PLAYING

        fun pressButton(keyCode: Int) {
            controller.dispatchMediaButtonEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
            controller.dispatchMediaButtonEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
        }

        fun reloadInfo() {
            val metadata = controller.metadata
            if (metadata != null) {
                info = MediaInfo()
                info!!.title = metadata.getText(MediaMetadata.METADATA_KEY_TITLE)
                info!!.artist = metadata.getText(MediaMetadata.METADATA_KEY_ARTIST)
                info!!.album = metadata.getText(MediaMetadata.METADATA_KEY_ALBUM)
            } else if (sbn != null) {
                info = MediaInfo()
                info!!.title = sbn.notification.extras.getCharSequence(Notification.EXTRA_TITLE)
            }
        }

        val packageName: String
            get() = controller.packageName

        init {
            this.sbn = sbn
            reloadInfo()
        }
    }

    companion object {
        private const val TAG = "MediaListener"
    }

}