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

package com.saggitt.omega

import android.content.Context
import android.os.Looper
import com.android.launcher3.R
import com.android.launcher3.util.Executors
import com.saggitt.omega.theme.ThemeManager
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException

class OmegaPreferences(val context: Context) : PreferenceHelpers(context) {
    private val TAG = "OmegaPreferences"

    /* --APP DRAWER-- */
    var sortMode by StringIntPref("pref_key__sort_mode", 0, reloadApps)

    /* --DESKTOP-- */
    var autoAddInstalled by BooleanPref("pref_add_icon_to_home", true, doNothing)

    /* --THEME-- */
    var launcherTheme by StringIntPref("pref_launcherTheme", 1) { ThemeManager.getInstance(context).updateTheme() }
    val accentColor by IntPref("pref_key__accent_color", R.color.colorAccent, restart)
    val primaryColor by IntPref("pref_key__primary_color", R.color.colorPrimary, restart)

    /* --ADVANCED-- */
    var settingsSearch by BooleanPref("pref_settings_search", true, restart)

    /* --BLUR--*/
    var enableBlur by BooleanPref("pref_enableBlur", omegaConfig.defaultEnableBlur(), updateBlur)
    val blurRadius by FloatPref("pref_blurRadius", omegaConfig.defaultBlurStrength, updateBlur)

    /* --DEV-- */
    var developerOptionsEnabled by BooleanPref("pref_showDevOptions", false, doNothing)
    val showDebugInfo by BooleanPref("pref_showDebugInfo", false, doNothing)
    val lowPerformanceMode by BooleanPref("pref_lowPerformanceMode", false, recreate)
    val enablePhysics get() = !lowPerformanceMode

    companion object {
        private var INSTANCE: OmegaPreferences? = null

        fun getInstance(context: Context): OmegaPreferences {
            if (INSTANCE == null) {
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    INSTANCE = OmegaPreferences(context.applicationContext)
                } else {
                    try {
                        return Executors.MAIN_EXECUTOR.submit(Callable { getInstance(context) }).get()
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