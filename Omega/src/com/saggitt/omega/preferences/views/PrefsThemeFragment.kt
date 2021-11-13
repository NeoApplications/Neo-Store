package com.saggitt.omega.preferences.views

import android.os.Bundle
import android.view.View
import androidx.preference.*
import com.android.launcher3.R
import com.saggitt.omega.*
import com.saggitt.omega.util.omegaPrefs

class PrefsThemeFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences_theme, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findPreference<ListPreference>(PREFS_THEME)?.apply {
            onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any ->
                    requireActivity().omegaPrefs.launcherTheme = newValue.toString().toInt()
                    true
                }
        }
        findPreference<SwitchPreferenceCompat>(PREFS_BLUR)?.apply {
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
        findPreference<SwitchPreferenceCompat>(PREFS_WINDOWCORNER)?.apply {
            onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any ->
                    requireActivity().omegaPrefs.customWindowCorner = newValue as Boolean
                    true
                }
        }
        findPreference<SeekBarPreference>(PREFS_WINDOWCORNER_RADIUS)?.apply {
            onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any ->
                    requireActivity().omegaPrefs.windowCornerRadius = newValue as Int
                    true
                }
        }
    }
}
