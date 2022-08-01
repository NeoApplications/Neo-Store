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
package com.saggitt.omega.icons

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.saggitt.omega.icons.IconShape.Companion.fromString
import com.saggitt.omega.preferences.OmegaPreferences
import com.saggitt.omega.wallpaper.WallpaperPreviewProvider

class CustomizeIconsPreview @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), OmegaPreferences.OnPreferenceChangeListener {
    private val wallpaper: Drawable
    private val viewLocation = IntArray(2)
    private val icons = arrayOfNulls<ImageView>(4)
    private val prefs: OmegaPreferences
    private val prefsToWatch = arrayOf(
        "pref_colored_background", "pref_adaptive_icon_pack"
    )
    private var count = 2
    private var isFirstLoad = true

    init {
        orientation = HORIZONTAL
        wallpaper = WallpaperPreviewProvider.getInstance(context!!).wallpaper
        prefs = Utilities.getOmegaPrefs(context)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        loadIcons()
        loadBackground(prefs.themeIconForceShapeless.onGetValue())
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        prefs.addOnPreferenceChangeListener(this, *prefsToWatch)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        prefs.reloadIcons()
        prefs.removeOnPreferenceChangeListener(this, *prefsToWatch)
    }

    private fun getShapeDrawable(): Drawable {
        val drawable =
            if (prefs.themeIconShape.onGetValue() == IconShapeManager.getSystemIconShape(context)) {
                IconShapeDrawable(IconShapeManager.getSystemIconShape(context))
            } else {
                IconShapeDrawable(fromString(prefs.themeIconShape.onGetValue().toString())!!)
            }

        return drawable
    }

    private fun loadBackground(shapeless: Boolean) {

        if (!shapeless) {
            if (prefs.themeIconColoredBackground.onGetValue()) {
                /*Omega*/
                val drawable1: Drawable = getShapeDrawable()
                drawable1.colorFilter =
                    PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN)
                icons[0]!!.background = drawable1

                /*NewPipe*/
                val drawable2: Drawable = getShapeDrawable()
                drawable2.colorFilter =
                    PorterDuffColorFilter(Color.rgb(0xbf, 0x19, 0x19), PorterDuff.Mode.SRC_IN)
                icons[1]!!.background = drawable2

                /*Signal*/
                val drawable3: Drawable = getShapeDrawable()
                drawable3.colorFilter =
                    PorterDuffColorFilter(Color.parseColor("#FF568AF4"), PorterDuff.Mode.SRC_IN)
                icons[2]!!.background = drawable3

                /*Photos*/
                val drawable4: Drawable = getShapeDrawable()
                drawable4.colorFilter =
                    PorterDuffColorFilter(Color.parseColor("#FFFFB969"), PorterDuff.Mode.SRC_IN)
                icons[3]!!.background = drawable4
            } else {
                val drawable = getShapeDrawable()
                drawable.colorFilter = PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
                for (icon in icons) {
                    icon!!.background = drawable
                }
            }
        } else {
            for (icon in icons) {
                icon!!.background = null
            }
        }
    }

    private fun loadIcons() {
        icons[0] = findViewById(R.id.icon_launcher)
        icons[1] = findViewById(R.id.icon_newpipe)
        icons[2] = findViewById(R.id.icon_signal)
        icons[3] = findViewById(R.id.icon_osmand)
    }

    override fun dispatchDraw(canvas: Canvas) {
        val width = wallpaper.intrinsicWidth
        val height = wallpaper.intrinsicHeight
        if (width == 0 || height == 0) {
            super.dispatchDraw(canvas)
            return
        }
        getLocationInWindow(viewLocation)
        val dm = resources.displayMetrics
        val scaleX = dm.widthPixels.toFloat() / width
        val scaleY = dm.heightPixels.toFloat() / height
        val scale = scaleX.coerceAtLeast(scaleY)
        canvas.save()
        canvas.translate(0f, (-viewLocation[1]).toFloat())
        canvas.scale(scale, scale)
        wallpaper.setBounds(0, 0, width, height)
        wallpaper.draw(canvas)
        canvas.restore()
        super.dispatchDraw(canvas)
    }

    override fun onValueChanged(key: String, prefs: OmegaPreferences, force: Boolean) {
        if (!isFirstLoad && count == 0) {
            loadIcons()
            loadBackground(prefs.themeIconForceShapeless.onGetValue())
            invalidate()
        } else {
            isFirstLoad = false
            count--
        }
    }

}