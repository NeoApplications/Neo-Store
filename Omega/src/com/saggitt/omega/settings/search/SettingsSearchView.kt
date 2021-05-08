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

package com.saggitt.omega.settings.search

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.TypefaceSpan
import android.util.AttributeSet
import androidx.appcompat.widget.SearchView
import com.android.launcher3.Utilities

class SettingsSearchView(context: Context, attrs: AttributeSet?) : SearchView(context, attrs) {

    private var customTypeface: Typeface? = null

    init {
        isIconified = false;
    }

    fun setTypeface(typeface: Typeface) {
        customTypeface = typeface
        queryHint = queryHint
    }

    override fun setQueryHint(hint: CharSequence?) {
        val typeface = customTypeface
        if (Utilities.ATLEAST_P && hint != null && typeface != null) {
            val styled = SpannableString(hint)
            styled.setSpan(TypefaceSpan(typeface), 0, styled.length,
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
            super.setQueryHint(styled)
        } else {
            super.setQueryHint(hint)
        }
    }
}

