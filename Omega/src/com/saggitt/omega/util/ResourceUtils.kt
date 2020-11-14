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

package com.saggitt.omega.util

import android.content.res.AssetManager
import android.content.res.Resources
import android.os.Build
import com.android.launcher3.Utilities

val setConfigurationMethod by lazy {
    if (Utilities.ATLEAST_OREO)
        AssetManager::class.java.getDeclaredMethod("setConfiguration",
                /* mcc */ Int::class.java, /* mnc */ Int::class.java, /* locale */ String::class.java,
                /* orientation*/ Int::class.java, /* touchscreen*/ Int::class.java, /* density*/ Int::class.java,
                /* keyboard */ Int::class.java, /* keyboardHidden */ Int::class.java, /* navigation */ Int::class.java,
                /* screenWidth */ Int::class.java, /* screenHeight */ Int::class.java, /* smallestScreenWidthDp */ Int::class.java,
                /* screenWidthDp */ Int::class.java, /* screenHeightDp */ Int::class.java, /* screenLayout */ Int::class.java,
                /* uiMode */ Int::class.java, /* colorMode */ Int::class.java, /* majorVersion */ Int::class.java)
    else
        AssetManager::class.java.getDeclaredMethod("setConfiguration",
                /* mcc */ Int::class.java, /* mnc */ Int::class.java, /* locale */ String::class.java,
                /* orientation*/ Int::class.java, /* touchscreen*/ Int::class.java, /* density*/ Int::class.java,
                /* keyboard */ Int::class.java, /* keyboardHidden */ Int::class.java, /* navigation */ Int::class.java,
                /* screenWidth */ Int::class.java, /* screenHeight */ Int::class.java, /* smallestScreenWidthDp */ Int::class.java,
                /* screenWidthDp */ Int::class.java, /* screenHeightDp */ Int::class.java, /* screenLayout */ Int::class.java,
                /* uiMode */ Int::class.java, /* majorVersion */ Int::class.java)
}

inline fun <T> Resources.overrideSdk(sdk: Int, body: Resources.() -> T): T {
    setResSdk(this, sdk)
    val ret = body(this)
    setResSdk(this, Build.VERSION.SDK_INT)
    return ret
}

fun setResSdk(res: Resources, sdk: Int): Resources {
    res.apply {
        displayMetrics.scaledDensity = displayMetrics.density * if (configuration.fontScale != 0f) configuration.fontScale else 1.0f

        val width: Int
        val height: Int
        if (displayMetrics.widthPixels >= displayMetrics.heightPixels) {
            width = displayMetrics.widthPixels
            height = displayMetrics.heightPixels
        } else {
            width = displayMetrics.heightPixels
            height = displayMetrics.widthPixels
        }

        if (Utilities.ATLEAST_OREO)
            setConfigurationMethod.invoke(assets, configuration.mcc, configuration.mnc,
                    configuration.locale.toLanguageTag(),
                    configuration.orientation,
                    configuration.touchscreen,
                    configuration.densityDpi, configuration.keyboard,
                    configuration.keyboardHidden, configuration.navigation, width, height,
                    configuration.smallestScreenWidthDp,
                    configuration.screenWidthDp, configuration.screenHeightDp,
                    configuration.screenLayout, configuration.uiMode,
                    configuration.colorMode, sdk)
        else
            setConfigurationMethod.invoke(assets, configuration.mcc, configuration.mnc,
                    configuration.locale.toLanguageTag(),
                    configuration.orientation,
                    configuration.touchscreen,
                    configuration.densityDpi, configuration.keyboard,
                    configuration.keyboardHidden, configuration.navigation, width, height,
                    configuration.smallestScreenWidthDp,
                    configuration.screenWidthDp, configuration.screenHeightDp,
                    configuration.screenLayout, configuration.uiMode,
                    sdk)
    }
    return res
}