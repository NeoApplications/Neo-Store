/*
 *     This file is part of Lawnchair Launcher.
 *
 *     Lawnchair Launcher is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Lawnchair Launcher is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Lawnchair Launcher.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.saggitt.omega.settings

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import android.view.View
import androidx.annotation.VisibleForTesting
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.preference.PreferenceGroup
import androidx.preference.PreferenceGroupAdapter
import androidx.preference.PreferenceViewHolder
import androidx.recyclerview.widget.RecyclerView
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.settings.SettingsActivity
import com.saggitt.omega.settings.SettingsActivity.BaseFragment

class HighlightablePreferenceGroupAdapter(
    preferenceGroup: PreferenceGroup, private val mHighlightKey: String?,
    var isHighlightRequested: Boolean
) : PreferenceGroupAdapter(preferenceGroup) {
    val mInvisibleBackground: Int

    @VisibleForTesting
    val mHighlightColor: Int
    private val mNormalBackgroundRes: Int

    @VisibleForTesting
    var mFadeInAnimated = false
    private var mHighlightPosition = RecyclerView.NO_POSITION
    override fun onBindViewHolder(holder: PreferenceViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        updateBackground(holder, position)
    }

    @VisibleForTesting
    fun updateBackground(holder: PreferenceViewHolder, position: Int) {
        val v = holder.itemView
        if (position == mHighlightPosition) {
            // This position should be highlighted. If it's highlighted before - skip animation.
            addHighlightBackground(v, !mFadeInAnimated)
        } else if (java.lang.Boolean.TRUE == v.getTag(R.id.preference_highlighted)) {
            // View with highlight is reused for a view that should not have highlight
            removeHighlightBackground(v, false /* animate */)
        }
    }

    fun requestHighlight(root: View, recyclerView: RecyclerView?) {
        if (isHighlightRequested || recyclerView == null || TextUtils.isEmpty(mHighlightKey)) {
            return
        }
        val count = itemCount
        for (i in 0 until count) {
            val pref = getItem(i)
        }
        val position = getPreferenceAdapterPosition(mHighlightKey)
        if (position < 0) {
            return
        }
        root.postDelayed({
            isHighlightRequested = true
            recyclerView.smoothScrollToPosition(position)
            mHighlightPosition = position
            notifyItemChanged(position)
        }, DELAY_HIGHLIGHT_DURATION_MILLIS)
    }

    @VisibleForTesting
    fun requestRemoveHighlightDelayed(v: View) {
        v.postDelayed({
            mHighlightPosition = RecyclerView.NO_POSITION
            removeHighlightBackground(v, true /* animate */)
        }, HIGHLIGHT_DURATION)
    }

    private fun addHighlightBackground(v: View, animate: Boolean) {
        v.setTag(R.id.preference_highlighted, true)
        if (!animate) {
            v.setBackgroundColor(mHighlightColor)
            Log.d(TAG, "AddHighlight: Not animation requested - setting highlight background")
            requestRemoveHighlightDelayed(v)
            return
        }
        mFadeInAnimated = true
        val colorFrom = mInvisibleBackground
        val colorTo = mHighlightColor
        val fadeInLoop = ValueAnimator.ofObject(
            ArgbEvaluator(), colorFrom, colorTo
        )
        fadeInLoop.duration = HIGHLIGHT_FADE_IN_DURATION
        fadeInLoop.addUpdateListener { animator: ValueAnimator -> v.setBackgroundColor(animator.animatedValue as Int) }
        fadeInLoop.repeatMode = ValueAnimator.REVERSE
        fadeInLoop.repeatCount = 4
        fadeInLoop.start()
        Log.d(TAG, "AddHighlight: starting fade in animation")
        requestRemoveHighlightDelayed(v)
    }

    private fun removeHighlightBackground(v: View, animate: Boolean) {
        if (!animate) {
            v.setTag(R.id.preference_highlighted, false)
            v.setBackgroundResource(mNormalBackgroundRes)
            Log.d(TAG, "RemoveHighlight: No animation requested - setting normal background")
            return
        }
        if (java.lang.Boolean.TRUE != v.getTag(R.id.preference_highlighted)) {
            // Not highlighted, no-op
            Log.d(TAG, "RemoveHighlight: Not highlighted - skipping")
            return
        }
        val colorFrom = mHighlightColor
        val colorTo = mInvisibleBackground
        v.setTag(R.id.preference_highlighted, false)
        val colorAnimation = ValueAnimator.ofObject(
            ArgbEvaluator(), colorFrom, colorTo
        )
        colorAnimation.duration = HIGHLIGHT_FADE_OUT_DURATION
        colorAnimation.addUpdateListener { animator: ValueAnimator -> v.setBackgroundColor(animator.animatedValue as Int) }
        colorAnimation.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                // Animation complete - the background is now white. Change to mNormalBackgroundRes
                // so it is white and has ripple on touch.
                v.setBackgroundResource(mNormalBackgroundRes)
            }
        })
        colorAnimation.start()
        Log.d(TAG, "Starting fade out animation")
    }

    companion object {
        @VisibleForTesting
        val DELAY_HIGHLIGHT_DURATION_MILLIS = 600L
        private const val TAG = "HighlightableAdapter"
        private const val HIGHLIGHT_DURATION = 15000L
        private const val HIGHLIGHT_FADE_OUT_DURATION = 500L
        private const val HIGHLIGHT_FADE_IN_DURATION = 200L

        /**
         * Tries to override initial expanded child count.
         *
         *
         * Initial expanded child count will be ignored if: 1. fragment contains request to highlight a
         * particular row. 2. count value is invalid.
         */
        fun adjustInitialExpandedChildCount(host: BaseFragment?) {
            if (host == null) {
                return
            }
            val screen = host.preferenceScreen ?: return
            val arguments = host.arguments
            if (arguments != null) {
                val highlightKey = arguments.getString(SettingsActivity.EXTRA_FRAGMENT_ARG_KEY)
                if (!TextUtils.isEmpty(highlightKey)) {
                    // Has highlight row - expand everything
                    screen.initialExpandedChildrenCount = Int.MAX_VALUE
                    return
                }
            }
            val initialCount = host.initialExpandedChildCount
            if (initialCount <= 0) {
                return
            }
            screen.initialExpandedChildrenCount = initialCount
        }
    }

    init {
        val context = preferenceGroup.context
        val outValue = TypedValue()
        context.theme.resolveAttribute(
            android.R.attr.selectableItemBackground,
            outValue, true /* resolveRefs */
        )
        mNormalBackgroundRes = outValue.resourceId
        context.theme.resolveAttribute(android.R.attr.windowBackground, outValue, true)
        mInvisibleBackground = ColorUtils
            .setAlphaComponent(ContextCompat.getColor(context, outValue.resourceId), 0)
        val accent = Utilities.getOmegaPrefs(context).accentColor
        mHighlightColor = ColorUtils.setAlphaComponent(accent, (255 * 0.26).toInt())
    }
}