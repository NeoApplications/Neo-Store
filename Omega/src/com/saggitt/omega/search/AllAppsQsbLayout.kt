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
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.RecyclerView
import com.android.launcher3.BaseRecyclerView
import com.android.launcher3.DeviceProfile
import com.android.launcher3.Insettable
import com.android.launcher3.LauncherAppState
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.allapps.AllAppsContainerView
import com.android.launcher3.allapps.SearchUiManager
import com.android.launcher3.icons.IconNormalizer
import com.saggitt.omega.OmegaLauncher.Companion.getLauncher
import com.saggitt.omega.preferences.views.PreferencesActivity
import com.saggitt.omega.search.providers.AppsSearchProvider
import com.saggitt.omega.util.Config
import kotlin.math.roundToInt

class AllAppsQsbLayout(context: Context, attrs: AttributeSet? = null) :
    AbstractQsbLayout(context, attrs), SearchUiManager, Insettable {

    var mDoNotRemoveFallback = false
    private val mVerticalOffset =
        resources.getDimensionPixelSize(R.dimen.all_apps_search_vertical_offset)

    private var mFallback: AllAppsQsbFallback? = null
    private lateinit var mAppsView: AllAppsContainerView
    private var mCancelButton: ImageButton? = null

    init {
        visibility = (if (prefs.drawerSearch.onGetValue()) View.VISIBLE else View.GONE)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        findViewById<ImageButton?>(R.id.settings_button).apply {
            setOnClickListener {
                val fragment = "com.saggitt.omega.preferences.views.PrefsSearchFragment"
                PreferencesActivity.startFragment(
                    context,
                    fragment,
                    context.resources.getString(R.string.title__general_search)
                )
            }
        }

        findViewById<ImageView?>(R.id.mic_icon).apply {
            if (!prefs.searchGlobal.onGetValue()) {
                visibility = View.GONE
            }
        }

        /*findViewById<ImageView?>(R.id.lens_icon).apply {
            if (!prefs.allAppsGlobalSearch) {
                visibility = View.GONE
            }
        }*/

        setOnClickListener {
            val provider = controller.searchProvider
            if (prefs.searchGlobal.onGetValue()) {
                provider.startSearch { intent: Intent? ->
                    mContext.startActivity(
                        intent,
                        ActivityOptionsCompat.makeClipRevealAnimation(this, 0, 0, width, height)
                            .toBundle()
                    )
                }
            } else {
                searchFallback("")
            }
        }

        mCancelButton = findViewById(R.id.search_cancel_button)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Update the width to match the grid padding
        val idp = LauncherAppState.getIDP(mContext)!!
        val dp: DeviceProfile = idp.getDeviceProfile(mContext)
        val myRequestedWidth = MeasureSpec.getSize(widthMeasureSpec)
        val rowWidth = (myRequestedWidth - mAppsView.activeRecyclerView.paddingLeft
                - mAppsView.activeRecyclerView.paddingRight)

        val cellWidth = DeviceProfile.calculateCellWidth(
            rowWidth, dp.cellLayoutBorderSpacingPx,
            dp.numShownHotseatIcons
        )
        val iconVisibleSize = (IconNormalizer.ICON_VISIBLE_AREA_FACTOR * dp.iconSizePx).roundToInt()
        val iconPadding = cellWidth - iconVisibleSize

        val myWidth = rowWidth - iconPadding + paddingLeft + paddingRight
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(myWidth, MeasureSpec.EXACTLY),
            heightMeasureSpec
        )
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        // Shift the widget horizontally so that its centered in the parent (b/63428078)
        val parent = parent as View
        val availableWidth = parent.width - parent.paddingLeft - parent.paddingRight
        val myWidth = right - left
        val expectedLeft = parent.paddingLeft + (availableWidth - myWidth) / 2
        val shift = expectedLeft - left
        translationX = shift.toFloat()

        var containerTopMargin = 0
        if (!prefs.drawerSearch.onGetValue()) {
            val mlp = layoutParams as MarginLayoutParams
            containerTopMargin = -(mlp.topMargin + mlp.height)
        }
        offsetTopAndBottom(mVerticalOffset - containerTopMargin)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        if (key == "pref_all_apps_global_search") {
            reloadPreferences(sharedPreferences)
        }
    }

    override fun getMicIcon(): Drawable? {
        return if (prefs.searchGlobal.onGetValue()) {
            if (searchProvider.supportsAssistant && mShowAssistant) {
                searchProvider.assistantIcon
            } else if (searchProvider.supportsVoiceSearch) {
                searchProvider.voiceIcon
            } else {
                micIconView?.visibility = View.GONE
                ColorDrawable(Color.TRANSPARENT)
            }
        } else {
            micIconView?.visibility = View.GONE
            ColorDrawable(Color.TRANSPARENT)
        }
    }

    override fun getIcon(): Drawable {
        return if (prefs.searchGlobal.onGetValue()) {
            super.getIcon()
        } else {
            AppsSearchProvider(mContext).icon
        }
    }

    override fun setInsets(insets: Rect?) {
        val mlp = layoutParams as MarginLayoutParams
        mlp.topMargin = insets!!.top
        requestLayout()
    }

    override fun initializeSearch(allAppsContainerView: AllAppsContainerView) {
        mAppsView = allAppsContainerView
        mAppsView.addElevationController(object : RecyclerView.OnScrollListener() {
            var initialElevation = 1f
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (mFallback != null) {
                    initialElevation = mFallback!!.elevation
                }
                val currentScrollY = (recyclerView as BaseRecyclerView).currentScrollY
                val elevationScale = Utilities.boundToRange(currentScrollY / 255f, 0f, 1f)
                if (prefs.drawerLayout.onGetValue() != Config.DRAWER_PAGED)
                    mFallback?.elevation = initialElevation + elevationScale * initialElevation
            }
        })
    }

    override fun resetSearch() {
        if (!mDoNotRemoveFallback) {
            removeFallbackView()
        }
    }

    override fun startSearch() {
        post { startSearch("") }
    }

    override fun startSearch(str: String?) {
        val provider = SearchProviderController.getInstance(mContext).searchProvider
        if (shouldUseFallbackSearch(provider)) {
            searchFallback(str)
        } else {
            provider.startSearch { intent: Intent? ->
                getLauncher(context).startActivity(intent)
            }
        }
    }

    private fun searchFallback(query: String?) {
        ensureFallbackView()
        mFallback?.setText(query)
        mFallback?.showKeyboard()
    }

    private fun ensureFallbackView() {
        if (mFallback == null) {
            mFallback = getLauncher().layoutInflater
                .inflate(
                    R.layout.search_container_all_apps_fallback,
                    this,
                    false
                ) as AllAppsQsbFallback
            val allAppsContainerView: AllAppsContainerView = mAppsView
            mFallback!!.allAppsQsbLayout = this
            mFallback!!.setCancelButton(mCancelButton!!)
            mFallback!!.initializeSearch(allAppsContainerView)
            addView(mFallback)
        }
    }

    private fun removeFallbackView() {
        if (mFallback != null) {
            mFallback!!.clearSearchResult()
            removeView(mFallback)
            mFallback = null
        }
    }

    private fun shouldUseFallbackSearch(provider: SearchProvider) =
        !prefs.searchGlobal.onGetValue()
                || provider is AppsSearchProvider
                || provider is WebSearchProvider
}