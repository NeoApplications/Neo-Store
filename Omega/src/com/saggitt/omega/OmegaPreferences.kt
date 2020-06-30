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

package com.saggitt.omega

import android.content.Context
import android.os.Looper
import com.saggitt.omega.util.MainThreadExecutor
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException

class OmegaPreferences(val context: Context) : PreferenceHelpers(context) {
    private val TAG = "OmegaPreferences"

    /* --APP DRAWER-- */
    fun getSortMode(): Int {
        val sort: String = sharedPrefs.getString("pref_key__sort_mode", "0")!!
        recreate
        return sort.toInt()
    }
    /* --BLUR--*/
    var enableBlur by BooleanPref("pref_enableBlur", omegaConfig.defaultEnableBlur(), updateBlur)
    val blurRadius by FloatPref("pref_blurRadius", omegaConfig.defaultBlurStrength, updateBlur)

    /* --DEV-- */
    var developerOptionsEnabled by BooleanPref("pref_developerOptionsEnabled", false, doNothing)

    companion object {
        private var INSTANCE: OmegaPreferences? = null

        fun getInstance(context: Context): OmegaPreferences {
            if (INSTANCE == null) {
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    INSTANCE = OmegaPreferences(context.applicationContext)
                } else {
                    try {
                        return MainThreadExecutor().submit(Callable { getInstance(context) }).get()
                    } catch (e: InterruptedException) {
                        throw RuntimeException(e)
                    } catch (e: ExecutionException) {
                        throw RuntimeException(e)
                    }

                }
            }
            return INSTANCE!!
        }
    }
}