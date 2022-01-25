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

package com.saggitt.omega.groups

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.RippleDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Space
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.saggitt.omega.util.dpToPx
import com.saggitt.omega.util.getColorAttr
import com.saggitt.omega.util.tintDrawable
import com.saggitt.omega.views.SettingsBottomSheet

@SuppressLint("ViewConstructor")
class DrawerTabTypeSelectionBottomSheet constructor(
        context: Context,
        selectionItems: Map<String, Array<Int>>,
        callback: (which: String) -> Unit
) : FrameLayout(context) {
    init {
        View.inflate(context, R.layout.drawer_tab_select_type_bottom_sheet, this)

        val accent = Utilities.getOmegaPrefs(context).accentColor
        val container = findViewById<ViewGroup>(R.id.types_container)

        val tintNormal = ColorUtils
                .setAlphaComponent(context.getColorAttr(android.R.attr.colorControlHighlight), 255)
        val tintList = ColorStateList(
                arrayOf(
                        intArrayOf(android.R.attr.state_selected),
                        intArrayOf()
                ),
                intArrayOf(
                        accent,
                        tintNormal
                )
        )
        val rippleTintList = ColorStateList(
                arrayOf(
                        intArrayOf(android.R.attr.state_selected),
                        intArrayOf()
                ),
                intArrayOf(
                        ColorUtils.setAlphaComponent(accent, 31),
                        ColorUtils.setAlphaComponent(tintNormal, 31)
                )
        )

        for (item in selectionItems) {
            val view = View.inflate(context, R.layout.drawer_tab_type_manual, null)

            view.background.setTintList(tintList)
            (view.background as RippleDrawable).setColor(rippleTintList)

            view.findViewById<TextView>(android.R.id.title).setText(item.value[0])
            view.findViewById<TextView>(android.R.id.summary).setText(item.value[1])
            view.findViewById<ImageView>(android.R.id.icon).apply {
                tintDrawable(accent)
                setImageResource(item.value[2])
            }

            view.setOnClickListener {
                callback(item.key)
            }

            container.addView(view)

            val spacer = Space(context)
            spacer.minimumHeight = dpToPx(16f).toInt()
            container.addView(spacer)
        }
    }

    companion object {
        fun show(
                context: Context,
                selectionItems: Map<String, Array<Int>>,
                callback: (which: String) -> Unit
        ) {
            val sheet = SettingsBottomSheet.inflate(context)
            sheet.show(DrawerTabTypeSelectionBottomSheet(context, selectionItems) {
                sheet.close(false)
                callback(it)
            }, true)
        }
    }
}