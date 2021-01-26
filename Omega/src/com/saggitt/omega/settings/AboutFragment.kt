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

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.DecelerateInterpolator
import android.view.animation.RotateAnimation
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.net.toUri
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.android.launcher3.R
import com.android.launcher3.util.Themes
import com.google.android.material.tabs.TabLayout
import com.saggitt.omega.util.AboutUtils
import kotlinx.android.synthetic.main.notification_content.view.*
import kotlinx.coroutines.launch
import java.util.*

class AboutFragment : Fragment() {

    private val MODE_TOTAL = 3
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.about, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val aboutMain = view.findViewById<NestedScrollView>(R.id.about_main)
        val license = view.findViewById<WebView>(R.id.webview_license)
        val changelog = view.findViewById<WebView>(R.id.webview_changelog)

        val lists = arrayOf(aboutMain, license, changelog)
        val titles = arrayOf(getString(R.string.title__general_about), getString(R.string.category__about_licenses),
                getString(R.string.title__about_changelog))

        val viewPager = view.findViewById<ViewPager>(R.id.pager).apply {
            offscreenPageLimit = MODE_TOTAL - 1
            adapter = AboutPagerAdapter(lists as Array<View>, titles)
        }

        requireActivity().findViewById<TabLayout>(R.id.sliding_tabs).apply {
            setupWithViewPager(viewPager)
        }

        val aboutUtils = AboutUtils(context)
        val isDark = Themes.getAttrBoolean(context, R.attr.isMainColorDark);
        val cssFile = if (isDark) {
            "about_dark.css"
        } else {
            "about_light.css"
        }

        lifecycleScope.launch {
            //Load view Items
            val logo = view.findViewById<ImageView>(R.id.app_logo)
            logo.setOnClickListener {
                val anim = AnimationSet(true)
                val rotate = RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
                rotate.duration = 800
                rotate.interpolator = DecelerateInterpolator()
                anim.addAnimation(rotate)
                logo.startAnimation(anim)
            }

            val version = view.findViewById<TextView>(R.id.app_version)
            version.text = getString(R.string.app_version) + ": " + aboutUtils.appVersionName

            val build = view.findViewById<TextView>(R.id.app_build)
            build.text = getString(R.string.app_build) + ": " + aboutUtils.appVersionCode

            val buildInfo = view.findViewById<TextView>(R.id.build_information)
            loadBuildInfo(aboutUtils, buildInfo)
            buildInfo.setOnClickListener {
                aboutUtils.setClipboard(it.text.toString())
            }

            val buttonSource = view.findViewById<Button>(R.id.source_code)
            buttonSource.setOnClickListener {
                aboutUtils.openWebBrowser(getString(R.string.about_source_url))
            }

            val buttonDonate = view.findViewById<Button>(R.id.donate)
            buttonDonate.setOnClickListener {
                aboutUtils.openWebBrowser(getString(R.string.app_donate_url))
            }

            val developer = view.findViewById<ConstraintLayout>(R.id.developer)
            developer.setOnClickListener {
                aboutUtils.openWebBrowser("https://github.com/otakuhqz")
            }
            val contrib1 = view.findViewById<ConstraintLayout>(R.id.contributor1)
            contrib1.setOnClickListener {
                aboutUtils.openWebBrowser("https://github.com/machiav3lli")
            }
            val contrib2 = view.findViewById<ConstraintLayout>(R.id.contributor2)
            contrib2.setOnClickListener {
                aboutUtils.openWebBrowser("https://github.com/rafaelvenancio98")
            }

            license.loadUrl("file:///android_asset/license.htm")
            license.webViewClient = object : WebViewClient() {

                override fun onPageFinished(view: WebView, url: String) {
                    if (url.startsWith("file:///android_asset")) {
                        // Inject CSS when page is done loading
                        injectCSS(license, cssFile)
                    }
                    super.onPageFinished(view, url)
                }

                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    if (url.contains("file://")) {
                        view.loadUrl(url)
                    } else {
                        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                        try {
                            startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            view.loadUrl(url)
                        }
                    }
                    return true
                }
            }

            changelog.loadUrl("file:///android_asset/changelog.htm")
            changelog.webViewClient = object : WebViewClient() {

                override fun onPageFinished(view: WebView, url: String) {
                    if (url.startsWith("file:///android_asset")) {
                        // Inject CSS when page is done loading
                        injectCSS(changelog, cssFile)
                    }
                    super.onPageFinished(view, url)
                }

                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    if (url.contains("file://")) {
                        view.loadUrl(url)
                    } else {
                        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                        try {
                            startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            view.loadUrl(url)
                        }
                    }
                    return true
                }
            }
        }
    }

    private fun loadBuildInfo(aboutUtils: AboutUtils, buildInfo: TextView) {
        var buildInfoText: String?
        var tmp: String?
        val locale = Locale.getDefault()

        buildInfoText = String.format(locale, "<b>Package:</b> %s <br><b>Version:</b> v%s (build %s)",
                aboutUtils.packageName, aboutUtils.appVersionName, aboutUtils.appVersionCode)
        buildInfoText += if (aboutUtils.bcstr("FLAVOR", "").also { tmp = it }.isEmpty()) ""
        else {
            "<br><b>Flavor:</b> " + tmp!!.replace("flavor", "")
        }
        buildInfoText += if (aboutUtils.bcstr("BUILD_TYPE", "").also { tmp = it }.isEmpty()) "" else " ($tmp)"
        buildInfoText += if (aboutUtils.bcstr("BUILD_DATE", "").also { tmp = it }.isEmpty()) "" else "<br><b>Build date:</b> $tmp"
        buildInfoText += if (aboutUtils.getAppInstallationSource().also { tmp = it }.isEmpty()) "" else "<br><b>ISource:</b> $tmp"
        buildInfoText += "<br><b>Manufacturer :</b> " + Build.MANUFACTURER
        buildInfoText += "<br><b>Model :</b> " + Build.MODEL
        buildInfoText += "<br><b>OS Version :</b> Android" + Build.VERSION.RELEASE
        buildInfo.text = Html.fromHtml(buildInfoText, Html.FROM_HTML_MODE_COMPACT)
    }

    private fun injectCSS(webView: WebView, cssAsset: String) {
        try {
            webView.settings.javaScriptEnabled = true
            val inputStream = requireActivity().assets.open(cssAsset)
            val buffer = ByteArray(inputStream.available())
            inputStream.read(buffer)
            inputStream.close()
            val encoded = Base64.encodeToString(buffer, Base64.NO_WRAP)
            webView.loadUrl("javascript:(function() {" +
                    "var parent = document.getElementsByTagName('head').item(0);" +
                    "var style = document.createElement('style');" +
                    "style.type = 'text/css';" +
                    // Tell the browser to BASE64-decode the string into your script !!!
                    "style.innerHTML = window.atob('" + encoded + "');" +
                    "parent.appendChild(style);" +
                    "})()")

            webView.settings.javaScriptEnabled = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}