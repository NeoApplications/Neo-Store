package com.machiav3lli.fdroid.ui.fragments

import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.android.material.circularreveal.CircularRevealFrameLayout
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.utility.Utils.getLocaleOfCode
import com.machiav3lli.fdroid.utility.Utils.languagesList
import com.machiav3lli.fdroid.utility.Utils.translateLocale

class PrefsUserFragment : PrefsNavFragmentX() {

    override fun setupPrefs(scrollLayout: CircularRevealFrameLayout) {
        val preferences = LinearLayout(scrollLayout.context)
        preferences.orientation = LinearLayout.VERTICAL
        scrollLayout.addView(
            preferences,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        preferences.addCategory(requireContext().getString(R.string.prefs_personalization)) {
            addList(
                Preferences.Key.Language,
                context.getString(R.string.prefs_language_title),
                languagesList
            ) { translateLocale(context.getLocaleOfCode(it)) }
            addEnumeration(Preferences.Key.Theme, getString(R.string.theme)) {
                when (it) {
                    is Preferences.Theme.System -> getString(R.string.system)
                    is Preferences.Theme.SystemBlack -> getString(R.string.system) + " " + getString(
                        R.string.amoled
                    )
                    is Preferences.Theme.Dynamic -> getString(R.string.dynamic)
                    is Preferences.Theme.Light -> getString(R.string.light)
                    is Preferences.Theme.Dark -> getString(R.string.dark)
                    is Preferences.Theme.Black -> getString(R.string.amoled)
                }
            }
            addEnumeration(Preferences.Key.DefaultTab, getString(R.string.default_tab)) {
                when (it) {
                    is Preferences.DefaultTab.Explore -> getString(R.string.explore)
                    is Preferences.DefaultTab.Latest -> getString(R.string.latest)
                    is Preferences.DefaultTab.Installed -> getString(R.string.installed)
                }
            }
            addSwitch(
                Preferences.Key.ShowScreenshots, getString(R.string.show_screenshots),
                getString(R.string.show_screenshots_description)
            )
            addEditInt(Preferences.Key.UpdatedApps, getString(R.string.prefs_updated_apps), 1..200)
            addEditInt(Preferences.Key.NewApps, getString(R.string.prefs_new_apps), 1..50)
        }
    }
}
