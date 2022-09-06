package com.saggitt.omega.preferences.views

import android.os.Bundle
import android.view.View
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreference
import com.android.launcher3.R
import com.jaredrummler.android.colorpicker.ColorPreferenceCompat
import com.saggitt.omega.PREFS_ACCENT
import com.saggitt.omega.PREFS_BLUR
import com.saggitt.omega.PREFS_BLUR_RADIUS
import com.saggitt.omega.PREFS_THEME
import com.saggitt.omega.PREFS_WINDOWCORNER
import com.saggitt.omega.PREFS_WINDOWCORNER_RADIUS
import com.saggitt.omega.preferences.custom.SeekbarPreference
import com.saggitt.omega.util.omegaPrefs
import com.saggitt.omega.util.recreateAnimated

class PrefsThemeFragment :
    BasePreferenceFragment(R.xml.preferences_theme, R.string.title__general_theme) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
                    requireActivity().omegaPrefs.themeBlurEnable.onSetValue(newValue as Boolean)
                    true
                }
        }
        findPreference<SeekBarPreference>(PREFS_BLUR_RADIUS)?.apply {
            onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any ->
                    requireActivity().omegaPrefs.themeBlurRadius.onSetValue(newValue as Float)
                    true
                }
        }
        findPreference<SwitchPreference>(PREFS_WINDOWCORNER)?.apply {
            onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any ->
                    requireActivity().omegaPrefs.themeCornerRadiusOverride.onSetValue(newValue as Boolean)
                    true
                }
        }
        findPreference<SeekbarPreference>(PREFS_WINDOWCORNER_RADIUS)?.apply {
            onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any ->
                    requireActivity().omegaPrefs.themeCornerRadius.onSetValue(newValue as Float)
                    true
                }
        }
    }
}