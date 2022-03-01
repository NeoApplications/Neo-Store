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

package com.saggitt.omega.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.google.systemui.smartspace.SmartSpaceView
import com.saggitt.omega.*
import com.saggitt.omega.iconpack.IconPackPreview
import com.saggitt.omega.preferences.OmegaPreferences

class SmartSpacePreview(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs),
    OmegaPreferences.OnPreferenceChangeListener {

    private val prefs = Utilities.getOmegaPrefs(context)
    private val usePillQsb = prefs::usePillQsb
    private val prefsToWatch = arrayOf(
        PREFS_SMARTSPACE_TIME, PREFS_SMARTSPACE_TIME_ABOVE,
        PREFS_TIME_24H, PREFS_SMARTSPACE_DATE, PREF_PILL_QSB
    )
    private val needsReinflate = setOf(PREF_PILL_QSB)
    private var currentView: SmartSpaceView? = null
    private val themedContext = IconPackPreview.PreviewContext(context)

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        prefs.addOnPreferenceChangeListener(this, *prefsToWatch)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        prefs.removeOnPreferenceChangeListener(this, *prefsToWatch)
    }

    override fun onValueChanged(key: String, prefs: OmegaPreferences, force: Boolean) {
        if (currentView == null || needsReinflate.contains(key)) {
            removeAllViews()
            inflateCurrentView()
        } else {
            currentView!!.reloadCustomizations()
        }
    }

    private fun inflateCurrentView() {
        val layout =
            if (usePillQsb.get()) R.layout.qsb_container_preview else R.layout.search_container_workspace
        addView(inflateView(layout))
    }

    private fun inflateView(layout: Int): View {
        val view = LayoutInflater.from(themedContext).inflate(layout, this, false)
        view.layoutParams.height =
            resources.getDimensionPixelSize(R.dimen.smartspace_preview_height)
        currentView = view as? SmartSpaceView
        return view
    }
}
