/*
 *  Copyright (c) 2020 Omega Launcher
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
 *
 */

package com.saggitt.omega.settings

import android.app.ActivityOptions
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.android.launcher3.InsettableFrameLayout
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.saggitt.omega.OmegaLayoutInflater
import com.saggitt.omega.theme.ThemeManager
import com.saggitt.omega.theme.ThemeOverride
import com.saggitt.omega.util.getBooleanAttr
import com.saggitt.omega.util.launcherAppState

open class SettingsBaseActivity : AppCompatActivity(), ThemeManager.ThemeableActivity {
    val dragLayer by lazy { SettingsDragLayer(this, null) }
    val decorLayout by lazy { DecorLayout(this, window) }

    private var currentTheme = 0
    private var paused = false
    private lateinit var themeOverride: ThemeOverride
    protected open val themeSet: ThemeOverride.ThemeSet get() = ThemeOverride.Settings()
    private val customLayoutInflater by lazy {
        OmegaLayoutInflater(super.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater, this)
    }

    private val fromSettings by lazy { intent.getBooleanExtra(EXTRA_FROM_SETTINGS, false) }

    override fun onCreate(savedInstanceState: Bundle?) {
        (layoutInflater as OmegaLayoutInflater).installFactory(delegate)
        themeOverride = ThemeOverride(themeSet, this)
        themeOverride.applyTheme(this)
        currentTheme = themeOverride.getTheme(this)

        super.onCreate(savedInstanceState ?: intent.getBundleExtra("state"))
        dragLayer.addView(decorLayout, InsettableFrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        super.setContentView(dragLayer)

        val prefs = Utilities.getOmegaPrefs(this)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setTitleTextColor(prefs.accentColor)
        setSupportActionBar(toolbar)

        var flags = window.decorView.systemUiVisibility
        val useLightBars = getBooleanAttr(R.attr.useLightSystemBars)
        flags = Utilities.setFlag(flags, View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR, useLightBars)
        if (Utilities.ATLEAST_OREO) {
            flags = Utilities.setFlag(flags, View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR, useLightBars)
        }

        flags = flags or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        flags = flags or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        flags = flags or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.decorView.systemUiVisibility = flags
    }

    protected fun overrideOpenAnim() {
        if (fromSettings) {
            overridePendingTransition("activity_open_enter", "activity_open_exit")
        }
    }

    protected fun overrideCloseAnim() {
        if (fromSettings) {
            overridePendingTransition("activity_close_enter", "activity_close_exit")
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

    override fun onBackPressed() {
        dragLayer.getTopOpenView()?.let {
            it.close(true)
            return
        }
        super.onBackPressed()
    }

    override fun setContentView(v: View) {
        val contentParent = getContentFrame()
        contentParent.removeAllViews()
        contentParent.addView(v)
    }

    override fun setContentView(resId: Int) {
        val contentParent = getContentFrame()
        contentParent.removeAllViews()
        LayoutInflater.from(this).inflate(resId, contentParent)
    }

    override fun setContentView(v: View, lp: ViewGroup.LayoutParams) {
        val contentParent = getContentFrame()
        contentParent.removeAllViews()
        contentParent.addView(v, lp)
    }

    fun getContentFrame(): ViewGroup {
        return decorLayout.findViewById(android.R.id.content)
    }

    override fun onResume() {
        super.onResume()
        paused = false
    }

    override fun onPause() {
        super.onPause()
        paused = true
    }

    protected open fun createRelaunchIntent(): Intent {
        val state = Bundle()
        onSaveInstanceState(state)
        return intent.putExtra("state", state)
    }

    protected fun getRelaunchInstanceState(savedInstanceState: Bundle?): Bundle? {
        return savedInstanceState ?: intent.getBundleExtra("state")
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

    override fun getSystemService(name: String): Any? {
        if (name == Context.LAYOUT_INFLATER_SERVICE) {
            return customLayoutInflater
        }
        return super.getSystemService(name)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        launcherAppState.launcher?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    companion object {

        const val EXTRA_FROM_SETTINGS = "fromSettings"

        fun getActivity(context: Context): SettingsBaseActivity {
            return context as? SettingsBaseActivity
                    ?: (context as ContextWrapper).baseContext as SettingsBaseActivity
        }
    }
}