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

package com.saggitt.omega.predictions

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Process
import com.android.launcher3.LauncherAppState
import com.android.launcher3.Utilities
import com.android.launcher3.util.ComponentKey
import com.saggitt.omega.allapps.PredictionsFloatingHeader
import com.saggitt.omega.util.runOnMainThread

open class OmegaEventPredictor(private val context: Context) : CustomAppPredictor(context) {

    private val packageManager by lazy { context.packageManager }
    private val launcher by lazy { LauncherAppState.getInstance(context).launcher }
    private val predictionsHeader by lazy { launcher.appsView.floatingHeaderView as PredictionsFloatingHeader }

    private val devicePrefs = Utilities.getDevicePrefs(context)
    private val appsList = CountRankedArrayPreference(devicePrefs, "recent_app_launches", 250)

    override fun updatePredictions() {
        super.updatePredictions()
        if (isPredictorEnabled) {
            runOnMainThread {
                predictionsHeader.setPredictedApps(isPredictorEnabled, predictions)
            }
        }
    }

    /* override fun logAppLaunch(v: View?, intent: Intent?, user: UserHandle?) {
         super.logAppLaunch(v, intent, user)
         logAppLaunchImpl(v, intent, user ?: Process.myUserHandle())
     }

     private fun logAppLaunchImpl(v: View?, intent: Intent?, user: UserHandle) {
         if (isPredictorEnabled) {
             //updatePredictions()
              if (intent?.component != null && mAppFilter.shouldShowApp(intent.component, user)) {
                 clearRemovedComponents()

                 var changed = false
                 val key = ComponentKey(intent.component, user).toString()
                 if (recursiveIsDrawer(v)) {
                     appsList.add(key)
                     changed = true
                 }
                 if (changed) {
                     updatePredictions()
                 }
             }
         }
     }
 */
    // TODO: Extension function?
    private fun clearRemovedComponents() {
        appsList.removeAll {
            val component = getComponentFromString(it).componentKey?.componentName
                    ?: return@removeAll true
            try {
                packageManager.getActivityInfo(component, 0)
                false
            } catch (ignored: PackageManager.NameNotFoundException) {
                val intent = packageManager.getLaunchIntentForPackage(component.packageName)
                if (intent != null) {
                    val componentInfo = intent.component
                    if (componentInfo != null) {
                        val key = ComponentKey(componentInfo, Process.myUserHandle())
                        appsList.replace(it, key.toString())
                        return@removeAll false
                    }
                }
                true
            }
        }
    }

    /**
     * A ranked list with roll over to get/store currently relevant events and rank them by occurence
     */
    inner class CountRankedArrayPreference(private val prefs: SharedPreferences, private val key: String, private val maxSize: Int = -1, private val delimiter: String = ";") {
        private var list = load()

        fun getRanked(): Set<String> = list.distinct().sortedBy { value -> list.count { it == value } }.reversed().toSet()

        fun add(string: String) {
            list.add(0, string)
            if (maxSize >= 0 && list.size > maxSize) {
                list = list.drop(maxSize).toMutableList()
            }
            save()
        }

        fun clear() {
            list.clear()
            prefs.edit().remove(key).apply()
        }

        fun removeAll(filter: (String) -> Boolean) = list.removeAll(filter)
        fun replace(filter: String, replacement: String) {
            list = list.map { if (it == filter) replacement else it }.toMutableList()
        }

        fun contains(element: String) = list.contains(element)

        private fun load() = (prefs.getString(key, "") ?: "").split(delimiter).toMutableList()
        private fun save() {
            val strValue = list.joinToString(delimiter)
            prefs.edit().putString(key, strValue).apply()
        }
    }
}