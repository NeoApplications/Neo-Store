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

import android.app.Activity
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.Keep
import com.android.launcher3.Utilities
import com.android.quickstep.RecentsActivity
import com.saggitt.omega.blur.BlurWallpaperProvider
import com.saggitt.omega.theme.ThemeManager

class OmegaApp : Application() {
    val activityHandler = ActivityHandler()

    var mismatchedQuickstepTarget = false
    val recentsEnabled by lazy { checkRecentsComponent() }

    fun onLauncherAppStateCreated() {
        registerActivityLifecycleCallbacks(activityHandler)
        BlurWallpaperProvider.getInstance(this)
        //Flowerpot.Manager.getInstance(this)
    }

    fun restart(recreateLauncher: Boolean = true) {
        if (recreateLauncher) {
            activityHandler.finishAll(recreateLauncher)
        } else {
            Utilities.restartLauncher(this)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        ThemeManager.getInstance(this).updateNightMode(newConfig)
    }

    class ActivityHandler : ActivityLifecycleCallbacks {

        val activities = HashSet<Activity>()
        var foregroundActivity: Activity? = null

        fun finishAll(recreateLauncher: Boolean = true) {
            HashSet(activities).forEach { if (recreateLauncher && it is OmegaLauncher) it.recreate() else it.finish() }
        }

        override fun onActivityPaused(activity: Activity) {

        }

        override fun onActivityResumed(activity: Activity) {
            foregroundActivity = activity
        }

        override fun onActivityStarted(activity: Activity) {

        }

        override fun onActivityDestroyed(activity: Activity) {
            if (activity == foregroundActivity)
                foregroundActivity = null
            activities.remove(activity)
        }

        override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {

        }

        override fun onActivityStopped(activity: Activity) {

        }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            activities.add(activity)
        }
    }

    @Keep
    fun checkRecentsComponent(): Boolean {
        if (!Utilities.ATLEAST_P) {
            Log.d("OmegaApp", "API < P, disabling recents")
            return false
        }
        if (!Utilities.HIDDEN_APIS_ALLOWED) {
            Log.d("OmegaApp", "Hidden APIs not allowed, disabling recents")
            return false
        }

        val resId = resources.getIdentifier("config_recentsComponentName", "string", "android")
        if (resId == 0) {
            Log.d("OmegaApp", "config_recentsComponentName not found, disabling recents")
            return false
        }
        val recentsComponent = ComponentName.unflattenFromString(resources.getString(resId))
        if (recentsComponent == null) {
            Log.d("OmegaApp", "config_recentsComponentName is empty, disabling recents")
            return false
        }
        val isRecentsComponent = recentsComponent.packageName == packageName
                && recentsComponent.className == RecentsActivity::class.java.name
        if (!isRecentsComponent) {
            Log.d("OmegaApp", "config_recentsComponentName ($recentsComponent) is not Lawnchair, disabling recents")
            return false
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            Log.d("OmegaApp", "Quickstep target doesn't match, disabling recents")
            mismatchedQuickstepTarget = true
            return false
        }
        return true
    }
}

val Context.omegaApp get() = applicationContext as OmegaApp