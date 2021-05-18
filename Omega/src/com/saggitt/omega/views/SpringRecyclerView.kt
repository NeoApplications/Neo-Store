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

package com.saggitt.omega.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.Keep
import androidx.recyclerview.widget.RecyclerView
import com.android.launcher3.Utilities
import com.saggitt.omega.util.getColorAttr
import com.saggitt.omega.util.omegaPrefs

open class SpringRecyclerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    private val springManager = SpringEdgeEffect.Manager(this)
    private val scrollBarColor by lazy {
        val colorControlNormal = context.getColorAttr(android.R.attr.colorControlNormal)
        val useAccentColor = colorControlNormal == context.omegaPrefs.accentColor
        if (useAccentColor) Utilities.getOmegaPrefs(context).accentColor else colorControlNormal
    }

    open var shouldTranslateSelf = true

    var isTopFadingEdgeEnabled = true

    init {
        edgeEffectFactory = springManager.createFactory()
    }

    override fun draw(canvas: Canvas) {
        springManager.withSpring(canvas, shouldTranslateSelf) {
            super.draw(canvas)
            false
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        springManager.withSpring(canvas, !shouldTranslateSelf) {
            super.dispatchDraw(canvas)
            false
        }
    }

    override fun getTopFadingEdgeStrength(): Float {
        return if (isTopFadingEdgeEnabled) super.getTopFadingEdgeStrength() else 0f
    }

    /**
     * Called by Android [android.view.View.onDrawScrollBars]
     */
    @Keep
    protected fun onDrawHorizontalScrollBar(
        canvas: Canvas,
        scrollBar: Drawable,
        l: Int,
        t: Int,
        r: Int,
        b: Int
    ) {
        springManager.withSpringNegative(canvas, shouldTranslateSelf) {
            scrollBar.setColorFilter(scrollBarColor, PorterDuff.Mode.SRC_ATOP)
            scrollBar.setBounds(l, t, r, b)
            scrollBar.draw(canvas)
            false
        }
    }

    /**
     * Called by Android [android.view.View.onDrawScrollBars]
     */
    @Keep
    protected fun onDrawVerticalScrollBar(
        canvas: Canvas,
        scrollBar: Drawable,
        l: Int,
        t: Int,
        r: Int,
        b: Int
    ) {
        springManager.withSpringNegative(canvas, shouldTranslateSelf) {
            scrollBar.setColorFilter(scrollBarColor, PorterDuff.Mode.SRC_ATOP)
            scrollBar.setBounds(l, t, r, b)
            scrollBar.draw(canvas)
            false
        }
    }
}

