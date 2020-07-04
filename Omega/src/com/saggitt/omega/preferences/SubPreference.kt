/*
 *  Copyright (c) 2020 Omega Launcher
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.saggitt.omega.preferences

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import com.android.launcher3.R
import com.saggitt.omega.settings.SettingsActivity

open class SubPreference(context: Context, attrs: AttributeSet) : StyledIconPreference(context, attrs),
        View.OnLongClickListener, ControlledPreference by ControlledPreference.Delegate(context, attrs) {

    private var mContent: Int = 0
    private var mLongClickContent: Int = 0
    private var mLongClick: Boolean = false
    private var mHasPreview: Boolean = false

    val content: Int
        get() = if (mLongClick) mLongClickContent else mContent

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.SubPreference)
        for (i in a.indexCount - 1 downTo 0) {
            val attr = a.getIndex(i)
            when (attr) {
                R.styleable.SubPreference_content -> mContent = a.getResourceId(attr, 0)
                R.styleable.SubPreference_longClickContent -> mLongClickContent = a.getResourceId(attr, 0)
                R.styleable.SubPreference_hasPreview -> mHasPreview = a.getBoolean(attr, false)
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