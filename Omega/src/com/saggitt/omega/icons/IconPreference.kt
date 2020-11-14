/*
 * Copyright (c) 2020 Omega Launcher
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
 */

package com.saggitt.omega.icons

import android.content.Context
import android.graphics.PorterDuff
import android.util.AttributeSet
import androidx.preference.Preference
import com.android.launcher3.R
import com.saggitt.omega.adaptive.IconShapeDrawable
import com.saggitt.omega.adaptive.IconShapeManager
import com.saggitt.omega.util.dpToPx
import com.saggitt.omega.util.getColorAttr

class IconPreference(context: Context, attrs: AttributeSet? = null) : Preference(context, attrs) {
    private var iconShape = IconShapeManager.getInstance(context).iconShape
    private var iconShapeString = iconShape.toString()
    private val drawable = IconShapeDrawable(dpToPx(48f).toInt(), iconShape).apply {
        setColorFilter(context.getColorAttr(android.R.attr.colorControlNormal), PorterDuff.Mode.SRC_IN)
    }

    init {
        layoutResource = R.layout.pref_with_preview_icon
        fragment = IconCustomizeFragment::class.java.name
        updatePreview()
    }

    private fun updatePreview() {
        try {
            summary = iconShapeString
            icon = drawable
        } catch (ignored: Exception) {
        }
    }
}