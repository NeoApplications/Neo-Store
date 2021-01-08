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

package com.saggitt.omega.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.SeekBar

class CustomGridView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs),
        SeekBar.OnSeekBarChangeListener {

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}

    override fun onStartTrackingTouch(seekBar: SeekBar) {

    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {}
    /*lateinit var currentValues: Values

    lateinit var gridCustomizer: InvariantDeviceProfile.GridCustomizer
    private var previewLoader: PreviewLoader? = null
        set(value) {
            field?.onFinishListener = null
            field = value
            field?.onFinishListener = ::onPreviewLoaded
            field?.loadPreview()
        }

    init {
        View.inflate(context, R.layout.custom_grid_view, this)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setInitialValues(values: Values) {
        currentValues = values
        heightSeekbar.progress = currentValues.height
        widthSeekbar.progress = currentValues.width
        numHotseatSeekbar.progress = currentValues.numHotseat
        workspacePaddingLeftSeekbar.progress = (currentValues.workspacePaddingScale.left * 100).toInt()
        workspacePaddingRightSeekbar.progress = (currentValues.workspacePaddingScale.right * 100).toInt()
        workspacePaddingTopSeekbar.progress = (currentValues.workspacePaddingScale.top * 100).toInt()
        workspacePaddingBottomSeekbar.progress = (currentValues.workspacePaddingScale.bottom * 100).toInt()
        heightSeekbar.let {
            it.min = 3
            it.max = 20
            it.setOnSeekBarChangeListener(this)
        }
        widthSeekbar.let {
            it.min = 3
            it.max = 20
            it.setOnSeekBarChangeListener(this)
        }
        numHotseatSeekbar.let {
            it.min = 3
            it.max = 9
            it.setOnSeekBarChangeListener(this)
        }
        workspacePaddingLeftSeekbar.let {
            it.min = 0
            it.max = 500
            it.setOnSeekBarChangeListener(this)
        }
        workspacePaddingRightSeekbar.let {
            it.min = 0
            it.max = 500
            it.setOnSeekBarChangeListener(this)
        }
        workspacePaddingTopSeekbar.let {
            it.min = 0
            it.max = 300
            it.setOnSeekBarChangeListener(this)
        }
        workspacePaddingBottomSeekbar.let {
            it.min = 0
            it.max = 300
            it.setOnSeekBarChangeListener(this)
        }
        updateText(currentValues)
        updatePreview()
    }

    private fun updateText(values: Values) {
        heightValue.text = "${values.height}"
        widthValue.text = "${values.width}"
        numHotseatValue.text = "${values.numHotseat}"
        workspacePaddingLeftValue.text = (values.workspacePaddingScale.left * 100).toInt().toString()
        workspacePaddingRightValue.text = (values.workspacePaddingScale.right * 100).toInt().toString()
        workspacePaddingTopValue.text = (values.workspacePaddingScale.top * 100).toInt().toString()
        workspacePaddingBottomValue.text = (values.workspacePaddingScale.bottom * 100).toInt().toString()
    }

    private fun updatePreview() {
        previewLoader = PreviewLoader(context, gridCustomizer)
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        val paddings = RectF(workspacePaddingLeftSeekbar.progress / 100f, workspacePaddingTopSeekbar.progress / 100f, workspacePaddingRightSeekbar.progress / 100f, workspacePaddingBottomSeekbar.progress / 100f)
        updateText(Values(heightSeekbar.progress, widthSeekbar.progress, numHotseatSeekbar.progress, paddings))
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {

    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        val paddings = RectF(workspacePaddingLeftSeekbar.progress / 100f, workspacePaddingTopSeekbar.progress / 100f, workspacePaddingRightSeekbar.progress / 100f, workspacePaddingBottomSeekbar.progress / 100f)
        setValues(Values(heightSeekbar.progress, widthSeekbar.progress, numHotseatSeekbar.progress, paddings))
    }

    fun setValues(newSize: Values) {
        if (currentValues != newSize) {
            currentValues = newSize
            updatePreview()
            heightSeekbar.progress = currentValues.height
            widthSeekbar.progress = currentValues.width
            numHotseatSeekbar.progress = currentValues.numHotseat
            workspacePaddingLeftSeekbar.progress = (currentValues.workspacePaddingScale.left * 100).toInt()
            workspacePaddingRightSeekbar.progress = (currentValues.workspacePaddingScale.right * 100).toInt()
            workspacePaddingTopSeekbar.progress = (currentValues.workspacePaddingScale.top * 100).toInt()
            workspacePaddingBottomSeekbar.progress = (currentValues.workspacePaddingScale.bottom * 100).toInt()
        }
    }

    private fun onPreviewLoaded(preview: Bitmap) {
        gridPreview.setImageDrawable(BitmapDrawable(resources, preview))
    }

    private class PreviewLoader(
            private val context: Context,
            private val gridCustomizer: InvariantDeviceProfile.GridCustomizer) {

        var onFinishListener: ((Bitmap) -> Unit)? = null

        fun loadPreview() {
            runOnUiWorkerThread {
                val preview = CustomGridProvider.getInstance(context).renderPreview(gridCustomizer).get()
                runOnMainThread {
                    onFinishListener?.invoke(preview)
                }
            }
        }
    }

    data class Values(
            val height: Int,
            val width: Int,
            val numHotseat: Int,
            val workspacePaddingScale: RectF)*/
}