/*
 *  This file is part of Omega Launcher
 *  Copyright (c) 2021   Saul Henriquez
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

package com.saggitt.omega.settings

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.view.WindowInsets
import androidx.annotation.VisibleForTesting
import androidx.core.view.WindowCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceGroup.PreferencePositionCallback
import androidx.preference.PreferenceScreen
import androidx.recyclerview.widget.RecyclerView
import com.android.launcher3.InvariantDeviceProfile
import com.android.launcher3.LauncherFiles
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.config.FeatureFlags
import com.android.launcher3.model.WidgetsModel
import com.android.launcher3.settings.DeveloperOptionsFragment
import com.android.launcher3.settings.PreferenceHighlighter
import com.android.launcher3.states.RotationHelper
import com.android.launcher3.uioverrides.plugins.PluginManagerWrapper

/**
 * Settings activity for Launcher. Currently implements the following setting: Allow rotation
 */
class SettingsActivity : FragmentActivity(),
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback,
    PreferenceFragmentCompat.OnPreferenceStartScreenCallback,
    SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        setActionBar(findViewById(R.id.action_bar))
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val intent = intent
        if (intent.hasExtra(EXTRA_FRAGMENT) || intent.hasExtra(EXTRA_FRAGMENT_ARGS)) {
            actionBar!!.setDisplayHomeAsUpEnabled(true)
        }
        if (savedInstanceState == null) {
            var args = intent.getBundleExtra(EXTRA_FRAGMENT_ARGS)
            if (args == null) {
                args = Bundle()
            }
            val prefKey = intent.getStringExtra(EXTRA_FRAGMENT_ARG_KEY)
            if (!TextUtils.isEmpty(prefKey)) {
                args.putString(EXTRA_FRAGMENT_ARG_KEY, prefKey)
            }
            val fm = supportFragmentManager
            val f = fm.fragmentFactory.instantiate(
                classLoader,
                preferenceFragment!!
            )
            f.arguments = args
            // Display the fragment as the main content.
            fm.beginTransaction().replace(R.id.content_frame, f).commit()
        }
        Utilities.getPrefs(applicationContext).registerOnSharedPreferenceChangeListener(this)
    }

    /**
     * Obtains the preference fragment to instantiate in this activity.
     *
     * @return the preference fragment class
     * @throws IllegalArgumentException if the fragment is unknown to this activity
     */
    private val preferenceFragment: String?
        private get() {
            val preferenceFragment = intent.getStringExtra(EXTRA_FRAGMENT)
            val defaultFragment = getString(R.string.settings_fragment_name)
            return if (TextUtils.isEmpty(preferenceFragment)) {
                defaultFragment
            } else if (preferenceFragment != defaultFragment
                && !VALID_PREFERENCE_FRAGMENTS.contains(preferenceFragment)
            ) {
                throw IllegalArgumentException(
                    "Invalid fragment for this activity: $preferenceFragment"
                )
            } else {
                preferenceFragment
            }
        }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {}
    private fun startPreference(fragment: String, args: Bundle, key: String): Boolean {
        if (Utilities.ATLEAST_P && supportFragmentManager.isStateSaved) {
            // Sometimes onClick can come after onPause because of being posted on the handler.
            // Skip starting new preferences in that case.
            return false
        }
        val fm = supportFragmentManager
        val f = fm.fragmentFactory.instantiate(classLoader, fragment)
        if (f is DialogFragment) {
            f.setArguments(args)
            f.show(fm, key)
        } else {
            startActivity(
                Intent(this, SettingsActivity::class.java)
                    .putExtra(EXTRA_FRAGMENT, fragment)
                    .putExtra(EXTRA_FRAGMENT_ARGS, args)
            )
        }
        return true
    }

    override fun onPreferenceStartFragment(
        preferenceFragment: PreferenceFragmentCompat, pref: Preference
    ): Boolean {
        return startPreference(pref.fragment, pref.extras, pref.key)
    }

    override fun onPreferenceStartScreen(
        caller: PreferenceFragmentCompat,
        pref: PreferenceScreen
    ): Boolean {
        val args = Bundle()
        args.putString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT, pref.key)
        return startPreference(getString(R.string.settings_fragment_name), args, pref.key)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * This fragment shows the launcher preferences.
     */
    class LauncherSettingsFragment : PreferenceFragmentCompat() {
        private var mHighLightKey: String? = null
        private var mPreferenceHighlighted = false
        private var mDeveloperOptionPref: Preference? = null
        override fun onCreatePreferences(savedInstanceState: Bundle, _rootKey: String) {
            var rootKey: String? = _rootKey
            val args = arguments
            mHighLightKey = args?.getString(EXTRA_FRAGMENT_ARG_KEY)
            if (rootKey == null && !TextUtils.isEmpty(mHighLightKey)) {
                rootKey = getParentKeyForPref(mHighLightKey)
            }
            if (savedInstanceState != null) {
                mPreferenceHighlighted = savedInstanceState.getBoolean(SAVE_HIGHLIGHTED_KEY)
            }
            preferenceManager.sharedPreferencesName = LauncherFiles.SHARED_PREFERENCES_KEY
            setPreferencesFromResource(R.xml.omega_preferences, rootKey)
            val screen = preferenceScreen
            for (i in screen.preferenceCount - 1 downTo 0) {
                val preference = screen.getPreference(i)
                if (!initPreference(preference)) {
                    screen.removePreference(preference)
                }
            }
            if (activity != null && !TextUtils.isEmpty(preferenceScreen.title)) {
                requireActivity().title = preferenceScreen.title
            }
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            val listView: View = listView
            val bottomPadding = listView.paddingBottom
            listView.setOnApplyWindowInsetsListener { v: View, insets: WindowInsets ->
                v.setPadding(
                    v.paddingLeft,
                    v.paddingTop,
                    v.paddingRight,
                    bottomPadding + insets.systemWindowInsetBottom
                )
                insets.consumeSystemWindowInsets()
            }
        }

        override fun onSaveInstanceState(outState: Bundle) {
            super.onSaveInstanceState(outState)
            outState.putBoolean(SAVE_HIGHLIGHTED_KEY, mPreferenceHighlighted)
        }

        protected fun getParentKeyForPref(key: String?): String? {
            return null
        }

        /**
         * Initializes a preference. This is called for every preference. Returning false here
         * will remove that preference from the list.
         */
        protected fun initPreference(preference: Preference): Boolean {
            when (preference.key) {
                NOTIFICATION_DOTS_PREFERENCE_KEY -> return !WidgetsModel.GO_DISABLE_NOTIFICATION_DOTS
                RotationHelper.ALLOW_ROTATION_PREFERENCE_KEY -> {
                    val deviceProfile =
                        InvariantDeviceProfile.INSTANCE[context].getDeviceProfile(context)
                    if (deviceProfile.allowRotation) {
                        // Launcher supports rotation by default. No need to show this setting.
                        return false
                    }
                    // Initialize the UI once
                    preference.setDefaultValue(false)
                    return true
                }
                FLAGS_PREFERENCE_KEY ->                     // Only show flag toggler UI if this build variant implements that.
                    return FeatureFlags.showFlagTogglerUi(context)
                DEVELOPER_OPTIONS_KEY -> {
                    mDeveloperOptionPref = preference
                    return updateDeveloperOption()
                }
            }
            return true
        }

        /**
         * Show if plugins are enabled or flag UI is enabled.
         *
         * @return True if we should show the preference option.
         */
        private fun updateDeveloperOption(): Boolean {
            val showPreference = (FeatureFlags.showFlagTogglerUi(context)
                    || PluginManagerWrapper.hasPlugins(context))
            if (mDeveloperOptionPref != null) {
                mDeveloperOptionPref!!.isEnabled = showPreference
                if (showPreference) {
                    preferenceScreen.addPreference(mDeveloperOptionPref)
                } else {
                    preferenceScreen.removePreference(mDeveloperOptionPref)
                }
            }
            return showPreference
        }

        override fun onResume() {
            super.onResume()
            updateDeveloperOption()
            if (isAdded && !mPreferenceHighlighted) {
                val highlighter = createHighlighter()
                if (highlighter != null) {
                    requireView().postDelayed(highlighter, DELAY_HIGHLIGHT_DURATION_MILLIS.toLong())
                    mPreferenceHighlighted = true
                } else {
                    requestAccessibilityFocus(listView)
                }
            }
        }

        private fun createHighlighter(): PreferenceHighlighter? {
            if (TextUtils.isEmpty(mHighLightKey)) {
                return null
            }
            val screen = preferenceScreen ?: return null
            val list = listView
            val callback = list.adapter as PreferencePositionCallback?
            val position = callback!!.getPreferenceAdapterPosition(mHighLightKey)
            return if (position >= 0) PreferenceHighlighter(
                list, position, screen.findPreference(mHighLightKey!!)
            ) else null
        }

        private fun requestAccessibilityFocus(rv: RecyclerView) {
            rv.post {
                if (!rv.hasFocus() && rv.childCount > 0) {
                    rv.getChildAt(0)
                        .performAccessibilityAction(
                            AccessibilityNodeInfoCompat.ACTION_ACCESSIBILITY_FOCUS,
                            null
                        )
                }
            }
        }
    }

    companion object {
        /**
         * List of fragments that can be hosted by this activity.
         */
        private val VALID_PREFERENCE_FRAGMENTS: List<String?> = listOf(
            DesktopFragment::class.java.name,
            DeveloperOptionsFragment::class.java.name
        )
        private const val DEVELOPER_OPTIONS_KEY = "pref_developer_options"
        private const val FLAGS_PREFERENCE_KEY = "flag_toggler"
        private const val NOTIFICATION_DOTS_PREFERENCE_KEY = "pref_icon_badging"
        const val EXTRA_FRAGMENT_ARG_KEY = ":settings:fragment_args_key"
        const val EXTRA_SHOW_FRAGMENT_ARGS = ":settings:show_fragment_args"
        private const val DELAY_HIGHLIGHT_DURATION_MILLIS = 600
        const val SAVE_HIGHLIGHTED_KEY = "android:preference_highlighted"

        @VisibleForTesting
        val EXTRA_FRAGMENT = ":settings:fragment"

        @VisibleForTesting
        val EXTRA_FRAGMENT_ARGS = ":settings:fragment_args"
    }
}
