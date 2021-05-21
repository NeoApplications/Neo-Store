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

package com.saggitt.omega.preferences

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import com.android.launcher3.R.styleable
import com.saggitt.omega.settings.SettingsActivity

open class SubPreference(context: Context, attrs: AttributeSet) :
    StyledIconPreference(context, attrs),
    View.OnLongClickListener,
    ControlledPreference by ControlledPreference.Delegate(context, attrs) {

    private var mContent: Int = 0
    private var mLongClickContent: Int = 0
    private var mLongClick: Boolean = false
    private var mHasPreview: Boolean = false

    val content: Int
        get() = if (mLongClick) mLongClickContent else mContent

    init {
        val a = context.obtainStyledAttributes(attrs, styleable.SubPreference)
        for (i in a.indexCount - 1 downTo 0) {
            when (val attr = a.getIndex(i)) {
                styleable.SubPreference_content -> mContent = a.getResourceId(attr, 0)
                styleable.SubPreference_longClickContent -> mLongClickContent = a.getResourceId(attr, 0)
                styleable.SubPreference_hasPreview -> mHasPreview = a.getBoolean(attr, false)
            }
        }
        a.recycle()
        fragment = SettingsActivity.SubSettingsFragment::class.java.name
    }

    override fun getExtras(): Bundle {
        val b = Bundle(2)
        b.putString(SettingsActivity.SubSettingsFragment.TITLE, title as String)
        b.putInt(SettingsActivity.SubSettingsFragment.CONTENT_RES_ID, content)
        b.putBoolean(SettingsActivity.SubSettingsFragment.HAS_PREVIEW, hasPreview())
        return b
    }

    fun hasPreview(): Boolean {
        return mHasPreview
    }

    override fun onClick() {
        mLongClick = false
        super.onClick()
    }

    override fun onLongClick(view: View): Boolean {
        if (mLongClickContent != 0) {
            mLongClick = true
            super.onClick()
            return true
        }
        return false
    }

    open fun start(context: Context) {
        val intent = Intent(context, SettingsActivity::class.java)
        intent.putExtra(SettingsActivity.SubSettingsFragment.TITLE, title)
        intent.putExtra(SettingsActivity.SubSettingsFragment.CONTENT_RES_ID, content)
        intent.putExtra(SettingsActivity.SubSettingsFragment.HAS_PREVIEW, hasPreview())
        context.startActivity(intent)
    }
}