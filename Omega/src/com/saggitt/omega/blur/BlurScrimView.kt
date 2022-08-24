/*
 *  This file is part of Neo Launcher
 *  Copyright (c) 2022   Neo Launcher Team
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

package com.saggitt.omega.blur

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.core.graphics.alpha
import com.android.launcher3.R
import com.android.launcher3.util.SystemUiController
import com.android.launcher3.util.Themes
import com.android.launcher3.views.ScrimView
import com.saggitt.omega.PREFS_DRAWER_OPACITY
import com.saggitt.omega.preferences.OmegaPreferences
import com.saggitt.omega.util.runOnMainThread
import kotlin.math.roundToInt

class BlurScrimView(context: Context, attrs: AttributeSet?) : ScrimView(context, attrs),
    OmegaPreferences.OnPreferenceChangeListener, BlurWallpaperProvider.Listener {
    private val prefs = OmegaPreferences.getInstance(context)
    private var drawerOpacity = prefs.drawerBackgroundColor.onGetValue().alpha / 255f

    private val prefsToWatch = arrayOf(PREFS_DRAWER_OPACITY)
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
    private var blurDrawable: BlurDrawable? = null
    private var mEndScrim = Themes.getAttrColor(context, R.attr.allAppsScrimColor)
    private var mEndAlpha = Color.alpha(mEndScrim)
    private val defaultEndAlpha = Color.alpha(mEndScrim)
    private val reInitUiRunnable = this::reInitUi

    private var radius = BOTTOM_CORNER_RADIUS_RATIO * Themes.getDialogCornerRadius(context)

    override fun updateSysUiColors() {
        val threshold = STATUS_BAR_COLOR_FORCE_UPDATE_THRESHOLD
        val forceChange = visibility == VISIBLE &&
                alpha > threshold && Color.alpha(mBackgroundColor) / (255f * drawerOpacity) > threshold
        with(systemUiController) {
            if (forceChange) {
                updateUiState(SystemUiController.UI_STATE_SCRIM_VIEW, !isScrimDark)
            } else {
                updateUiState(SystemUiController.UI_STATE_SCRIM_VIEW, 0)
            }
        }
    }

    private fun createBlurDrawable(): BlurDrawable? {
        blurDrawable?.let { if (isAttachedToWindow) it.stopListening() }
        return if (BlurWallpaperProvider.isEnabled) {
            provider.createDrawable(radius, 0f).apply {
                callback = blurDrawableCallback
                setBounds(left, top, right, bottom)
                if (isAttachedToWindow) startListening()
            }
        } else {
            null
        }
    }

    override fun isScrimDark() = if (drawerOpacity <= 0.3f) {
        !Themes.getAttrBoolean(context, R.attr.isWorkspaceDarkText)
    } else {
        super.isScrimDark()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        prefs.addOnPreferenceChangeListener(this, *prefsToWatch)
        BlurWallpaperProvider.getInstance(context).addListener(this)
        blurDrawable?.startListening()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        prefs.removeOnPreferenceChangeListener(this, *prefsToWatch)
        BlurWallpaperProvider.getInstance(context).removeListener(this)
        blurDrawable?.stopListening()
    }

    override fun onValueChanged(key: String, prefs: OmegaPreferences, force: Boolean) {
        when (key) {
            PREFS_DRAWER_OPACITY -> {
                mEndAlpha = (prefs.drawerBackgroundColor.onGetValue().alpha / 255f)
                    .roundToInt()
                    .takeIf { it > 0 }
                    ?: defaultEndAlpha
                updateSysUiColors()
            }
        }
    }

    private fun reInitUi() {
        blurDrawable = createBlurDrawable()
        blurDrawable?.alpha = 0
    }

    companion object {
        private const val BOTTOM_CORNER_RADIUS_RATIO = 2f
    }
}