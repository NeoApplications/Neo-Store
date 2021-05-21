package com.saggitt.omega.util

import android.graphics.*
import kotlin.math.abs

// TODO not used, should be deleted?
class ColorManipulation {
    private lateinit var mPixels: IntArray
    private lateinit var mBitmap: Bitmap
    private var mCanvas: Canvas? = null
    private var mPaint: Paint? = null
    private val mMatrix: Matrix = Matrix()

    fun dB(ew: Bitmap?): Boolean {
        var mEw = ew
        var height = mEw!!.height
        var width = mEw.width
        if (height > 64 || width > 64) {
            if (mBitmap == null) {
                mBitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)
                mCanvas = Canvas(mBitmap)
                mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
                mPaint!!.isFilterBitmap = true
            }
            mMatrix.reset()
            mMatrix.setScale(64f / width, 64f / height, 0f, 0f)
            mCanvas!!.drawColor(0, PorterDuff.Mode.SRC)
            mCanvas!!.drawBitmap(mEw, mMatrix, mPaint)
            mEw = mBitmap
            width = 64
            height = 64
        }
        val bitmap: Bitmap = mEw
        val pixelCount = height * width
        resizeIfNecessary(pixelCount)
        bitmap.getPixels(mPixels, 0, width, 0, 0, width, height)
        for (i in 0 until pixelCount) {
            if (!dC(mPixels[i])) {
                return false
            }
        }
        return true
    }

    private fun resizeIfNecessary(pixelCount: Int) {
        if (mPixels.size < pixelCount) {
            mPixels = IntArray(pixelCount)
        }
    }

    companion object {
        fun dC(RGBA: Int): Boolean {
            val maxDiff = 20
            if (RGBA shr 24 and 0xFF < 50) {
                return true
            }
            val red = RGBA shr 16 and 0xFF
            val green = RGBA shr 8 and 0xFF
            val blue = RGBA and 0xFF
            var returnValue = true
            if (abs(red - green) < maxDiff && abs(red - blue) < maxDiff) {
                if (abs(green - blue) >= maxDiff) {
                    returnValue = false
                }
            } else {
                returnValue = false
            }
            return returnValue
        }
    }

}