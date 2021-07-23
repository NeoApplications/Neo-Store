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
import android.content.res.ColorStateList
import android.graphics.Color
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
import android.widget.*
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.compose.ui.platform.ComposeView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.net.toUri
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.android.launcher3.BuildConfig
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.google.android.material.tabs.TabLayout
import com.saggitt.omega.theme.ThemeManager
import com.saggitt.omega.theme.ThemeOverride
import com.saggitt.omega.theme.ThemedContextProvider
import com.saggitt.omega.util.Config
import com.saggitt.omega.util.CustomPagerAdapter
import com.saggitt.omega.util.applyColor
import com.saggitt.omega.views.LoadTranslators
import kotlinx.coroutines.launch
import java.util.*


class AboutFragment : Fragment() {

    private val modeTotal = 3

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.about, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val aboutMain = view.findViewById<NestedScrollView>(R.id.about_main)
        val license = view.findViewById<WebView>(R.id.webview_license)
        val changelog = view.findViewById<WebView>(R.id.webview_changelog)

        val lists = arrayOf(aboutMain, license, changelog)
        val titles = arrayOf(
            getString(R.string.title__general_about), getString(R.string.category__about_licenses),
            getString(R.string.title__about_changelog)
        )

        val viewPager = view.findViewById<ViewPager>(R.id.pager).apply {
            offscreenPageLimit = modeTotal - 1
            adapter = CustomPagerAdapter(lists as Array<View>, titles)
        }

        requireActivity().findViewById<TabLayout>(R.id.sliding_tabs).apply {
            setupWithViewPager(viewPager)
        }

        val config = Config(requireContext())
        val themedContext =
            ThemedContextProvider(requireContext(), null, ThemeOverride.Settings()).get()
        val isDark = ThemeManager.getInstance(themedContext).isDark

        lifecycleScope.launch {
            //Load view Items
            val logo = view.findViewById<AppCompatImageView>(R.id.app_logo)
            logo.setOnClickListener {
                val anim = AnimationSet(true)
                val rotate = RotateAnimation(
                    0f,
                    360f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f
                )
                rotate.duration = 800
                rotate.interpolator = DecelerateInterpolator()
                anim.addAnimation(rotate)
                logo.startAnimation(anim)
            }

            val version = view.findViewById<AppCompatTextView>(R.id.app_version)
            version.text = getString(R.string.app_version) + ": " + config.appVersionName

            val build = view.findViewById<AppCompatTextView>(R.id.app_build)
            build.text = getString(R.string.app_build) + ": " + config.appVersionCode

            val buildInfo = view.findViewById<AppCompatTextView>(R.id.build_information)
            loadBuildInfo(config, buildInfo)

            val accent = Utilities.getOmegaPrefs(context).accentColor
            view.findViewById<AppCompatButton>(R.id.source_code).apply {
                applyColor(accent)
                setTextColor(accent)
                compoundDrawableTintList = if (isDark) {
                    ColorStateList.valueOf(Color.WHITE)
                } else {
                    ColorStateList.valueOf(Color.BLACK)
                }

                setOnClickListener {
                    Utilities.openURLinBrowser(context, getString(R.string.about_source_url))
                }
            }

            view.findViewById<AppCompatButton>(R.id.donate).apply {
                applyColor(accent)
                setTextColor(accent)
                compoundDrawableTintList = if (isDark) {
                    ColorStateList.valueOf(Color.WHITE)
                } else {
                    ColorStateList.valueOf(Color.BLACK)
                }
                setOnClickListener {
                    //TODO: replace with Google Pay Dialog
                    Utilities.openURLinBrowser(context, getString(R.string.app_donate_url))
                }
            }

            val developer = view.findViewById<ConstraintLayout>(R.id.developer)
            developer.setOnClickListener {
                Utilities.openURLinBrowser(requireContext(), "https://github.com/otakuhqz")
            }

            val contrib1 = view.findViewById<ConstraintLayout>(R.id.contributor1)
            contrib1.setOnClickListener {
                Utilities.openURLinBrowser(requireContext(), "https://github.com/machiav3lli")
            }

            val contrib2 = view.findViewById<ConstraintLayout>(R.id.contributor2)
            contrib2.setOnClickListener {
                Utilities.openURLinBrowser(requireContext(), "https://github.com/nonaybay")
            }

            val hiddenView = view.findViewById<LinearLayout>(R.id.hidden_view)
            val translators: List<String> =
                (Utilities.readTextfileFromRawRes(R.raw.translators, context, "", "")
                    .trim() + "\n\n").split("\n")

            view.findViewById<ComposeView>(R.id.translators_view).setContent {
                LoadTranslators(translators)
            }

            val arrow = view.findViewById<ImageButton>(R.id.arrow_button)
            arrow.setOnClickListener {
                if (hiddenView.visibility == View.VISIBLE) {
                    hiddenView.visibility = View.GONE
                    arrow.setImageResource(R.drawable.ic_baseline_expand_more_24)
                } else {
                    hiddenView.visibility = View.VISIBLE
                    arrow.setImageResource(R.drawable.ic_baseline_expand_less_24)
                }
            }
            val container = view.findViewById<RelativeLayout>(R.id.translators_container)
            container.setOnClickListener {
                if (hiddenView.visibility == View.VISIBLE) {
                    hiddenView.visibility = View.GONE
                    arrow.setImageResource(R.drawable.ic_baseline_expand_more_24)
                } else {
                    hiddenView.visibility = View.VISIBLE
                    arrow.setImageResource(R.drawable.ic_baseline_expand_less_24)
                }
            }

            val cssFile = if (isDark) {
                "about_dark.css"
            } else {
                "about_light.css"
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

    private fun loadBuildInfo(config: Config, buildInfo: TextView) {
        var buildInfoText: String?
        var tmp: String?
        val locale = Locale.getDefault()

        buildInfoText = String.format(
            locale, "<b>Package:</b> %s <br><b>Version:</b> v%s (build %s)",
            BuildConfig.APPLICATION_ID, config.appVersionName, config.appVersionCode
        )
        buildInfoText += if (config.getBuildConfigValue("FLAVOR", "").also { tmp = it }
                .isEmpty()) ""
        else {
            "<br><b>Flavor:</b> " + tmp!!.replace("flavor", "")
        }
        buildInfoText += if (config.getBuildConfigValue("BUILD_TYPE", "").also { tmp = it }
                .isEmpty()) "" else " ($tmp)"
        buildInfoText += if (config.getBuildConfigValue("BUILD_DATE", "").also { tmp = it }
                .isEmpty()) "" else "<br><b>Build date:</b> $tmp"
        buildInfoText += if (config.installSource.also { tmp = it }
                .isEmpty()) "" else "<br><b>ISource:</b> $tmp"
        buildInfoText += "<br><b>Manufacturer :</b> " + Build.MANUFACTURER
        buildInfoText += "<br><b>Model :</b> " + Build.MODEL
        buildInfoText += "<br><b>OS Version :</b> Android " + Build.VERSION.RELEASE
        buildInfo.text = Html.fromHtml(buildInfoText, Html.FROM_HTML_MODE_COMPACT)
        buildInfo.setOnClickListener {
            Toast.makeText(context, R.string.debug_component_name_copied, Toast.LENGTH_SHORT).show()
            Utilities.setClipboard(requireContext(), buildInfo.text)
        }
    }

    private fun injectCSS(webView: WebView, cssAsset: String) {
        try {
            webView.settings.javaScriptEnabled = true
            val inputStream = requireActivity().assets.open(cssAsset)
            val buffer = ByteArray(inputStream.available())
            inputStream.read(buffer)
            inputStream.close()
            val encoded = Base64.encodeToString(buffer, Base64.NO_WRAP)
            webView.loadUrl(
                "javascript:(function() {" +
                        "var parent = document.getElementsByTagName('head').item(0);" +
                        "var style = document.createElement('style');" +
                        "style.type = 'text/css';" +
                        // Tell the browser to BASE64-decode the string into your script !!!
                        "style.innerHTML = window.atob('" + encoded + "');" +
                        "parent.appendChild(style);" +
                        "})()"
            )

            webView.settings.javaScriptEnabled = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}