package com.saggitt.omega.views

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.view.ViewDebug.ExportedProperty
import android.view.ViewGroup
import android.widget.LinearLayout
import com.android.launcher3.Insettable
import com.android.launcher3.R.styleable

class InsettableLinearLayout(context: Context?, attrs: AttributeSet?) :
    LinearLayout(context, attrs), Insettable {
    @ExportedProperty(category = "launcher")
    protected var mInsets = Rect()
    private var mInsetsSet = false
    fun getInsets(): Rect {
        return mInsets
    }

    override fun setInsets(insets: Rect) {
        check(orientation == VERTICAL) { "Doesn't support horizontal orientation" }
        mInsetsSet = true
        val n = childCount
        for (i in 0 until n) {
            val child = getChildAt(i)
            setLinearLayoutChildInsets(child, insets, mInsets)
        }
        mInsets.set(insets)
    }

    fun setLinearLayoutChildInsets(child: View, newInsets: Rect, oldInsets: Rect) {
        val lp = child.layoutParams as LayoutParams
        val childIndex = indexOfChild(child)
        val newTop = if (childIndex == 0) newInsets.top else 0
        val oldTop = if (childIndex == 0) oldInsets.top else 0
        val newBottom = if (childIndex == childCount - 1) newInsets.bottom else 0
        val oldBottom = if (childIndex == childCount - 1) oldInsets.bottom else 0
        if (child is Insettable) {
            (child as Insettable).setInsets(
                Rect(
                    newInsets.left,
                    newTop,
                    newInsets.right,
                    newBottom
                )
            )
        } else if (!lp.ignoreInsets) {
            lp.topMargin += newTop - oldTop
            lp.leftMargin += newInsets.left - oldInsets.left
            lp.rightMargin += newInsets.right - oldInsets.right
            lp.bottomMargin += newBottom - oldBottom
        }
        child.layoutParams = lp
    }

    override fun generateLayoutParams(attrs: AttributeSet): LayoutParams {
        return LayoutParams(context, attrs)
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    // Override to allow type-checking of LayoutParams.
    override fun checkLayoutParams(p: ViewGroup.LayoutParams): Boolean {
        return p is LayoutParams
    }

    override fun generateLayoutParams(p: ViewGroup.LayoutParams): LayoutParams {
        return LayoutParams(p)
    }

    override fun onViewAdded(child: View) {
        super.onViewAdded(child)
        check(!mInsetsSet) { "Cannot modify views after insets are set" }
    }

    override fun onViewRemoved(child: View) {
        super.onViewRemoved(child)
        check(!mInsetsSet) { "Cannot modify views after insets are set" }
    }

    class LayoutParams : LinearLayout.LayoutParams {
        var ignoreInsets = false

        constructor(c: Context, attrs: AttributeSet?) : super(c, attrs) {
            val a = c.obtainStyledAttributes(attrs, styleable.InsettableFrameLayout_Layout)
            ignoreInsets = a.getBoolean(
                styleable.InsettableFrameLayout_Layout_layout_ignoreInsets, false
            )
            a.recycle()
        }

        constructor(width: Int, height: Int) : super(width, height)
        constructor(lp: ViewGroup.LayoutParams?) : super(lp)
    }
}