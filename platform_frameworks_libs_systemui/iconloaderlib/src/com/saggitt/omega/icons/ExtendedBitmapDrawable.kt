package com.saggitt.omega.icons

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable

open class ExtendedBitmapDrawable(
    res: Resources,
    bitmap: Bitmap,
    val isFromIconPack: Boolean
) : BitmapDrawable(res, bitmap) {

    companion object {

        fun wrap(res: Resources, drawable: Drawable?, isFromIconPack: Boolean): Drawable? {
            return if (drawable is BitmapDrawable) {
                ExtendedBitmapDrawable(res, drawable.bitmap, isFromIconPack)
            } else {
                drawable
            }
        }

        @JvmStatic
        val Drawable.isFromIconPack
            get() = (this as? ExtendedBitmapDrawable)?.isFromIconPack ?: false
    }
}