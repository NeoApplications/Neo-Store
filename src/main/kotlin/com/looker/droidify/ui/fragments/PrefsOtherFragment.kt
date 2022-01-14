package com.looker.droidify.ui.fragments

import android.view.ViewGroup
import androidx.appcompat.widget.LinearLayoutCompat
import com.google.android.material.circularreveal.CircularRevealFrameLayout
import com.looker.droidify.BuildConfig
import com.looker.droidify.R
import com.looker.droidify.content.Preferences

class PrefsOtherFragment : PrefsNavFragmentX() {

    override fun setupPrefs(scrollLayout: CircularRevealFrameLayout) {
        val preferences = LinearLayoutCompat(scrollLayout.context)
        preferences.orientation = LinearLayoutCompat.VERTICAL
        scrollLayout.addView(
            preferences,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        preferences.addCategory(getString(R.string.proxy)) {
            addEnumeration(Preferences.Key.ProxyType, getString(R.string.proxy_type)) {
                when (it) {
                    is Preferences.ProxyType.Direct -> getString(R.string.no_proxy)
                    is Preferences.ProxyType.Http -> getString(R.string.http_proxy)
                    is Preferences.ProxyType.Socks -> getString(R.string.socks_proxy)
                }
            }
            addEditString(Preferences.Key.ProxyHost, getString(R.string.proxy_host))
            addEditInt(Preferences.Key.ProxyPort, getString(R.string.proxy_port), 1..65535)
        }
        preferences.addCategory(getString(R.string.credits)) {
            addText(
                title = "Based on Foxy-Droid",
                summary = "FoxyDroid",
                url = "https://github.com/kitsunyan/foxy-droid/"
            )
            addText(
                title = getString(R.string.application_name),
                summary = "v ${BuildConfig.VERSION_NAME}",
                url = "https://github.com/iamlooker/Droid-ify/"
            )
        }
    }
}
