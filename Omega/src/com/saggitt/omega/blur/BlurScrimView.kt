/*
 * Copyright (C) 2018 paphonb@xda
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.saggitt.omega.blur

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.core.graphics.ColorUtils
import com.android.launcher3.BuildConfig
import com.android.launcher3.LauncherState
import com.android.launcher3.LauncherState.BACKGROUND_APP
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.anim.Interpolators.ACCEL_2
import com.android.launcher3.anim.Interpolators.LINEAR
import com.android.launcher3.util.Themes
import com.android.quickstep.SysUINavigationMode
import com.android.quickstep.views.ShelfScrimView
import com.saggitt.omega.OmegaApp
import com.saggitt.omega.OmegaPreferences
import com.saggitt.omega.util.dpToPx
import com.saggitt.omega.util.omegaPrefs
import com.saggitt.omega.util.runOnMainThread
import kotlin.math.roundToInt

class BlurScrimView(context: Context, attrs: AttributeSet) : ShelfScrimView(context, attrs),
    OmegaPreferences.OnPreferenceChangeListener,
    BlurWallpaperProvider.Listener {

    private val prefsToWatch =
        arrayOf(
            KEY_RADIUS, KEY_OPACITY, KEY_DOCK_OPACITY, KEY_DOCK_ARROW, KEY_SEARCH_RADIUS,
            KEY_DEBUG_STATE, KEY_DRAWER_BG, KEY_DOCK_BG
        )

    private val blurDrawableCallback by lazy {
        object : Drawable.Callback {
            override fun unscheduleDrawable(who: Drawable, what: Runnable) {}

            override fun invalidateDrawable(who: Drawable) {
                runOnMainThread { invalidate() }
            }

            override fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long) {}
        }
    }

    private val provider by lazy { BlurWallpaperProvider.getInstance(context) }
    private val useFlatColor get() = mLauncher.deviceProfile.isVerticalBarLayout
    private var blurDrawable: BlurDrawable? = null

    private val insets = Rect()

    private val colorRanges = ArrayList<ColorRange>()

    private var allAppsBackground = context.omegaPrefs.drawerBackgroundColor
    private var dockBackground = context.omegaPrefs.dockBackgroundColor
    private val defaultAllAppsBackground =
        Themes.getAttrColor(context, R.attr.allAppsScrimColor)

    private val reInitUiRunnable = Runnable { reInitUi() }
    private var fullBlurProgress = 0f

    private var shouldDrawDebug = false
    private val debugTextPaint = Paint().apply {
        textSize = DEBUG_TEXT_SIZE
        color = Color.RED
        typeface = Typeface.DEFAULT_BOLD
    }

    private val defaultEndAlpha = Color.alpha(mEndScrim)
    private val prefs = Utilities.getOmegaPrefs(context)

    val currentBlurAlpha get() = blurDrawable?.alpha

    private fun createBlurDrawable(): BlurDrawable? {
        blurDrawable?.let { if (isAttachedToWindow) it.stopListening() }
        return if (BlurWallpaperProvider.isEnabled) {
            provider.createDrawable(mRadius, 0f).apply {
                callback = blurDrawableCallback
                setBounds(left, top, right, bottom)
                if (isAttachedToWindow) startListening()
            }
        } else {
            null
        }
    }

    override fun reInitUi() {
        blurDrawable = createBlurDrawable()
        blurDrawable?.alpha = 0
        rebuildColors()
        super.reInitUi()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        prefs.addOnPreferenceChangeListener(this, *prefsToWatch)
        BlurWallpaperProvider.getInstance(context).addListener(this)
        blurDrawable?.startListening()
    }


    override fun onValueChanged(key: String, prefs: OmegaPreferences, force: Boolean) {
        when (key) {
            KEY_RADIUS -> {
                mRadius = dpToPx(prefs.dockRadius)
                blurDrawable?.also {
                    it.blurRadii = BlurDrawable.Radii(mRadius, 0f)
                }
            }
            KEY_OPACITY -> {
                mEndAlpha = prefs.allAppsOpacity.takeIf { it >= 0 } ?: defaultEndAlpha
                calculateEndScrim()
                mEndFlatColorAlpha = Color.alpha(mEndFlatColor)
                postReInitUi()
            }
            KEY_DOCK_OPACITY -> {
                postReInitUi()
            }
            KEY_DOCK_ARROW -> {
                updateDragHandleVisibility()
            }
            KEY_DOCK_BG -> {
                dockBackground = prefs.dockBackgroundColor
                postReInitUi()
            }
            KEY_DRAWER_BG -> {
                allAppsBackground = if (prefs.customBackground) {
                    prefs.drawerBackgroundColor
                } else {
                    defaultAllAppsBackground
                }
                calculateEndScrim()
                postReInitUi()
            }
        }
    }

    private fun calculateEndScrim() {
        mEndScrim = ColorUtils.setAlphaComponent(allAppsBackground, mEndAlpha)
        mEndFlatColor = ColorUtils.compositeColors(
            mEndScrim, ColorUtils.setAlphaComponent(
                mScrimColor, mMaxScrimAlpha
            )
        )
    }

    private fun rebuildColors() {
        val recentsProgress = LauncherState.OVERVIEW.getScrimProgress(mLauncher)

        val hasRecents = OmegaApp.isRecentsEnabled && recentsProgress < 1f

        val fullShelfColor = ColorUtils.setAlphaComponent(allAppsBackground, mEndAlpha)
        val recentsShelfColor = ColorUtils.setAlphaComponent(allAppsBackground, getMidAlpha())
        val nullShelfColor = ColorUtils.setAlphaComponent(allAppsBackground, 0)

        val colors = ArrayList<Pair<Float, Int>>()
        colors.add(Pair(Float.NEGATIVE_INFINITY, fullShelfColor))
        colors.add(Pair(0.5f, fullShelfColor))
        fullBlurProgress = 0.5f
        if (hasRecents) {
            colors.add(Pair(recentsProgress, recentsShelfColor))
            fullBlurProgress = recentsProgress
        }
        colors.add(Pair(1f, nullShelfColor))
        colors.add(Pair(Float.POSITIVE_INFINITY, nullShelfColor))

        colorRanges.clear()
        for (i in (1 until colors.size)) {
            val color1 = colors[i - 1]
            val color2 = colors[i]
            colorRanges.add(ColorRange(color1.first, color2.first, color1.second, color2.second))
        }
    }

    private fun getMidAlpha(): Int {
        return prefs.dockOpacity.takeIf { it >= 0 } ?: mMidAlpha
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        prefs.removeOnPreferenceChangeListener(this, *prefsToWatch)
        BlurWallpaperProvider.getInstance(context).removeListener(this)
        blurDrawable?.stopListening()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (shouldDrawDebug) {
            drawDebug(canvas)
        }
    }

    override fun setInsets(insets: Rect) {
        super.setInsets(insets)
        this.insets.set(insets)
        postReInitUi()
    }

    override fun onDrawFlatColor(canvas: Canvas) {
        blurDrawable?.run {
            setBounds(0, 0, width, height)
            draw(canvas, true)
        }
    }

    override fun onDrawRoundRect(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        rx: Float,
        ry: Float,
        paint: Paint
    ) {
        blurDrawable?.run {
            setBlurBounds(left, top, right, bottom)
            draw(canvas)
        }
        super.onDrawRoundRect(canvas, left, top, right, bottom, rx, ry, paint)
    }

    override fun updateColors() {
        super.updateColors()
        val alpha = when {
            useFlatColor -> ((1 - mProgress) * 255).toInt()
            mProgress >= fullBlurProgress -> (255 * ACCEL_2.getInterpolation(
                0f.coerceAtLeast(1 - mProgress) / (1 - fullBlurProgress)
            )).roundToInt()
            else -> 255
        }
        blurDrawable?.alpha = alpha

        mDragHandleOffset = 0f.coerceAtLeast(mDragHandleBounds.top + mDragHandleSize.y - mShelfTop)

        if (!useFlatColor) {
            mShelfColor =
                if (mProgress >= 1 && mSysUINavigationMode == SysUINavigationMode.Mode.NO_BUTTON
                    && mLauncher.stateManager.state == BACKGROUND_APP
                ) {
                    ColorUtils.setAlphaComponent(allAppsBackground, mMidAlpha)
                } else {
                    getColorForProgress(mProgress)
                }
        }
    }

    private fun getColorForProgress(progress: Float): Int {
        val interpolatedProgress: Float = when {
            progress >= 1 -> progress
            progress >= mMidProgress -> Utilities.mapToRange(
                progress, mMidProgress, 1f, mMidProgress, 1f,
                mBeforeMidProgressColorInterpolator
            )
            else -> Utilities.mapToRange(
                progress, 0f, mMidProgress, 0f, mMidProgress,
                mAfterMidProgressColorInterpolator
            )
        }
        colorRanges.forEach {
            if (interpolatedProgress in it) {
                return it.getColor(interpolatedProgress)
            }
        }
        return 0
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (useFlatColor) {
            blurDrawable?.setBounds(left, top, right, bottom)
        }
    }

    override fun onEnabledChanged() {
        postReInitUi()
    }

    private fun drawDebug(canvas: Canvas) {
        listOf(
            "version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
            "state: ${mLauncher.stateManager.state::class.java.simpleName}",
            "toState: ${mLauncher.stateManager.toState::class.java.simpleName}"
        ).forEachIndexed { index, line ->
            canvas.drawText(line, 50f, 200f + (DEBUG_LINE_HEIGHT * index), debugTextPaint)
        }
    }

    private fun postReInitUi() {
        handler?.removeCallbacks(reInitUiRunnable)
        handler?.post(reInitUiRunnable)
    }

    fun setOverlayScroll(scroll: Float) {
        blurDrawable?.viewOffsetX = scroll
    }

    fun getShelfColor(): Int {
        return mShelfColor
    }

    companion object {
        private const val DEBUG_TEXT_SIZE = 30f
        private const val DEBUG_LINE_HEIGHT = DEBUG_TEXT_SIZE + 3f

        private const val KEY_RADIUS = "pref_dockRadius"
        private const val KEY_OPACITY = "pref_allAppsOpacitySB"
        private const val KEY_DOCK_OPACITY = "pref_hotseatCustomOpacity"
        private const val KEY_DOCK_ARROW = "pref_hotseatShowArrow"
        private const val KEY_SEARCH_RADIUS = "pref_searchbarRadius"
        private const val KEY_DEBUG_STATE = "pref_debugDisplayState"
        private const val KEY_DRAWER_BG = "pref_drawer_background_color"
        private const val KEY_DOCK_BG = "pref_dock_background_color"
    }

    class ColorRange(
        private val start: Float, private val end: Float,
        private val startColor: Int, private val endColor: Int
    ) {

        private val range = start..end

        fun getColor(progress: Float): Int {
            if (start == Float.NEGATIVE_INFINITY) return endColor
            if (end == Float.POSITIVE_INFINITY) return startColor
            val amount = Utilities.mapToRange(progress, start, end, 0f, 1f, LINEAR)
            return ColorUtils.blendARGB(startColor, endColor, amount)
        }

        operator fun contains(value: Float) = value in range
    }
}
