/*
 *     This file is part of Lawnchair Launcher.
 *
 *     Lawnchair Launcher is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Lawnchair Launcher is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Lawnchair Launcher.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saggitt.omega.gestures.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.android.launcher3.util.ActivityTracker
import com.saggitt.omega.OmegaLauncher
import com.saggitt.omega.gestures.BlankGestureHandler
import com.saggitt.omega.gestures.GestureController
import com.saggitt.omega.gestures.GestureHandler
import com.saggitt.omega.gestures.OmegaShortcutActivity

class RunHandlerActivity : AppCompatActivity() {
    private val fallback by lazy { BlankGestureHandler(this, null) }
    private val launcher = OmegaLauncher.getLauncher(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.action == OmegaShortcutActivity.START_ACTION) {
            val handlerString = intent.getStringExtra(OmegaShortcutActivity.EXTRA_HANDLER)
            if (handlerString != null) {
                val handler = GestureController.createGestureHandler(
                    this.applicationContext,
                    handlerString,
                    fallback
                )
                if (handler.requiresForeground) {
                    val homeIntent =
                        Intent(Intent.ACTION_MAIN)
                            .addCategory(Intent.CATEGORY_HOME)
                            .setPackage(packageName)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    RunHandlerActivityTracker(handler).addToIntent(homeIntent)
                    Log.d("RunHandlerActivity", "Abriendo actividad...")
                    startActivity(homeIntent)
                } else {
                    triggerGesture(handler)
                }
            }
        } else if (intent.action == Intent.ACTION_ASSIST || intent.action == Intent.ACTION_SEARCH_LONG_PRESS) {
            launcher.gestureController.onLaunchAssistant()
        }
        finish()
    }

    private fun triggerGesture(handler: GestureHandler) =
        handler.onGestureTrigger(launcher.gestureController)

    class RunHandlerActivityTracker(private val handler: GestureHandler) :
        ActivityTracker.SchedulerCallback<OmegaLauncher> {
        override fun init(activity: OmegaLauncher, alreadyOnHome: Boolean): Boolean {
            handler.onGestureTrigger(activity.gestureController)
            return true
        }
    }
}