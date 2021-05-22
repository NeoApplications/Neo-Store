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
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.saggitt.omega.OmegaPreferences
import com.saggitt.omega.adaptive.IconShape.Companion.fromString
import com.saggitt.omega.adaptive.IconShapeDrawable
import com.saggitt.omega.wallpaper.WallpaperPreviewProvider

class PreviewFrameView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), OmegaPreferences.OnPreferenceChangeListener {
    private val wallpaper: Drawable
    private val viewLocation = IntArray(2)
    private val icons = arrayOfNulls<ImageView>(4)
    private val prefs: OmegaPreferences
    private val prefsToWatch = arrayOf(
        "pref_iconShape", "pref_colorizeGeneratedBackgrounds",
        "pref_enableWhiteOnlyTreatment", "pref_enableLegacyTreatment",
        "pref_generateAdaptiveForIconPack", "pref_forceShapeless"
    )
    private var count = 6
    private var isFirstLoad = true
    override fun onFinishInflate() {
        super.onFinishInflate()
        loadIcons()
        loadBackground(prefs.forceShapeless)
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

    private fun loadBackground(shapeless: Boolean) {
        val drawable: Drawable = IconShapeDrawable(fromString(prefs.iconShape)!!)
        if (!shapeless) {
            if (prefs.enableWhiteOnlyTreatment) {
                /*Instagram*/
                val drawable1: Drawable = IconShapeDrawable(fromString(prefs.iconShape)!!)
                drawable1.colorFilter =
                    PorterDuffColorFilter(Color.rgb(0x9f, 0x47, 0xd2), PorterDuff.Mode.SRC_IN)
                icons[0]!!.background = drawable1

                /*Youtube*/
                val drawable2: Drawable = IconShapeDrawable(fromString(prefs.iconShape)!!)
                drawable2.colorFilter =
                    PorterDuffColorFilter(Color.rgb(0xbf, 0x19, 0x19), PorterDuff.Mode.SRC_IN)
                icons[1]!!.background = drawable2

                /*WhatsApp*/
                val drawable3: Drawable = IconShapeDrawable(fromString(prefs.iconShape)!!)
                drawable3.colorFilter =
                    PorterDuffColorFilter(Color.rgb(0x5e, 0xea, 0x7f), PorterDuff.Mode.SRC_IN)
                icons[2]!!.background = drawable3

                /*Photos*/
                val drawable4: Drawable = IconShapeDrawable(fromString(prefs.iconShape)!!)
                drawable4.colorFilter =
                    PorterDuffColorFilter(Color.rgb(0x1c, 0x60, 0xd8), PorterDuff.Mode.SRC_IN)
                icons[3]!!.background = drawable4
            } else {
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
        icons[0] = findViewById(R.id.icon_instagram)
        icons[1] = findViewById(R.id.icon_youtube)
        icons[2] = findViewById(R.id.icon_whatsapp)
        icons[3] = findViewById(R.id.icon_photos)
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
            Log.d("IconPreview", "Cambiando preferencia $key")
            loadIcons()
            loadBackground(prefs.forceShapeless)
            invalidate()
        } else {
            isFirstLoad = false
            count--
        }
    }

    init {
        orientation = HORIZONTAL
        wallpaper = WallpaperPreviewProvider.getInstance(context!!).wallpaper
        prefs = Utilities.getOmegaPrefs(context)
    }
}