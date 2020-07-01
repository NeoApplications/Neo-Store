/*
 *
 *  *
 *  *  * Copyright (c) 2020 Omega Launcher
 *  *  *
 *  *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  *  * you may not use this file except in compliance with the License.
 *  *  * You may obtain a copy of the License at
 *  *  *
 *  *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *  *
 *  *  * Unless required by applicable law or agreed to in writing, software
 *  *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  * See the License for the specific language governing permissions and
 *  *  * limitations under the License.
 *  *
 *
 */

package com.saggitt.omega.views

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import com.android.launcher3.Insettable

open class InsettableRecyclerView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : SpringRecyclerView(context, attrs, defStyleAttr), Insettable {

    private val currentInsets = Rect()
    private val currentPadding = Rect()

    override fun setInsets(insets: Rect) {
        super.setPadding(
                paddingLeft + insets.left - currentInsets.left,
                paddingTop + insets.top - currentInsets.top,
                paddingRight + insets.right - currentInsets.right,
                paddingBottom + insets.bottom - currentInsets.bottom)
        currentInsets.set(insets)
    }

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        super.setPadding(
                paddingLeft + left - currentPadding.left,
                paddingTop + top - currentPadding.top,
                paddingRight + right - currentPadding.right,
                paddingBottom + bottom - currentPadding.bottom)
        currentPadding.set(left, top, right, bottom)
    }
}
