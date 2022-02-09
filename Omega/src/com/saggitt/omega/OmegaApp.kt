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
import com.android.launcher3.InvariantDeviceProfile
import com.android.launcher3.Utilities
import com.android.quickstep.RecentsActivity
import com.android.systemui.shared.system.QuickStepContract
import com.saggitt.omega.blur.BlurWallpaperProvider
import com.saggitt.omega.smartspace.OmegaSmartSpaceController
import com.saggitt.omega.theme.ThemeManager
import com.wind.hiddenapi.bypass.HiddenApiBypass
import java.io.File

class OmegaApp : Application() {
    private val TAG = "OmegaApp"

    val activityHandler = ActivityHandler()

    val smartspace by lazy { OmegaSmartSpaceController(this) }
    var mismatchedQuickstepTarget = false
    private val recentsEnabled by lazy { checkRecentsComponent() }
    var accessibilityService: OmegaAccessibilityService? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
        QuickStepContract.sRecentsDisabled = !recentsEnabled
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            HiddenApiBypass.startBypass();
        }
    }

    fun onLauncherAppStateCreated() {
        registerActivityLifecycleCallbacks(activityHandler)
        BlurWallpaperProvider.getInstance(this)
    }

    fun restart(recreateLauncher: Boolean = true) {
        if (recreateLauncher) {
            activityHandler.finishAll(recreateLauncher)
        } else {
            Utilities.restartLauncher(this)
        }
    }

    fun migrateDbName(dbName: String) {
        val dbFile = getDatabasePath(dbName)
        if (dbFile.exists()) return
        val prefs = Utilities.getOmegaPrefs(this)
        val dbJournalFile = getJournalFile(dbFile)
        val oldDbSlot = prefs.StringPref("pref_currentDbSlot", "a").onGetValue()
        val oldDbName = if (oldDbSlot == "a") "launcher.db" else "launcher.db_b"
        val oldDbFile = getDatabasePath(oldDbName)
        val oldDbJournalFile = getJournalFile(oldDbFile)
        if (oldDbFile.exists()) {
            oldDbFile.copyTo(dbFile)
            oldDbJournalFile.copyTo(dbJournalFile)
        }
    }

    fun cleanUpDatabases() {
        val idp = InvariantDeviceProfile.INSTANCE.get(this)
        val dbName = idp.dbFile
        val dbFile = getDatabasePath(dbName)
        dbFile?.parentFile?.listFiles()?.forEach { file ->
            val name = file.name
            if (name.startsWith("launcher") && !name.startsWith(dbName)) {
                file.delete()
            }
        }
    }

    private fun getJournalFile(file: File): File =
            File(file.parentFile, "${file.name}-journal")

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

    private fun checkRecentsComponent(): Boolean {
        if (!Utilities.ATLEAST_R) {
            Log.d(TAG, "API < P, disabling recents")
            return false
        }

        val resId = resources.getIdentifier("config_recentsComponentName", "string", "android")
        if (resId == 0) {
            Log.d(TAG, "config_recentsComponentName not found, disabling recents")
            return false
        }
        val recentsComponent = ComponentName.unflattenFromString(resources.getString(resId))
        if (recentsComponent == null) {
            Log.d(TAG, "config_recentsComponentName is empty, disabling recents")
            return false
        }
        val isRecentsComponent = recentsComponent.packageName == packageName
                && recentsComponent.className == RecentsActivity::class.java.name
        if (!isRecentsComponent) {
            Log.d(
                TAG,
                "config_recentsComponentName ($recentsComponent) is not Omega, disabling recents"
            )
            return false
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
            Log.d(TAG, "Quickstep target doesn't match, disabling recents")
            mismatchedQuickstepTarget = true
            return false
        }
        return true
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

    companion object {
        @JvmStatic
        var instance: OmegaApp? = null
            private set

        @JvmStatic
        val isRecentsEnabled: Boolean
            get() = instance?.recentsEnabled == true
    }
}

val Context.omegaApp get() = applicationContext as OmegaApp