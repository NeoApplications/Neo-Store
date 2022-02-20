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
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import com.android.launcher3.*
import com.android.launcher3.graphics.IconShape
import com.android.launcher3.graphics.NinePatchDrawHelper
import com.android.launcher3.icons.ShadowGenerator.Builder
import com.android.launcher3.views.ActivityContext
import com.saggitt.omega.OmegaLauncher
import com.saggitt.omega.preferences.OmegaPreferences
import com.saggitt.omega.util.Config
import com.saggitt.omega.util.getColorAttr
import kotlin.math.round

abstract class AbstractQsbLayout(context: Context, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs), SearchProviderController.OnProviderChangeListener,
    SharedPreferences.OnSharedPreferenceChangeListener {
    var mContext: Context = context
    protected var prefs: OmegaPreferences = Utilities.getOmegaPrefs(mContext)
    protected var controller = SearchProviderController.getInstance(getContext())
    protected var searchProvider: SearchProvider = controller.searchProvider
    private var mShowAssistant = false
    protected var mIsRtl = Utilities.isRtl(resources)
    private var mAllAppsBgColor = mContext.getColorAttr(R.attr.popupColorPrimary)
    private var mShadowHelper = NinePatchDrawHelper()
    protected var mActivity: ActivityContext? = ActivityContext.lookupContext(context)

    private var micIconView: ImageView? = null
    private var searchLogoView: ImageView? = null
    private var lensIconView: ImageView? = null

    private var mAllAppsShadowBitmap: Bitmap? = null
    private var mClearBitmap: Bitmap? = null

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
                    controller.searchProvider.startSearch { intent: Intent? ->
                        context.startActivity(
                            intent,
                            ActivityOptionsCompat.makeClipRevealAnimation(this, 0, 0, width, height)
                                .toBundle()
                        )
                    }
                }
            }
        }

        micIconView = findViewById<AppCompatImageView?>(R.id.mic_icon).apply {
            if (searchProvider.supportsVoiceSearch) {
                if (searchProvider.supportsAssistant) {
                    val micIcon: Drawable? = searchProvider.assistantIcon
                    micIcon?.setTint(prefs.accentColor)
                    setImageDrawable(micIcon)
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

            visibility =
                if (searchProvider.packageName == Config.GOOGLE_QSB
                    && searchProvider !is WebSearchProvider
                    && mContext.packageManager.resolveActivity(
                        lensIntent,
                        0
                    ) != null
                ) {
                    setImageResource(R.drawable.ic_lens_color)

                    setOnClickListener {
                        mContext.startActivity(lensIntent)
                    }
                    View.VISIBLE
                } else {
                    View.GONE
                }
        }

        setOnClickListener {
            val provider = controller.searchProvider
            provider.startSearch { intent: Intent? ->
                mContext.startActivity(
                    intent,
                    ActivityOptionsCompat.makeClipRevealAnimation(this, 0, 0, width, height)
                        .toBundle()
                )
            }

        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        getDevicePreferences().registerOnSharedPreferenceChangeListener(this)
        SearchProviderController.getInstance(mContext).addOnProviderChangeListener(this)
        addOrUpdateSearchRipple()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        getDevicePreferences().unregisterOnSharedPreferenceChangeListener(this)
        SearchProviderController.getInstance(mContext).removeOnProviderChangeListener(this)
    }

    override fun draw(canvas: Canvas) {
        ensureAllAppsShadowBitmap()
        drawShadow(mAllAppsShadowBitmap, canvas)
        super.draw(canvas)
    }

    abstract fun startSearch(str: String?)

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        when (key) {
            "opa_enabled",
            "opa_assistant",
            ->
                reloadPreferences(sharedPreferences)
        }
    }

    override fun onSearchProviderChanged() {
        reloadPreferences(Utilities.getPrefs(mContext))
    }

    private fun ensureAllAppsShadowBitmap() {
        if (mAllAppsShadowBitmap == null) {
            mAllAppsShadowBitmap = createShadowBitmap(mAllAppsBgColor, true)
            mClearBitmap = null
            if (Color.alpha(mAllAppsBgColor) != 255) {
                mClearBitmap = createShadowBitmap(-0x1000000, false)
            }
        }
    }

    private fun drawShadow(bitmap: Bitmap?, canvas: Canvas?) {
        drawPill(mShadowHelper, bitmap, canvas)
    }

    /* Draw search bar background */
    private fun drawPill(helper: NinePatchDrawHelper, bitmap: Bitmap?, canvas: Canvas?) {
        val shadowDimens: Int = getShadowDimens(bitmap!!)
        val left = paddingLeft - shadowDimens
        val top = paddingTop - (bitmap.height - getHeightWithoutPadding()) / 2
        val right = width - paddingRight + shadowDimens
        helper.draw(bitmap, canvas, left.toFloat(), top.toFloat(), right.toFloat())
    }

    private fun getShadowDimens(bitmap: Bitmap): Int {
        return (bitmap.width - (getHeightWithoutPadding() + 20)) / 2
    }

    private fun createShadowBitmap(bgColor: Int, withShadow: Boolean): Bitmap {
        val f =
            LauncherAppState.getInstance(context).invariantDeviceProfile.iconBitmapSize.toFloat()
        return createShadowBitmap(0.010416667f * f, f * 0.020833334f, bgColor, withShadow)
    }

    private fun createShadowBitmap(
        shadowBlur: Float,
        keyShadowDistance: Float,
        color: Int,
        withShadow: Boolean
    ): Bitmap {
        val height = getHeightWithoutPadding()
        val builder = Builder(color)
        builder.shadowBlur = shadowBlur
        builder.keyShadowDistance = keyShadowDistance
        if (!withShadow) {
            builder.ambientShadowAlpha = 0
        }
        builder.keyShadowAlpha = builder.ambientShadowAlpha
        val pill: Bitmap = if (getCornerRadius() < 0) {
            val edgeRadius = IconShape.getShape().getAttrValue(R.attr.qsbEdgeRadius)
            if (edgeRadius != null) {
                builder.createPill(
                    height + 20, height,
                    edgeRadius.getDimension(resources.displayMetrics)
                )
            } else {
                builder.createPill(height + 20, height)
            }
        } else {
            builder.createPill(height + 20, height, getCornerRadius())
        }
        return if (Utilities.ATLEAST_P) {
            pill.copy(Bitmap.Config.HARDWARE, false)
        } else pill
    }

    /*
    * Create the searchbar background when clicked
    */
    private fun addOrUpdateSearchRipple() {
        val insetDrawable: InsetDrawable = createRipple().mutate() as InsetDrawable
        background = insetDrawable

        val oldRipple: RippleDrawable = insetDrawable.drawable as RippleDrawable
        oldRipple.setLayerInset(0, 0, 0, 0, 0)

        searchLogoView?.apply {
            val searchIconRipple: RippleDrawable =
                oldRipple.constantState!!.newDrawable().mutate() as RippleDrawable
            searchIconRipple.setLayerInset(0, 0, 2, 0, 2)
            background = searchIconRipple
            setPadding(8, 8, 8, 8)
        }

        micIconView?.apply {
            val micIconRipple: RippleDrawable =
                oldRipple.constantState!!.newDrawable().mutate() as RippleDrawable
            micIconRipple.setLayerInset(0, 0, 2, 0, 2)
            background = micIconRipple
            setPadding(12, 12, 12, 12)
        }

        lensIconView?.apply {
            val lensIconRipple: RippleDrawable =
                oldRipple.constantState!!.newDrawable().mutate() as RippleDrawable
            lensIconRipple.setLayerInset(0, 0, 2, 0, 2)
            background = lensIconRipple
            setPadding(12, 12, 12, 12)
        }
        lensIconView?.requestLayout()
    }

    /*
     * Get the searchbar width
     */
    open fun getMeasuredWidth(width: Int, dp: DeviceProfile): Int {
        val leftRightPadding = (dp.desiredWorkspaceLeftRightMarginPx
                + dp.cellLayoutPaddingLeftRightPx)
        return width - leftRightPadding * 2
    }

    protected open fun getHeightWithoutPadding(): Int {
        return height - paddingTop - paddingBottom
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
            val providerSupported =
                searchProvider.supportsAssistant || searchProvider.supportsVoiceSearch
            val showMic = sharedPreferences.getBoolean("opa_enabled", true) && providerSupported
            mShowAssistant = sharedPreferences.getBoolean("opa_assistant", true)
            searchLogoView?.setImageDrawable(getIcon())
            if (showMic || mShowAssistant) {
                micIconView?.visibility = View.VISIBLE
                micIconView?.setImageDrawable(getMicIcon())
            } else {
                micIconView?.visibility = View.GONE
            }
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

    open fun getLauncher(): OmegaLauncher {
        return mActivity as OmegaLauncher
    }
}