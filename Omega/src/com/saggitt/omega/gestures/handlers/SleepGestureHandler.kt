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

package com.saggitt.omega.gestures.handlers

import android.accessibilityservice.AccessibilityService
import android.annotation.TargetApi
import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.View
import androidx.annotation.Keep
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.saggitt.omega.gestures.GestureController
import com.saggitt.omega.gestures.GestureHandler
import com.saggitt.omega.omegaApp
import org.json.JSONObject

@Keep
class SleepGestureHandler(context: Context, config: JSONObject?) : GestureHandler(context, config) {
    override val displayName: String = context.getString(R.string.action_sleep)
    override val displayNameRes = R.string.action_sleep

    override fun onGestureTrigger(controller: GestureController, view: View?) {
        method!!.sleep(controller)
    }

    // Preferred methods should appear earlier in the list
    private val method: SleepMethod? by lazy {
        listOf(
            SleepMethodPieAccessibility(context),
            SleepMethodDeviceAdmin(context)
        ).firstOrNull { it.supported }
    }

    override val isAvailable = true // At least the device admin method is always going to work

    abstract class SleepMethod(protected val context: Context) {
        abstract val supported: Boolean
        abstract fun sleep(controller: GestureController?)
    }
}

class SleepMethodPieAccessibility(context: Context) : SleepGestureHandler.SleepMethod(context) {
    override val supported = Utilities.ATLEAST_P

    @TargetApi(Build.VERSION_CODES.P)
    override fun sleep(controller: GestureController?) {
        context.omegaApp.performGlobalAction(AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN)
    }
}

class SleepMethodDeviceAdmin(context: Context) : SleepGestureHandler.SleepMethod(context) {
    override val supported = true

    override fun sleep(controller: GestureController?) {
        val devicePolicyManager =
            context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        if (devicePolicyManager.isAdminActive(
                ComponentName(
                    context,
                    SleepDeviceAdmin::class.java
                )
            )
        ) {
            devicePolicyManager.lockNow()
        } else {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            intent.putExtra(
                DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                ComponentName(context, SleepDeviceAdmin::class.java)
            )
            intent.putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                context.getString(R.string.dt2s_admin_hint)
            )
            context.startActivity(intent)
        }
    }

    class SleepDeviceAdmin : DeviceAdminReceiver() {

        override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
            return context.getString(R.string.dt2s_admin_warning)
        }
    }
}
