/*
 * Copyright (c) 2020 Omega Launcher
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.saggitt.omega.wallpaper

import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.max

class WallpaperBackgroundImageView(context: Context, attrs: AttributeSet?) :
        AppCompatImageView(context, attrs) {

    private val wallpaper = WallpaperPreviewProvider.getInstance(context).wallpaper
    private val wallpaperMatrix = Matrix()

    override fun draw(canvas: Canvas) {
        canvas.save()
        canvas.setMatrix(wallpaperMatrix)
        wallpaper.setBounds(0, 0, wallpaper.intrinsicWidth, wallpaper.intrinsicHeight)
        wallpaper.draw(canvas)
        canvas.restore()

        super.draw(canvas)
    }

    private fun resetMatrix() {
        wallpaperMatrix.reset()

        val width = wallpaper.intrinsicWidth
        val height = wallpaper.intrinsicHeight
        if (width > 0 && height > 0) {
            val scaleX = measuredWidth.toFloat() / width
            val scaleY = measuredHeight.toFloat() / height
            val scale = max(scaleX, scaleY)
            wallpaperMatrix.setScale(scale, scale)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val dm = resources.displayMetrics
        val screenAspectRatio = dm.heightPixels.toFloat() / dm.widthPixels
        setMeasuredDimension((measuredHeight / screenAspectRatio).toInt(), measuredHeight)
        resetMatrix()
    }
}