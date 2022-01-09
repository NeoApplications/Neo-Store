package com.saggitt.omega.preferences.views

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.android.launcher3.BuildConfig
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.databinding.PreferencesActivityBinding
import com.farmerbb.taskbar.lib.Taskbar
import com.saggitt.omega.PREFS_PROTECTED_APPS
import com.saggitt.omega.PREFS_TRUST_APPS
import com.saggitt.omega.changeDefaultHome
import com.saggitt.omega.preferences.custom.CustomDialogPreference
import com.saggitt.omega.theme.ThemeManager
import com.saggitt.omega.theme.ThemeOverride
import com.saggitt.omega.util.Config
import com.saggitt.omega.util.omegaPrefs

open class PreferencesActivity : AppCompatActivity(),
        ThemeManager.ThemeableActivity {
    private lateinit var binding: PreferencesActivityBinding
    private var currentTheme = 0
    private lateinit var themeOverride: ThemeOverride
    private val themeSet: ThemeOverride.ThemeSet get() = ThemeOverride.Settings()
    private var paused = false

    override fun onCreate(savedInstanceState: Bundle?) {
        themeOverride = ThemeOverride(themeSet, this)
        themeOverride.applyTheme(this)
        val config = Config(this)
        config.setAppLanguage(omegaPrefs.language)

        currentTheme = themeOverride.getTheme(this)
        theme.applyStyle(
                resources.getIdentifier(
                        Integer.toHexString(omegaPrefs.accentColor),
                        "style",
                        packageName
                ), true
        )
        super.onCreate(savedInstanceState)
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
                    AppCompatResources.getDrawable(this, R.drawable.ic_sysbar_back)
            }
        }

        DEFAULT_HOME = resolveDefaultHome()
    }

    private fun getSettingFragment(): Fragment {
        val fragment: String = intent.getStringExtra(EXTRA_FRAGMENT) ?: ""
        return if (fragment.isNotEmpty()) {
            Fragment.instantiate(this, fragment, intent.getBundleExtra(EXTRA_FRAGMENT_ARGS))
        } else {
            PrefsMainFragment()
        }
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
                    val transaction: FragmentTransaction =
                        requireFragmentManager().beginTransaction()
                    transaction.replace(R.id.fragment_container, PrefsDevFragment())
                    transaction.addToBackStack(null)
                    transaction.commit()
                }
                else -> return false
            }
            return true
        }
    }

    class PrefsDockFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences_dock, rootKey)
        }

        override fun onResume() {
            super.onResume()
            requireActivity().title = requireActivity().getString(R.string.title__general_dock)
        }
    }

    class PrefsDrawerFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences_drawer, rootKey)
        }

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
                onPreferenceClickListener = Preference.OnPreferenceClickListener { preference ->
                    if (
                        Utilities.getOmegaPrefs(requireContext()).enableProtectedApps &&
                        Utilities.ATLEAST_R
                    ) {
                        Config.showLockScreen(
                            requireContext(),
                            getString(R.string.trust_apps_manager_name)
                        ) {
                            val fragment = "com.saggitt.omega.allapps.HiddenAppsFragment"
                            startFragment(
                                context,
                                fragment,
                                context.resources.getString(R.string.title__drawer_hide_apps)
                            )
                        }
                    } else {
                        val fragment = "com.saggitt.omega.allapps.HiddenAppsFragment"
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

        override fun onResume() {
            super.onResume()
            requireActivity().title = requireActivity().getString(R.string.title__general_drawer)
        }

        override fun onDisplayPreferenceDialog(preference: Preference) {
            val f: DialogFragment
            fragmentManager?.let {
                f = if (preference is CustomDialogPreference) {
                    PreferenceDialogFragment.newInstance(preference)
                } else {
                    super.onDisplayPreferenceDialog(preference)
                    return
                }
                f.setTargetFragment(this, 0)
                f.show(it, "android.support.v7.preference.PreferenceFragment.DIALOG")
            }
        }
    }

    class PrefsSearchFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences_search, rootKey)
        }

        override fun onResume() {
            super.onResume()
            requireActivity().title = requireActivity().getString(R.string.title__general_search)
        }
    }

    class PrefsAdvancedFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences_advanced, rootKey)
        }

        override fun onResume() {
            super.onResume()
            requireActivity().title = requireActivity().getString(R.string.title__general_advanced)
        }
    }

    class PrefsDevFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences_dev, rootKey)
        }

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

        override fun onResume() {
            super.onResume()
            requireActivity().title = requireActivity().getString(R.string.developer_options_title)
        }
    }

    override fun onThemeChanged() {
        if (currentTheme == themeOverride.getTheme(this)) return
        if (paused) {
            recreate()
        } else {
            finish()
            startActivity(
                createRelaunchIntent(), ActivityOptions.makeCustomAnimation(
                    this, android.R.anim.fade_in, android.R.anim.fade_out
                ).toBundle()
            )
        }
    }

    fun createRelaunchIntent(): Intent {
        val state = Bundle()
        onSaveInstanceState(state)
        return intent.putExtra("state", state)
    }

    companion object {
        var DEFAULT_HOME: String? = ""

        const val EXTRA_TITLE = "title"
        const val EXTRA_FRAGMENT = "fragment"
        const val EXTRA_FRAGMENT_ARGS = "fragmentArgs"

        fun startFragment(
            context: Context,
            fragment: String?,
            title: String?
        ) {
            context.startActivity(createFragmentIntent(context, fragment, title))
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
    }
}