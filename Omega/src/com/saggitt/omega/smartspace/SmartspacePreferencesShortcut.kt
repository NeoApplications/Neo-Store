package com.saggitt.omega.smartspace

import android.content.Intent
import android.view.View
import android.view.View.OnLongClickListener
import com.android.launcher3.Launcher
import com.android.launcher3.R
import com.android.launcher3.logging.StatsLogManager.EventEnum
import com.android.launcher3.views.OptionsPopupView.OptionItem
import com.saggitt.omega.settings.SettingsActivity

class SmartspacePreferencesShortcut(eventId: EventEnum?) : OptionItem(
    R.string.customize,
    R.drawable.ic_smartspace_preferences,
    eventId,
    OnLongClickListener { view: View -> startSmartspacePreferences(view) }) {

    companion object {
        private fun startSmartspacePreferences(view: View): Boolean {
            val launcher = Launcher.getLauncher(view.context)
            launcher.startActivitySafely(
                view, Intent(launcher, SettingsActivity::class.java)
                    .putExtra(
                        SettingsActivity.SubSettingsFragment.TITLE,
                        launcher.getString(R.string.home_widget)
                    )
                    .putExtra(
                        SettingsActivity.SubSettingsFragment.CONTENT_RES_ID,
                        R.xml.omega_preferences_smartspace
                    )
                    .putExtra(SettingsActivity.SubSettingsFragment.HAS_PREVIEW, true), null, null
            )
            return true
        }
    }
}