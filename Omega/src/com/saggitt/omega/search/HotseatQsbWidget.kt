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

package com.saggitt.omega.search

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.view.animation.Interpolator
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.launcher3.*
import com.android.launcher3.InvariantDeviceProfile.OnIDPChangeListener
import com.android.launcher3.LauncherState.HOTSEAT_SEARCH_BOX
import com.android.launcher3.allapps.AllAppsContainerView
import com.android.launcher3.allapps.SearchUiManager
import com.android.launcher3.anim.PropertySetter
import com.google.android.apps.nexuslauncher.qsb.AbstractQsbLayout
import com.google.android.apps.nexuslauncher.qsb.FallbackAppsSearchView
import com.google.android.apps.nexuslauncher.qsb.QsbChangeListener
import com.google.android.apps.nexuslauncher.qsb.QsbConfiguration
import com.saggitt.omega.search.providers.AppsSearchProvider

/*
class HotseatQsbWidget(context: Context,  attrs: AttributeSet): AbstractQsbLayout(context, attrs),
    QsbChangeListener, OnIDPChangeListener, SearchUiManager{

    private var qsbConfiguration: QsbConfiguration? = null
    private var hintTextView: TextView? = null
    private var mShadowAlpha = 0
    private var shadowBitmap: Bitmap? = null
    private var mAppsView: AllAppsContainerView? = null
    private var mLowPerformanceMode = false
    private var mTopAdjusting = 0
    private var mVerticalOffset = 0
    private val mFixedTranslationY: Int = resources.getDimensionPixelSize(R.dimen.qsb_widget_height) / 2
    private val mMarginTopAdjusting: Int = resources.getDimensionPixelSize(R.dimen.search_widget_top_shift)

    init {
        qsbConfiguration = QsbConfiguration.getInstance(mContext)
        mLowPerformanceMode = prefs.lowPerformanceMode
        mTopAdjusting = resources.getDimensionPixelSize(R.dimen.qsb_margin_top_adjusting)
        mVerticalOffset = resources.getDimensionPixelSize(R.dimen.all_apps_search_vertical_offset)
    }

    override fun initialize(allAppsContainerView: AllAppsContainerView) {
        mAppsView = allAppsContainerView
        mAppsView!!.addElevationController(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                setShadowAlpha((recyclerView as BaseRecyclerView).currentScrollY)
            }
        })
        mAppsView!!.setRecyclerViewVerticalFadingEdgeEnabled(!mLowPerformanceMode)
    }

    override fun resetSearch() {
        setShadowAlpha(0)
    }

    override fun preDispatchKeyEvent(event: KeyEvent) {

    }

    override fun setTextSearchEnabled(isEnabled: Boolean): EditText? {
        // TODO: Implement
        return null
    }

    override fun setInsets(insets: Rect) {
        visibility = if (mActivity.deviceProfile.isVerticalBarLayout) View.GONE else View.VISIBLE
        val mlp: MarginLayoutParams = layoutParams as MarginLayoutParams
        mlp.topMargin = (-mFixedTranslationY).coerceAtLeast(insets.top - mMarginTopAdjusting)
        val padding: Rect = mActivity.deviceProfile.hotseatLayoutPadding
        setPaddingUnchecked(padding.left, 0, padding.right, 0)
        requestLayout()
    }

    override fun getScrollRangeDelta(insets: Rect): Float {
        return if (mActivity.deviceProfile.isVerticalBarLayout) {
            0.0f
        } else {
            val dp: DeviceProfile = mActivity.deviceProfile
            val percentageOfAvailSpaceFromBottom = 0.45f
            val center = ((dp.hotseatBarSizePx - dp.hotseatCellHeightPx
                    - layoutParams.height - insets.bottom) * percentageOfAvailSpaceFromBottom).toInt()
            val bottomMargin = insets.bottom + center
            val topMargin = (-mFixedTranslationY).coerceAtLeast(insets.top - mMarginTopAdjusting)
            val myBot: Int = layoutParams.height + topMargin + mFixedTranslationY
            (bottomMargin + myBot).toFloat()
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        offsetTopAndBottom(mFixedTranslationY)
    }


    protected fun setPaddingUnchecked(left: Int, top: Int, right: Int, bottom: Int) {
        super.setPadding(left, top, right, bottom)
    }

    override fun setContentVisibility(visibleElements: Int, setter: PropertySetter, interpolator: Interpolator?) {
        val hotseatQsbEnabled = prefs.dockSearchBar
        val hotseatQsbVisible = visibleElements and HOTSEAT_SEARCH_BOX !== 0
        val qsbVisible = hotseatQsbEnabled && hotseatQsbVisible
        setter.setViewAlpha(this, (if (qsbVisible) 1 else 0).toFloat(), interpolator)
    }

    override fun startSearch() {
        post { startSearch("", mResult) }
    }

    override fun startSearch(str: String, result: Int) {
        startHotseatSearch(str, result)
    }

    private fun startHotseatSearch(str: String, result: Int) {
    }

    override fun onChange() {
        updateConfiguration()
        invalidate()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        hintTextView = findViewById(R.id.qsb_hint)
    }
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        LauncherAppState.getIDP(context).addOnChangeListener(this)
        updateConfiguration()
        qsbConfiguration!!.addListener(this)
    }

    override fun onDetachedFromWindow() {
        LauncherAppState.getIDP(context).removeOnChangeListener(this)
        qsbConfiguration!!.removeListener(this)
        super.onDetachedFromWindow()
    }

    private fun updateConfiguration() {
        setBubbleBgColor(mAllAppsBgColor)
        addOrUpdateSearchPaint(qsbConfiguration!!.micStrokeWidth())
        showHintAssitant = qsbConfiguration!!.hintIsForAssistant()
        setHintText(qsbConfiguration!!.hintTextValue(), hintTextView)
        addOrUpdateSearchRipple()
    }

    override fun onIdpChanged(changeFlags: Int, profile: InvariantDeviceProfile?) {
        if (changeFlags and InvariantDeviceProfile.CHANGE_FLAG_ICON_PARAMS != 0) {
            mClearBitmap = null
            mBubbleShadowBitmap = mClearBitmap
            mAllAppsShadowBitmap = mBubbleShadowBitmap
            addOrUpdateSearchRipple()
        }
    }

    override fun useTwoBubbles(): Boolean {
        return (mMicFrame != null && mMicFrame.visibility == VISIBLE && prefs.dualBubbleSearch)
    }

    override fun drawQsb(canvas: Canvas) {
        if (mShadowAlpha > 0) {
            if (shadowBitmap == null) {
                shadowBitmap = createShadowBitmap(
                    resources.getDimension(R.dimen.hotseat_qsb_scroll_shadow_blur_radius),
                    resources.getDimension(R.dimen.hotseat_qsb_scroll_key_shadow_offset),
                    0, true
                )
            }
            mShadowHelper.paint.alpha = mShadowAlpha
            drawShadow(shadowBitmap, canvas)
            mShadowHelper.paint.alpha = 255
        }
        super.drawQsb(canvas)
    }

    fun setShadowAlpha(mAlpha: Int) {
        val alpha = Utilities.boundToRange(mAlpha, 0, 255)
        if (mShadowAlpha != alpha) {
            mShadowAlpha = alpha
            invalidate()
        }
    }

    class HotseatQsbFragment : QsbContainerView.QsbFragment() {
        override fun isQsbEnabled(): Boolean {
            return true
        }
    }
}

*/

class HotseatQsbWidget(context: Context, attrs: AttributeSet) : AbstractQsbLayout(context, attrs),
    SearchUiManager, QsbChangeListener,
    OnIDPChangeListener {
    private var qsbConfiguration: QsbConfiguration? = QsbConfiguration.getInstance(mContext)
    private var mLowPerformanceMode = prefs.lowPerformanceMode
    private var mVerticalOffset =
        resources.getDimensionPixelSize(R.dimen.all_apps_search_vertical_offset)
    var mDoNotRemoveFallback = false
    private var mShadowAlpha = 0
    private var shadowBitmap: Bitmap? = null
    private var mUseFallbackSearch = false
    private var mFallback: FallbackAppsSearchView? = null
    private var hintTextView: TextView? = null
    private var mAppsView: AllAppsContainerView? = null
    private val mFixedTranslationY: Int =
        resources.getDimensionPixelSize(R.dimen.qsb_widget_height) / 2
    private val mMarginTopAdjusting: Int =
        resources.getDimensionPixelSize(R.dimen.search_widget_top_shift)

    init {
        setOnClickListener(this)
        clipToPadding = false
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        hintTextView = findViewById(R.id.qsb_hint)
    }

    override fun getScrollRangeDelta(insets: Rect): Float {
        return if (mActivity.deviceProfile.isVerticalBarLayout) {
            0.0f
        } else {
            val dp: DeviceProfile = mActivity.deviceProfile
            val percentageOfAvailSpaceFromBottom = 0.45f
            val center = ((dp.hotseatBarSizePx - dp.hotseatCellHeightPx
                    - layoutParams.height - insets.bottom) * percentageOfAvailSpaceFromBottom).toInt()
            val bottomMargin = insets.bottom + center
            val topMargin = (-mFixedTranslationY).coerceAtLeast(insets.top - mMarginTopAdjusting)
            val myBot: Int = layoutParams.height + topMargin + mFixedTranslationY
            (bottomMargin + myBot).toFloat()
        }
    }

    override fun setInsets(insets: Rect) {
        visibility = if (mActivity.deviceProfile.isVerticalBarLayout) View.GONE else View.VISIBLE
        val mlp: MarginLayoutParams = layoutParams as MarginLayoutParams
        mlp.topMargin = (-mFixedTranslationY).coerceAtLeast(insets.top - mMarginTopAdjusting)
        val padding: Rect = mActivity.deviceProfile.hotseatLayoutPadding
        setPaddingUnchecked(padding.left, 0, padding.right, 0)
        requestLayout()
    }

    private fun setPaddingUnchecked(left: Int, top: Int, right: Int, bottom: Int) {
        super.setPadding(left, top, right, bottom)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        LauncherAppState.getIDP(context).addOnChangeListener(this)
        updateConfiguration()
        qsbConfiguration!!.addListener(this)
    }

    override fun onDetachedFromWindow() {
        LauncherAppState.getIDP(context).removeOnChangeListener(this)
        qsbConfiguration!!.removeListener(this)
        super.onDetachedFromWindow()
    }

    override fun onIdpChanged(changeFlags: Int, profile: InvariantDeviceProfile) {
        if (changeFlags and InvariantDeviceProfile.CHANGE_FLAG_ICON_PARAMS != 0) {
            mClearBitmap = null
            mBubbleShadowBitmap = mClearBitmap
            mAllAppsShadowBitmap = mBubbleShadowBitmap
            addOrUpdateSearchRipple()
        }
    }

    override fun useTwoBubbles(): Boolean {
        return (mMicFrame != null && mMicFrame.visibility == VISIBLE && prefs.dualBubbleSearch)
    }

    override fun getIcon(colored: Boolean): Drawable {
        return if (prefs.allAppsGlobalSearch) {
            super.getIcon(colored)
        } else {
            AppsSearchProvider(context).getIcon(colored)
        }
    }

    override fun initialize(allAppsContainerView: AllAppsContainerView) {
        mAppsView = allAppsContainerView
        mAppsView!!.addElevationController(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                setShadowAlpha((recyclerView as BaseRecyclerView).currentScrollY)
            }
        })
        mAppsView!!.setRecyclerViewVerticalFadingEdgeEnabled(!mLowPerformanceMode)
    }

    override fun onChange() {
        updateConfiguration()
        invalidate()
    }

    private fun updateConfiguration() {
        setBubbleBgColor(mAllAppsBgColor)
        addOrUpdateSearchPaint(qsbConfiguration!!.micStrokeWidth())
        showHintAssitant = qsbConfiguration!!.hintIsForAssistant()
        setHintText(qsbConfiguration!!.hintTextValue(), hintTextView)
        addOrUpdateSearchRipple()
    }

    override fun onClick(view: View) {
        super.onClick(view)
        startSearch("", mResult)
    }

    override fun startSearch() {
        post { startSearch("", mResult) }
    }

    override fun startSearch(str: String, result: Int) {
        startHotseatSearch(str, result)
    }

    private fun startHotseatSearch(str: String, result: Int) {

    }

    override fun resetSearch() {
        setShadowAlpha(0)
        if (mUseFallbackSearch) {
            resetFallbackView()
        } else if (!mDoNotRemoveFallback) {
            removeFallbackView()
        }
    }

    private fun removeFallbackView() {
        if (mFallback != null) {
            mFallback!!.clearSearchResult()
            setOnClickListener(this)
            removeView(mFallback)
            mFallback = null
        }
    }

    private fun resetFallbackView() {
        if (mFallback != null) {
            mFallback!!.reset()
            mFallback!!.clearSearchResult()
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        offsetTopAndBottom(mFixedTranslationY)
    }

    override fun drawQsb(canvas: Canvas) {
        if (mShadowAlpha > 0) {
            if (shadowBitmap == null) {
                shadowBitmap = createShadowBitmap(
                    resources.getDimension(R.dimen.hotseat_qsb_scroll_shadow_blur_radius),
                    resources.getDimension(R.dimen.hotseat_qsb_scroll_key_shadow_offset),
                    0, true
                )
            }
            mShadowHelper.paint.alpha = mShadowAlpha
            drawShadow(shadowBitmap, canvas)
            mShadowHelper.paint.alpha = 255
        }
        super.drawQsb(canvas)
    }

    fun setShadowAlpha(mAlpha: Int) {
        val alpha = Utilities.boundToRange(mAlpha, 0, 255)
        if (mShadowAlpha != alpha) {
            mShadowAlpha = alpha
            invalidate()
        }
    }

    override fun dK(): Boolean {
        return if (mFallback != null) {
            false
        } else super.dK()
    }

    override fun hasOverlappingRendering(): Boolean {
        return false
    }

    override fun preDispatchKeyEvent(keyEvent: KeyEvent) {}

    override fun clearMainPillBg(canvas: Canvas) {}

    override fun clearPillBg(canvas: Canvas, left: Int, top: Int, right: Int) {
        if (!mLowPerformanceMode && mClearBitmap != null) {
            mClearShadowHelper.draw(
                mClearBitmap,
                canvas,
                left.toFloat(),
                top.toFloat(),
                right.toFloat()
            )
        }
    }

    override fun setContentVisibility(
        visibleElements: Int,
        setter: PropertySetter,
        interpolator: Interpolator
    ) {
        val hotseatQsbEnabled = prefs.dockSearchBar
        val hotseatQsbVisible = visibleElements and HOTSEAT_SEARCH_BOX !== 0
        val qsbVisible = hotseatQsbEnabled && hotseatQsbVisible
        setter.setViewAlpha(this, (if (qsbVisible) 1 else 0).toFloat(), interpolator)
    }

    override fun isQsbVisible(visibleElements: Int): Boolean {
        return false
    }

    override fun setTextSearchEnabled(isEnabled: Boolean): EditText? {
        return null
    }
}