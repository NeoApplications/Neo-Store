package com.saggitt.omega.compose.components

import android.animation.PropertyValuesHolder
import android.content.Context
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import com.android.launcher3.Launcher
import com.android.launcher3.R
import com.android.launcher3.anim.Interpolators
import com.android.launcher3.util.SystemUiController
import com.android.launcher3.util.Themes
import com.android.launcher3.views.AbstractSlideInView
import com.android.launcher3.views.BaseDragLayer
import com.saggitt.omega.theme.OmegaAppTheme
import com.saggitt.omega.views.BaseBottomSheet

class BaseComposeBottomSheet(context: Context) : AbstractSlideInView<Launcher>(context, null, 0) {

    private val container = ComposeView(context)
    private val mLauncher = Launcher.getLauncher(context)

    init {
        layoutParams =
            BaseDragLayer.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                .apply { ignoreInsets = true }
        gravity = Gravity.BOTTOM
        setWillNotDraw(false)
        mContent = LinearLayout(context).apply {
            addView(container)
            setPadding(8, 8, 8, 8) // Is it needed?
        }
    }

    fun setContent(content: @Composable BaseComposeBottomSheet.() -> Unit) {
        container.setContent {
            OmegaAppTheme {
                Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.shapes.large
                        )
                        .wrapContentHeight()
                        .fillMaxWidth()
                ) {
                    content(this@BaseComposeBottomSheet)
                }
            }
        }
    }

    fun show(animate: Boolean) {
        val parent = parent
        if (parent is ViewGroup) parent.removeView(this)
        removeAllViews()
        addView(mContent)
        attachToContainer()
        animateOpen(animate)
    }

    private fun animateOpen(animate: Boolean) {
        if (mIsOpen || mOpenCloseAnimator.isRunning) {
            return
        }
        mIsOpen = true
        setupNavBarColor() //
        mOpenCloseAnimator.setValues(
            PropertyValuesHolder.ofFloat(TRANSLATION_SHIFT, TRANSLATION_SHIFT_OPENED)
        )
        mOpenCloseAnimator.interpolator = Interpolators.FAST_OUT_SLOW_IN
        if (!animate) mOpenCloseAnimator.duration = 0
        mOpenCloseAnimator.start()
    }

    private fun setupNavBarColor() {
        val isSheetDark = Themes.getAttrBoolean(mLauncher, R.attr.isMainColorDark)
        mLauncher.systemUiController.updateUiState(
            SystemUiController.UI_STATE_WIDGET_BOTTOM_SHEET,
            if (isSheetDark) SystemUiController.FLAG_DARK_NAV else SystemUiController.FLAG_LIGHT_NAV
        )
    }

    override fun handleClose(animate: Boolean) {
        handleClose(animate, BaseBottomSheet.DEFAULT_CLOSE_DURATION.toLong())
    }

    override fun isOfType(type: Int): Boolean {
        return type and TYPE_SETTINGS_SHEET != 0
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        setTranslationShift(mTranslationShift)
    }

    companion object {
        fun show(
            context: Context,
            animate: Boolean = true,
            content: @Composable BaseComposeBottomSheet.() -> Unit
        ) {
            val bottomSheet = BaseComposeBottomSheet(context)
            bottomSheet.setContent(content)
            bottomSheet.show(animate)
        }
    }
}