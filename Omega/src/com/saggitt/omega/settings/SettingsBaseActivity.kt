/*
 *
 *  *
 *  *  * Copyright (c) 2020 Omega Launcher
 *  *  *
 *  *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  *  * you may not use this file except in compliance with the License.
 *  *  * You may obtain a copy of the License at
 *  *  *
 *  *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *  *
 *  *  * Unless required by applicable law or agreed to in writing, software
 *  *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  * See the License for the specific language governing permissions and
 *  *  * limitations under the License.
 *  *
 *
 */

package com.saggitt.omega.settings

import android.app.ActivityOptions
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.android.launcher3.InsettableFrameLayout
import com.saggitt.omega.theme.ThemeManager
import com.saggitt.omega.theme.ThemeOverride

open class SettingsBaseActivity : AppCompatActivity(), ThemeManager.ThemeableActivity {
    val dragLayer by lazy { SettingsDragLayer(this, null) }
    val decorLayout by lazy { DecorLayout(this, window) }

    private var currentTheme = 0
    private var paused = false
    private lateinit var themeOverride: ThemeOverride
    protected open val themeSet: ThemeOverride.ThemeSet get() = ThemeOverride.Settings()
    private val fromSettings by lazy { intent.getBooleanExtra(EXTRA_FROM_SETTINGS, false) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState ?: intent.getBundleExtra("state"))
        dragLayer.addView(decorLayout, InsettableFrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        super.setContentView(dragLayer)
    }

    protected fun overrideOpenAnim() {
        if (fromSettings) {
            overridePendingTransition("activity_open_enter", "activity_open_exit")
        }
    }

    private fun getAndroidAnimRes(name: String): Int {
        return resources.getIdentifier(name, "anim", "android")
    }

    private fun overridePendingTransition(enter: String, exit: String) {
        val enterRes = getAndroidAnimRes(enter)
        val exitRes = getAndroidAnimRes(exit)
        if (enterRes != 0 && exitRes != 0) {
            overridePendingTransition(enterRes, exitRes)
        }
    }

    protected fun getRelaunchInstanceState(savedInstanceState: Bundle?): Bundle? {
        return savedInstanceState ?: intent.getBundleExtra("state")
    }

    protected open fun createRelaunchIntent(): Intent {
        val state = Bundle()
        onSaveInstanceState(state)
        return intent.putExtra("state", state)
    }

    override fun onThemeChanged() {
        if (currentTheme == themeOverride.getTheme(this)) return
        if (paused) {
            recreate()
        } else {
            finish()
            startActivity(createRelaunchIntent(), ActivityOptions.makeCustomAnimation(
                    this, android.R.anim.fade_in, android.R.anim.fade_out).toBundle())
        }
    }

    companion object {

        const val EXTRA_FROM_SETTINGS = "fromSettings"

        fun getActivity(context: Context): SettingsBaseActivity {
            return context as? SettingsBaseActivity
                    ?: (context as ContextWrapper).baseContext as SettingsBaseActivity
        }
    }
}