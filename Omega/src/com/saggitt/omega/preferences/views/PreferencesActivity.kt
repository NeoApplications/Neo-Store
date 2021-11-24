package com.saggitt.omega.preferences.views

import android.app.ActivityOptions
import android.app.Dialog
import android.content.*
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.android.launcher3.BuildConfig
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.databinding.PreferencesActivityBinding
import com.android.launcher3.notification.NotificationListener
import com.android.launcher3.settings.NotificationDotsPreference
import com.saggitt.omega.PREFS_PROTECTED_APPS
import com.saggitt.omega.changeDefaultHome
import com.saggitt.omega.theme.ThemeManager
import com.saggitt.omega.theme.ThemeOverride
import com.saggitt.omega.util.SettingsObserver
import com.saggitt.omega.util.omegaPrefs

open class PreferencesActivity : AppCompatActivity(), ThemeManager.ThemeableActivity {
    private lateinit var binding: PreferencesActivityBinding
    private var currentTheme = 0
    private lateinit var themeOverride: ThemeOverride
    private val themeSet: ThemeOverride.ThemeSet get() = ThemeOverride.Settings()
    private var paused = false

    override fun onCreate(savedInstanceState: Bundle?) {
        themeOverride = ThemeOverride(themeSet, this)
        themeOverride.applyTheme(this)
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
            .replace(R.id.fragment_container, PrefsMainFragment()).commit()
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
    }

    class PrefsDrawerFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences_drawer, rootKey)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            findPreference<SwitchPreferenceCompat>(PREFS_PROTECTED_APPS)?.apply {
                onPreferenceChangeListener =
                    Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any ->
                        requireActivity().omegaPrefs.enableProtectedApps = newValue as Boolean
                        true
                    }

                isVisible = Utilities.ATLEAST_R
            }
        }
    }

    class PrefsSearchFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences_search, rootKey)
        }
    }

    class PrefsGesturesFragment : PreferenceFragmentCompat() {
        private var mIconBadgingObserver: IconBadgingObserver? = null

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences_gestures, rootKey)
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            val iconBadgingPref: NotificationDotsPreference = findPreference<Preference>(
                NOTIFICATION_DOTS_PREFERENCE_KEY
            ) as NotificationDotsPreference
            // Listen to system notification badge settings while this UI is active.
            mIconBadgingObserver = IconBadgingObserver(
                iconBadgingPref, requireContext().contentResolver, fragmentManager
            )
            mIconBadgingObserver?.register(
                NOTIFICATION_BADGING,
                NOTIFICATION_ENABLED_LISTENERS
            )
        }
    }

    class PrefsAdvancedFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences_advanced, rootKey)
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

    class NotificationAccessConfirmation : DialogFragment(), DialogInterface.OnClickListener {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val context: Context? = activity
            val msg = context!!.getString(
                R.string.msg_missing_notification_access,
                context.getString(R.string.derived_app_name)
            )
            return AlertDialog.Builder(context)
                .setTitle(R.string.title_missing_notification_access)
                .setMessage(msg)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.title_change_settings, this)
                .create()
        }

        override fun onClick(dialogInterface: DialogInterface, i: Int) {
            val cn = ComponentName(requireActivity(), NotificationListener::class.java)
            val intent: Intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(":settings:fragment_args_key", cn.flattenToString())
            requireActivity().startActivity(intent)
        }
    }

    /**
     * Content observer which listens for system badging setting changes, and updates the launcher
     * badging setting subtext accordingly.
     */
    private class IconBadgingObserver(
        val badgingPref: NotificationDotsPreference, val resolver: ContentResolver,
        val fragmentManager: FragmentManager?
    ) : SettingsObserver.Secure(resolver), Preference.OnPreferenceClickListener {
        private var serviceEnabled = true
        override fun onSettingChanged(keySettingEnabled: Boolean) {
            var summary =
                if (keySettingEnabled) R.string.notification_dots_desc_on else R.string.notification_dots_desc_off
            if (keySettingEnabled) {
                // Check if the listener is enabled or not.
                val enabledListeners =
                    Settings.Secure.getString(
                        resolver,
                        NOTIFICATION_ENABLED_LISTENERS
                    )
                val myListener =
                    ComponentName(badgingPref.context, NotificationListener::class.java)
                serviceEnabled = enabledListeners != null &&
                        (enabledListeners.contains(myListener.flattenToString()) ||
                                enabledListeners.contains(myListener.flattenToShortString()))
                if (!serviceEnabled) {
                    summary = R.string.title_missing_notification_access
                }
            }
            badgingPref.setWidgetFrameVisible(!serviceEnabled)
            badgingPref.onPreferenceClickListener =
                if (serviceEnabled && Utilities.ATLEAST_OREO) null else this
            badgingPref.setSummary(summary)
        }

        override fun onPreferenceClick(preference: Preference): Boolean {
            if (!Utilities.ATLEAST_OREO && serviceEnabled) {
                val cn = ComponentName(
                    preference.context,
                    NotificationListener::class.java
                )
                val intent: Intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra(":settings:fragment_args_key", cn.flattenToString())
                preference.context.startActivity(intent)
            } else {
                fragmentManager?.let {
                    NotificationAccessConfirmation()
                        .show(it, "notification_access")
                }
            }
            return true
        }
    }


    companion object {
        var DEFAULT_HOME: String? = ""

        const val NOTIFICATION_BADGING = "notification_badging"
        private const val NOTIFICATION_DOTS_PREFERENCE_KEY = "pref_icon_badging"

        /**
         * Hidden field Settings.Secure.ENABLED_NOTIFICATION_LISTENERS
         */
        private const val NOTIFICATION_ENABLED_LISTENERS = "enabled_notification_listeners"
    }
}