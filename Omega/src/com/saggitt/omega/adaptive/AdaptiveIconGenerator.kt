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
package com.saggitt.omega.adaptive

import android.content.Context
import android.graphics.Color
import android.graphics.RectF
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.util.SparseIntArray
import androidx.core.graphics.ColorUtils
import com.android.launcher3.AdaptiveIconCompat
import com.android.launcher3.Utilities
import com.android.launcher3.icons.ColorExtractor
import com.android.launcher3.icons.FixedScaleDrawable
import com.android.launcher3.icons.LauncherIcons
import com.saggitt.omega.icons.CustomIconProvider
import kotlin.math.roundToInt

// TODO: Make this thing async somehow (maybe using some drawable wrappers?)
class AdaptiveIconGenerator constructor(
    private val context: Context, icon: Drawable, roundIcon: Drawable?
) {
    private var icon: Drawable
    private val roundIcon: Drawable?
    private val extractColor: Boolean
    private val treatWhite: Boolean
    private var ranLoop = false
    private val shouldWrap: Boolean
    private var backgroundColor = Color.WHITE
    private var isFullBleed = false
    private var noMixinNeeded = false
    private var fullBleedChecked = false
    private var matchesMaskShape = false
    private var isBackgroundWhite = false
    private var scale = 0f
    private var height = 0
    private var aHeight = 0f
    private var width = 0
    private var aWidth = 0f
    private var result: Drawable? = null
    private var tmp: AdaptiveIconCompat? = null

    private fun loop() {
        if (Utilities.ATLEAST_OREO && shouldWrap) {
            if (roundIcon != null && roundIcon is AdaptiveIconCompat) {
                icon = roundIcon
            }
            var extractee = icon
            if (extractee is AdaptiveIconCompat) {
                if (!treatWhite) {
                    onExitLoop()
                    return
                }
                val aid = extractee
                // we still check this separately as this is the only information we need from the background
                if (!ColorExtractor.isSingleColor(aid.background, Color.WHITE)) {
                    onExitLoop()
                    return
                }
                isBackgroundWhite = true
                extractee = aid.foreground
            }
            if (extractee == null) {
                Log.e("AdaptiveIconGenerator", "extractee is null, skipping.")
                onExitLoop()
                return
            }
            val li = LauncherIcons.obtain(context)
            val normalizer = li.normalizer
            li.recycle()
            val outShape = BooleanArray(1)
            val bounds = RectF()
            initTmpIfNeeded()
            scale =
                normalizer.getScale(extractee, bounds, tmp!!.iconMask, outShape, MIN_VISIBLE_ALPHA)
            matchesMaskShape = outShape[0]
            if (extractee is ColorDrawable) {
                isFullBleed = true
                fullBleedChecked = true
            }
            width = extractee.intrinsicWidth
            height = extractee.intrinsicHeight
            aWidth = width * (1 - (bounds.left + bounds.right))
            aHeight = height * (1 - (bounds.top + bounds.bottom))

            // Check if the icon is squareish
            val ratio = aHeight / aWidth
            val isSquareish = 0.999 < ratio && ratio < 1.0001
            val almostSquarish = isSquareish || 0.97 < ratio && ratio < 1.005
            if (!isSquareish) {
                isFullBleed = false
                fullBleedChecked = true
            }
            val bitmap = Utilities.drawableToBitmap(extractee)
            if (bitmap == null) {
                onExitLoop()
                return
            }
            if (!bitmap.hasAlpha()) {
                isFullBleed = true
                fullBleedChecked = true
            }
            val size = height * width
            val rgbScoreHistogram = SparseIntArray(NUMBER_OF_COLORS_GUESSTIMATION)
            val pixels = IntArray(size)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

            /*
             *   Calculate the number of padding pixels around the actual icon (i)
             *   +----------------+
             *   |      top       |
             *   +---+--------+---+
             *   |   |        |   |
             *   | l |    i   | r |
             *   |   |        |   |
             *   +---+--------+---+
             *   |     bottom     |
             *   +----------------+
             */
            val adjHeight = height - bounds.top - bounds.bottom
            val l = bounds.left * width * adjHeight
            val top = bounds.top * height * width
            val r = bounds.right * width * adjHeight
            val bottom = bounds.bottom * height * width
            val addPixels = (l + top + r + bottom).roundToInt()

            // Any icon with less than 10% transparent pixels (padding excluded) is considered "full-bleed-ish"
            val maxTransparent = ((size * .10).roundToInt() + addPixels)
            // Any icon with less than 27% transparent pixels (padding excluded) doesn't need a color mix-in
            val noMixinScore = ((size * .27).roundToInt() + addPixels)
            var highScore = 0
            var bestRGB = 0
            var transparentScore = 0
            for (pixel in pixels) {
                val alpha = 0xFF and (pixel shr 24)
                if (alpha < MIN_VISIBLE_ALPHA) {
                    // Drop mostly-transparent pixels.
                    transparentScore++
                    if (transparentScore > maxTransparent) {
                        isFullBleed = false
                        fullBleedChecked = true
                        if (!extractColor && transparentScore > noMixinScore) {
                            break
                        }
                    }
                    continue
                }
                // Reduce color complexity.
                val rgb = ColorExtractor.posterize(pixel)
                if (rgb < 0) {
                    // Defensively avoid array bounds violations.
                    continue
                }
                val currentScore = rgbScoreHistogram[rgb] + 1
                rgbScoreHistogram.append(rgb, currentScore)
                if (currentScore > highScore) {
                    highScore = currentScore
                    bestRGB = rgb
                }
            }

            // add back the alpha channel
            bestRGB = bestRGB or (0xff shl 24)

            // not yet checked = not set to false = has to be full bleed, isBackgroundWhite = true = is adaptive
            isFullBleed = isFullBleed or (!fullBleedChecked && !isBackgroundWhite)

            // return early if a mix-in isnt needed
            noMixinNeeded =
                !isFullBleed && !isBackgroundWhite && almostSquarish && transparentScore <= noMixinScore
            if (isFullBleed || noMixinNeeded) {
                backgroundColor = bestRGB
                onExitLoop()
                return
            }
            if (!extractColor) {
                backgroundColor = Color.WHITE
                onExitLoop()
                return
            }

            // "single color"
            val numColors = rgbScoreHistogram.size()
            val singleColor = numColors <= SINGLE_COLOR_LIMIT

            // Convert to HSL to get the lightness and adjust the color
            val hsl = FloatArray(3)
            ColorUtils.colorToHSL(bestRGB, hsl)
            val lightness = hsl[2]
            val light = lightness > .5
            // Apply dark background to mostly white icons
            val veryLight = lightness > .75 && singleColor
            // Apply light background to mostly dark icons
            val veryDark = lightness < .35 && singleColor

            // Generate pleasant pastel colors for saturated icons
            if (hsl[1] > .5f && lightness > .2) {
                hsl[1] = 1f
                hsl[2] = .875f
                backgroundColor = ColorUtils.HSLToColor(hsl)
                onExitLoop()
                return
            }

            // Adjust color to reach suitable contrast depending on the relationship between the colors
            val opaqueSize = size - transparentScore
            val pxPerColor = opaqueSize / numColors.toFloat()
            val mixRatio = (pxPerColor / highScore)
                .coerceAtLeast(.15f)
                .coerceAtMost(.7f)

            // Vary color mix-in based on lightness and amount of colors
            val fill = if (light && !veryLight || veryDark) -0x1 else -0xcccccd
            backgroundColor = ColorUtils.blendARGB(bestRGB, fill, mixRatio)
        }
        onExitLoop()
    }

    private fun onExitLoop() {
        ranLoop = true
        result = genResult()
    }

    private fun genResult(): Drawable? {
        if (!shouldWrap) {
            return if (roundIcon != null) {
                if (icon is AdaptiveIconCompat &&
                    roundIcon !is AdaptiveIconCompat
                ) icon
                else roundIcon
            } else icon
        }
        if (icon is AdaptiveIconCompat) {
            if (!treatWhite || !isBackgroundWhite)
                return icon
            if ((icon as AdaptiveIconCompat).background is ColorDrawable) {
                val mutIcon = icon.mutate() as AdaptiveIconCompat
                (mutIcon.background as ColorDrawable).color = backgroundColor
                return mutIcon
            }
            return AdaptiveIconCompat(
                ColorDrawable(backgroundColor),
                (icon as AdaptiveIconCompat).foreground
            )
        }
        return genTmp()
    }

    private fun genTmp(): AdaptiveIconCompat? {
        initTmpIfNeeded()
        (tmp!!.foreground as FixedScaleDrawable).drawable = icon
        if (matchesMaskShape || isFullBleed || noMixinNeeded) {
            val scale: Float = if (noMixinNeeded) {
                val upScale = (width / aWidth).coerceAtMost(height / aHeight)
                NO_MIXIN_ICON_SCALE * upScale
            } else {
                val upScale = (width / aWidth).coerceAtLeast(height / aHeight)
                FULL_BLEED_ICON_SCALE * upScale
            }
            (tmp!!.foreground as FixedScaleDrawable).setScale(scale)
        } else {
            (tmp!!.foreground as FixedScaleDrawable).setScale(scale)
        }
        (tmp!!.background as ColorDrawable).color = backgroundColor
        return tmp
    }

    private fun initTmpIfNeeded() {
        if (tmp == null) {
            tmp = CustomIconProvider.getAdaptiveIconDrawableWrapper(context)
            tmp!!.setBounds(0, 0, 1, 1)
        }
    }

    fun getResult(): Drawable {
        if (!ranLoop) loop()
        return result!!
    }

    companion object {
        // Average number of derived colors (based on averages with ~100 icons and performance testing)
        private const val NUMBER_OF_COLORS_GUESSTIMATION = 45

        // Found after some experimenting, might be improved with some more testing
        private const val FULL_BLEED_ICON_SCALE = 1.44f

        // Found after some experimenting, might be improved with some more testing
        private const val NO_MIXIN_ICON_SCALE = 1.40f

        // Icons with less than 5 colors are considered as "single color"
        private const val SINGLE_COLOR_LIMIT = 5

        // Minimal alpha to be considered opaque
        private const val MIN_VISIBLE_ALPHA = 0xEF
    }

    init {
        this.icon = AdaptiveIconCompat.wrap(icon)
        this.roundIcon = AdaptiveIconCompat.wrapNullable(roundIcon)
        val prefs = Utilities.getOmegaPrefs(context)
        shouldWrap = prefs.enableLegacyTreatment
        extractColor = shouldWrap && prefs.colorizedLegacyTreatment
        treatWhite = extractColor && prefs.enableWhiteOnlyTreatment
    }
}