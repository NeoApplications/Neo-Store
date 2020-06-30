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

package com.saggitt.omega.preferences

import android.content.Context
import android.util.AttributeSet
import com.android.launcher3.R

interface ControlledPreference {

    val controller: PreferenceController?

    class Delegate(private val context: Context, attrs: AttributeSet?) : ControlledPreference {

        override var controller: PreferenceController? = null

        init {
            parseAttributes(attrs)
        }

        fun parseAttributes(attrs: AttributeSet?) {
            if (attrs == null) return
            val a = context.obtainStyledAttributes(attrs, R.styleable.ControlledPreference)
            for (i in a.indexCount - 1 downTo 0) {
                val attr = a.getIndex(i)
                if (attr == R.styleable.ControlledPreference_controllerClass) {
                    setControllerClass(a.getString(attr))
                }
            }
            a.recycle()
        }

        private fun setControllerClass(controllerClass: String?) {
            controller = PreferenceController.create(context, controllerClass)
        }
    }
}
