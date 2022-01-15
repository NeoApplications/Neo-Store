package com.saggitt.omega.preferences.views

import android.os.Bundle
import android.view.View
import androidx.preference.*
import com.android.launcher3.R
import com.jaredrummler.android.colorpicker.ColorPreferenceCompat
import com.saggitt.omega.*
import com.saggitt.omega.preferences.custom.SeekbarPreference
import com.saggitt.omega.theme.ThemeManager
import com.saggitt.omega.util.*

class PrefsThemeFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences_theme, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findPreference<ListPreference>(PREFS_THEME)?.apply {
            onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any ->
                    var newTheme = (newValue as String).toInt()
                    if (newTheme.hasFlag(ThemeManager.THEME_FOLLOW_DAYLIGHT) && !requireContext().checkLocationAccess())
                        BlankActivity.requestPermission(
                            context, android.Manifest.permission.ACCESS_COARSE_LOCATION,
                            Config.REQUEST_PERMISSION_LOCATION_ACCESS
                        ) {
                            if (!it)
                                newTheme = newTheme.removeFlag(ThemeManager.THEME_DARK_MASK)
                        }
                    this.value = newTheme.toString()
                    requireActivity().omegaPrefs.launcherTheme = newValue.toString().toInt()
                    true
                }
        }
        findPreference<ColorPreferenceCompat>(PREFS_ACCENT)?.apply {
            onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _: Preference?, _: Any ->
                    requireActivity().recreateAnimated()
                    true
                }
        }
        findPreference<SwitchPreference>(PREFS_BLUR)?.apply {
            onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any ->
                    requireActivity().omegaPrefs.enableBlur = newValue as Boolean
                    true
                }
        }
        findPreference<SeekBarPreference>(PREFS_BLUR_RADIUS)?.apply {
            onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any ->
                    requireActivity().omegaPrefs.blurRadius = newValue as Int
                    true
                }
        }
        findPreference<SwitchPreference>(PREFS_WINDOWCORNER)?.apply {
            onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any ->
                    requireActivity().omegaPrefs.customWindowCorner = newValue as Boolean
                    true
                }
        }
        findPreference<SeekbarPreference>(PREFS_WINDOWCORNER_RADIUS)?.apply {
            onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any ->
                    requireActivity().omegaPrefs.windowCornerRadius = newValue as Float
                    true
                }
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().title = requireActivity().getString(R.string.title__general_theme)
    }
}
