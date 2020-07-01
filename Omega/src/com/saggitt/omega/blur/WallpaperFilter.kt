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

package com.saggitt.omega.blur

import android.graphics.Bitmap
import com.saggitt.omega.OmegaPreferences

interface WallpaperFilter {

    fun applyPrefs(prefs: OmegaPreferences)

    fun apply(wallpaper: Bitmap): ApplyTask

    class ApplyTask {

        val emitter = Emitter()

        private var result: Bitmap? = null
        private var error: Throwable? = null

        private var callback: ((Bitmap?, Throwable?) -> Unit)? = null

        fun setCallback(callback: (Bitmap?, Throwable?) -> Unit): ApplyTask {
            result?.let {
                callback(it, null)
                return this
            }
            error?.let {
                callback(null, it)
                return this
            }
            this.callback = callback
            return this
        }

        inner class Emitter {

            fun onSuccess(result: Bitmap) {
                callback?.let {
                    it(result, null)
                    return
                }
                this@ApplyTask.result = result
            }

            fun onError(error: Throwable) {
                callback?.let {
                    it(null, error)
                    return
                }
                this@ApplyTask.error = error
            }
        }

        companion object {

            inline fun create(source: (Emitter) -> Unit): ApplyTask {
                return ApplyTask().also { source(it.emitter) }
            }
        }
    }
}
