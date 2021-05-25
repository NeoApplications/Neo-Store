/*
 *  This file is part of Omega Launcher
 *  Copyright (c) 2021   Saul Henriquez
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
package com.saggitt.omega.dash

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.RelativeLayout
import com.saggitt.omega.dash.DashItemAdapter.DashItemChangeListener
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class DashListView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttrs: Int = 0
) : RelativeLayout(context, attrs, defStyleAttrs), DashItemChangeListener {
    private var itemWidth = 0f
    private var itemHeight = 0f
    private var layoutWidth = 0f
    private var layoutHeight = 0f
    private var layoutCenterX = 0f
    private var layoutCenterY = 0f
    var radius = 0f
    private var itemViewList: ArrayList<View>? = null
    private var intervalAngle = PI / 4
    private var preIntervalAngle = PI / 4
    private var dashAdapter: DashItemAdapter? = null
    private val moveAccumulator = 1

    init {
        post {
            Log.d("CircularListView", "get layout width and height")
            layoutWidth = width.toFloat()
            layoutHeight = height.toFloat()
            layoutCenterX = layoutWidth / 2
            layoutCenterY = layoutHeight / 2
            radius = layoutWidth / 3
        }
        itemViewList = ArrayList()
    }

    fun altSetRadius(r: Float) {
        radius = if (r < 0) 0f else r
        if (dashAdapter != null) dashAdapter!!.notifyItemChange()
    }

    fun setAdapter(adapter: DashItemAdapter?) {
        // register item change listener
        dashAdapter = adapter
        dashAdapter!!.setOnItemChangeListener(this)
        setItemPosition()
    }

    override fun onDashItemChange() {
        setItemPosition()
    }

    private fun setItemPosition() {
        val itemCount = dashAdapter!!.count
        val existChildCount = childCount
        val isLayoutEmpty = existChildCount == 0
        preIntervalAngle = if (isLayoutEmpty) 0.0 else 2.0f * PI / existChildCount.toDouble()
        intervalAngle = 2.0f * PI / itemCount.toDouble()

        // add all item view into parent layout

        for (i in 0 until itemCount) {
            val item = dashAdapter!!.getItemAt(i)

            // add item if no parent
            if (item.parent == null) {
                item.visibility = INVISIBLE
                addView(item)
                println("do add :$item")
            }

            // wait for view drawn to get width and height
            item.post {
                itemWidth = item.width.toFloat()
                itemHeight = item.height.toFloat()
                /*
                 * position items according to circle formula
                 * margin left -> x = h + r * cos(theta)
                 * margin top -> y = k + r * sin(theta)
                 *
                 */
                val valueAnimator = ValueAnimator()
                valueAnimator.setFloatValues(preIntervalAngle.toFloat(), intervalAngle.toFloat())
                valueAnimator.duration = 500
                valueAnimator.interpolator = OvershootInterpolator()
                valueAnimator.addUpdateListener { animation: ValueAnimator ->
                    val value = animation.animatedValue as Float
                    val params = item.layoutParams as LayoutParams
                    params.setMargins(
                        (layoutCenterX - itemWidth / 2 + radius *
                                cos(i * value + moveAccumulator * PI * 2)).toInt(),
                        (layoutCenterY - itemHeight / 2 + radius *
                                sin(i * value + moveAccumulator * PI * 2)).toInt(),
                        0,
                        0
                    )
                    item.layoutParams = params
                }
                valueAnimator.start()
                item.visibility = VISIBLE
            }
        }

        // remove item from parent if it has been remove from list
        for (i in itemViewList!!.indices) {
            val itemAfterChanged = itemViewList!![i]
            if (dashAdapter!!.allViews.indexOf(itemAfterChanged) == -1) {
                println("do remove :$itemAfterChanged")
                removeView(itemAfterChanged)
            }
        }
        itemViewList = dashAdapter!!.allViews.clone() as ArrayList<View>
    }
}