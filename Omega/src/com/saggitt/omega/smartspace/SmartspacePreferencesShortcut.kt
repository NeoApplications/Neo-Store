package com.saggitt.omega.smartspace

import android.content.Context
import android.view.View
import android.view.View.OnLongClickListener
import com.android.launcher3.R
import com.android.launcher3.logging.StatsLogManager.EventEnum
import com.android.launcher3.views.OptionsPopupView.OptionItem
import com.saggitt.omega.preferences.views.PreferencesActivity

class SmartspacePreferencesShortcut(context: Context, eventId: EventEnum?) : OptionItem(
    context.getString(R.string.customize),
    context.getDrawable(R.drawable.ic_smartspace_preferences),
    eventId,
    OnLongClickListener { view: View -> startSmartspacePreferences(context) }) {

    companion object {
        private fun startSmartspacePreferences(context: Context): Boolean {
            val fragment = "com.saggitt.omega.preferences.views.PrefsWidgetFragment"
            PreferencesActivity.startFragment(
                context,
                fragment,
                context.resources.getString(R.string.home_widget)
            )

            return true
        }
    }
}