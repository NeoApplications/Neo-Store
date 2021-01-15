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

import android.os.Handler
import android.text.TextUtils
import android.view.View
import androidx.annotation.Keep
import androidx.core.graphics.drawable.toBitmap
import com.android.launcher3.R
import com.android.launcher3.util.Executors.MAIN_EXECUTOR
import com.saggitt.omega.smartspace.OmegaSmartspaceController
import com.saggitt.omega.smartspace.OmegaSmartspaceController.CardData
import com.saggitt.omega.smartspace.OmegaSmartspaceController.Line
import com.saggitt.omega.util.loadSmallIcon

@Keep
class NowPlayingProvider(controller: OmegaSmartspaceController) :
        OmegaSmartspaceController.NotificationBasedDataProvider(controller) {

    private val media = MediaListener(context, this::reload, Handler(MAIN_EXECUTOR.looper))
    private val defaultIcon = context.getDrawable(R.drawable.ic_music_note)!!.toBitmap()

    init {
        startListening()
    }

    override fun startListening() {
        super.startListening()

        media.onResume()
    }

    private fun getEventCard(): CardData? {
        val tracking = media.tracking ?: return null
        val title = tracking.info.title ?: return null

        val sbn = tracking.sbn
        val icon = sbn?.loadSmallIcon(context)?.toBitmap() ?: defaultIcon

        val mediaInfo = tracking.info
        val lines = mutableListOf<Line>()
        lines.add(Line(title))
        if (!TextUtils.isEmpty(mediaInfo.artist)) {
            lines.add(Line(mediaInfo.artist.toString()))
        } else if (sbn != null) {
            lines.add(Line(getApp(sbn).toString()))
        } else {
            lines.add(Line(getApp(tracking.packageName)))
        }
        val intent = sbn?.notification?.contentIntent
        return if (intent != null) {
            CardData(icon, lines, intent, true)
        } else {
            CardData(icon, lines, View.OnClickListener {
                media.toggle(true)
            }, true)
        }
    }

    private fun reload() {
        updateData(null, getEventCard())
    }

    override fun stopListening() {
        super.stopListening()
        media.onPause()
    }
}
