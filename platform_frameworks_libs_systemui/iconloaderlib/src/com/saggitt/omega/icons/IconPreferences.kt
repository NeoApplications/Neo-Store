package com.saggitt.omega.icons

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette

val Context.prefs
    get() = applicationContext.getSharedPreferences(
        "com.saggitt.omega.alpha_preferences",
        Context.MODE_PRIVATE
    )!!

fun shouldWrapAdaptive(context: Context) = context.prefs.getBoolean("prefs_wrapAdaptive", false)

fun getWrapperBackgroundColor(context: Context, icon: Drawable): Int {
    val lightness = context.prefs.getFloat("pref_coloredBackgroundLightness", 0.9f)
    val palette = Palette.Builder(drawableToBitmap(icon)).generate()
    val dominantColor = palette.getDominantColor(Color.WHITE)
    return setLightness(dominantColor, lightness)
}

private fun setLightness(color: Int, lightness: Float): Int {
    if (color == Color.WHITE) {
        return color
    }
    val outHsl = floatArrayOf(0f, 0f, 0f)
    ColorUtils.colorToHSL(color, outHsl)
    outHsl[2] = lightness
    return ColorUtils.HSLToColor(outHsl)
}

fun drawableToBitmap(drawable: Drawable): Bitmap {
    if (drawable is BitmapDrawable) {
        return drawable.bitmap
    }

    val width = drawable.intrinsicWidth.coerceAtLeast(1)
    val height = drawable.intrinsicHeight.coerceAtLeast(1)
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}