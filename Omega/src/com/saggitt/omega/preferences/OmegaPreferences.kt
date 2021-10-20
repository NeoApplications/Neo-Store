/*
 *  This file is part of Omega Launcher
 *  Copyright (c) 2021   Saul Henriquez
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saggitt.omega.preferences

import android.content.Context
import android.os.Looper
import com.android.launcher3.R
import com.android.launcher3.util.Executors.MAIN_EXECUTOR
import com.saggitt.omega.theme.ThemeManager
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException

class OmegaPreferences(context: Context) : BasePreferences(context) {
    //HOME SCREEN PREFERENCES
    var autoAddInstalled by BooleanPref("pref_add_icon_to_home", true, doNothing)

    //THEME
    var launcherTheme by StringIntPref(
        "pref_launcherTheme",
        ThemeManager.getDefaultTheme()
    ) { ThemeManager.getInstance(context).updateTheme() }
    val accentColor by IntPref("pref_key__accent_color", R.color.colorAccent, recreate)
    var enableBlur by BooleanPref("pref_enableBlur", false, updateBlur)
    val blurRadius by FloatPref("pref_blurRadius", 75f, updateBlur)

    //ADVANCED
    var settingsSearch by BooleanPref("pref_settings_search", true, recreate)

    //DEVELOPER PREFERENCES
    var developerOptionsEnabled by BooleanPref("pref_showDevOptions", false, recreate)
    private val lowPerformanceMode by BooleanPref("pref_lowPerformanceMode", false, recreate)
    val enablePhysics get() = !lowPerformanceMode
    val showDebugInfo by BooleanPref("pref_showDebugInfo", false, doNothing)

    companion object {

        fun getInstance(context: Context): OmegaPreferences {
            return if (Looper.myLooper() == Looper.getMainLooper()) {
                OmegaPreferences(context.applicationContext)
            } else {
                try {
                    MAIN_EXECUTOR.submit(Callable { getInstance(context) }).get()
                } catch (e: InterruptedException) {
                    throw RuntimeException(e)
                } catch (e: ExecutionException) {
                    throw RuntimeException(e)
                }

            }
        }
    }
}