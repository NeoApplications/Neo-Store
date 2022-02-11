/*
 * This file is part of Omega Launcher
 * Copyright (c) 2022   Omega Launcher Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saggitt.omega.search

import android.content.Context
import android.content.SharedPreferences
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.FrameLayout
import android.widget.ImageView
import com.android.launcher3.R
import com.android.launcher3.ResourceUtils
import com.android.launcher3.Utilities
import com.android.launcher3.graphics.IconShape
import com.saggitt.omega.preferences.OmegaPreferences
import kotlin.math.round

abstract class AbstractQsbLayout(context: Context, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs),
    SearchProviderController.OnProviderChangeListener,
    SharedPreferences.OnSharedPreferenceChangeListener {

    protected val mContext = context

    private var mMicFrame: FrameLayout? = null
    private var mLogoIconView: ImageView? = null
    private var mMicIconView: ImageView? = null

    protected var mPrefs: OmegaPreferences = Utilities.getOmegaPrefs(context)
    protected var sharedPrefs: SharedPreferences = Utilities.getPrefs(context)

    private var mShowAssistant = false
    protected var mIsRtl = false
    private var mResult = 0
    private var mShadowMargin = 0
    private var mSearchIconWidth = 0

    protected var searchProvider: SearchProvider? = null

    init {
        mSearchIconWidth = resources.getDimensionPixelSize(R.dimen.qsb_mic_width)
        mShadowMargin = resources.getDimensionPixelSize(R.dimen.qsb_shadow_margin)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        loadIcons()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        sharedPrefs.registerOnSharedPreferenceChangeListener(this)
        SearchProviderController.getInstance(mContext).addOnProviderChangeListener(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        SearchProviderController.getInstance(context).removeOnProviderChangeListener(this)
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        when (key) {
            "opa_enabled",
            "opa_assistant",
            "pref_bubbleSearchStyle",
            "pref_searchbarRadius" ->
                reloadPreferences(sharedPreferences)
        }
    }

    override fun onSearchProviderChanged() {
        reloadPreferences(sharedPrefs)
    }

    abstract fun startSearch(query: String?, result: Int)

    protected fun loadIcons() {
        mLogoIconView = findViewById<ImageView?>(R.id.search_logo).apply {
            setOnClickListener {
                val controller = SearchProviderController.getInstance(mContext)
                val provider = controller.searchProvider
                if (provider.supportsFeed) {
                    provider.startFeed { intent ->
                        mContext.startActivity(intent)
                    }
                } else {
                    startSearch("", mResult)
                }
            }
        }
        mMicIconView = findViewById<ImageView?>(R.id.mic_icon).apply {
            setOnClickListener {
                val controller = SearchProviderController.getInstance(mContext)
                val provider = controller.searchProvider
                if (mShowAssistant && provider.supportsAssistant) {
                    provider.startAssistant { intent ->
                        mContext.startActivity(intent)
                    }
                } else if (provider.supportsVoiceSearch) {
                    provider.startVoiceSearch { intent ->
                        mContext.startActivity(intent)
                    }
                }
            }
        }


        mMicFrame = findViewById(R.id.mic_frame)
    }

    protected open fun reloadPreferences(sharedPrefs: SharedPreferences) {
        post {
            invalidate()
        }
    }

    fun getCornerRadius(): Float {
        return getCornerRadius(
            context, ResourceUtils.pxFromDp(100f, resources.displayMetrics).toFloat()
        )
    }

    fun getCornerRadius(context: Context, defaultRadius: Float): Float {
        val radius: Float = round(mPrefs.searchBarRadius)
        if (radius > 0f) {
            return radius
        }
        val edgeRadius: TypedValue? = IconShape.getShape().getAttrValue(R.attr.qsbEdgeRadius)
        return edgeRadius?.getDimension(context.resources.displayMetrics) ?: defaultRadius
    }
}