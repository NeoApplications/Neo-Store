/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.saggitt.omega.preferences

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import com.android.launcher3.Launcher
import com.android.launcher3.R
import com.android.launcher3.util.Themes

class CaretDrawable(context: Context) : Drawable() {
    private var mCaretProgress = PROGRESS_CARET_NEUTRAL
    private val mShadowPaint = Paint()
    private val mCaretPaint = Paint()
    private val mPath = Path()
    private val mCaretSizePx: Int
    private val mUseShadow: Boolean
    private val mWorkspaceTextColor: Int
    var isHidden = false
    private var mForceDark = false

    override fun getIntrinsicHeight(): Int {
        return mCaretSizePx
    }

    override fun getIntrinsicWidth(): Int {
        return mCaretSizePx
    }

    override fun draw(canvas: Canvas) {
        if (isHidden) {
            return
        }

        // Assumes caret paint is more important than shadow paint
        if (java.lang.Float.compare(mCaretPaint.alpha.toFloat(), 0f) == 0) {
            return
        }

        // Assumes shadow stroke width is larger
        val width = bounds.width() - mShadowPaint.strokeWidth
        val height = bounds.height() - mShadowPaint.strokeWidth
        val left = bounds.left + mShadowPaint.strokeWidth / 2
        val top = bounds.top + mShadowPaint.strokeWidth / 2

        // When the bounds are square, this will result in a caret with a right angle
        val verticalInset = height / 4
        val caretHeight = height - verticalInset * 2
        mPath.reset()
        mPath.moveTo(left, top + caretHeight * (1 - normalizedCaretProgress))
        mPath.lineTo(left + width / 2, top + caretHeight * normalizedCaretProgress)
        mPath.lineTo(left + width, top + caretHeight * (1 - normalizedCaretProgress))
        if (mUseShadow && !mForceDark) {
            canvas.drawPath(mPath, mShadowPaint)
        }
        canvas.drawPath(mPath, mCaretPaint)
    }
    /**
     * Returns the caret progress
     *
     * @return The progress
     */
    /**
     * Sets the caret progress
     *
     * @param newValue The progress ({@value #PROGRESS_CARET_POINTING_UP} for pointing up,
     * {@value #PROGRESS_CARET_POINTING_DOWN} for pointing down, {@value #PROGRESS_CARET_NEUTRAL}
     * for neutral)
     */
    var caretProgress: Float
        get() = mCaretProgress
        set(newValue) {
            mCaretProgress = newValue
            invalidateSelf()
        }

    /**
     * Returns the caret progress normalized to [0..1]
     *
     * @return The normalized progress
     */
    val normalizedCaretProgress: Float
        get() = (mCaretProgress - PROGRESS_CARET_POINTING_UP) /
                (PROGRESS_CARET_POINTING_DOWN - PROGRESS_CARET_POINTING_UP)

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun setAlpha(alpha: Int) {
        mCaretPaint.alpha = alpha
        mShadowPaint.alpha = alpha
        invalidateSelf()
    }

    override fun setColorFilter(cf: ColorFilter?) {
        // no-op
    }

    fun setForceDark(forceDark: Boolean) {
        if (mForceDark != forceDark) {
            mForceDark = forceDark
            mCaretPaint.color = if (mForceDark) -0x67000000 else mWorkspaceTextColor
            invalidateSelf()
        }
    }

    companion object {
        const val PROGRESS_CARET_POINTING_UP = 1f
        const val PROGRESS_CARET_POINTING_DOWN = 0f
        const val PROGRESS_CARET_NEUTRAL = 0.5f
    }

    init {
        val res = context.resources
        val strokeWidth = res.getDimensionPixelSize(R.dimen.all_apps_caret_stroke_width)
        val shadowSpread = res.getDimensionPixelSize(R.dimen.all_apps_caret_shadow_spread)
        val isLauncher = Launcher.fromContext<Launcher>(context) != null
        mWorkspaceTextColor = Themes.getAttrColor(
            context,
            if (isLauncher) R.attr.workspaceTextColor else android.R.attr.textColorPrimary
        )
        mCaretPaint.color = mWorkspaceTextColor
        mCaretPaint.isAntiAlias = true
        mCaretPaint.strokeWidth = strokeWidth.toFloat()
        mCaretPaint.style = Paint.Style.STROKE
        mCaretPaint.strokeCap = Paint.Cap.ROUND
        mCaretPaint.strokeJoin = Paint.Join.ROUND
        mShadowPaint.color = res.getColor(R.color.default_shadow_color_no_alpha, null)
        mShadowPaint.alpha = Themes.getAlpha(context, android.R.attr.spotShadowAlpha)
        mShadowPaint.isAntiAlias = true
        mShadowPaint.strokeWidth = strokeWidth + shadowSpread * 2f
        mShadowPaint.style = Paint.Style.STROKE
        mShadowPaint.strokeCap = Paint.Cap.ROUND
        mShadowPaint.strokeJoin = Paint.Join.ROUND
        mUseShadow = isLauncher && !Themes.getAttrBoolean(context, R.attr.isWorkspaceDarkText)
        mCaretSizePx = res.getDimensionPixelSize(R.dimen.all_apps_caret_size)
    }
}