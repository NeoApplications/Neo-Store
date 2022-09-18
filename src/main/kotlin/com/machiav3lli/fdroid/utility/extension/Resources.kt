@file:Suppress("PackageDirectoryMismatch")

package com.machiav3lli.fdroid.utility.extension.resources

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.textview.MaterialTextView
import kotlin.math.roundToInt

fun Context.getColorFromAttr(@AttrRes attrResId: Int): ColorStateList {
    val typedArray = obtainStyledAttributes(intArrayOf(attrResId))
    val (colorStateList, resId) = try {
        Pair(typedArray.getColorStateList(0), typedArray.getResourceId(0, 0))
    } finally {
        typedArray.recycle()
    }
    return colorStateList ?: ContextCompat.getColorStateList(this, resId)!!
}

fun Context.getDrawableCompat(@DrawableRes resId: Int): Drawable =
    ResourcesCompat.getDrawable(resources, resId, theme) ?: ContextCompat.getDrawable(this, resId)!!


fun Resources.sizeScaled(size: Int): Int {
    return (size * displayMetrics.density).roundToInt()
}

fun MaterialTextView.setTextSizeScaled(size: Int) {
    val realSize = (size * resources.displayMetrics.scaledDensity).roundToInt()
    setTextSize(TypedValue.COMPLEX_UNIT_PX, realSize.toFloat())
}
