package com.saggitt.omega.compose.components

import android.animation.PropertyValuesHolder
import android.content.Context
import android.util.AttributeSet
import android.util.FloatProperty
import android.view.Gravity
import android.view.ViewGroup
import android.view.animation.Interpolator
import android.widget.LinearLayout
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.android.launcher3.Launcher
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.anim.Interpolators
import com.android.launcher3.anim.PendingAnimation
import com.android.launcher3.util.SystemUiController
import com.android.launcher3.util.Themes
import com.android.launcher3.views.AbstractSlideInView
import com.android.launcher3.views.BaseDragLayer
import com.saggitt.omega.theme.OmegaAppTheme
import com.saggitt.omega.views.BaseBottomSheet

class ComposeBottomSheet(context: Context, attrs: AttributeSet? = null) :
    AbstractSlideInView<Launcher>(context, attrs, 0) {

    private val container = ComposeView(context)
    private val mLauncher = Launcher.getLauncher(context)
    private var imeShift = 0f
    private var _hintCloseProgress = mutableStateOf(0f)
    private val hintCloseProgress get() = _hintCloseProgress.value
    var hintCloseDistance = 0f
        private set

    init {
        layoutParams =
            BaseDragLayer.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                .apply { ignoreInsets = true }
        gravity = Gravity.BOTTOM
        setWillNotDraw(false)

        mContent = LinearLayout(context).apply {
            addView(container)
        }
    }

    fun setContent(
        content: @Composable ComposeBottomSheet.() -> Unit,
        contentPaddings: PaddingValues = PaddingValues(
            top = 0.dp,
            bottom = 50.dp,
            start = 0.dp,
            end = 0.dp
        )
    ) {
        container.setContent {
            OmegaAppTheme {
                /*Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.shapes.large
                        )
                        .padding(bottom = 50.dp)
                        .wrapContentHeight()
                        .fillMaxWidth()
                ) {
                    content(this@ComposeBottomSheet)
                }*/
                ContentWrapper(contentPaddings) {
                    content(this)
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
        setupNavBarColor()
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

    private fun setImeShift(shift: Float) {
        imeShift = shift
        updateContentShift()
    }

    override fun addHintCloseAnim(
        distanceToMove: Float,
        interpolator: Interpolator,
        target: PendingAnimation
    ) {
        super.addHintCloseAnim(distanceToMove, interpolator, target)
        hintCloseDistance = distanceToMove
        target.setFloat(this, HINT_CLOSE_PROGRESS, 1f, interpolator)
    }

    private fun updateContentShift() {
        if (!Utilities.getOmegaPrefs(context).showDebugInfo.onGetValue()) {
            mContent.translationY = mTranslationShift * mContent.height + imeShift
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        setTranslationShift(mTranslationShift)
    }

    @Composable
    private fun ContentWrapper(
        contentPaddings: PaddingValues = PaddingValues(all = 0.dp),
        content: @Composable ComposeBottomSheet.() -> Unit
    ) {
        val imePaddings = WindowInsets.ime
            .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
            .asPaddingValues()

        val translation = imePaddings - contentPaddings
        setImeShift(with(LocalDensity.current) { -translation.calculateBottomPadding().toPx() })

        Surface(
            modifier = Modifier
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier
                    .padding(contentPaddings)
                    .graphicsLayer(
                        alpha = 1f - (hintCloseProgress * 0.5f),
                        translationY = hintCloseProgress * -hintCloseDistance
                    )
            ) {
                content(this@ComposeBottomSheet)
            }
        }
    }

    companion object {

        private val HINT_CLOSE_PROGRESS =
            object : FloatProperty<ComposeBottomSheet>("hintCloseProgress") {
                override fun setValue(view: ComposeBottomSheet, value: Float) {
                    view._hintCloseProgress.value = value
                }

                override fun get(view: ComposeBottomSheet) = view._hintCloseProgress.value
            }

        fun show(
            context: Context,
            animate: Boolean = true,
            content: @Composable ComposeBottomSheet.() -> Unit
        ) {
            val bottomSheet = ComposeBottomSheet(context)
            bottomSheet.setContent(content)
            bottomSheet.show(animate)
        }
    }
}