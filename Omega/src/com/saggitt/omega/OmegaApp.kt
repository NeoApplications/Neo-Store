/*
 *  This file is part of Omega Launcher.
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

package com.saggitt.omega

import android.app.Activity
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.annotation.Keep
import com.android.launcher3.Utilities
import com.android.quickstep.RecentsActivity
import com.saggitt.omega.blur.BlurWallpaperProvider
import com.saggitt.omega.flowerpot.Flowerpot
import com.saggitt.omega.smartspace.OmegaSmartspaceController
import com.saggitt.omega.theme.ThemeManager

class OmegaApp : Application() {
    val activityHandler = ActivityHandler()
    val smartspace by lazy { OmegaSmartspaceController(this) }
    var mismatchedQuickstepTarget = false
    val recentsEnabled by lazy { checkRecentsComponent() }
    var accessibilityService: OmegaAccessibilityService? = null

    fun onLauncherAppStateCreated() {
        sApplication = this
        registerActivityLifecycleCallbacks(activityHandler)
        BlurWallpaperProvider.getInstance(this)
        Flowerpot.Manager.getInstance(this)
    }

    fun restart(recreateLauncher: Boolean = true) {
        if (recreateLauncher) {
            activityHandler.finishAll(recreateLauncher)
        } else {
            Utilities.restartLauncher(this)
        }
    }

    fun performGlobalAction(action: Int): Boolean {
        return if (accessibilityService != null) {
            accessibilityService!!.performGlobalAction(action)
        } else {
            startActivity(
                Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            false
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
            Log.d(
                "OmegaApp",
                "config_recentsComponentName ($recentsComponent) is not Omega, disabling recents"
            )
            return false
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            Log.d("OmegaApp", "Quickstep target doesn't match, disabling recents")
            mismatchedQuickstepTarget = true
            return false
        }
        return true
    }


    companion object {
        @JvmStatic
        fun getContext(): Context? {
            return getApplication()?.applicationContext
        }

        private var sApplication: Application? = null

        fun getApplication(): Application? {
            return sApplication
        }
    }
}

val Context.omegaApp get() = applicationContext as OmegaApp