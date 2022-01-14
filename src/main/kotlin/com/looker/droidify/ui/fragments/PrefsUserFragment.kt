package com.looker.droidify.ui.fragments

import android.view.ViewGroup
import androidx.appcompat.widget.LinearLayoutCompat
import com.google.android.material.circularreveal.CircularRevealFrameLayout
import com.looker.droidify.R
import com.looker.droidify.content.Preferences
import com.looker.droidify.utility.Utils.getLocaleOfCode
import com.looker.droidify.utility.Utils.languagesList
import com.looker.droidify.utility.Utils.translateLocale

class PrefsUserFragment : PrefsNavFragmentX() {

    override fun setupPrefs(scrollLayout: CircularRevealFrameLayout) {
        val preferences = LinearLayoutCompat(scrollLayout.context)
        preferences.orientation = LinearLayoutCompat.VERTICAL
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
                    is Preferences.Theme.AmoledSystem -> getString(R.string.system) + " " + getString(
                        R.string.amoled
                    )
                    is Preferences.Theme.Light -> getString(R.string.light)
                    is Preferences.Theme.Dark -> getString(R.string.dark)
                    is Preferences.Theme.Amoled -> getString(R.string.amoled)
                }
            }
            addSwitch(
                Preferences.Key.ListAnimation, getString(R.string.list_animation),
                getString(R.string.list_animation_description)
            )
        }
    }
}
