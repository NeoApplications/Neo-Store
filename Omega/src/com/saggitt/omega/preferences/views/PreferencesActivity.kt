/*
 * This file is part of Omega Launcher
 * Copyright (c) 2022   Omega Launcher Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saggitt.omega.preferences.views

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.os.ResultReceiver
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.android.launcher3.BuildConfig
import com.android.launcher3.InsettableFrameLayout
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.databinding.PreferencesActivityBinding
import com.android.launcher3.util.ComponentKey
import com.android.launcher3.util.Executors.MAIN_EXECUTOR
import com.farmerbb.taskbar.lib.Taskbar
import com.saggitt.omega.OmegaLayoutInflater
import com.saggitt.omega.PREFS_PROTECTED_APPS
import com.saggitt.omega.PREFS_TRUST_APPS
import com.saggitt.omega.changeDefaultHome
import com.saggitt.omega.groups.DrawerTabs
import com.saggitt.omega.settings.SettingsDragLayer
import com.saggitt.omega.theme.ThemeManager
import com.saggitt.omega.theme.ThemeOverride
import com.saggitt.omega.util.Config
import com.saggitt.omega.util.omegaPrefs
import com.saggitt.omega.util.recreateAnimated
import com.saggitt.omega.views.DecorLayout

open class PreferencesActivity : AppCompatActivity(), ThemeManager.ThemeableActivity {
    private lateinit var binding: PreferencesActivityBinding
    override var currentTheme = 0
    override var currentAccent = 0
    private lateinit var themeOverride: ThemeOverride
    private val themeSet: ThemeOverride.ThemeSet get() = ThemeOverride.Settings()
    private var paused = false
    val dragLayer by lazy { SettingsDragLayer(this, null) }
    private val decorLayout by lazy { DecorLayout(this) }
    private val customLayoutInflater by lazy {
        OmegaLayoutInflater(super.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater, this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        themeOverride = ThemeOverride(themeSet, this)
        themeOverride.applyTheme(this)
        val config = Config(this)
        config.setAppLanguage(omegaPrefs.language)

        currentAccent = omegaPrefs.accentColor
        currentTheme = themeOverride.getTheme(this)
        theme.applyStyle(
                resources.getIdentifier(
                        Integer.toHexString(currentAccent),
                        "style",
                        packageName
                ), true
        )
        super.onCreate(savedInstanceState)
        dragLayer.addView(decorLayout, InsettableFrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        super.setContentView(dragLayer)

        binding = PreferencesActivityBinding.inflate(layoutInflater)

        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, getSettingFragment()).commit()
        setContentView(binding.root)
        setSupportActionBar(binding.actionBar)
        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0) {
                binding.actionBar.setNavigationOnClickListener { super.onBackPressed() }
                binding.actionBar.navigationIcon = null
            } else {
                binding.actionBar.setNavigationOnClickListener { supportFragmentManager.popBackStack() }
                binding.actionBar.navigationIcon =
                        AppCompatResources.getDrawable(this, R.drawable.ic_arrow_back)
            }
        }

        DEFAULT_HOME = resolveDefaultHome()
    }

    private fun getSettingFragment(): Fragment {
        val fragment: String = intent.getStringExtra(EXTRA_FRAGMENT) ?: ""
        return if (fragment.isNotEmpty()) {
            supportFragmentManager.fragmentFactory
                    .instantiate(ClassLoader.getSystemClassLoader(), fragment).apply {
                        arguments = intent.getBundleExtra(EXTRA_FRAGMENT_ARGS)
                    }
        } else {
            PrefsMainFragment()
        }
    }

    override fun onBackPressed() {
        dragLayer.getTopOpenView()?.let {
            it.close(true)
            return
        }
        super.onBackPressed()
    }

    fun getContentFrame(): ViewGroup {
        return decorLayout.findViewById(android.R.id.content)
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

    override fun getSystemService(name: String): Any? {
        if (name == Context.LAYOUT_INFLATER_SERVICE) {
            return customLayoutInflater
        }
        return super.getSystemService(name)
    }


    private fun resolveDefaultHome(): String? {
        val homeIntent: Intent = Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_HOME)
        val info: ResolveInfo? = packageManager
                .resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
        return if (info?.activityInfo != null) {
            info.activityInfo.packageName
        } else {
            null
        }
    }

    override fun onThemeChanged() {
        if (currentTheme == themeOverride.getTheme(this)) return
        if (paused) {
            recreate()
        } else {
            recreateAnimated()
        }
    }

    // TODO should any of those sub classes get larger, then it should be moved to own class
    class PrefsMainFragment : PreferenceFragmentCompat() {
        private var mShowDevOptions = false
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            mShowDevOptions = Utilities.getOmegaPrefs(activity).developerOptionsEnabled
            setHasOptionsMenu(true)

            findPreference<Preference>("pref_showDevOptions")?.apply {
                isVisible = Utilities.getOmegaPrefs(context).developerOptionsEnabled
            }
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences_main, rootKey)
        }

        override fun onResume() {
            super.onResume()
            requireActivity().title = requireActivity().getString(R.string.settings_button_text)
            val dev = Utilities.getOmegaPrefs(activity).developerOptionsEnabled
            if (dev != mShowDevOptions) {
                activity?.recreate()
            }
        }

        override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
            inflater.inflate(R.menu.menu_settings, menu)
            if (BuildConfig.APPLICATION_ID != DEFAULT_HOME) {
                inflater.inflate(R.menu.menu_change_default_home, menu)
            }
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.action_change_default_home -> changeDefaultHome(requireContext())
                R.id.action_restart_launcher -> Utilities.killLauncher()
                R.id.action_dev_options -> {
                    requireActivity().supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, PrefsDevFragment()).commit()
                }
                else -> return false
            }
            return true
        }
    }

    class PrefsDockFragment :
            BasePreferenceFragment(R.xml.preferences_dock, R.string.title__general_dock)

    class PrefsDrawerFragment :
            BasePreferenceFragment(R.xml.preferences_drawer, R.string.title__general_drawer) {

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            findPreference<SwitchPreference>(PREFS_PROTECTED_APPS)?.apply {
                onPreferenceChangeListener =
                        Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any ->
                            requireActivity().omegaPrefs.enableProtectedApps = newValue as Boolean
                            true
                        }

                isVisible = Utilities.ATLEAST_R
            }

            findPreference<Preference>(PREFS_TRUST_APPS)?.apply {
                onPreferenceClickListener = Preference.OnPreferenceClickListener {
                    if (
                            Utilities.getOmegaPrefs(requireContext()).enableProtectedApps &&
                            Utilities.ATLEAST_R
                    ) {
                        Config.showLockScreen(
                                requireContext(),
                                getString(R.string.trust_apps_manager_name)
                        ) {
                            val fragment = "com.saggitt.omega.preferences.views.HiddenAppsFragment"
                            startFragment(
                                    context,
                                    fragment,
                                    context.resources.getString(R.string.title__drawer_hide_apps)
                            )
                        }
                    } else {
                        val fragment = "com.saggitt.omega.preferences.views.HiddenAppsFragment"
                        startFragment(
                                context,
                                fragment,
                                context.resources.getString(R.string.title__drawer_hide_apps)
                        )
                    }
                    false
                }
            }
        }
    }

    class PrefsSearchFragment :
            BasePreferenceFragment(R.xml.preferences_search, R.string.title__general_search)

    class PrefsAdvancedFragment :
            BasePreferenceFragment(R.xml.preferences_advanced, R.string.title__general_advanced)

    class PrefsDevFragment :
            BasePreferenceFragment(R.xml.preferences_dev, R.string.developer_options_title) {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            findPreference<Preference>("kill")?.setOnPreferenceClickListener {
                Utilities.killLauncher()
                false
            }
            findPreference<Preference>("pref_desktop_mode_settings")?.setOnPreferenceClickListener {
                Taskbar.openSettings(
                        requireContext(),
                        ThemeOverride.Settings().getTheme(requireContext())
                )
                true
            }
        }
    }

    companion object {
        var DEFAULT_HOME: String? = ""

        private const val EXTRA_TITLE = "title"
        const val EXTRA_FRAGMENT = "fragment"
        const val EXTRA_FRAGMENT_ARGS = "fragmentArgs"

        fun startFragment(
                context: Context,
                fragment: String?,
                title: String?
        ) {
            context.startActivity(createFragmentIntent(context, fragment, title))
        }

        /* Used for SelectableAppsFragment */
        const val KEY_SELECTION = "selection"
        const val KEY_CALLBACK = "callback"
        const val KEY_FILTER_IS_WORK = "filterIsWork"

        fun startFragment(
                context: Context,
                fragment: String?,
                title: String?,
                selection: Collection<ComponentKey>,
                callback: (Collection<ComponentKey>?) -> Unit,
                profile: DrawerTabs.Profile
        ) {
            val args = Bundle()
            val intent = Intent(context, PreferencesActivity::class.java).apply {
                args.putStringArrayList(KEY_SELECTION, ArrayList(selection.map { it.toString() }))
                args.putParcelable(KEY_CALLBACK, object : ResultReceiver(MAIN_EXECUTOR.handler) {

                    override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                        if (resultCode == RESULT_OK) {
                            callback(resultData!!.getStringArrayList(KEY_SELECTION)!!.map {
                                Utilities.makeComponentKey(context, it)
                            })
                        } else {
                            callback(null)
                        }
                    }
                })
                args.putParcelable(KEY_FILTER_IS_WORK, profile)

                putExtra(EXTRA_FRAGMENT_ARGS, args)
                putExtra(EXTRA_FRAGMENT, fragment)
                if (title != null) {
                    putExtra(EXTRA_TITLE, title)
                }
            }
            context.startActivity(intent)
        }

        private fun createFragmentIntent(
                context: Context,
                fragment: String?,
                title: CharSequence?
        ): Intent {
            val intent = Intent(context, PreferencesActivity::class.java)
            intent.putExtra(EXTRA_FRAGMENT, fragment)
            if (title != null) {
                intent.putExtra(EXTRA_TITLE, title)
            }

            return intent
        }

        fun getActivity(context: Context): PreferencesActivity {
            return context as? PreferencesActivity
                    ?: (context as ContextWrapper).baseContext as PreferencesActivity
        }
    }
}