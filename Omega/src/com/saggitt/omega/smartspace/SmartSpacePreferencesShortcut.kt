package com.saggitt.omega.smartspace

import android.content.Context
import android.view.View
import android.view.View.OnLongClickListener
import com.android.launcher3.R
import com.android.launcher3.logging.StatsLogManager.EventEnum
import com.android.launcher3.views.OptionsPopupView.OptionItem
import com.saggitt.omega.compose.PrefsActivityX
import com.saggitt.omega.compose.navigation.Routes

class SmartSpacePreferencesShortcut(context: Context, eventId: EventEnum?) : OptionItem(
    context.getString(R.string.customize),
    context.getDrawable(R.drawable.ic_smartspace_preferences),
    eventId,
    OnLongClickListener { view: View -> startSmartspacePreferences(context) }) {

    companion object {
        private fun startSmartspacePreferences(context: Context): Boolean {
            context.startActivity(
                PrefsActivityX.createIntent(
                    context,
                    "${Routes.PREFS_WIDGETS}/"
                )
            )

            return true
        }
    }
}