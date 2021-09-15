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

package com.saggitt.omega.allapps

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.android.launcher3.*
import com.android.launcher3.dragndrop.DragOptions
import com.android.launcher3.dragndrop.DragView
import com.android.launcher3.model.data.AppInfo
import com.android.launcher3.model.data.ItemInfo
import com.android.launcher3.shortcuts.ShortcutDragPreviewProvider
import java.lang.ref.WeakReference

class AllAppsIconRowView(context: Context, attrs: AttributeSet) :
    LinearLayout(context, attrs), View.OnTouchListener {

    lateinit var icon: BubbleTextView
    lateinit var title: TextView

    var text: CharSequence?
        get() = title.text
        set(value) {
            title.text = value
        }

    var textColor = Utilities.getOmegaPrefs(context).drawerLabelColor

    private val iconShift = Point()
    private val iconLastTouchPos = Point()
    private val tempPoint = Point()
    private val launcher = Launcher.getLauncher(context)
    var dragView: WeakReference<DragView?> = WeakReference(null as DragView?)

    init {
        setOnTouchListener(this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        icon = findViewById<BubbleTextView?>(R.id.icon).apply {
            width = launcher.deviceProfile.allAppsIconSizePx
            height = launcher.deviceProfile.allAppsIconSizePx
        }
        title = findViewById(R.id.title)
    }

    fun applyFromApplicationInfo(appInfo: AppInfo) {
        icon.applyFromApplicationInfo(appInfo)
    }

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> iconLastTouchPos.set(
                motionEvent.x.toInt(),
                motionEvent.y.toInt()
            )
            MotionEvent.ACTION_MOVE -> iconLastTouchPos.set(
                motionEvent.x.toInt(),
                motionEvent.y.toInt()
            )
        }
        return false
    }

    private fun getIconCenter(): Point {
        val halfHeight = measuredHeight / 2
        tempPoint.x = halfHeight
        tempPoint.y = halfHeight
        if (Utilities.isRtl(resources))
            tempPoint.x = measuredWidth - tempPoint.x
        return tempPoint
    }

    fun beginDrag(source: DragSource, options: DragOptions) {
        iconShift.x = iconLastTouchPos.x - getIconCenter().x
        iconShift.y = iconLastTouchPos.y - launcher.deviceProfile.iconSizePx
        // Long clicked on a shortcut.
        dragView = WeakReference(
            launcher.workspace.beginDragShared(
                icon, null, source,
                icon.tag as ItemInfo,
                ShortcutDragPreviewProvider(icon, iconShift), options
            )
        )
        dragView.get()?.shift(-iconShift.x, -iconShift.y)
    }
}