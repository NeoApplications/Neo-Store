/*
 *  This file is part of Omega Launcher
 *  Copyright (c) 2021   Omega Launcher Team
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

package com.saggitt.omega.preferences.views

import android.app.Dialog
import android.content.*
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.preference.Preference
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.notification.NotificationListener
import com.android.launcher3.settings.NotificationDotsPreference
import com.saggitt.omega.NOTIFICATION_BADGING
import com.saggitt.omega.NOTIFICATION_DOTS_PREFERENCE_KEY
import com.saggitt.omega.util.SettingsObserver

class PrefsGesturesFragment :
    BasePreferenceFragment(R.xml.preferences_gestures, R.string.title__general_notifications) {
    private var mIconBadgingObserver: IconBadgingObserver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val iconBadgingPref: NotificationDotsPreference = findPreference<Preference>(
            NOTIFICATION_DOTS_PREFERENCE_KEY
        ) as NotificationDotsPreference
        // Listen to system notification badge settings while this UI is active.
        mIconBadgingObserver = IconBadgingObserver(
            iconBadgingPref, requireContext().contentResolver, fragmentManager
        )
        mIconBadgingObserver?.register(
            NOTIFICATION_BADGING,
            NOTIFICATION_ENABLED_LISTENERS
        )
    }

    /**
     * Content observer which listens for system badging setting changes, and updates the launcher
     * badging setting subtext accordingly.
     */
    private class IconBadgingObserver(
        val badgingPref: NotificationDotsPreference, val resolver: ContentResolver,
        val fragmentManager: FragmentManager?
    ) : SettingsObserver.Secure(resolver), Preference.OnPreferenceClickListener {
        private var serviceEnabled = true
        override fun onSettingChanged(keySettingEnabled: Boolean) {
            var summary =
                if (keySettingEnabled) R.string.notification_dots_desc_on else R.string.notification_dots_desc_off
            if (keySettingEnabled) {
                // Check if the listener is enabled or not.
                val enabledListeners =
                    Settings.Secure.getString(
                        resolver,
                        NOTIFICATION_ENABLED_LISTENERS
                    )
                val myListener =
                    ComponentName(badgingPref.context, NotificationListener::class.java)
                serviceEnabled = enabledListeners != null &&
                        (enabledListeners.contains(myListener.flattenToString()) ||
                                enabledListeners.contains(myListener.flattenToShortString()))
                if (!serviceEnabled) {
                    summary = R.string.title_missing_notification_access
                }
            }
            badgingPref.setWidgetFrameVisible(!serviceEnabled)
            badgingPref.onPreferenceClickListener =
                if (serviceEnabled && Utilities.ATLEAST_OREO) null else this
            badgingPref.setSummary(summary)
        }

        override fun onPreferenceClick(preference: Preference): Boolean {
            if (!Utilities.ATLEAST_OREO && serviceEnabled) {
                val cn = ComponentName(
                    preference.context,
                    NotificationListener::class.java
                )
                val intent: Intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra(":settings:fragment_args_key", cn.flattenToString())
                preference.context.startActivity(intent)
            } else {
                fragmentManager?.let {
                    NotificationAccessConfirmation()
                        .show(it, "notification_access")
                }
            }
            return true
        }
    }

    class NotificationAccessConfirmation : DialogFragment(), DialogInterface.OnClickListener {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val context: Context? = activity
            val msg = context!!.getString(
                R.string.msg_missing_notification_access,
                context.getString(R.string.derived_app_name)
            )
            return AlertDialog.Builder(context)
                .setTitle(R.string.title_missing_notification_access)
                .setMessage(msg)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.title_change_settings, this)
                .create()
        }

        override fun onClick(dialogInterface: DialogInterface, i: Int) {
            val cn = ComponentName(requireActivity(), NotificationListener::class.java)
            val intent: Intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(":settings:fragment_args_key", cn.flattenToString())
            requireActivity().startActivity(intent)
        }
    }

    companion object {
        /**
         * Hidden field Settings.Secure.ENABLED_NOTIFICATION_LISTENERS
         */
        const val NOTIFICATION_ENABLED_LISTENERS = "enabled_notification_listeners"
    }
}