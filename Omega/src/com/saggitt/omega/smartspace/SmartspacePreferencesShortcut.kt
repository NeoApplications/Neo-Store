package com.saggitt.omega.smartspace

import android.content.Context
import android.content.Intent
import android.view.View
import android.view.View.OnLongClickListener
import com.android.launcher3.Launcher
import com.android.launcher3.R
import com.android.launcher3.logging.StatsLogManager.EventEnum
import com.android.launcher3.views.OptionsPopupView.OptionItem
import com.saggitt.omega.preferences.views.PreferencesActivity

class SmartspacePreferencesShortcut(context:Context,eventId: EventEnum?) : OptionItem(
    context.getString(R.string.customize),
    context.getDrawable(R.drawable.ic_smartspace_preferences),
    eventId,
    OnLongClickListener { view: View -> startSmartspacePreferences(view) }) {

    companion object {
        private fun startSmartspacePreferences(view: View): Boolean {
            val launcher = Launcher.getLauncher(view.context)
            launcher.startActivitySafely(
                view, Intent(launcher, PreferencesActivity::class.java)
                    .putExtra(
                        "title",
                        launcher.getString(R.string.home_widget)
                    )
                    .putExtra(
                        "content_res_id",
                        R.xml.preferences_smartspace
                    )
                    .putExtra("has_preview", true), null
            )
            return true
        }
    }
}