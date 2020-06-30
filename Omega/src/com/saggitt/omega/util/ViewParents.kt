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

package com.saggitt.omega.util

import android.view.View
import android.view.ViewParent

class ViewParents(private val view: View) : Iterable<ViewParent> {

    override fun iterator(): Iterator<ViewParent> = ViewParentIterator(view.parent)

    class ViewParentIterator(private var parent: ViewParent?) : Iterator<ViewParent> {

        override fun hasNext() = parent != null

        override fun next(): ViewParent {
            val current = parent ?: throw IllegalStateException("no next parent")
            parent = current.parent
            return current
        }
    }
}

val View.parents get() = ViewParents(this)