/*
 * Copyright (c) 2020 Omega Launcher
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
 */

package com.saggitt.omega.views

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.android.launcher3.Insettable
import com.android.launcher3.Launcher
import com.android.launcher3.R
import com.android.launcher3.views.OptionsPopupView
import com.saggitt.omega.OmegaPreferences
import com.saggitt.omega.util.forEachChild
import com.saggitt.omega.util.isVisible
import com.saggitt.omega.util.omegaPrefs
import kotlinx.android.synthetic.omega.options_view.view.*

class OptionsPanel(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs),
        Insettable, View.OnClickListener, OmegaPreferences.OnPreferenceChangeListener {

    private val launcher = Launcher.getLauncher(context)

    override fun onFinishInflate() {
        super.onFinishInflate()

        forEachChild { it.setOnClickListener(this) }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        context.omegaPrefs.addOnPreferenceChangeListener("pref_lockDesktop", this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        context.omegaPrefs.removeOnPreferenceChangeListener("pref_lockDesktop", this)
    }

    override fun onValueChanged(key: String, prefs: OmegaPreferences, force: Boolean) {
        widget_button.isVisible = !prefs.lockDesktop
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.wallpaper_button -> OptionsPopupView.startWallpaperPicker(v)
            R.id.widget_button -> OptionsPopupView.onWidgetsClicked(v)
            R.id.settings_button -> OptionsPopupView.startSettings(v)
        }
    }

    override fun setInsets(insets: Rect) {
        val deviceProfile = launcher.deviceProfile

        layoutParams = (layoutParams as FrameLayout.LayoutParams).also { lp ->
            lp.width = deviceProfile.availableWidthPx
            lp.height = (deviceProfile.availableHeightPx * .2f).toInt()
            lp.bottomMargin = insets.bottom
        }
    }
}
