@file:Suppress("PackageDirectoryMismatch")

package com.machiav3lli.fdroid.utility.extension.resources

import android.content.Context
import android.content.res.ColorStateList
import androidx.annotation.AttrRes
import androidx.core.content.ContextCompat

fun Context.getColorFromAttr(@AttrRes attrResId: Int): ColorStateList {
    val typedArray = obtainStyledAttributes(intArrayOf(attrResId))
    val (colorStateList, resId) = try {
        Pair(typedArray.getColorStateList(0), typedArray.getResourceId(0, 0))
    } finally {
        typedArray.recycle()
    }
    return colorStateList ?: ContextCompat.getColorStateList(this, resId)!!
}
