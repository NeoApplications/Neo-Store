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

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import com.android.launcher3.R
import com.android.launcher3.ResourceUtils
import com.android.launcher3.Utilities
import com.android.launcher3.graphics.IconShape
import com.saggitt.omega.preferences.OmegaPreferences
import com.saggitt.omega.util.Config
import kotlin.math.round


abstract class AbstractQsbLayout(context: Context, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs), SharedPreferences.OnSharedPreferenceChangeListener {
    var mContext: Context = context
    protected var prefs: OmegaPreferences = Utilities.getOmegaPrefs(mContext)
    protected var controller = SearchProviderController.getInstance(getContext())
    protected var searchProvider: SearchProvider = controller.searchProvider
    private var mRadius = -1.0f
    private var mShowAssistant = false
    protected var mIsRtl = Utilities.isRtl(resources)

    private var micIconView: ImageView? = null
    private var searchLogoView: ImageView? = null
    private var lensIconView: ImageView? = null
    private var mShadowMargin = 0

    var micStrokeWidth = 0f
    protected var mMicStrokePaint: Paint? = null

    init {
        mShadowMargin = resources.getDimensionPixelSize(R.dimen.qsb_shadow_margin);
        mMicStrokePaint = Paint(1);
        mMicStrokePaint?.setColor(Color.WHITE);
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        searchLogoView = findViewById<AppCompatImageView?>(R.id.search_engine_logo).apply {
            setImageDrawable(searchProvider.icon)
            setOnClickListener {
                if (searchProvider.supportsFeed) {
                    searchProvider.startFeed { intent ->
                        mContext.startActivity(intent)
                    }
                } else {
                    mContext.startActivity(
                        Intent("android.search.action.GLOBAL_SEARCH").addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK or
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK
                        ).setPackage(searchProvider.packageName)
                    )
                }
            }
        }

        micIconView = findViewById<AppCompatImageView?>(R.id.mic_icon).apply {
            if (searchProvider.supportsVoiceSearch) {
                if (searchProvider.supportsAssistant) {
                    setImageDrawable(searchProvider.assistantIcon)
                    setOnClickListener {
                        searchProvider.startAssistant { intent -> mContext.startActivity(intent) }
                    }
                } else {
                    setImageDrawable(searchProvider.voiceIcon)
                    setOnClickListener {
                        searchProvider.startVoiceSearch { intent -> mContext.startActivity(intent) }
                    }
                }
                visibility = View.VISIBLE
            } else {
                visibility = View.GONE
            }
        }

        lensIconView = findViewById<ImageView?>(R.id.lens_icon).apply {
            val lensIntent = Intent.makeMainActivity(
                ComponentName(
                    Config.LENS_PACKAGE,
                    Config.LENS_ACTIVITY
                )
            ).addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            )

            if (searchProvider.packageName == Config.GOOGLE_QSB && mContext.packageManager.resolveActivity(
                    lensIntent,
                    0
                ) != null
            ) {
                setImageResource(R.drawable.ic_lens_color)

                setOnClickListener {
                    mContext.startActivity(lensIntent)
                }
                visibility = View.VISIBLE
            } else {
                visibility = View.GONE
            }
        }

        setOnClickListener { view: View? ->
            mContext.startActivity(
                Intent("android.search.action.GLOBAL_SEARCH").addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
                ).setPackage(searchProvider.packageName)
            )
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        getDevicePreferences().registerOnSharedPreferenceChangeListener(this)

        addOrUpdateSearchRipple()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        getDevicePreferences().unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        when (key) {
            "opa_enabled",
            "opa_assistant",
            "pref_searchbarRadius" ->
                reloadPreferences(sharedPreferences)
        }
    }

    /*
    * Create the search background when clicked
    * */
    private fun addOrUpdateSearchRipple() {
        val insetDrawable: InsetDrawable = createRipple().mutate() as InsetDrawable
        background = insetDrawable

        val oldRipple: RippleDrawable = insetDrawable.drawable as RippleDrawable
        oldRipple.setLayerInset(0, 0, 0, 0, 0)

        searchLogoView?.apply {
            val searchIconRipple: RippleDrawable =
                oldRipple.constantState!!.newDrawable().mutate() as RippleDrawable
            searchIconRipple.setLayerInset(0, 0, 2, 0, 2);
            background = searchIconRipple
            setPadding(8, 8, 8, 8)
        }

        micIconView?.apply {
            val micIconRipple: RippleDrawable =
                oldRipple.constantState!!.newDrawable().mutate() as RippleDrawable
            micIconRipple.setLayerInset(0, 0, 2, 0, 2);
            background = micIconRipple
            setPadding(12, 12, 12, 12)
        }

        lensIconView?.apply {
            val lensIconRipple: RippleDrawable =
                oldRipple.constantState!!.newDrawable().mutate() as RippleDrawable
            lensIconRipple.setLayerInset(0, 0, 2, 0, 2);
            background = lensIconRipple
            setPadding(12, 12, 12, 12)
        }
        lensIconView?.requestLayout()
    }

    private fun createRipple(): InsetDrawable {
        val shape = GradientDrawable()
        shape.shape = GradientDrawable.RECTANGLE
        shape.cornerRadius = getCornerRadius()
        shape.setColor(ContextCompat.getColor(context, R.color.colorAccent))
        val rippleColor: ColorStateList =
            ContextCompat.getColorStateList(context, R.color.focused_background)!!
        val ripple = RippleDrawable(rippleColor, null, shape)
        return InsetDrawable(ripple, resources.getDimensionPixelSize(R.dimen.qsb_shadow_margin))
    }

    protected fun getDevicePreferences(): SharedPreferences {
        val devicePrefs = Utilities.getPrefs(context)
        reloadPreferences(devicePrefs)
        return devicePrefs
    }

    private fun reloadPreferences(sharedPreferences: SharedPreferences) {
        post {
            searchProvider = SearchProviderController.getInstance(mContext).searchProvider
            mShowAssistant = sharedPreferences.getBoolean("opa_assistant", true)
            searchLogoView?.setImageDrawable(getIcon())
            micIconView?.visibility = View.VISIBLE
            micIconView?.setImageDrawable(getMicIcon())
            mRadius = getCornerRadius()

            invalidate()
        }
    }

    protected open fun getCornerRadius(): Float {
        val defaultRadius = ResourceUtils.pxFromDp(100f, resources.displayMetrics).toFloat()
        val radius: Float = round(Utilities.getOmegaPrefs(context).searchBarRadius)
        if (radius >= 0f) {
            return radius
        }
        val edgeRadius: TypedValue? = IconShape.getShape().getAttrValue(R.attr.qsbEdgeRadius)
        return edgeRadius?.getDimension(context.resources.displayMetrics) ?: defaultRadius
    }

    private fun getMicIcon(): Drawable? {
        return if (prefs.allAppsGlobalSearch) {
            if (searchProvider.supportsAssistant && mShowAssistant) {
                searchProvider.getAssistantIcon(true)
            } else if (searchProvider.supportsVoiceSearch) {
                searchProvider.getVoiceIcon(true)
            } else {
                micIconView?.visibility = GONE
                ColorDrawable(Color.TRANSPARENT)
            }
        } else {
            micIconView?.visibility = GONE
            ColorDrawable(Color.TRANSPARENT)
        }
    }

    protected fun getIcon(): Drawable {
        return searchProvider.getIcon(true)
    }

}